package com.waxd.pos.fcmb.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.waxd.pos.fcmb.room.dao.UploadDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class FirebaseDispatchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: UploadDao
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val BATCH_LIMIT = 50
    }

    override suspend fun doWork(): Result {
        val wm = WorkManager.getInstance(applicationContext)

        // 1) pick next batch
        val batch = dao.pickFirebasePending(limit = BATCH_LIMIT)
        if (batch.isEmpty()) return Result.success()

        val uploadRequests = mutableListOf<OneTimeWorkRequest>()
        // 2) loop and try to reserve each item (mark QUEUED)
        for (item in batch) {
            try {
                // Defensive: if already synced according to DB, skip
                if (item.isSyncedOverFirebase) {
                    continue
                }

                // Defensive check: if unique work with same name already exists and is active, skip
                val uniqueName = "firebase:${item.s3Key}"
                val infosFuture = wm.getWorkInfosForUniqueWork(uniqueName)
                val infos = try { infosFuture.get() } catch (e: Exception) { emptyList<WorkInfo>() }

                val alreadyActive = infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.BLOCKED }

                if (alreadyActive) {
                    // It is already enqueued elsewhere — revert QUEUED -> PENDING or leave QUEUED (choose your policy)
                    // Revert to isSyncedOverFirebase = false so next dispatch can re-reserve if needed:
                    dao.update(item.copy(isSyncedOverFirebase = false))
                    continue
                }

                // Build request
                val req = OneTimeWorkRequestBuilder<FirebaseUpdateWorker>()
                    .setId(UUID.nameUUIDFromBytes(item.s3Key.toByteArray() + item.uniqueId.toByteArray()))
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                    .setInputData(workDataOf("upload_id" to item.id))
                    .addTag("firebase:${item.s3Key}")
                    .build()

                uploadRequests += req

            } catch (_: Exception) {
                // If anything goes wrong during reservation, make sure the row is not left QUEUED forever:
                try { dao.update(item.copy(isSyncedOverFirebase = false)) } catch (_: Exception) {}
            }
        }

        // 3) If nothing new to schedule
        if (uploadRequests.isEmpty()) {
            // If batch was full, assume there MAY be more items (but they were either already queued or raced)
            if (batch.size == BATCH_LIMIT) {
                // schedule a delayed self-dispatch to re-attempt later (avoid tight loop)
                val retrySelf = OneTimeWorkRequestBuilder<FirebaseDispatchWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS) // tweak as needed
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                wm.enqueueUniqueWork("firebase_dispatch_retry", ExistingWorkPolicy.KEEP, retrySelf)
            }
            return Result.success()
        }

        // 4) Build nextDispatch if more batches might exist
        val nextDispatch: OneTimeWorkRequest? = if (batch.size == BATCH_LIMIT) {
            OneTimeWorkRequestBuilder<FirebaseDispatchWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag("firebase_dispatch")
                .build()
        } else null

        // Use a unique batch name so beginUniqueWork creates a unique chaining continuation
        val batchName = "firebase_batch_${UUID.randomUUID()}"

        if (nextDispatch != null) {
            wm.beginUniqueWork(batchName, ExistingWorkPolicy.APPEND_OR_REPLACE, uploadRequests)
                .then(nextDispatch)
                .enqueue()
        } else {
            wm.beginUniqueWork(batchName, ExistingWorkPolicy.APPEND_OR_REPLACE, uploadRequests)
                .enqueue()
        }

        return Result.success()
    }
}
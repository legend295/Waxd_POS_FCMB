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
import com.waxd.pos.fcmb.work.FileUploadWorker
import com.waxd.pos.fcmb.room.UploadStatus
import com.waxd.pos.fcmb.room.dao.UploadDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class UploadDispatchWorker @AssistedInject constructor(
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
        val batch = dao.pickPending(limit = BATCH_LIMIT)
        if (batch.isEmpty()) return Result.success()

        val uploadRequests = mutableListOf<OneTimeWorkRequest>()
        // 2) loop and try to reserve each item (mark QUEUED)
        for (item in batch) {
            try {
                // Defensive: if already queued/active according to DB, skip
                if (item.status == UploadStatus.QUEUED || item.status == UploadStatus.UPLOADING) {
                    continue
                }

                // Try to atomically mark it QUEUED so other dispatchers skip it
                dao.updateStatus(item.id, UploadStatus.QUEUED)

                // Defensive check: if unique work with same name already exists and is active, skip
                val uniqueName = "upload:${item.s3Key}"
                val infosFuture = wm.getWorkInfosForUniqueWork(uniqueName)
                val infos = try { infosFuture.get() } catch (e: Exception) { emptyList<WorkInfo>() }

                val alreadyActive = infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.BLOCKED }

                if (alreadyActive) {
                    // It is already enqueued elsewhere — revert QUEUED -> PENDING or leave QUEUED (choose your policy)
                    // Revert to PENDING so next dispatch can re-reserve if needed:
                    dao.updateStatus(item.id, UploadStatus.PENDING)
                    continue
                }

                // Build request
                val req = OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .setId(UUID.nameUUIDFromBytes(item.s3Key.toByteArray()))
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                    .setInputData(workDataOf("upload_id" to item.id))
                    .addTag("upload:${item.s3Key}")
                    .build()

                uploadRequests += req

            } catch (ex: Exception) {
                // If anything goes wrong during reservation, make sure the row is not left QUEUED forever:
                try { dao.updateStatus(item.id, UploadStatus.PENDING) } catch (_: Exception) {}
            }
        }

        // 3) If nothing new to schedule
        if (uploadRequests.isEmpty()) {
            // If batch was full, assume there MAY be more items (but they were either already queued or raced)
            if (batch.size == BATCH_LIMIT) {
                // schedule a delayed self-dispatch to re-attempt later (avoid tight loop)
                val retrySelf = OneTimeWorkRequestBuilder<UploadDispatchWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS) // tweak as needed
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                wm.enqueueUniqueWork("upload_dispatch_retry", ExistingWorkPolicy.KEEP, retrySelf)
            }
            return Result.success()
        }

        // 4) Build nextDispatch if more batches might exist
        val nextDispatch: OneTimeWorkRequest? = if (batch.size == BATCH_LIMIT) {
            OneTimeWorkRequestBuilder<UploadDispatchWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag("upload_dispatch")
                .build()
        } else null

        // Use a unique batch name so beginUniqueWork creates a unique chaining continuation
        val batchName = "upload_batch_${UUID.randomUUID()}"

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

   /* override suspend fun doWork(): Result {
        *//*val pending = dao.pickPending(limit = 50)
        val wm = WorkManager.getInstance(applicationContext)
        pending.forEach { item ->
            val req = OneTimeWorkRequestBuilder<FileUploadWorker>()
                .setId(UUID.nameUUIDFromBytes(item.s3Key.toByteArray()))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, java.util.concurrent.TimeUnit.SECONDS)
                .setInputData(workDataOf("upload_id" to item.id))
                .addTag("upload:${item.s3Key}")
                .build()
            wm.enqueueUniqueWork("upload:${item.s3Key}", ExistingWorkPolicy.KEEP, req)
        }

        // 🔁 Re-dispatch if more pending left
        if (pending.size == 50) {
            val selfReq = OneTimeWorkRequestBuilder<UploadDispatchWorker>().build()
            wm.enqueue(selfReq)
        }

        return Result.success()*//*

        val wm = WorkManager.getInstance(applicationContext)

        // Pick the next batch (max 50)
        val batch = dao.pickPending(limit = 50)

        if (batch.isEmpty()) {
            // ✅ No more work, stop the chain
            return Result.success()
        }

        val uploadRequests = batch.map { item ->
            OneTimeWorkRequestBuilder<FileUploadWorker>()
                .setId(UUID.nameUUIDFromBytes(item.s3Key.toByteArray()))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setInputData(workDataOf("upload_id" to item.id))
                .addTag("upload:${item.s3Key}")
                .build()
        }

        if (batch.size == 50) {
            // ✅ Assume there may be more → schedule another dispatcher
            val nextDispatch = OneTimeWorkRequestBuilder<UploadDispatchWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
//                .addTag("upload_dispatch")
                .build()

            wm.beginUniqueWork(
                "upload_dispatch",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                uploadRequests
            ).then(nextDispatch).enqueue()
        } else {
            // ✅ No next batch → just enqueue current ones
            wm.beginUniqueWork(
                "upload_dispatch",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                uploadRequests
            ).enqueue()
        }

        return Result.success()
    }*/

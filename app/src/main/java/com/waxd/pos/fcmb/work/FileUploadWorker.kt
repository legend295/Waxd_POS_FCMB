package com.waxd.pos.fcmb.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.room.UploadStatus
import com.waxd.pos.fcmb.room.dao.UploadDao
import com.waxd.pos.fcmb.room.entity.UploadEntity
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import com.waxd.pos.fcmb.utils.s3.S3Uploader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: UploadDao,
    private val uploader: S3Uploader
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong("upload_id", -1)
        val item = dao.getById(id) ?: return Result.success()

        // jitter (100-1000ms)
        delay((100..1000).random().toLong())

        val file = File(item.filePath)
        if (!file.exists() || file.length() == 0L) {
            dao.update(item.copy(status = UploadStatus.ABORTED))
            return Result.success()
        }

        try {
            dao.update(item.copy(status = UploadStatus.UPLOADING, lastAttemptAt = System.currentTimeMillis()))

            val res = uploader.upload(file, item.s3Key)
            if (res.isSuccess) {
                try {
                    file.delete()
                } catch (_: Throwable) {
                }
                dao.update(item.copy(status = UploadStatus.SUCCESS))
                enqueueFirebaseWorker(item)
                return Result.success()
            } else {
                val bumped = item.copy(
                    status = UploadStatus.FAILED,
                    retryCount = item.retryCount + 1,
                    nextAttemptAt = System.currentTimeMillis() + computeNextDelay(item.retryCount)
                )
                dao.update(bumped)
                return Result.retry()
            }
        } catch (_: Exception) {
            val bumped = item.copy(
                status = UploadStatus.FAILED,
                retryCount = item.retryCount + 1,
                nextAttemptAt = System.currentTimeMillis() + computeNextDelay(item.retryCount)
            )
            dao.update(bumped)
            return Result.retry()
        }
    }

    private fun enqueueFirebaseWorker(item: UploadEntity) {
        // chain Firebase sync worker
        val syncReq = OneTimeWorkRequestBuilder<FirebaseUpdateWorker>()
            .setInputData(workDataOf("upload_id" to item.id))
            .setId(UUID.nameUUIDFromBytes(item.s3Key.toByteArray() + item.uniqueId.toByteArray()))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .addTag("firebase:${item.s3Key}")
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "firebase:${item.s3Key}",
                ExistingWorkPolicy.KEEP,
                syncReq
            )
    }

    private fun computeNextDelay(retryCount: Int): Long {
        val base = 5_000L * (1L shl retryCount.coerceAtMost(6))
        val jitter = (0..2_000).random()
        return base + jitter
    }
}
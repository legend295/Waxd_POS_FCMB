package com.waxd.pos.fcmb.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.waxd.pos.fcmb.room.UploadStatus
import com.waxd.pos.fcmb.room.dao.UploadDao
import com.waxd.pos.fcmb.utils.s3.S3Uploader
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.io.File

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
                try { file.delete() } catch (_: Throwable) {}
                dao.update(item.copy(status = UploadStatus.SUCCESS))
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

    private fun computeNextDelay(retryCount: Int): Long {
        val base = 5_000L * (1L shl retryCount.coerceAtMost(6))
        val jitter = (0..2_000).random()
        return base + jitter
    }
}
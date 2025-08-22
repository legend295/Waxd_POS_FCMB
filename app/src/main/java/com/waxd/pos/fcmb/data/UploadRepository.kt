package com.waxd.pos.fcmb.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.waxd.pos.fcmb.model.StatusCounts
import com.waxd.pos.fcmb.room.dao.UploadDao
import com.waxd.pos.fcmb.room.entity.UploadEntity
import com.waxd.pos.fcmb.utils.Util.buildS3Key
import com.waxd.pos.fcmb.utils.Util.calculateSha256
import com.waxd.pos.fcmb.utils.Util.deviceId
import com.waxd.pos.fcmb.utils.Util.folderName
import com.waxd.pos.fcmb.utils.Util.nowIsoStamp
import com.waxd.pos.fcmb.work.FileUploadWorker
import com.waxd.pos.fcmb.work.UploadDispatchWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val dao: UploadDao
) {
    suspend fun enqueueFile(file: File, mimeType: String): Long {
        val folderName = file.folderName()
        val key = buildS3Key(file, folderName, deviceId(ctx), nowIsoStamp())
        val entity = UploadEntity(
            folderId = file.folderName(),
            filePath = file.absolutePath,
            s3Key = key,
            mimeType = mimeType,
            sizeBytes = file.length(),
            sha256 = file.calculateSha256()
        )
        val id = dao.tryInsert(entity)
        if (id != 0L) enqueuePerFileWork(key, id)
        return id
    }

    fun dispatchNow() {
        val req = OneTimeWorkRequestBuilder<UploadDispatchWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(ctx).enqueue(req)
    }

    private fun enqueuePerFileWork(s3Key: String, id: Long) {
        val req = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setId(UUID.nameUUIDFromBytes(s3Key.toByteArray()))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .setInputData(workDataOf("upload_id" to id))
            .addTag("upload:$s3Key")
            .build()
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork("upload:$s3Key", ExistingWorkPolicy.KEEP, req)
    }

    fun getPendingUploadsCount(): Flow<StatusCounts> = dao.getStatusCounts()
}
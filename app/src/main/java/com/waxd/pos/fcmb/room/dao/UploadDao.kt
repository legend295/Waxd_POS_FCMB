package com.waxd.pos.fcmb.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.waxd.pos.fcmb.model.StatusCounts
import com.waxd.pos.fcmb.room.UploadStatus
import com.waxd.pos.fcmb.room.entity.UploadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun tryInsert(upload: UploadEntity): Long

    @Update
    suspend fun update(upload: UploadEntity)

    @Query("SELECT * FROM uploads WHERE status IN ('PENDING','FAILED') ORDER BY createdAt ASC LIMIT :limit")
    suspend fun pickPending(limit: Int = 20): List<UploadEntity>

    @Query("SELECT * FROM uploads WHERE isSyncedOverFirebase = :flag AND status IN ('SUCCESS') ORDER BY createdAt ASC LIMIT :limit")
    suspend fun pickFirebasePending(flag: Boolean = false, limit: Int = 20): List<UploadEntity>

    @Query("SELECT * FROM uploads WHERE folderId = :folderId")
    suspend fun getFilesInFolder(folderId: String): List<UploadEntity>

    @Query("SELECT * FROM uploads WHERE id = :id")
    suspend fun getById(id: Long): UploadEntity?

    @Query("UPDATE uploads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: UploadStatus)

    @Query("DELETE FROM uploads WHERE status = 'SUCCESS' AND createdAt < :olderThanMs")
    suspend fun purgeOldSuccess(olderThanMs: Long)

    @Query("SELECT COUNT(*) FROM uploads  WHERE folderId = :folderId AND status != :status")
    suspend fun getPendingFileCountInFolder(
        folderId: String,
        status: UploadStatus = UploadStatus.SUCCESS
    ): Int

    @Query(
        """
            SELECT 
                COUNT(*) as totalCount,
                SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pendingCount,
                SUM(CASE WHEN status = 'UPLOADING' THEN 1 ELSE 0 END) as inProgressCount,
                SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failedCount,
                SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount
            FROM uploads
            """
    )
    fun getStatusCounts(): Flow<StatusCounts>

    @Query("SELECT * FROM uploads WHERE uniqueId = :uniqueId")
    fun getByUniqueId(uniqueId: String): Flow<List<UploadEntity>>
}

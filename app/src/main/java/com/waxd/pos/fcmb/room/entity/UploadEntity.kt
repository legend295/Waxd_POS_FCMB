package com.waxd.pos.fcmb.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.waxd.pos.fcmb.room.UploadStatus

@Entity(tableName = "uploads", indices = [Index("s3Key", unique = true)])
data class UploadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: String,
    val filePath: String,
    val s3Key: String,                 // deterministic idempotent key (e.g., sha256 + ext)
    val mimeType: String,
    val sizeBytes: Long,
    val sha256: String,                // or MD5 if you prefer; for integrity checks
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
    val nextAttemptAt: Long? = null,
    val status: UploadStatus = UploadStatus.PENDING,
    val retryCount: Int = 0,
    // Multipart resume fields
    val isMultipart: Boolean = false,
    val multipartUploadId: String? = null,
    val completedPartsJson: String? = null // JSON array of {partNumber, eTag}
)

package com.waxd.pos.fcmb.utils.s3

import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class S3Uploader @Inject constructor(private val transferUtility: TransferUtility) {

    suspend fun upload(file: File, s3Key: String, deleteFileKey: String? = null): Result<Pair<String, String>> =
        suspendCancellableCoroutine { cont ->
            val observer = transferUtility.upload(AWSKeys.BUCKET_NAME, s3Key, file)

            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    when (state) {
                        TransferState.COMPLETED -> {
                            if (cont.isActive) cont.resume(Result.success(s3Key to id.toString()))
                        }

                        TransferState.FAILED, TransferState.CANCELED -> {
                            if (cont.isActive) cont.resume(Result.failure(Exception("Upload $state id=$id")))
                        }

                        else -> Unit
                    }
                }

                override fun onProgressChanged(id: Int, current: Long, total: Long) {
                    val pct = if (total == 0L) 0 else ((current.toDouble() / total) * 100).toInt()
                    Log.d("S3Uploader", "id=$id progress=$pct%")
                }

                override fun onError(id: Int, ex: Exception) {
                    if (cont.isActive) cont.resume(Result.failure(ex))
                }
            })

            cont.invokeOnCancellation {
                try {
                    transferUtility.cancel(observer.id)
                } catch (_: Exception) {
                }
            }
        }
}
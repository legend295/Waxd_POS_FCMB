package com.waxd.pos.fcmb.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.room.dao.UploadDao
import com.waxd.pos.fcmb.room.entity.UploadEntity
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@HiltWorker
class FirebaseUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val firebaseWrapper: FirebaseWrapper,
    private val dao: UploadDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong("upload_id", -1)
        val item = dao.getById(id) ?: return Result.success()

        // Only sync if not already synced
        if (item.isSyncedOverFirebase) {
            return Result.success()
        }

        return try {
            if (item.uniqueId.isNotEmpty()) {
                val ok = syncToFirebase(item)
                if (ok) {
                    dao.update(item.copy(isSyncedOverFirebase = true))
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.success()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun syncToFirebase(item: UploadEntity): Boolean =
        suspendCancellableCoroutine { cont ->
            firebaseWrapper.getFarmerById(item.uniqueId) { farmerRes ->
                when (farmerRes) {
                    is DataResult.Failure -> {
                        cont.resume(false) {}
                    }

                    DataResult.Loading -> { /*no-op*/
                    }

                    is DataResult.Success -> {
                        firebaseWrapper.updateFarmer(
                            item.uniqueId,
                            mapOf("biometrics" to FieldValue.arrayUnion(item.s3Key))
                        ) { updateRes ->
                            when (updateRes) {
                                is DataResult.Failure -> {
                                    cont.resume(false) {}
                                }

                                DataResult.Loading -> {
                                    // no-op
                                }

                                is DataResult.Success<*> -> {
                                    cont.resume(true) {}
                                }
                            }

                        }
                    }
                }
            }
        }
}
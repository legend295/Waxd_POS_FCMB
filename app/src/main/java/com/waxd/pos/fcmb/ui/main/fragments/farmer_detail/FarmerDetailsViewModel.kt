package com.waxd.pos.fcmb.ui.main.fragments.farmer_detail

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.rest.UploadImageResponse
import com.waxd.pos.fcmb.utils.Util.convertImageFileToBase64
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FarmerDetailsViewModel @Inject constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val restRepository: RestRepository
) :
    ViewModel() {

    var farmerId: String? = null
    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()
    val uploadImageResponse: MutableLiveData<DataResult<UploadImageResponse>> = MutableLiveData()

    fun getFarmerById(callback: (DataResult<FarmerData>) -> Unit) {
        farmerId?.let {
            firebaseWrapper.getFarmerById(it) { farmerData ->
                callback(farmerData)
            }
        }
    }

    fun uploadProfileImage(context: Context, file: File) {
        viewModelScope.launch {
            uploadImageResponse.value = DataResult.Loading
            val compressedFile = Compressor.compress(context, file) {
                this.size(1048576)
            }
            val base64 = convertImageFileToBase64(compressedFile)
            base64 ?: return@launch
            restRepository.uploadProfileImage(
                hashMapOf(
                    Constants.ApiKeys.BASE_64_IMAGE to "$base64",
                    Constants.ApiKeys.FARMER_ID to farmerId
                )
            )
                .collect {
                    uploadImageResponse.value = it
                }
        }
    }
}
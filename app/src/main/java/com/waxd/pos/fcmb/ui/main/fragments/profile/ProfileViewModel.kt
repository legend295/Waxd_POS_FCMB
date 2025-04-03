package com.waxd.pos.fcmb.ui.main.fragments.profile

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Base64OutputStream
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.base.SingletonWrapper
import com.waxd.pos.fcmb.rest.AgentProfileResponse
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.rest.UploadImageResponse
import com.waxd.pos.fcmb.utils.Util.convertImageFileToBase64
import com.waxd.pos.fcmb.utils.constants.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val singletonWrapper: SingletonWrapper
) :
    ViewModel() {

    val response: MutableLiveData<DataResult<AgentProfileResponse>> = singletonWrapper.agentProfile
    val uploadImageResponse: MutableLiveData<DataResult<UploadImageResponse>> = MutableLiveData()
    var file: File? = null


    fun getAgentById(uid: String) {
        viewModelScope.launch {
            if (response.value == null)
                response.value = DataResult.Loading
            restRepository.getAgentById(uid).collect {
                singletonWrapper.agentProfile.value = it
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
            restRepository.uploadProfileImage(hashMapOf(Constants.ApiKeys.BASE_64_IMAGE to "$base64"))
                .collect {
                    uploadImageResponse.value = it
                }
        }
    }


    private fun getBase64String(file: File): String {
        // Step 1: Read the file into a ByteArray
        val fileInputStream = FileInputStream(file)
        val bytes = fileInputStream.readBytes()
        fileInputStream.close()

        // Step 2: Encode the ByteArray to Base64
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    private fun convertImageFileToDataUri(file: File): String? {
        return try {
            // Step 1: Read the file into a ByteArray
            val fileInputStream = FileInputStream(file)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()

            // Step 2: Encode the ByteArray to Base64
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            // Step 3: Determine the MIME type
            val mimeType = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> null // Unsupported format
            }

            // Step 4: Construct the Data URI
            if (mimeType != null) {
                "data:$mimeType;base64,$base64"
            } else {
                null // Unsupported file format
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Handle error
        }
    }

    private fun writeFileIntoStorage(name: String, data: String) {
        val fileName = "Base64Data$name.txt"
        val file = File(FcmbApp.instance.filesDir.path, fileName)

        try {
            FileOutputStream(file).use {
                it.write(data.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
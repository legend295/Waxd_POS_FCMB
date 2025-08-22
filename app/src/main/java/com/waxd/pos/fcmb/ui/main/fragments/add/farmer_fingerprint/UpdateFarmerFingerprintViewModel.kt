package com.waxd.pos.fcmb.ui.main.fragments.add.farmer_fingerprint

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.data.UploadRepository
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.rest.UserResponse
import com.waxd.pos.fcmb.utils.Util.folderName
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateFarmerFingerprintViewModel @Inject constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val repo: UploadRepository
) :
    ViewModel() {

    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()
    val userResponse: MutableLiveData<DataResult<UserResponse>> = MutableLiveData()

    fun getUserById() {
        farmerData.value?.let {
            it.id?.let { it1 ->
                firebaseWrapper.getUserById(it1) { response ->
                    userResponse.value = response
                }
            }
        }
    }

    fun enqueue(file: File, mime: String) {
        viewModelScope.launch {
            repo.enqueueFile(file, mime)
        }
    }

    fun dispatchNow() {
        repo.dispatchNow()
    }
}
package com.waxd.pos.fcmb.ui.main.fragments.add.farmer_fingerprint

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.rest.UserResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateFarmerFingerprintViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()
    val userResponse: MutableLiveData<DataResult<UserResponse>> = MutableLiveData()

    fun getUserById() {
        farmerData.value?.let {
            it.id?.let { it1 ->
                firebaseWrapper.getUserById(it1) { response->
                    userResponse.value = response
                }
            }
        }
    }
}
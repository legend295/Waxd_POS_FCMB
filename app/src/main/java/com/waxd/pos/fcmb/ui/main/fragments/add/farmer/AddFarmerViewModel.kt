package com.waxd.pos.fcmb.ui.main.fragments.add.farmer

import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerCreateRequest
import com.waxd.pos.fcmb.rest.FarmerCreateResponse
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFarmerViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val firebaseWrapper: FirebaseWrapper
) :
    ViewModel() {

    val request: MutableLiveData<FarmerCreateRequest> = MutableLiveData(FarmerCreateRequest())
    val response: MutableLiveData<DataResult<FarmerCreateResponse>> = MutableLiveData()
    val farmerUpdateResponse: MutableLiveData<DataResult<FarmerData>> = MutableLiveData()
    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()

    val checkChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        when (checkedId) {
            R.id.rbYes -> request.value?.haveBvnNumber = true
            R.id.rbNo -> request.value?.haveBvnNumber = false
        }
        request.value = request.value
    }

    val ninCheckChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        when (checkedId) {
            R.id.rbYesNin -> request.value?.haveNinNumber = true
            R.id.rbNoNin -> request.value?.haveNinNumber = false
        }
        request.value = request.value
    }

    fun registerFarmer() {
        viewModelScope.launch {
            request.value?.let {
                response.value = DataResult.Loading
                restRepository.registerFarmer(it).collect { response ->
                    this@AddFarmerViewModel.response.value = response
                }
            }
        }
    }

    fun updateFarmer() {
        farmerData.value?.let {
            it.id?.let { it1 ->
                firebaseWrapper.updateFarmer(it1, getMap()) { response ->
                    farmerUpdateResponse.value = response
                }
            } ?: run {
                farmerUpdateResponse.value =
                    DataResult.Failure(status = "400", message = "Farmer id not found.")
            }
        }
    }

    private fun getMap(): Map<String, Any> {
        val updatedFields = mutableMapOf<String, Any>()

        // Check and add updated fields
        if (farmerData.value?.firstName != request.value?.firstName) {
            updatedFields["first_name"] = request.value?.firstName ?: ""
        }
        if (farmerData.value?.lastName != request.value?.lastName) {
            updatedFields["last_name"] = request.value?.lastName ?: ""
        }
        if (farmerData.value?.address != request.value?.address) {
            updatedFields["address"] = request.value?.address ?: ""
        }
        if (farmerData.value?.city != request.value?.city) {
            updatedFields["city"] = request.value?.city ?: ""
        }
        if (farmerData.value?.state != request.value?.state) {
            updatedFields["state"] = request.value?.state ?: ""
        }
        if (farmerData.value?.phoneNumber != request.value?.phoneNumber) {
            updatedFields["phone_number"] = request.value?.phoneNumber ?: ""
        }
        if (farmerData.value?.email != request.value?.email) {
            updatedFields["email"] = request.value?.email ?: ""
        }
        if (farmerData.value?.dob != request.value?.dob) {
            updatedFields["dob"] = request.value?.dob ?: ""
        }
        if (farmerData.value?.bvnNumber != request.value?.bvnNumber) {
            updatedFields["bvn_number"] = request.value?.bvnNumber ?: ""
        }
        if (farmerData.value?.ninNumber != request.value?.ninNumber) {
            updatedFields["nin_number"] = request.value?.ninNumber ?: ""
        }
        if (farmerData.value?.haveBvnNumber != request.value?.haveBvnNumber) {
            updatedFields["have_bvn_number"] = request.value?.haveBvnNumber ?: false
        }
        if (farmerData.value?.haveNinNumber != request.value?.haveNinNumber) {
            updatedFields["have_nin_number"] = request.value?.haveNinNumber ?: false
        }
        return updatedFields
    }
}
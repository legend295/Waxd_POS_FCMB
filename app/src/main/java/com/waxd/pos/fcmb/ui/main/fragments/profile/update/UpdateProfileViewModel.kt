package com.waxd.pos.fcmb.ui.main.fragments.profile.update

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.base.SingletonWrapper
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.rest.UpdateAgentProfileRequest
import com.waxd.pos.fcmb.rest.UpdateProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val singletonWrapper: SingletonWrapper
) : ViewModel() {

    val request: MutableLiveData<UpdateAgentProfileRequest> =
        MutableLiveData(UpdateAgentProfileRequest())
    val response: MutableLiveData<DataResult<UpdateProfileResponse>> = MutableLiveData()

    fun setProfileData(viewLifecycleOwner: LifecycleOwner) {
        singletonWrapper.agentProfile.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {}
                DataResult.Loading -> {}
                is DataResult.Success -> {
                    request.value?.setProfileData(it.data)
                    request.value = request.value
                }
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            request.value?.let {
                response.value = DataResult.Loading
                restRepository.updateAgentProfile(it).collect { response->
                    this@UpdateProfileViewModel.response.value = response
                }
            }
        }
    }
}
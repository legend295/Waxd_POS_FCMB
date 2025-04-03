package com.waxd.pos.fcmb.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.base.SingletonWrapper
import com.waxd.pos.fcmb.rest.AgentProfileResponse
import com.waxd.pos.fcmb.rest.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val restRepository: RestRepository,
    private val singletonWrapper: SingletonWrapper
) : ViewModel() {

    val response: MutableLiveData<DataResult<AgentProfileResponse>> = singletonWrapper.agentProfile

    fun getAgentById(uid: String) {
        viewModelScope.launch {
            if (response.value == null)
                singletonWrapper.agentProfile.value = DataResult.Loading
            restRepository.getAgentById(uid).collect {
                singletonWrapper.agentProfile.value = it
            }
        }
    }

    fun clear(){
        singletonWrapper.clear()
    }
}
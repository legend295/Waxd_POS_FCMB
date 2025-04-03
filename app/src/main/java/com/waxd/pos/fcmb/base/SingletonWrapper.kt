package com.waxd.pos.fcmb.base

import androidx.lifecycle.MutableLiveData
import com.waxd.pos.fcmb.rest.AgentProfileResponse


class SingletonWrapper {
    var agentProfile: MutableLiveData<DataResult<AgentProfileResponse>> = MutableLiveData()

    fun clear() {
        agentProfile = MutableLiveData()
    }
}
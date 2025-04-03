package com.waxd.pos.fcmb.ui.main.fragments.recentactivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecentActivityViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) : ViewModel() {

    val response: MutableLiveData<DataResult<ArrayList<FarmerResponse>>> = MutableLiveData()

    fun getRecentFarmers() {
        firebaseWrapper.getRecentFarmers {
            response.value = it
        }
    }
}
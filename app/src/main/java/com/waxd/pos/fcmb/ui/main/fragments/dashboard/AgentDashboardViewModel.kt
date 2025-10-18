package com.waxd.pos.fcmb.ui.main.fragments.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.data.UploadRepository
import com.waxd.pos.fcmb.model.StatusCounts
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AgentDashboardViewModel @Inject constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val repo: UploadRepository
) :
    ViewModel() {

    val response: MutableLiveData<DataResult<ArrayList<FarmerResponse>>> = MutableLiveData()

    fun getRecentFarmers() {
        firebaseWrapper.getRecentFarmers {
            response.value = it
        }
    }

    fun enqueue(uniqueId: String, file: File, mime: String) {
        viewModelScope.launch {
            repo.enqueueFile(uniqueId,file, mime)
        }
    }


}
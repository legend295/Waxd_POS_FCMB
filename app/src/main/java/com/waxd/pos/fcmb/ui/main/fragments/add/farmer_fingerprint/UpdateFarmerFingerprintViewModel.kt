package com.waxd.pos.fcmb.ui.main.fragments.add.farmer_fingerprint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.data.UploadRepository
import com.waxd.pos.fcmb.model.StatusCounts
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.rest.UserResponse
import com.waxd.pos.fcmb.room.entity.UploadEntity
import com.waxd.pos.fcmb.utils.Util.folderName
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateFarmerFingerprintViewModel @Inject constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val repo: UploadRepository
) :
    ViewModel() {

    //    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()
    val userResponse: MutableLiveData<DataResult<UserResponse>> = MutableLiveData()

    private val _farmerData = MutableLiveData<FarmerData>()
    val farmerData: LiveData<FarmerData> get() = _farmerData

    @OptIn(ExperimentalCoroutinesApi::class)
    val farmerS3Files: StateFlow<List<UploadEntity>> =
        _farmerData.asFlow()
            .filterNotNull()
            .flatMapLatest { farmer ->
                repo.getByUniqueId(farmer.id ?: "")
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setFarmer(farmer: FarmerData) {
        _farmerData.value = farmer
    }

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
            farmerData.value?.id?.let { repo.enqueueFile(it, file, mime) }
        }
    }

    fun dispatchNow() {
        repo.dispatchNow()
    }
}
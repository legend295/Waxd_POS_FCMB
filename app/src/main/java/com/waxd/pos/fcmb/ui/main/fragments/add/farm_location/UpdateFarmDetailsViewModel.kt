package com.waxd.pos.fcmb.ui.main.fragments.add.farm_location

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmCoordinates
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.FARM_LOCATIONS
import dagger.hilt.android.lifecycle.HiltViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateFarmDetailsViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    val farmerData: MutableLiveData<FarmerData> = MutableLiveData()
    val farmerUpdateResponse: MutableLiveData<DataResult<FarmerData>> = MutableLiveData()
    var coordinates: ArrayList<LatLng>? = ArrayList()
    val farmImageUpdateResponse: MutableLiveData<DataResult<FarmerData>> = MutableLiveData()
    val farmImageDeleteResponse: MutableLiveData<DataResult<FarmerData>> = MutableLiveData()

    fun updateFarmer() {
        val map = ArrayList<FarmCoordinates>()
        coordinates?.forEach {
            map.add(FarmCoordinates(it.latitude, it.longitude))
        }
        farmerData.value?.let {
            it.id?.let { it1 ->
                firebaseWrapper.updateFarmer(it1, hashMapOf(FARM_LOCATIONS to map)) { response ->
                    farmerUpdateResponse.value = response
                }
            } ?: run {
                farmerUpdateResponse.value =
                    DataResult.Failure(status = "400", message = "Farmer id not found.")
            }
        }
    }

    fun uploadFarmImage(context: Context, file: File) {
        viewModelScope.launch {
            farmImageUpdateResponse.value = DataResult.Loading
            val compressedFile = Compressor.compress(context, file) {
                this.size(1048576)
            }
            val compressedUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                compressedFile
            )
            farmerData.value?.let {
                it.id?.let { it1 ->
                    firebaseWrapper.uploadFarmImage(it1, compressedUri, it) { response ->
                        farmImageUpdateResponse.value = response
                    }
                } ?: run {
                    farmImageUpdateResponse.value =
                        DataResult.Failure(status = "400", message = "Farmer id not found.")
                }
            }
        }
    }

    fun deleteImage(path: String) {
        farmerData.value?.let {
            it.id?.let { it1 ->
                firebaseWrapper.deleteFarmImage(it1, path, it) { response ->
                    farmImageDeleteResponse.value = response
                }
            }
        }
    }

}
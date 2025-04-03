package com.waxd.pos.fcmb.utils.firebase

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.rest.UserResponse

interface IFirebaseWrapper {

    fun getFarmers(searchQuery: String? = null, lastVisibleDocument: DocumentSnapshot?, callback: (ArrayList<FarmerResponse>) -> Unit)

    fun getRecentFarmers(callback: (DataResult<ArrayList<FarmerResponse>>) -> Unit)

    fun getFarmerById(farmerId: String, callback: (DataResult<FarmerData>) -> Unit)
    fun getUserById(farmerId: String, callback: (DataResult<UserResponse>) -> Unit)

    fun updateFarmer(
        farmerId: String,
        farmerData: Map<String, Any>,
        callback: (DataResult<FarmerData>) -> Unit
    )

    fun uploadFarmImage(
        farmerId: String,
        uri: Uri,
        farmerData: FarmerData,
        callback: (DataResult<FarmerData>) -> Unit
    )

    fun deleteFarmImage(
        farmerId: String,
        path: String,
        farmerData: FarmerData,
        callback: (DataResult<FarmerData>) -> Unit
    )
}
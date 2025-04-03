package com.waxd.pos.fcmb.utils.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.rest.FarmCoordinates
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.rest.UserData
import com.waxd.pos.fcmb.rest.UserResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.BVN_NUMBER
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.FARMERS
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.FIRST_NAME
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.LAST_NAME
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.REGISTERED_BY
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper.FirebaseKeys.USERS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseWrapper(private val context: Context) : IFirebaseWrapper {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val agentId: String
        get() = context.decryptData(KeyStore.USER_UID)


    object FirebaseKeys {
        const val FARMERS = "farmers"
        const val USERS = "users"
        const val REGISTERED_BY = "registered_by"
        const val BVN_NUMBER = "bvn_number"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val FARM_LOCATIONS = "farm_locations"
    }


    override fun getFarmers(
        searchQuery: String?, // Optional search query
        lastVisibleDocument: DocumentSnapshot?,
        callback: (ArrayList<FarmerResponse>) -> Unit
    ) {
        if (currentUser != null) {
            val filterList = mutableListOf<Filter>()
            // Apply search filter if searchQuery is provided

            if (!searchQuery.isNullOrEmpty()) {
                val isNumeric = searchQuery.toLongOrNull() != null
                if (isNumeric) {
                    filterList.add(Filter.equalTo(BVN_NUMBER, searchQuery))
                } else
                    filterList.add(
                        Filter.or(
                            Filter.equalTo(FIRST_NAME, searchQuery),
                            Filter.equalTo(LAST_NAME, searchQuery)
                        )
                    )
            }

            filterList.add(Filter.equalTo(REGISTERED_BY, agentId))
            // Combine all filters using Filter.and()
            val combinedFilter = Filter.and(*filterList.toTypedArray())

            // Create a query with pagination
            var query = Firebase.firestore.collection(FARMERS)
                .where(combinedFilter)
                .orderBy("date_created", Query.Direction.DESCENDING)
                .limit(10)


            // Add startAfter if lastVisibleDocument is provided
            if (lastVisibleDocument != null) {
                query = query.startAfter(lastVisibleDocument)
            }

            /*Firebase.firestore.collection(FARMERS).where(combinedFilter)
                .orderBy("date_created", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .limit(10)*/
            query.get()
                .addOnSuccessListener { querySnapshot ->
                    val list = ArrayList<FarmerResponse>()
                    querySnapshot.documents.forEach { document ->
                        list.add(FarmerResponse(documentToFarmerData(document), document))
                    }
                    callback(list)
                }
                .addOnFailureListener { exception ->
                    println("Failed to get farmers: ${exception.message}")
                    callback(ArrayList()) // Return empty list on failure
                }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    override fun getFarmerById(farmerId: String, callback: (DataResult<FarmerData>) -> Unit) {
        if (currentUser != null) {
            callback(DataResult.Loading)
            Firebase.firestore.collection(FARMERS).document(farmerId).get()
                .addOnSuccessListener { querySnapshot ->
                    callback(
                        DataResult.Success(
                            200,
                            documentToFarmerData(querySnapshot)
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    println("Failed to get farmer: ${exception.message}")
                    callback(DataResult.Failure(status = "400", message = exception.message))
                }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    override fun getUserById(farmerId: String, callback: (DataResult<UserResponse>) -> Unit) {
        if (currentUser != null) {
            callback(DataResult.Loading)
            Firebase.firestore.collection(USERS).document(farmerId).get()
                .addOnSuccessListener { querySnapshot ->
                    callback(
                        DataResult.Success(
                            200,
                            UserResponse(
                                documentToUserData(querySnapshot),
                                document = querySnapshot
                            )
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    println("Failed to get farmer: ${exception.message}")
                    callback(DataResult.Failure(status = "400", message = exception.message))
                }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    override fun updateFarmer(
        farmerId: String,
        farmerData: Map<String, Any>,
        callback: (DataResult<FarmerData>) -> Unit
    ) {
        if (currentUser != null) {
            callback(DataResult.Loading)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val currentDate = dateFormat.format(System.currentTimeMillis())
            val updatedFarmerData = farmerData.toMutableMap().apply {
                put("date_updated", currentDate)
            }
            Firebase.firestore.collection(FARMERS).document(farmerId).update(updatedFarmerData)
                .addOnSuccessListener {
                    callback(
                        DataResult.Success(200, FarmerData(id = farmerId))
                    )
                }
                .addOnFailureListener { exception ->
                    println("Failed to get farmer: ${exception.message}")
                    callback(DataResult.Failure(status = "400", message = exception.message))
                }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    fun uploadFarmImages(
        farmerId: String,
        list: ArrayList<Uri>,
        callback: (DataResult<FarmerData>) -> Unit
    ) {
        if (currentUser != null) {
            val storageRef = Firebase.storage.reference
            val uploadedFileRefs = ArrayList<String>()
            callback(DataResult.Loading)
            // Launch a coroutine on the IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Upload files one by one
                    for (uri in list) {
                        uploadImage(storageRef, farmerId, uri, uploadedFileRefs)
                    }

                    // All files uploaded, update the farmer document
                    withContext(Dispatchers.Main) {
                        updateFarmer(
                            farmerId,
                            hashMapOf("farm_photos" to uploadedFileRefs), callback
                        )
                    }
                } catch (e: Exception) {
                    // Handle errors
                    withContext(Dispatchers.Main) {
                        // Notify the user or log the error
                        println("Error uploading files: ${e.message}")
                        callback(DataResult.Failure(status = "400", message = e.message))
                    }
                }
            }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    override fun uploadFarmImage(
        farmerId: String,
        uri: Uri,
        farmerData: FarmerData,
        callback: (DataResult<FarmerData>) -> Unit
    ) {
        if (currentUser != null) {
            val storageRef = Firebase.storage.reference
            val uploadedFileRefs = ArrayList<String>()
            farmerData.farmPhotos?.forEach {
                if (it is String) {
                    uploadedFileRefs.add(it)
                }
            }
            // Launch a coroutine on the IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    uploadImage(storageRef, farmerId, uri, uploadedFileRefs)

                    // All files uploaded, update the farmer document
                    withContext(Dispatchers.Main) {
                        updateFarmer(
                            farmerId,
                            hashMapOf("farm_photos" to uploadedFileRefs), callback
                        )
                    }
                } catch (e: Exception) {
                    // Handle errors
                    withContext(Dispatchers.Main) {
                        // Notify the user or log the error
                        println("Error uploading files: ${e.message}")
                        callback(DataResult.Failure(status = "400", message = e.message))
                    }
                }
            }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    private suspend fun uploadImage(
        storageRef: StorageReference,
        farmerId: String,
        uri: Uri,
        uploadedFileRefs: ArrayList<String>
    ) {
        uri.lastPathSegment?.let {
            // Upload files one by one
            val fileRef =
                storageRef.child("farm_images/${farmerId}/${uri.lastPathSegment}")
            val uploadTask =
                fileRef.putFile(uri).await() // Wait for the upload to complete
//        val downloadUrl = fileRef.downloadUrl.await() // Get the download URL
            uploadedFileRefs.add(it)
        }
    }

    override fun deleteFarmImage(
        farmerId: String,
        path: String,
        farmerData: FarmerData,
        callback: (DataResult<FarmerData>) -> Unit
    ) {
        if (currentUser != null) {
            callback(DataResult.Loading)
            val storageRef = Firebase.storage.reference
            val uploadedFileRefs = ArrayList<String>()
            farmerData.farmPhotos?.forEach {
                if (it is String) {
                    uploadedFileRefs.add(it)
                }
            }
            // Launch a coroutine on the IO dispatcher
            try {
                val fileRef =
                    storageRef.child("farm_images/${farmerId}/${path}")
                fileRef.delete().addOnSuccessListener {
                    // All files uploaded, update the farmer document
                    uploadedFileRefs.remove(path)
                    updateFarmer(
                        farmerId,
                        hashMapOf("farm_photos" to uploadedFileRefs), callback
                    )
                }.addOnFailureListener {
                    callback(DataResult.Failure(status = "400", message = it.message))
                }


            } catch (e: Exception) {
                // Handle errors
                // Notify the user or log the error
                println("Error uploading files: ${e.message}")
                callback(DataResult.Failure(status = "400", message = e.message))
            }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    override fun getRecentFarmers(callback: (DataResult<ArrayList<FarmerResponse>>) -> Unit) {
        if (currentUser != null) {
            callback(DataResult.Loading)
            Firebase.firestore.collection(FARMERS)
                .where(Filter.equalTo(REGISTERED_BY, agentId))
                .orderBy("date_updated", Query.Direction.DESCENDING) // Order by updatedAt DESC
                .limit(5) // Limit to 5 documents
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val list = ArrayList<FarmerResponse>()
                    querySnapshot.documents.forEach { document ->
                        list.add(FarmerResponse(documentToFarmerData(document), document))
                    }
                    callback(DataResult.Success(200, list))
                }
                .addOnFailureListener { exception ->
                    println("Failed to get farmers: ${exception.message}")
                    callback(DataResult.Failure(status = "400", message = exception.message))
                }
        } else {
            FcmbApp.instance.logoutHandler()?.logout()
        }
    }

    private fun documentToFarmerData(document: DocumentSnapshot): FarmerData {
        return FarmerData(
            id = document.id,
            address = document.getString("address"),
            city = document.getString("city"),
            state = document.getString("state"),
            bvnNumber = document.getString("bvn_number"),
            dateCreated = document.getString("date_created"),
            dateUpdated = document.getString("date_updated"),
            dob = document.getString("dob"),
            email = document.getString("email"),
            firstName = document.getString("first_name"),
            haveBvnNumber = document.getBoolean("have_bvn_number"),
            haveNinNumber = document.getBoolean("have_nin_number"),
            lastName = document.getString("last_name"),
            ninNumber = document.getString("nin_number"),
            phoneNumber = document.getString("phone_number"),
            registeredBy = document.getString("registered_by"),
            walletId = document.getString("wallet_id"),
            profileImage = document.getString("profile_image"),
            biometrics = document.get("biometrics") as? ArrayList<String>,
            farmPhotos = document.get("farm_photos") as? ArrayList<*>,
            farmLocations = document.getFarmLocations(),
        )
    }

    private fun documentToUserData(document: DocumentSnapshot): UserData {
        return UserData(
            id = document.id,
            fingerPrintCloudPath = document.get("fingerPrintCloudPath") as? ArrayList<String>,
            fingerPrintSyncedOnCloud = document.getBoolean("fingerPrintSyncedOnCloud"),
        )
    }

    private fun DocumentSnapshot.getFarmLocations(): ArrayList<FarmCoordinates> {
        val list = ArrayList<FarmCoordinates>()
        (get("farm_locations") as? List<*>)?.forEach {
            if (it is Map<*, *>)
                list.add(
                    FarmCoordinates(
                        ((it["lat"]) ?: 0.0) as Double,
                        (it["lng"] ?: 0.0) as Double
                    )
                )
        }
        return list
    }
}
package com.waxd.pos.fcmb.rest

import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginResponse(
    @SerializedName("token") val token: String,
)

data class RegistrationResponse(
    @SerializedName("agentData")
    val agentData: AgentData? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("uid")
    val uid: String? = null
)

data class AgentData(
    @SerializedName("age")
    val age: String? = null,
    @SerializedName("bvn_number")
    val bvnNumber: String? = null,
    @SerializedName("createdAt")
    val createdAt: CreatedAt? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("mobile")
    val mobile: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("nin_number")
    val ninNumber: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("user_name")
    val userName: String? = null
)

data class AgentProfileResponse(
    @SerializedName("age")
    val age: String? = null,
    @SerializedName("bvn_number")
    val bvnNumber: String? = null,
    @SerializedName("createdAt")
    val createdAt: CreatedAt? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("mobile")
    val mobile: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("nin_number")
    val ninNumber: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("uid")
    val uid: String? = null,
    @SerializedName("user_name")
    val userName: String? = null,
    var profileUri: Uri? = null
)

data class CreatedAt(
    @SerializedName("_nanoseconds")
    val nanoseconds: Int? = null,
    @SerializedName("_seconds")
    val seconds: Int? = null
)

data class UploadImageResponse(
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("message")
    val message: String? = null
)

/*{
    "message": "Farmer registered and wallet created successfully",
    "farmerId": "InpuUsL28ZnDgR66ULks",
    "wallet": "4000024074"
}*/
data class FarmerCreateResponse(
    @SerializedName("farmerId")
    val farmerId: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("wallet")
    val wallet: String? = null
)

data class UpdateProfileResponse(
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("updatedProfile")
    var updatedProfile: UpdatedProfile? = null
)

data class UpdatedProfile(
    @SerializedName("age")
    var age: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("gender")
    var gender: String? = null,
    @SerializedName("mobile")
    var mobile: String? = null,
    @SerializedName("name")
    var name: String? = null
)

data class FarmerResponse(
    val farmerData: FarmerData? = null,
    val document: DocumentSnapshot? = null
)

data class FarmerData(
    var id: String? = null,
    @PropertyName("address")
    var address: String? = null,
    @PropertyName("city")
    var city: String? = null,
    @PropertyName("state")
    var state: String? = null,
    @PropertyName("bvn_number")
    var bvnNumber: String? = null,
    @PropertyName("date_created")
    var dateCreated: String? = null,
    @PropertyName("date_updated")
    var dateUpdated: String? = null,
    @PropertyName("dob")
    var dob: String? = null,
    @PropertyName("email")
    var email: String? = null,
    @PropertyName("first_name")
    var firstName: String? = null,
    @PropertyName("have_bvn_number")
    var haveBvnNumber: Boolean? = false,
    @PropertyName("have_nin_number")
    var haveNinNumber: Boolean? = false,
    @PropertyName("last_name")
    var lastName: String? = null,
    @PropertyName("nin_number")
    var ninNumber: String? = null,
    @PropertyName("phone_number")
    var phoneNumber: String? = null,
    @PropertyName("registered_by")
    var registeredBy: String? = null,
    @PropertyName("wallet_id")
    var walletId: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    @SerializedName("biometrics")
    val biometrics: ArrayList<String>? = null,
    @SerializedName("farm_photos")
    val farmPhotos: ArrayList<*>? = null,
    @SerializedName("farm_locations")
    val farmLocations: ArrayList<FarmCoordinates>? = null,
) : Serializable

data class FarmCoordinates(val lat: Double, val lng: Double) : Serializable


data class UserResponse(
    val farmerData: UserData? = null,
    val document: DocumentSnapshot? = null
)


data class UserData(
    var id: String? = null,
    val fingerPrintCloudPath: ArrayList<String>? = null,
    val fingerPrintSyncedOnCloud: Boolean? = null
) : Serializable
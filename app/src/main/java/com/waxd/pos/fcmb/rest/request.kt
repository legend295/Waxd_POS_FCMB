package com.waxd.pos.fcmb.rest

import com.google.gson.annotations.SerializedName
import com.waxd.pos.fcmb.utils.Util.isValidEmail
import com.waxd.pos.fcmb.utils.Util.isValidMobile

class NotValidException(msg: String) : Exception(msg)

data class LoginRequest(
    var email: String? = "harry@gmail.com", var password: String? = "securePass123"
) {
    @Throws(NotValidException::class)
    fun isValid() {
        when {
            email == null || email?.trim()
                ?.isEmpty() == true -> throw NotValidException("Email is required.")

            email?.isValidEmail() == false -> throw NotValidException("Please enter a valid email.")

            password == null || password?.trim()
                ?.isEmpty() == true -> throw NotValidException("Password is required.")

            (password?.length
                ?: 0) < 6 -> throw NotValidException("Password length should be greater than 5.")
        }
    }
}


data class RegistrationRequest(
    var name: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("mobile") var phoneNumber: String? = null,
    @SerializedName("password") var password: String? = null,
    var age: String? = null,
    @SerializedName("bvn_number") var bvnNumber: String? = null,
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("nin_number") var ninNumber: String? = null,
    @SerializedName("profile_image") var profileImage: String? = null,
    @SerializedName("user_name") var userName: String? = null
) {
    @Throws(NotValidException::class)
    fun isFirstScreenValid() {
        when {
            name?.trim().isNullOrEmpty() -> throw NotValidException("Name is required.")
            userName?.trim().isNullOrEmpty() -> throw NotValidException("Username is required.")
            email == null || email?.trim()
                ?.isEmpty() == true -> throw NotValidException("Email is required.")

            email?.isValidEmail() == false -> throw NotValidException("Please enter a valid email.")

            phoneNumber == null || phoneNumber?.trim()
                ?.isEmpty() == true -> throw NotValidException("Phone number required.")

            phoneNumber?.isValidMobile() == false -> throw NotValidException("Please enter a valid phone number.")

            password == null || password?.trim()
                ?.isEmpty() == true -> throw NotValidException("Password is required.")

            (password?.length
                ?: 0) < 6 -> throw NotValidException("Password length should be greater than 5.")

        }
    }

    @Throws(NotValidException::class)
    fun isSecondScreenValid() {
        when {
            age?.trim().isNullOrEmpty() -> throw NotValidException("Age is required.")
            (age?.toInt()
                ?: 0) < 14 -> throw NotValidException("Age should be greater than 13 required.")

            gender?.trim().isNullOrEmpty() -> throw NotValidException("Gender is required.")
            bvnNumber?.trim().isNullOrEmpty() -> throw NotValidException("BVN number is required.")
            (bvnNumber?.length ?: 0) < 11 -> throw NotValidException("BVN number should be valid.")

            ninNumber?.trim().isNullOrEmpty() -> throw NotValidException("NIN number is required.")
            (ninNumber?.length ?: 0) < 11 -> throw NotValidException("NIN number should be valid.")
        }
    }


}

data class FarmerCreateRequest(
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("biometrics")
    var biometrics: ArrayList<Any?>? = null,
    @SerializedName("bvn_number")
    var bvnNumber: String? = null,
    @SerializedName("city")
    var city: String? = null,
    @SerializedName("dob")
    var dob: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("farm_locations")
    var farmLocations: ArrayList<Any?>? = null,
    @SerializedName("farm_photos")
    var farmPhotos: ArrayList<Any?>? = null,
    @SerializedName("first_name")
    var firstName: String? = null,
    @SerializedName("have_bvn_number")
    var haveBvnNumber: Boolean? = false,
    @SerializedName("have_nin_number")
    var haveNinNumber: Boolean? = false,
    @SerializedName("last_name")
    var lastName: String? = null,
    @SerializedName("nin_number")
    var ninNumber: String? = null,
    @SerializedName("phone_number")
    var phoneNumber: String? = null,
    @SerializedName("profile_image")
    var profileImage: String? = null,
    @SerializedName("state")
    var state: String? = null
) {
    @Throws(NotValidException::class)
    fun isValid() {
        when {
            haveBvnNumber == true && bvnNumber?.trim().isNullOrEmpty() -> throw NotValidException("BVN number is required.")
            haveBvnNumber == true && (bvnNumber?.length ?: 0) < 11 -> throw NotValidException("BVN number should be valid.")

            haveNinNumber == true && ninNumber?.trim().isNullOrEmpty() -> throw NotValidException("NIN number is required.")
            haveNinNumber == true && (ninNumber?.length ?: 0) < 11 -> throw NotValidException("NIN number should be valid.")

            firstName?.trim().isNullOrEmpty() -> throw NotValidException("First name is required.")
            lastName?.trim().isNullOrEmpty() -> throw NotValidException("Last name is required.")
            phoneNumber == null || phoneNumber?.trim()
                ?.isEmpty() == true -> throw NotValidException("Phone number is required.")

            phoneNumber?.isValidMobile() == false -> throw NotValidException("Please enter a valid phone number.")
            email == null || email?.trim()
                ?.isEmpty() == true -> throw NotValidException("Email is required.")

            email?.isValidEmail() == false -> throw NotValidException("Please enter a valid email.")
            dob?.trim().isNullOrEmpty() -> throw NotValidException("Date of birth is required.")
            address?.trim().isNullOrEmpty() -> throw NotValidException("Address is required.")
            city?.trim().isNullOrEmpty() -> throw NotValidException("City is required.")
            state?.trim().isNullOrEmpty() -> throw NotValidException("State is required.")

//            biometrics.isNullOrEmpty() -> throw NotValidException("Biometrics are required.")

//            farmLocations.isNullOrEmpty() -> throw NotValidException("Farm locations are required.")
//            farmPhotos.isNullOrEmpty() -> throw NotValidException("Farm photos are required.")

        }
    }

    fun setFarmerData(farmerData: FarmerData) {
        address = farmerData.address
        bvnNumber = farmerData.bvnNumber
        city = farmerData.city
        dob = farmerData.dob
        email = farmerData.email
        firstName = farmerData.firstName
        haveBvnNumber = farmerData.haveBvnNumber
        haveNinNumber = farmerData.haveNinNumber
        lastName = farmerData.lastName
        ninNumber = farmerData.ninNumber
        phoneNumber = farmerData.phoneNumber
        profileImage = farmerData.profileImage
        state = farmerData.state
    }
}

data class UpdateAgentProfileRequest(
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
) {
    fun setProfileData(profile: AgentProfileResponse) {
        name = profile.name
        email = profile.email
        mobile = profile.mobile
        age = profile.age
        gender = profile.gender
    }

    @Throws(NotValidException::class)
    fun isValid() {
        when {
            name?.trim().isNullOrEmpty() -> throw NotValidException("Name is required.")

            email == null || email?.trim()
                ?.isEmpty() == true -> throw NotValidException("Email is required.")

            email?.isValidEmail() == false -> throw NotValidException("Please enter a valid email.")

            mobile == null || mobile?.trim()
                ?.isEmpty() == true -> throw NotValidException("Phone number required.")

            mobile?.isValidMobile() == false -> throw NotValidException("Please enter a valid phone number.")

            age?.trim().isNullOrEmpty() -> throw NotValidException("Age is required.")
            (age?.toInt()
                ?: 0) < 14 -> throw NotValidException("Age should be greater than 13 required.")

            gender?.trim().isNullOrEmpty() -> throw NotValidException("Gender is required.")

        }
    }


}
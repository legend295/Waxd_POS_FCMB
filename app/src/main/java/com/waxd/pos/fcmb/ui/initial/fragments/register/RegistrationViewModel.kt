package com.waxd.pos.fcmb.ui.initial.fragments.register

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.waxd.pos.fcmb.base.BaseApiResponse
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.base.SingletonWrapper
import com.waxd.pos.fcmb.rest.LoginRequest
import com.waxd.pos.fcmb.rest.LoginResponse
import com.waxd.pos.fcmb.rest.RegistrationRequest
import com.waxd.pos.fcmb.rest.RegistrationResponse
import com.waxd.pos.fcmb.rest.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.truncate

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val restRepository: RestRepository
) :
    ViewModel() {

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    val isSecondStep = MutableLiveData(false)
    val request: MutableLiveData<RegistrationRequest> = MutableLiveData(RegistrationRequest())
    val response: MutableLiveData<DataResult<RegistrationResponse>> = MutableLiveData()

    fun createUser() {
        viewModelScope.launch {
            request.value?.apply {
                if (phoneNumber?.startsWith("234") == false && (phoneNumber?.length ?: 0) <= 10) {
                    phoneNumber = "234$phoneNumber"
                }
                response.value = DataResult.Loading
                restRepository.registerAgent(this).collect {
                    response.value = it
                }
            }

        }
    }

    private fun Activity.sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Verification email sent to ${user.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun RegistrationRequest.updateProfile(callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.let {
            // Store additional user data in Firestore
            val userData = hashMapOf(
                "name" to name,
                "email" to email
            )
            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { e ->
                    callback(false)
                }
        }
    }

}


/* auth.createUserWithEmailAndPassword(email!!, password!!)
               .addOnCompleteListener(context) { task ->
                   if (task.isSuccessful) {
                       auth.currentUser?.let { context.sendVerificationEmail(it) }
                       updateProfile {
                           response.value = DataResult.Success(data = task.result)
                       }
                   }
               }.addOnFailureListener {
                   response.value = DataResult.Failure(
                       message = it.message ?: "Registration failed. Please try again.",
                       status = "404"
                   )
               }*/
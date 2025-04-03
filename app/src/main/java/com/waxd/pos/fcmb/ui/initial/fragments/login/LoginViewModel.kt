package com.waxd.pos.fcmb.ui.initial.fragments.login

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.PhoneAuthOptions
import com.waxd.pos.fcmb.base.BaseApiResponse
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.LoginRequest
import com.waxd.pos.fcmb.rest.LoginResponse
import com.waxd.pos.fcmb.rest.RestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val restRepository: RestRepository) : ViewModel() {

    // Initialize Firebase Auth
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val request: MutableLiveData<LoginRequest> = MutableLiveData(LoginRequest())
    val response: MutableLiveData<DataResult<AuthResult>> = MutableLiveData()
    val tokenResponse: MutableLiveData<DataResult<GetTokenResult>> = MutableLiveData()


    fun signInUser(context: Activity) {
        request.value?.apply {
            email ?: return
            password ?: return
            response.value = DataResult.Loading
            auth.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(context) { task ->
                    if (task.isSuccessful) {
                        response.value = DataResult.Success(data = task.result)

                    }
                }.addOnFailureListener {
                    response.value = DataResult.Failure(
                        message = it.message ?: "Login failed. Please try again.", status = "404"
                    )
                }
        }
    }

    fun getToken() {
        tokenResponse.value = DataResult.Loading
        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                tokenResponse.value = DataResult.Success(data = tokenTask.result)
            } else {
                tokenResponse.value = DataResult.Failure(
                    message = "Login failed. Please try again.",
                    status = "404"
                )
            }
        }
    }

    private fun Activity.sendVerificationEmail(user: FirebaseUser, callback: (Boolean) -> Unit) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Verification email sent to ${user.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(true)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send verification email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
    }
}
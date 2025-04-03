package com.waxd.pos.fcmb.ui.initial.fragments.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentLoginBinding
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.encryptData
import com.waxd.pos.fcmb.rest.NotValidException
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(), ViewClickHandler {

    private val TAG = "LoginFragment"
    private val viewModel: LoginViewModel by viewModels()

    override fun getTitle(): String =""

    override fun getLayoutRes(): Int = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            binding.viewModel = viewModel
            init()
            binding.viewClickHandler = this
        }
    }

    override fun init() {
        setObserver()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
//            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }



        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
//            storedVerificationId = verificationId
//            resendToken = token
        }
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                    updateBtnLoginUI(isVisible = false)
                }

                DataResult.Loading -> {
                    updateBtnLoginUI(isVisible = true)
                }

                is DataResult.Success -> {
                    it.data.user?.uid?.let { it1 ->
                        KeyStore.generateKey(KeyStore.USER_UID)
                        context?.encryptData(KeyStore.USER_UID, it1)
                        val options = PhoneAuthOptions.newBuilder(viewModel.auth)
                            .setPhoneNumber("+917347364276")
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(requireActivity()).setCallbacks(callbacks).build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                        viewModel.getToken()
                    }

                }
            }
        }

        viewModel.tokenResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {}
                DataResult.Loading -> {}
                is DataResult.Success -> {
                    it.data.token?.let { it1 ->
                        KeyStore.generateKey(KeyStore.USER_TOKEN)
                        context?.encryptData(KeyStore.USER_TOKEN, it1)
                        (activity as InitialActivity?)?.startMainActivity()
                    }

                }
            }
        }
    }

    private fun updateBtnLoginUI(isVisible: Boolean) {
        binding.progressBar.visible(isVisible)
        binding.btnLogin.isEnabled = !isVisible
        binding.btnLogin.alpha = if (isVisible) 0.5f else 1f
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnLogin -> {
                activity?.let {
                    try {
                        viewModel.request.value?.isValid()
                        viewModel.signInUser(it)
                    } catch (e: NotValidException) {
                        e.message?.let { message ->
                            showToast(message)
                        }
                    }
                }
            }

            R.id.tvRegister -> {
                this.view?.findNavController()
                    ?.navigate(R.id.action_loginFragment_to_registrationFragment)
            }
        }
    }

}
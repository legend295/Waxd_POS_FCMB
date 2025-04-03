package com.waxd.pos.fcmb.ui.initial.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentSplashBinding
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun getTitle(): String =""

    override fun getLayoutRes(): Int = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {

        lifecycleScope.launch {
            val token = context?.decryptData(KeyStore.USER_TOKEN)
            delay(1000)
            if (token.isNullOrEmpty()) {
                this@SplashFragment.view?.findNavController()?.navigate(R.id.getStartedFragment)
            } else {
                (activity as InitialActivity?)?.startMainActivity()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({

        }, 1000)
    }


}
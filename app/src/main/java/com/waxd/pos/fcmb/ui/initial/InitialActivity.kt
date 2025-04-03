package com.waxd.pos.fcmb.ui.initial

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseActivity
import com.waxd.pos.fcmb.databinding.ActivityInitialBinding
import com.waxd.pos.fcmb.ui.initial.fragments.register.RegistrationFragment
import com.waxd.pos.fcmb.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InitialActivity : BaseActivity() {

    private var navHostFragment: NavHostFragment? = null

    private var binding: ActivityInitialBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_initial)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_initial) as NavHostFragment

        handleBackPress()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val splashScreen = installSplashScreen()
          /*  splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()

            }*/
        } else {
//            startMainActivity()
        }


    }

    fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun handleBackPress() {
        val backHandler = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container_initial)
                val currentDestination = navHostFragment?.navController?.currentDestination
                if (currentDestination?.id == R.id.loginFragment) {
                    finish()
                } else if(currentDestination?.id == R.id.registrationFragment){
                    if (fragment?.childFragmentManager?.fragments?.get(0) is RegistrationFragment){
                        (fragment.childFragmentManager.fragments[0] as RegistrationFragment).handleBack()
                    }
                } else {
                    navHostFragment?.navController?.popBackStack()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this,backHandler)
    }

}
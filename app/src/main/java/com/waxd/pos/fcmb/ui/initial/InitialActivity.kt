package com.waxd.pos.fcmb.ui.initial

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseActivity
import com.waxd.pos.fcmb.databinding.ActivityInitialBinding
import com.waxd.pos.fcmb.ui.main.MainActivity
import kotlinx.coroutines.launch

class InitialActivity : BaseActivity() {

    private var binding: ActivityInitialBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_initial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

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

}
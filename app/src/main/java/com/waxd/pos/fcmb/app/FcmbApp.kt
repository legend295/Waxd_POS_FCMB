package com.waxd.pos.fcmb.app

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.scanner.utils.builder.ThemeOptions
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.utils.NetworkMonitor
import com.waxd.pos.fcmb.utils.handlers.ILogoutHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FcmbApp : Application(), Configuration.Provider {

    companion object {
        lateinit var instance: FcmbApp
        val farmerImagesMap = HashMap<String, Uri>()
        val farmImagesMap = HashMap<String, Uri>()
        val themeOptions = ThemeOptions().apply {
            buttonColor = R.color.pear
            buttonTextColor = R.color.forestGreen
            messageColor = R.color.forestGreen
            titleTextColor = R.color.black
            contentTextColor = R.color.black
            buttonBackground = R.drawable.bg_round_corner_8
            popUpBackground = R.drawable.bg_round_corner_8
        }
    }


    private var logOutHandler: ILogoutHandler? = null

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        instance = this

        // Register network monitor to trigger immediate dispatch when internet available
        networkMonitor.register()
    }

    fun setLogoutHandler(handler: ILogoutHandler) {
        this.logOutHandler = handler
    }

    fun logoutHandler() = logOutHandler

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: java.lang.Exception) {
            if (e.message!!.contains("Unknown calling package name")) {
                // Retry after short delay
                print("Unknown calling package name")
                Handler(Looper.getMainLooper()).postDelayed(Runnable { initializeFirebase() }, 1000)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
package com.waxd.pos.fcmb.app

import android.app.Application
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.scanner.utils.builder.ThemeOptions
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.utils.handlers.ILogoutHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FcmbApp : Application() {

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

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this);
        instance = this
    }

    fun setLogoutHandler(handler: ILogoutHandler) {
        this.logOutHandler = handler
    }

    fun logoutHandler() = logOutHandler
}
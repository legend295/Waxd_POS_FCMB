package com.waxd.pos.fcmb.app

import android.app.Application
import com.google.firebase.FirebaseApp

class FcmbApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this);
    }
}
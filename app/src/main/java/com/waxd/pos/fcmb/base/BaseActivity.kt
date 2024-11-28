package com.waxd.pos.fcmb.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity(), BaseHandler {

    override fun showToast(msg: String, length: Int) {
        Toast.makeText(this, msg, length).show()
    }
}
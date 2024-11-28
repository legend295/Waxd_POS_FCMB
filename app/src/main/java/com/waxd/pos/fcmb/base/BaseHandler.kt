package com.waxd.pos.fcmb.base

import android.widget.Toast

interface BaseHandler {

    fun showToast(msg: String, length: Int = Toast.LENGTH_SHORT)
}
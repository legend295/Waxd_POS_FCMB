package com.waxd.pos.fcmb.base

import android.widget.Toast
import androidx.navigation.NavOptions
import com.waxd.pos.fcmb.utils.handlers.LocationPermissionHandler

interface BaseHandler {

    fun showToast(msg: String, length: Int = Toast.LENGTH_SHORT)

    fun isLocationPermissionGranted(handler: LocationPermissionHandler)

    fun isLocationEnabled(): Boolean
    fun checkStoragePermission(): Boolean

    fun getStoragePermissionArray(): Array<String>

    fun getNavOptions(): NavOptions
}
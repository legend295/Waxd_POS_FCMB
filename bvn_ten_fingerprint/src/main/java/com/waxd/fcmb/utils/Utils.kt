package com.waxd.fcmb.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.waxd.fcmb.models.NigerianState
import java.io.InputStream

object Utils {

    fun Context.loadJsonArrayFromRaw(resourceId: Int): NigerianState? {
        return try {
            val inputStream: InputStream = resources.openRawResource(resourceId)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<NigerianState>() {}.type
            Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
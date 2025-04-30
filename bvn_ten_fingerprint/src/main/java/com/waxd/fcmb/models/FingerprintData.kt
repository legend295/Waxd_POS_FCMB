package com.waxd.fcmb.models

import android.net.Uri
import com.waxd.fcmb.enums.FingerPrints

data class FingerprintData(
    val fingerPrintType: FingerPrints,
    var title: String,
    var imagePath: String? = null,
    var imageUri: Uri? = null
)

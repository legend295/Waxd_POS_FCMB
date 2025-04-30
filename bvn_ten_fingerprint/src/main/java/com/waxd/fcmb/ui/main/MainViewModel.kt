package com.waxd.fcmb.ui.main

import androidx.lifecycle.ViewModel
import com.waxd.fcmb.enums.FingerPrints
import com.waxd.fcmb.models.FingerprintData

class MainViewModel : ViewModel() {

    val list = ArrayList<FingerprintData>()

    init {
        list.clear()
        list.addAll(getFingerListList())
    }

    private fun getFingerListList() = ArrayList<FingerprintData>().apply {
        add(FingerprintData(fingerPrintType = FingerPrints.THUMB_LEFT, title = "Left Thumb"))
        add(FingerprintData(fingerPrintType = FingerPrints.THUMB_RIGHT, title = "Right Thumb"))
        add(FingerprintData(fingerPrintType = FingerPrints.INDEX_LEFT, title = "Left Index"))
        add(FingerprintData(fingerPrintType = FingerPrints.INDEX_RIGHT, title = "Right Index"))
        add(FingerprintData(fingerPrintType = FingerPrints.MIDDLE_LEFT, title = "Left Middle"))
        add(FingerprintData(fingerPrintType = FingerPrints.MIDDLE_RIGHT, title = "Right Middle"))
        add(FingerprintData(fingerPrintType = FingerPrints.RING_LEFT, title = "Left Ring"))
        add(FingerprintData(fingerPrintType = FingerPrints.RING_RIGHT, title = "Right Ring"))
        add(FingerprintData(fingerPrintType = FingerPrints.LITTLE_LEFT, title = "Left Little"))
        add(FingerprintData(fingerPrintType = FingerPrints.LITTLE_RIGHT, title = "Right Little"))
    }
}
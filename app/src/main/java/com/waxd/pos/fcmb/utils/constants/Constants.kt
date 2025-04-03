package com.waxd.pos.fcmb.utils.constants

object Constants {

    const val SESSION_EXPIRED = "session_expired"
    const val MESSAGE_INTENT = "message"
    const val CONNECTION_ERROR =
        "You are not connected to the internet. Please check your connection."
    const val STATUS_CODE = "status_code"

    object ApiKeys {
        const val BASE_64_IMAGE = "base64Image"
        const val FARMER_ID = "farmerId"
    }

    object FromScreen {
        const val FROM = "from"
        const val PROFILE = 1
        const val FARMER_DETAILS = 2
    }

    object IntentKeys {
        const val FARMER_ID = "farmer_id"
        const val DATA = "data"
        const val CO_ORDINATES = "co_ordinates"
        const val CO_ORDINATES_DATA = "co_ordinates_data"
    }
}
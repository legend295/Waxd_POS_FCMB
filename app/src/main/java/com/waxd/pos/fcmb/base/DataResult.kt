package com.waxd.pos.fcmb.base

sealed class DataResult<out T> {

    data object Loading : DataResult<Nothing>()
    data class Success<out T>(val statusCode: Int? = 0, val data: T) :
        DataResult<T>()

    data class Failure(
        val statusCode: Int? = 0,//404, 401, 422
        val status: String,
        val message: String? = null,
        val data: String? = ""
    ) : DataResult<Nothing>()
}
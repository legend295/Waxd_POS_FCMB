package com.waxd.pos.fcmb.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.datastore.DataStoreWrapper
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import com.waxd.pos.fcmb.ui.main.MainActivity
import com.waxd.pos.fcmb.utils.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseDataSource(
    val context: Context,
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
) {
    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): DataResult<T> {
        var responseData: DataResult<T>

        try {
            val response = withContext(dispatcherProvider.io) {
                call.invoke()
            }

            responseData = if (response.isSuccessful) {
                DataResult.Success(response.code(), response.body()!!)
            } else {
                val responseBody = response.errorBody()!!
                val errorMessage = convertJson(responseBody)
                if (response.code() == 403) {
                    handleSessionExpiry()

                }
                DataResult.Failure(
                    response.code(),
                    status = "error",
                    message = if (errorMessage.isEmpty()) if (response.code() == 404) "Not Found." else "Something went wrong. Please try again." else errorMessage[0].toString(),
                    data = null
                )

            }
        } catch (e: Exception) {
            withContext(dispatcherProvider.main) {
                e.printStackTrace()
                responseData = when (e) {
                    is ConnectException, is SocketTimeoutException, is UnknownHostException, is IOException -> {
                        DataResult.Failure(
                            status = "error",
                            message = "You are not connected to internet."
                        )
                    }

                    else -> {
                        DataResult.Failure(status = "error", message = e.message)
                    }
                }
            }
        }

        return responseData
    }

    private fun convertJson(responseBody: ResponseBody): ArrayList<Any> {
        val list = ArrayList<Any>()
        return try {
            val jsonObject = JSONObject(responseBody.string())
//            list.add(jsonObject.getString("status"))
            list.add(jsonObject.getString("error"))
            list
        } catch (e: Exception) {
            list
        }
    }

    private fun handleSessionExpiry() {
        FcmbApp.instance.logoutHandler()?.logout()

    }
}
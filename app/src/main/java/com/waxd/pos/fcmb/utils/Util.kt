package com.waxd.pos.fcmb.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import com.waxd.pos.fcmb.BuildConfig
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.utils.Util.loadImage
import com.waxd.pos.fcmb.utils.constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sin

object Util {

    fun Context.showToast(msg: String?, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, "$msg", length).show()
    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun String.isValidMobile(): Boolean {
        return Patterns.PHONE.matcher(this).matches()
    }

    fun View.visible(isVisible: Boolean, animate: Boolean = false, duration: Long = 500) {
        if (visibility == if (isVisible) View.VISIBLE else View.GONE) return

        if (animate && isVisible) {
            visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(
                this,
                "alpha",
                0f,
                1f
            )
            animator.duration = duration
            animator.start()
        } else {
            visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    fun Context.isInternetAvailable(showMessage: Boolean = false): Boolean {
        val result: Boolean
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: run {
            if (showMessage)
                showToast(Constants.CONNECTION_ERROR)
            return false
        }
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: run {
                if (showMessage)
                    showToast(Constants.CONNECTION_ERROR)
                return false
            }
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        if (showMessage && !result) {
            showToast(Constants.CONNECTION_ERROR)
        }

        return result
    }

    fun ImageView.loadImage(url: Any?, color: Int = R.color.northTexasGreen) {
        try {
            if (url == null) {
                Glide.with(this).applyDefaultRequestOptions(RequestOptions().fitCenter())
                    .load(ContextCompat.getDrawable(this.context, R.drawable.placeholder))
                    .apply(RequestOptions.centerCropTransform()).into(this)
                return
            }
            Glide.with(this@loadImage).load(url)
                .placeholder(
                    this@loadImage.getCircularDrawable(
                        ContextCompat.getColor(
                            this@loadImage.context,
                            color
                        )
                    )
                ).error({
                    ContextCompat.getDrawable(this@loadImage.context, R.drawable.placeholder)
                }).apply(RequestOptions.centerCropTransform())
                .addListener(this@loadImage.listener())
                .into(this@loadImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ImageView.listener(): RequestListener<Drawable> {
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                e?.let {
                    if (it.rootCauses.isNotEmpty()) {
                        if (e.rootCauses[0].cause is FileNotFoundException) {
                            val drawable = ContextCompat.getDrawable(
                                this@listener.context,
                                R.drawable.placeholder
                            )
                            Handler(Looper.getMainLooper()).post {
                                Glide.with(this@listener)
                                    .applyDefaultRequestOptions(RequestOptions().fitCenter())
                                    .load(drawable).into(this@listener)
                            }
                        }
                    }
                }
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        }
    }


    private fun ImageView.getCircularDrawable(color: Int): CircularProgressDrawable {
        val circularProgressDrawable =
            CircularProgressDrawable(this.context)
        circularProgressDrawable.strokeWidth = 4f
        circularProgressDrawable.centerRadius = 15f
        circularProgressDrawable.setColorSchemeColors(color)
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    fun ImageView.loadFarmerImage(imageUrl: String?, color: Int = R.color.northTexasGreen) {
        var url = imageUrl
        if (url == null) {
            Glide.with(this).applyDefaultRequestOptions(RequestOptions().fitCenter())
                .load(ContextCompat.getDrawable(this.context, R.drawable.placeholder))
                .apply(RequestOptions.centerCropTransform()).into(this)
            return
        }
        try {
            val imageName = url.split("/")[5]
            if (FcmbApp.farmerImagesMap.containsKey(imageName)) {
                val uri = FcmbApp.farmerImagesMap[imageName]
                loadImage(uri, color)
            } else {
                url = BuildConfig.FARMER_STORAGE_URL + imageName
                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(url).downloadUrl.addOnSuccessListener { uri ->
                        FcmbApp.farmerImagesMap[imageName] = uri
                        loadImage(uri, color)
                    }.addOnFailureListener {
                        loadImage(R.drawable.placeholder)
                    }
            }
        } catch (e: Exception) {
            loadImage(R.drawable.placeholder)
        }
    }

    fun ImageView.loadFarmImage(imageUrl: String?, color: Int = R.color.northTexasGreen) {
        var url = imageUrl
        if (url == null) {
            Glide.with(this).applyDefaultRequestOptions(RequestOptions().fitCenter())
                .load(ContextCompat.getDrawable(this.context, R.drawable.placeholder))
                .apply(RequestOptions.centerCropTransform()).into(this)
            return
        }
        try {
            val imageName = url/*.split("/")[5]*/
            if (FcmbApp.farmImagesMap.containsKey(imageName)) {
                val uri = FcmbApp.farmImagesMap[imageName]
                loadImage(uri, color)
            } else {
                url = BuildConfig.FARM_STORAGE_URL + imageName
                FirebaseStorage.getInstance()
                    .getReferenceFromUrl(url).downloadUrl.addOnSuccessListener { uri ->
                        FcmbApp.farmImagesMap[imageName] = uri
                        loadImage(uri, color)
                    }.addOnFailureListener {
                        loadImage(R.drawable.placeholder)
                    }
            }
        } catch (e: Exception) {
            loadImage(R.drawable.placeholder)
        }
    }


    @OptIn(FlowPreview::class)
    fun EditText.debouncedTextChanges(
        debounceDuration: Long = 300, // Debounce time in milliseconds
        coroutineScope: CoroutineScope,
        onTextChanged: (String) -> Unit
    ) {
        val textFlow = MutableStateFlow("")

        // Observe text changes and emit to the flow
        this.doAfterTextChanged { editable ->
            val text = editable?.toString() ?: ""
            coroutineScope.launch {
                textFlow.emit(text)
            }
        }

        // Collect the flow with debounce
        coroutineScope.launch(Dispatchers.Main) {
            textFlow
                .debounce(debounceDuration) // Apply debounce
                .collectLatest { text ->
                    onTextChanged(text) // Invoke the callback
                }
        }
    }

    fun convertImageFileToBase64(imageFile: File): String? {
        try {
            if (imageFile.exists())
                return ByteArrayOutputStream().use { outputStream ->
                    Base64OutputStream(outputStream, Base64.NO_WRAP).use { base64FilterStream ->
                        imageFile.inputStream().use { inputStream ->
                            inputStream.copyTo(base64FilterStream)
                        }
                    }
                    // Step 3: Determine the MIME type
                    val mimeType = when (imageFile.extension.lowercase()) {
                        "png" -> "image/png"
                        "jpg", "jpeg" -> "image/jpeg"
                        "gif" -> "image/gif"
                        "webp" -> "image/webp"
                        else -> null // Unsupported format
                    }
                    return@use "data:$mimeType;base64,${outputStream.toString(Charsets.UTF_8.name())}"
                }
            else return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun calculateArea(coordinates: List<LatLng>): Double {
        val earthRadius = 6371000.0 // Earth radius in meters
        val latLngList = coordinates + listOf(coordinates[0]) // Close the polygon

        var area = 0.0
        for (i in 0 until latLngList.size - 1) {
            val p1 = latLngList[i]
            val p2 = latLngList[i + 1]
            area += Math.toRadians(p2.longitude - p1.longitude) *
                    (2 + sin(Math.toRadians(p1.latitude)) + sin(Math.toRadians(p2.latitude)))
        }
        area = abs(area * earthRadius * earthRadius / 2)
        return area
    }

    fun String.covertTimeToText(): String? {
        var convTime: String? = null
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val pasTime = dateFormat.parse(this)
            val nowTime = Date()
            pasTime?.let {
                val dateDiff = nowTime.time - pasTime.time
                val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
                val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
                val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
                val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
                if (second < 60) {
                    convTime = "$second Seconds $suffix"
                } else if (minute < 60) {
                    convTime = "$minute ${if (minute.toInt() == 1) "Minute" else "Minutes"} $suffix"
                } else if (hour < 24) {
                    convTime = "$hour ${if (hour.toInt() == 1) "Hour" else "Hours"} $suffix"
                } else if (day < 6) {
                    convTime = "$day ${if (day.toInt() == 1) "Day" else "Days"} $suffix"
                } else if (day >= 7) {
                    convTime = if (day > 360) {
                        (day / 360).toString() + " Years " + suffix
                    } else if (day > 30) {
                        (day / 30).toString() + " Months " + suffix
                    } else {
                        (day / 7).toString() + " Week " + suffix
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message ?: "")
        }
        return convTime
    }
}
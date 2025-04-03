package com.waxd.pos.fcmb.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class TopCropTransformation : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // Calculate the aspect ratio of the target dimensions
        val aspectRatio = outWidth.toFloat() / outHeight.toFloat()

        // Get the original image dimensions
        val originalWidth = toTransform.width
        val originalHeight = toTransform.height

        // Calculate the new height based on the aspect ratio
        val newHeight = (originalWidth / aspectRatio).toInt()

        // Crop the top part of the image
        return Bitmap.createBitmap(
            toTransform,
            0, // Start from the left
            0, // Start from the top
            originalWidth,
            newHeight
        )
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("top_crop".toByteArray())
    }
}
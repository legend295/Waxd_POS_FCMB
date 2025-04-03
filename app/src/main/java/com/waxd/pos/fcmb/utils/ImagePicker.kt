package com.waxd.pos.fcmb.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.waxd.pos.fcmb.base.BaseFragment
import java.io.File
import java.io.IOException

interface OnImagePickedListener {
    fun onImagePicked(file: File, uri: Uri?)
}

class ImagePicker(
    private val fragment: BaseFragment<*>,
    private val listener: OnImagePickedListener
) {

    private var cameraUri: Uri? = null

    private val permissionLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isGranted = it.values.all { value -> value }
            if (isGranted) {
                openImageChoiceSheet()
            }
        }

    fun openImagePicker() {
        if (fragment.checkStoragePermission()) {
            openImageChoiceSheet()
        } else permissionLauncher.launch(fragment.getStoragePermissionArray())
    }

    private fun openImageChoiceSheet() {
        fragment.context?.showImagePickerDialog { isCamera ->
            if (isCamera) {
                openCamera()
            } else {
                openGallery()
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        var photoFile: File? = null
        try {
            photoFile = fragment.requireContext().createImageFile()
        } catch (ex: IOException) {
            Log.d("DEBUG", "Exception while creating file: $ex")
        }
        if (photoFile != null) {
            Log.d("DEBUG", "Photo file not null")
            cameraUri = FileProvider.getUriForFile(
                fragment.requireActivity(),
                "${fragment.requireActivity().packageName}.provider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            cameraImageLauncher.launch(takePictureIntent)
        }

    }

    private fun Context.createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = System.currentTimeMillis().toString()
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpeg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        val actualImage = image.absolutePath
        Log.d("DEBUG", "Path: $actualImage")
        return image
    }

    private fun openGallery() {
        val mimeType = arrayOf("image/jpeg", "image/png")
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        galleryImageLauncher.launch(intent)
    }

    private var galleryImageLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    val url = data?.data
                    url?.let { uri ->
                        val file = FileUtil.uriToFile(fragment.requireContext(), uri)
                        file?.let { listener.onImagePicked(it, uri) }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private var cameraImageLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val file = cameraUri?.let {
                        FileUtil.uriToFile(fragment.requireActivity(), it)
                    }
                    file?.let { listener.onImagePicked(it, cameraUri) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
}
package com.waxd.pos.fcmb.ui.main.fragments.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.common.GlobalHistogramBinarizer
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentProfileBinding
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.rest.AgentProfileResponse
import com.waxd.pos.fcmb.ui.main.MainActivity
import com.waxd.pos.fcmb.utils.FileUtil
import com.waxd.pos.fcmb.utils.FileUtil.getMimeType
import com.waxd.pos.fcmb.utils.Util.loadImage
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.showImagePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(), ViewClickHandler {

    private val viewModel: ProfileViewModel by viewModels()
    private var cameraUri: Uri? = null
    private var agentProfile: AgentProfileResponse? = null

    override fun getTitle(): String = "Profile"

    override fun getLayoutRes(): Int = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) init()
    }

    override fun init() {
        binding.viewClickHandler = this
        setObserver()
        context?.let {
            val uid = it.decryptData(KeyStore.USER_UID)
            viewModel.getAgentById(uid)
        }
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {

                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {
                }

                is DataResult.Success -> {
                    agentProfile = it.data
                    binding.user = it.data
                    if (it.data.profileUri != null) {
                        binding.ivUser.loadImage(it.data.profileUri)
                    } else
                        it.data.profileImage?.let { it1 ->
                            val url =
                                "gs://fcmb-aggri-staging.firebasestorage.app/profile_images/" + it1.split(
                                    "/"
                                )[5]
                            println(url)
                            FirebaseStorage.getInstance()
                                .getReferenceFromUrl(url).downloadUrl.addOnSuccessListener { uri ->
                                    println(uri)
                                    it.data.profileUri = uri
                                    binding.ivUser.loadImage(uri)
                                }

                        }

                }
            }
        }

        viewModel.uploadImageResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                    if (agentProfile?.profileUri != null) {
                        binding.ivUser.loadImage(agentProfile?.profileUri)
                    } else
                        agentProfile?.profileImage?.let { it1 ->
                            val url =
                                "gs://fcmb-aggri-staging.firebasestorage.app/profile_images/" + it1.split(
                                    "/"
                                )[5]
                            println(url)
                            FirebaseStorage.getInstance()
                                .getReferenceFromUrl(url).downloadUrl.addOnSuccessListener { uri ->
                                    println(uri)
                                    agentProfile?.profileUri = uri
                                    binding.ivUser.loadImage(uri)
                                }

                        } ?: run {
                            Glide.with(binding.ivUser).load(R.drawable.ic_user).into(binding.ivUser)
                        }
                    handleUploadUI(isInProgress = false)
                }

                DataResult.Loading -> {
                    handleUploadUI(true)
                }

                is DataResult.Success -> {
                    handleUploadUI(isInProgress = false)
                    context?.let { context ->
                        val uid = context.decryptData(KeyStore.USER_UID)
                        viewModel.getAgentById(uid)
                    }
                }
            }
        }
    }

    private fun handleUploadUI(isInProgress: Boolean) {
        binding.ivEdit.visible(isVisible = !isInProgress)
        binding.ivUploadBlur.visible(isInProgress)
        binding.progressBar.visible(isInProgress)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvLogout -> {
                (activity as MainActivity?)?.logOut()
            }

            R.id.ivEdit -> {
                openImagePicker()
            }

            R.id.tvEditProfile -> {
                this.view?.findNavController()?.navigate(R.id.updateProfileFragment)
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isGranted = it.values.all { value -> value }
            if (isGranted) {
                openImageChoiceSheet()
            }
        }

    private fun openImagePicker() {
        if (checkStoragePermission()) {
            openImageChoiceSheet()
        } else permissionLauncher.launch(getStoragePermissionArray())
    }

    private fun openImageChoiceSheet() {
        requireContext().showImagePickerDialog { isCamera ->
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
            photoFile = requireContext().createImageFile()
        } catch (ex: IOException) {
            Log.d("DEBUG", "Exception while creating file: $ex")
        }
        if (photoFile != null) {
            Log.d("DEBUG", "Photo file not null")
            cameraUri = FileProvider.getUriForFile(
                requireActivity(), "${requireActivity().packageName}.provider", photoFile
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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    val url = data?.data
                    url?.let { uri ->
                        val file = FileUtil.uriToFile(requireContext(), uri)
                        file?.let { handleImagePicked(it) }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private var cameraImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val file = cameraUri?.let { FileUtil.uriToFile(requireActivity(), it) }
                    file?.let { handleImagePicked(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private fun handleImagePicked(file: File) {
        context?.let {
            Glide.with(binding.ivUser).load(file).into(binding.ivUser)
            viewModel.uploadProfileImage(it, file)
        }
    }

}
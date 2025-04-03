package com.waxd.pos.fcmb.ui.main.fragments.add.farm_location

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentUpdateFarmDetailsBinding
import com.waxd.pos.fcmb.model.FarmImagesData
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.ui.main.fragments.add.farm_location.adapter.FarmImagesAdapter
import com.waxd.pos.fcmb.utils.ImagePicker
import com.waxd.pos.fcmb.utils.OnImagePickedListener
import com.waxd.pos.fcmb.utils.Util.calculateArea
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.serializable
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class UpdateFarmDetailsFragment : BaseFragment<FragmentUpdateFarmDetailsBinding>(),
    OnImagePickedListener, ViewClickHandler, OnMapReadyCallback {

    private val viewModel: UpdateFarmDetailsViewModel by viewModels()
    private val adapter by lazy { FarmImagesAdapter() }
    private var imagePicker: ImagePicker? = null
    private var googleMap: GoogleMap? = null
    private var uploadingPosition = -1
    private var deletingPosition = -1

    override fun getLayoutRes(): Int = R.layout.fragment_update_farm_details
    override fun getTitle(): String = "Update Farm Details"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) init()
    }

    override fun init() {
        binding.viewClickHandler = this
        imagePicker = ImagePicker(this, this)

        updateAdapter()
        setObserver()

        val farmerData = arguments?.serializable<FarmerData>(Constants.IntentKeys.DATA)
        farmerData?.let {
            viewModel.farmerData.value = it
            binding.data = it
            it.id?.let { it1 -> adapter.setFarmerId(it1) }
            viewModel.coordinates?.clear()
            it.farmLocations?.forEach { location ->
                viewModel.coordinates?.add(LatLng(location.lat, location.lng))
            }

            it.farmPhotos?.forEach { images ->
                adapter.add(
                    FarmImagesData(
                        images,
                        if (images is String) images else "",
                        isUploading = false
                    )
                )
            }
        }

        // Initialize Google Maps
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.viewMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Set up the result listener
        setFragmentResultListener(Constants.IntentKeys.CO_ORDINATES) { requestKey, bundle ->
            // Retrieve the data from the bundle
            val coordinates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArrayList(
                    Constants.IntentKeys.CO_ORDINATES_DATA,
                    LatLng::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelableArrayList(Constants.IntentKeys.CO_ORDINATES_DATA)
            }
            if (coordinates != null) {
                finishCapture(coordinates)
            }
        }

    }

    private fun setObserver() {
        viewModel.farmerUpdateResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    this.view?.findNavController()?.popBackStack()
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Farmer Location Updated successfully."
                        )
                    }
                    this.view?.findNavController()?.navigate(R.id.successFragment, bundle)
                }
            }

            handleUpdateButtonUI(isInProgress = it == DataResult.Loading)
        }

        viewModel.farmImageUpdateResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    if (uploadingPosition != -1) {
                        adapter.getList()[uploadingPosition].isUploading = false
                        adapter.notifyItemChanged(uploadingPosition)
                    }
                }
            }

            handleUpdateButtonUI(isInProgress = it == DataResult.Loading)
        }

        viewModel.farmImageDeleteResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    if (deletingPosition != -1)
                        adapter.getList()[deletingPosition].isUploading = false
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {
                    if (deletingPosition != -1)
                        adapter.getList()[deletingPosition].isUploading = true
                }

                is DataResult.Success -> {
                    if (deletingPosition != -1) {
                        adapter.removeAt(deletingPosition)
                        deletingPosition = -1
                    }
                }
            }
        }
    }

    private fun handleUpdateButtonUI(isInProgress: Boolean) {
        binding.tvUpdateFarmLocation.isEnabled = !isInProgress
        binding.tvUpdateFarmLocation.alpha = if (isInProgress) .5f else 1f
        binding.progressBarApi.visibility = if (isInProgress) View.VISIBLE else View.GONE
    }

    private fun finishCapture(coordinates: ArrayList<LatLng>) {
        viewModel.coordinates = coordinates
        binding.tvUpdateFarmLocation.visible(isVisible = !viewModel.coordinates.isNullOrEmpty())
        if (coordinates.size >= 3) {
            // Draw the polygon on the map
            val polygonOptions = PolygonOptions()
                .addAll(coordinates)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 255, 0, 0))
            googleMap?.clear()
            googleMap?.addPolygon(polygonOptions)
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(calculateBounds(coordinates), 100)
            )
            // Calculate the area of the farm
            val area = calculateArea(coordinates)
            binding.tvSqFt.text =
                StringBuilder().append("Total Area: ${"%.2f".format(area)} sq meters")
        }
    }

    private fun calculateBounds(coordinates: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.builder()
        for (coordinate in coordinates) {
            builder.include(coordinate)
        }
        return builder.build()
    }

    private fun updateAdapter() {
        binding.rvFarmImages.adapter = adapter
        adapter.emptyClickListener = {
            imagePicker?.openImagePicker()
        }

        adapter.itemClickHandler = {

        }

        adapter.deleteClickHandler = { data, position ->
            context?.let { context ->
                if (context.isInternetAvailable(showMessage = true)) {
                    if (deletingPosition == -1) {
                        AlertDialog.Builder(context).apply {
                            setTitle("Delete Image")
                            setMessage("Are you sure you want to delete this image?")
                            setPositiveButton("Yes") { _, _ ->
                                if (data.url is String) {
                                    deletingPosition = position
                                    viewModel.deleteImage(data.url)
                                } else if (data.path.isNotEmpty()) {
                                    deletingPosition = position
                                    viewModel.deleteImage(data.path)
                                } else showToast("Unable to delete image.")
                            }
                            setNegativeButton("No", null)
                        }.show()
                    } else {
                        showToast("Image delete in progress")
                    }
                }
            }

        }
    }

    override fun onImagePicked(file: File, uri: Uri?) {
        adapter.add(FarmImagesData(file, uri?.lastPathSegment ?: "", isUploading = true))
        uploadingPosition = adapter.getList().size - 1
        context?.let {
            viewModel.uploadFarmImage(it, file)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvCaptureFarmLocation -> {
                this.view?.findNavController()?.navigate(R.id.captureLocationFragment)
            }

            R.id.tvUpdateFarmLocation -> {
                if (context?.isInternetAvailable(showMessage = true) == true) {
                    viewModel.updateFarmer()
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        if (viewModel.coordinates != null) {
            finishCapture(viewModel.coordinates!!)
        }
    }


}
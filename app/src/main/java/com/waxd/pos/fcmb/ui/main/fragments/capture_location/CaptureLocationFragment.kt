package com.waxd.pos.fcmb.ui.main.fragments.capture_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentCaptureLocationBinding
import com.waxd.pos.fcmb.utils.Util.showToast
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.LocationPermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.sin

@AndroidEntryPoint
class CaptureLocationFragment : BaseFragment<FragmentCaptureLocationBinding>(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val capturedCoordinates = mutableListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userCurrentLocation: LatLng? = null

    private val viewModel: CaptureLocationViewModel by viewModels()

    override fun getTitle(): String = "Farm Location"

    override fun getLayoutRes(): Int = R.layout.fragment_capture_location

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()

            // Initialize Google Maps
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.viewMap) as SupportMapFragment
            mapFragment.getMapAsync(this)

            // Initialize Location Services
            activity?.let {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
            }

        }
    }

    override fun init() {
        binding.tvCaptureFarmCoordinates.setOnClickListener {
            captureLatLng()
        }

        binding.tvFinishCapture.setOnClickListener {
            finishCapture()
            binding.tvRecapture.visible(true)
            binding.tvDone.visible(true)
            binding.tvFinishCapture.visible(isVisible = false)
            binding.tvCaptureFarmCoordinates.visible(isVisible = false)
        }

        binding.tvRecapture.setOnClickListener {
            recapture()
        }

        binding.tvDone.setOnClickListener {
            // Prepare the data to send back
            val result = Bundle().apply {
                putParcelableArrayList(Constants.IntentKeys.CO_ORDINATES_DATA, ArrayList(capturedCoordinates))
            }

            // Set the result
            setFragmentResult(Constants.IntentKeys.CO_ORDINATES, result)
            this.view?.findNavController()?.navigateUp()
        }
    }


    private fun setRequestPermissionListener() {
        isLocationPermissionGranted(object : LocationPermissionHandler {
            @SuppressLint("MissingPermission")
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
                if (isPermissionGranted) googleMap?.isMyLocationEnabled = true
            }
        })
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        setRequestPermissionListener()
        getLastLocation { }
    }

    private fun captureLatLng() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    if (capturedCoordinates.isEmpty() || calculateDistanceInMeters(
                            capturedCoordinates.last(),
                            latLng
                        ) > 30
                    ) {
                        capturedCoordinates.add(latLng)
                        binding.tvFinishCapture.visible(capturedCoordinates.size > 3)
                        // Add a marker on the map for the captured coordinate
                        googleMap?.addMarker(
                            MarkerOptions().position(latLng)
                                .title("Corner ${capturedCoordinates.size}")
                        )

                        // Optionally, move the camera to the new marker
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))
                    } else {
                        context?.showToast("Distance should be greater than 30 meter")
                    }
                }
            }
    }

    private fun recapture() {
        capturedCoordinates.clear()
        googleMap?.clear()
        binding.tvFinishCapture.visible(false)
        binding.tvRecapture.visible(isVisible = false)
        binding.tvDone.visible(isVisible = false)
        binding.tvCaptureFarmCoordinates.visible(isVisible = true)
    }

    private fun calculateDistanceInMeters(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            results
        )
        return results[0]
    }

    private fun finishCapture() {
        if (capturedCoordinates.size >= 3) {
            // Draw the polygon on the map
            val polygonOptions = PolygonOptions()
                .addAll(capturedCoordinates)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 255, 0, 0))
            googleMap?.addPolygon(polygonOptions)

            // Calculate the area of the farm
//            val area = calculateArea(capturedCoordinates)
//            binding.tvSqFt.text = StringBuilder().append("Total Area: ${"%.2f".format(area)} sq meters")
        } else {
            Toast.makeText(
                requireContext(),
                "Capture at least 3 corners to calculate area",
                Toast.LENGTH_SHORT
            ).show()
        }
    }




    private fun getLastLocation(isSuccess: (Boolean) -> Unit) {
        isLocationPermissionGranted(object : LocationPermissionHandler {
            @SuppressLint("MissingPermission")
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
                if (isPermissionGranted)
                    if (isLocationEnabled()) {
                        val cts = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cts.token
                        ).addOnSuccessListener { location ->
                            location?.let {
                                val latLng = LatLng(it.latitude, it.longitude)
                                userCurrentLocation = latLng
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 19f
                                    )
                                )
                            }
                        }
                        fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                            val location: Location? = task.result
                            if (location == null) {
                                requestNewLocationData(isSuccess)
                            } else {
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            location.latitude,
                                            location.longitude
                                        ), 19f
                                    )
                                )
                            }
                        }
                    } else {
                        isSuccess(false)
                    }

            }
        })

    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(isSuccess: (Boolean) -> Unit) {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
                .setMinUpdateDistanceMeters(10f).build()
        Looper.myLooper()?.let {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val mLastLocation: Location? = locationResult.lastLocation

                        mLastLocation?.let {
                            val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                            isSuccess(true)

                        }
                    }
                },
                it
            )
        }
    }


}
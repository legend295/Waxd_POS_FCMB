package com.waxd.pos.fcmb.ui.main.fragments.add.loan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
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
import com.waxd.pos.fcmb.databinding.FragmentCreateLoanApplicationBinding
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.LocationPermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import kotlin.math.abs

@AndroidEntryPoint
class CreateLoanApplicationFragment : BaseFragment<FragmentCreateLoanApplicationBinding>(),
    OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val capturedCoordinates = mutableListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userCurrentLocation: LatLng? = null

    override fun getTitle(): String = "Create Loan Application"


    override fun getLayoutRes(): Int = R.layout.fragment_create_loan_application

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
        binding.tvSubmitApplication.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to submit the details.")
                .setPositiveButton("Confirm") { _, _ ->
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Loan application is submitted successfully.\nLoan Application No - FCMB121"
                        )
                    }
                    this.view?.findNavController()?.navigate(R.id.successFragment, bundle)
                }.setNegativeButton("Cancel", null).show()
        }

        binding.tvCancel.setOnClickListener {
            AlertDialog.Builder(requireContext()).setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Confirm") { _, _ ->
                    this.view?.findNavController()?.popBackStack()
                }.setNegativeButton("Cancel", null).show()
        }

        binding.tvCaptureFarmCoordinates.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.captureLocationFragment)
        }

        binding.tvUploadLandDocuments.setOnClickListener {
//            finishCapture()
        }
    }

    private fun setRequestPermissionListener() {
        isLocationPermissionGranted(object : LocationPermissionHandler {
            @SuppressLint("MissingPermission")
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
//                if (isPermissionGranted) googleMap?.isMyLocationEnabled = true
            }
        })
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap?.uiSettings?.isZoomControlsEnabled = false
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
                    capturedCoordinates.add(latLng)

                    // Add a marker on the map for the captured coordinate
                    googleMap?.addMarker(
                        MarkerOptions().position(latLng).title("Corner ${capturedCoordinates.size}")
                    )

                    // Optionally, move the camera to the new marker
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))
                }
            }
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
            val area = calculateArea(capturedCoordinates)
            binding.tvSqFt.text =
                StringBuilder().append("Total Area: ${"%.2f".format(area)} sq meters")
        } else {
            Toast.makeText(
                requireContext(),
                "Capture at least 3 corners to calculate area",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun calculateArea(coordinates: List<LatLng>): Double {
        val earthRadius = 6371000.0 // Earth radius in meters
        val latLngList = coordinates + listOf(coordinates[0]) // Close the polygon

        var area = 0.0
        for (i in 0 until latLngList.size - 1) {
            val p1 = latLngList[i]
            val p2 = latLngList[i + 1]
            area += Math.toRadians(p2.longitude - p1.longitude) *
                    (2 + Math.sin(Math.toRadians(p1.latitude)) + Math.sin(Math.toRadians(p2.latitude)))
        }
        area = abs(area * earthRadius * earthRadius / 2)
        return area
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
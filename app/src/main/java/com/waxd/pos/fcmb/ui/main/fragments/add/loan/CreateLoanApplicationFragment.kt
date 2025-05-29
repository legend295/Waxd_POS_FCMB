package com.waxd.pos.fcmb.ui.main.fragments.add.loan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentCreateLoanApplicationBinding
import com.waxd.pos.fcmb.rest.FarmCoordinates
import com.waxd.pos.fcmb.rest.NotValidException
import com.waxd.pos.fcmb.utils.Util
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.LocationPermissionHandler
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.abs

@AndroidEntryPoint
class CreateLoanApplicationFragment : BaseFragment<FragmentCreateLoanApplicationBinding>(),
    OnMapReadyCallback, ViewClickHandler {

    private var googleMap: GoogleMap? = null
    private val capturedCoordinates = mutableListOf<LatLng>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userCurrentLocation: LatLng? = null
    private val viewModel: CreateLoanApplicationViewModel by viewModels()
    private var currentlyCapturedCoordinates: ArrayList<LatLng>? = null

    override fun getTitle(): String = "Create Loan Application"


    override fun getLayoutRes(): Int = R.layout.fragment_create_loan_application

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) {
            init()
            binding.viewModel = viewModel

            // Initialize Google Maps
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.viewMap) as SupportMapFragment
            mapFragment.getMapAsync(this)

            // Initialize Location Services
            activity?.let {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
            }

            // Set up the result listener
            setFragmentResultListener(Constants.IntentKeys.CO_ORDINATES) { requestKey, bundle ->
                // Retrieve the data from the bundle
                currentlyCapturedCoordinates =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        bundle.getParcelableArrayList(
                            Constants.IntentKeys.CO_ORDINATES_DATA,
                            LatLng::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        bundle.getParcelableArrayList(Constants.IntentKeys.CO_ORDINATES_DATA)
                    }
                if (currentlyCapturedCoordinates != null) {
                    finishCapture(currentlyCapturedCoordinates!!)
                }
            }

        }
    }

    override fun init() {
        binding.viewClickHandler = this

        setObserver()
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Loan application is submitted successfully.\nLoan Application No - ${it.data.loanApplicationNumber}"
                        )
                    }
                    this.view?.findNavController()?.popBackStack()
                    this.view?.findNavController()
                        ?.navigate(R.id.successFragment, bundle, getNavOptions())
                }
            }
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
        if (viewModel.coordinates != null) {
            finishCapture(viewModel.coordinates!!)
        }
//        setRequestPermissionListener()
//        getLastLocation { }
    }


    private fun finishCapture(coordinates: ArrayList<LatLng>) {
        viewModel.coordinates = coordinates
        viewModel.request.value?.farmLocations =
            coordinates.map { FarmCoordinates(it.latitude, it.longitude) }

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
            val area = Util.calculateArea(coordinates)
            binding.tvSqFt.text =
                StringBuilder().append("${"%.2f".format(area)} sq meters")
        }
    }

    private fun calculateBounds(coordinates: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.builder()
        for (coordinate in coordinates) {
            builder.include(coordinate)
        }
        return builder.build()
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvSubmitApplication -> {
                try {
                    viewModel.request.value?.isValid()
                    AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to submit the details.")
                        .setPositiveButton("Confirm") { _, _ ->
                            context?.let { viewModel.createLoanApplication(it) }
                        }.setNegativeButton("Cancel", null).show()
                } catch (e: NotValidException) {
                    e.message?.let { showToast(it) }
                }
            }

            R.id.tvCancel -> {
                AlertDialog.Builder(requireContext()).setMessage("Are you sure you want to cancel?")
                    .setPositiveButton("Confirm") { _, _ ->
                        this.view?.findNavController()?.popBackStack()
                    }.setNegativeButton("Cancel", null).show()
            }

            R.id.tvCaptureFarmCoordinates -> {
                this.view?.findNavController()?.navigate(
                    R.id.captureLocationFragment,
                    Bundle(), getNavOptions()
                )
            }


        }
    }
}
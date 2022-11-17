package com.nicolas.boolder.view.map

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraOptions.Builder
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.nicolas.boolder.R
import com.nicolas.boolder.databinding.FragmentMapBinding
import com.nicolas.boolder.viewBinding


class MapFragment : Fragment() {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var gpsActivationRequest: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isWaitingPosition = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        gpsActivationRequest = registerForActivityResult(StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                moveCameraToUserPosition()
            }
        }

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {

            if ((it[permission.ACCESS_FINE_LOCATION] == true || it[permission.ACCESS_COARSE_LOCATION] == true) && isWaitingPosition) {
                moveCameraToUserPosition()
            } else {
                // No location access granted.
            }
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(2.5968216, 48.3925623))
            .zoom(10.2)
            .build()

        binding.mapView.getMapboxMap().apply {
            loadStyleUri("mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2")
            setCamera(cameraOptions)
        }
        binding.mapView.apply {
            gestures.pitchEnabled = false
            gestures.simultaneousRotateAndPinchToZoomEnabled = false
            scalebar.enabled = false
        }


        binding.fabLocation.setOnClickListener {
            isWaitingPosition = true
            if (checkPermission()) {
                enableGPS()
            } else {
                requestLocationPermission()
            }
        }

    }

    private fun enableGPS() {

        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
            fastestInterval = 50
            interval = 100

        }

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(locationSettingsRequest.build())
            .addOnSuccessListener {
                //GPS is activated
                moveCameraToUserPosition()
            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS is disabled
                    try {
                        // Ask user to enable GPS
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                        gpsActivationRequest.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToUserPosition() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener {
            val point = Point.fromLngLat(it.longitude, it.latitude)

            binding.mapView.getMapboxMap()
                .setCamera(Builder().center(point).bearing(it.bearing.toDouble()).build())
            binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(point)
            binding.mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }

        }
    }


    private fun checkPermission(): Boolean {
        val isFineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val isCoarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val shouldShowFineRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as AppCompatActivity,
            permission.ACCESS_FINE_LOCATION
        )

        val shouldShowCoarseRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as AppCompatActivity,
            permission.ACCESS_COARSE_LOCATION
        )

        when {
            isFineGranted && isCoarseGranted -> {
                return true
            }

            shouldShowFineRationale || shouldShowCoarseRationale -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                // TODO showInContextUI(...)
                return false
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                return false
            }
        }
    }

    private fun rationale() {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("GPS Disabled")
//        builder.setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device")
//        builder.setPositiveButton("Enable GPS",
//            DialogInterface.OnClickListener { dialog, which ->
//                requireActivity().startActivityForResult(
//                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
//                    1
//                )
//            }).setNegativeButton("No, Just Exit",
//            DialogInterface.OnClickListener { dialog, which -> })
//        mGPSDialog = builder.create()
//        mGPSDialog.show()
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION))
    }

}
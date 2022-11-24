package com.boolder.boolder.utils

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.boolder.boolder.R.string
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

interface LocationCallback {
    fun onGPSLocation(location: Location)
}

class LocationProvider(private val activity: AppCompatActivity, private val callback: LocationCallback) {

    private var isWaitingPosition = false
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>> = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

        if ((it[permission.ACCESS_FINE_LOCATION] == true || it[permission.ACCESS_COARSE_LOCATION] == true) && isWaitingPosition) {
            moveCameraToUserPosition()
        } else {
            // No location access granted.
        }
    }

    private var gpsActivationRequest: ActivityResultLauncher<IntentSenderRequest> = activity.registerForActivityResult(
        StartIntentSenderForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            moveCameraToUserPosition()
        }
    }

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    // Entry point
    fun askForPosition() {
        isWaitingPosition = true
        if (checkPermission()) {
            enableGPS()
        } else {
            requestLocationPermission()
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

        LocationServices.getSettingsClient(activity)
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
            callback.onGPSLocation(it)
            isWaitingPosition = false
        }
    }


    private fun checkPermission(): Boolean {
        val isFineGranted = ContextCompat.checkSelfPermission(
            activity,
            permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val isCoarseGranted = ContextCompat.checkSelfPermission(
            activity,
            permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val shouldShowFineRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            permission.ACCESS_FINE_LOCATION
        )

        val shouldShowCoarseRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
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
                showRationaleDialog()
                return false
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                return false
            }
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(string.rational_title))
            .setMessage(activity.getString(string.rationale_message))
            .setPositiveButton(activity.getString(string.rationale_positive_btn)) { _, _ -> enableGPS() }
            .setNegativeButton(activity.getString(string.rationale_negative_Btn)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION))
    }
}
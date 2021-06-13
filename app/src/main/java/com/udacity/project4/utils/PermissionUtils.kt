@file:Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")

package com.udacity.project4.utils

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import timber.log.Timber


const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_TURN_DEVICE_LOCATION_ON = 20
const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34

val runningQOrLater = VERSION.SDK_INT >= VERSION_CODES.Q




fun Context.isPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Helper functions to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

    // Background permissions didn't exit prior to Q, so it's approved by default.
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        VERSION.SDK_INT < VERSION_CODES.Q) {
        return true
    }

    return ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}


/**
 * Requests permission and if the user denied a previous request, but didn't check
 * "Don't ask again", we provide additional rationale.
 *
 * Note: The Snackbar should have an action to request the permission.
 */
fun Fragment.requestPermissionWithRationale(
    permission: String,
    requestCode: Int,
    snackbar: Snackbar
) {
    val provideRationale = shouldShowRequestPermissionRationale(permission)

    if (provideRationale) {
        snackbar.show()
    } else {
        requestPermissions(arrayOf(permission), requestCode)
    }
}


fun Fragment.checkDeviceLocationSettings(
    resolve: Boolean = true
) {

    /*
    * Get the best and most recent location of the device, which may be null in rare
    * cases when a location is not available.
    */
    val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_LOW_POWER
    }

    //Get Current Location Request
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

    val settingsClient = LocationServices.getSettingsClient(requireActivity())
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())


    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve) {
            try {

                //Show location setting
                exception.startResolutionForResult(
                    requireActivity(),
                    REQUEST_TURN_DEVICE_LOCATION_ON
                )

                startIntentSenderForResult(
                    exception.resolution.intentSender,
                    REQUEST_TURN_DEVICE_LOCATION_ON,
                    null,
                    0,
                    0,
                    0,
                    null
                )

            } catch (sendEx: IntentSender.SendIntentException) {
                Timber.d(getString(R.string.error_location_settings), "... ${sendEx.message}")
            }
        }
                    Timber.i("Location is off")
                    Snackbar.make(
                        requireView(),
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettings()
                    }.show()

    }


    //SET GEO FENCE
    locationSettingsResponseTask.addOnSuccessListener {
        if (locationSettingsResponseTask.isSuccessful) {
            Timber.i("Location Settings is on")
        }

    }

}






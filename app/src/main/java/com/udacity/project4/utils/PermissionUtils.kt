@file:Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")

package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.ActivityCompat


const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

val runningQOrLater = VERSION.SDK_INT >= VERSION_CODES.Q


fun isPermissionGranted(context: Context?): Boolean {
    return context?.let {
        ActivityCompat.checkSelfPermission(
            it,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } === PackageManager.PERMISSION_GRANTED
}

@TargetApi(29)
fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
    val foregroundLocationPermissionApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION))
    val backgroundLocationPermissionApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            //Return true if the device is running lower than Q
            true
        }
    return foregroundLocationPermissionApproved && backgroundLocationPermissionApproved
}

@TargetApi(29 )
fun requestForegroundAndBackgroundLocationPermissions(context: Context) {
    if (foregroundAndBackgroundLocationPermissionApproved(context))
        return
    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    }
    ActivityCompat.requestPermissions(
        context as Activity,
        permissionsArray,
        resultCode
    )
}


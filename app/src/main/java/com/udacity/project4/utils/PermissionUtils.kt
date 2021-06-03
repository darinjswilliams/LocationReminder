@file:Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")

package com.udacity.project4.utils

import android.Manifest
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






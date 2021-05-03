package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //implement the onReceive method to receive the geofencing events at the background
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        Timber.i("BROADCAST RECEIVER ENTER")

        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
//        if (geofenceEvent.hasError()) {
//            Timber.e(
//                context.getString(R.string.geofence_broadcast_error),
//                ".....${geofenceEvent.errorCode}"
//            )
//            return
//        }
//
//
//        when (geofenceEvent.geofenceTransition) {
//            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
//                Timber.i(context.getString(R.string.geofence_entered))
//            }
//        }

    }


}
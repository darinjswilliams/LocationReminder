package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {

    companion object {
        private const val GEOFENCE_RADIUS = 100f
        private const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.locationreminders.action.ACTIONGEOFENCE_EVENT"
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel by sharedViewModel<SaveReminderViewModel>()
    private lateinit var binding: FragmentSaveReminderBinding


    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        binding.saveReminder.setOnClickListener {
            saveReminderViewModel()
        }
    }

    private fun saveReminderViewModel() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminderDataItem = ReminderDataItem(
            title, description, location,
            latitude, longitude
        )

        _viewModel.validateAndSaveReminder(reminderDataItem)

        if (latitude != null && longitude != null && !TextUtils.isEmpty(title) && !isDetached) {

            //Check location permission corresponding location permissions (foreground and background permissions)
            //Check to see if location is enable before adding geoFence
              checkDeviceLocationSettingsAndStartGeofence()

                if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
                    addGeoFenceReference(
                        LatLng(latitude, longitude),
                        GEOFENCE_RADIUS,
                        reminderDataItem.id
                    )
                }
            }
    }



    @SuppressLint("MissingPermission")
    private fun addGeoFenceReference(latLng: LatLng, geofenceRadius: Float, id: String) {

        //Create GeoFence parameter
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
            .setExpirationDuration(TimeUnit.HOURS.toMillis(1))
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // When removeGeofences() completes, regardless of its success or failure, add the new geofences.
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        Timber.i("GEO FENCE ADDED")
                    }

                    addOnFailureListener {
                        Timber.i("GEO FENCE FAILED")
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
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
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    @TargetApi(29)
    fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundLocationPermissionApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundLocationPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                //Return true if the device is running lower than Q
                true
            }
        return foregroundLocationPermissionApproved && backgroundLocationPermissionApproved
    }
}

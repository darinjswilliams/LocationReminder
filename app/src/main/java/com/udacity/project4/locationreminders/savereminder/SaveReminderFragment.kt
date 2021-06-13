package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
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
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
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

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            requireView(),
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.permission_ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    SaveReminderFragment.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            requireView(),
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.permission_ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    SaveReminderFragment.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            Timber.i("Device is on")
        } else {
            Timber.i("Location is not turned on")
        }
    }

    //TODO not sure if i need this
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE ->
                when {
                    grantResults.isEmpty() -> {
                        Timber.i(("request was canceled by user"))
                    }
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        checkDeviceLocationSettings()
                    }
                    else -> {
                        // Permission denied.

                        // Notify the user via a SnackBar that they have rejected a core permission for the
                        // app, which makes the Activity useless. In a real app, core permissions would
                        // typically be best requested during a welcome-screen flow.
                        Timber.i(("request was denied"))
                        requestPermissions(permissions, requestCode)
                    }
                }
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

        //Validate EnterData
        _viewModel.validateEnteredData(reminderDataItem)

        if (latitude != null && longitude != null && !TextUtils.isEmpty(title) && !isDetached) {


            //Check  permissions (foreground and background permissions)
            if (requestPermissions()) {

                //Check to see if location is enable before saving reminder and adding geoFence
                checkDeviceLocationSettings()
                //Add Geo Fence
                addGeoFenceReference(
                    LatLng(latitude, longitude),
                    reminderDataItem
                )
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun addGeoFenceReference(latLng: LatLng, reminderDataItem: ReminderDataItem) {

        //Create GeoFence parameter
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
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
                        //Save Reminder
                        _viewModel.validateAndSaveReminder(reminderDataItem)
                    }

                    addOnFailureListener {
                        Timber.i("GEO FENCE FAILED")
                        Toast.makeText(context, R.string.geofences_not_added, Toast.LENGTH_LONG).show()

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


    private fun requestPermissions(): Boolean {

        val permissionAccessFineLocationApproved =
            context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        Timber.i("Forground Permission:..." + permissionAccessFineLocationApproved)

        val backgroundLocationPermissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        Timber.i("BackgroundLocation:..." + backgroundLocationPermissionApproved)

        val shouldProvideRationale =
            permissionAccessFineLocationApproved == true &&
                    backgroundLocationPermissionApproved == true

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            return true

        } else {

            Timber.i("Requestion permission denied")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".

            if (permissionAccessFineLocationApproved == false) {

                requestPermissionWithRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                    fineLocationRationalSnackbar
                )

            }

            if (backgroundLocationPermissionApproved == false) {
                requestPermissionWithRationale(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                    backgroundRationalSnackbar
                )

            }

        }

        return shouldProvideRationale
    }
}

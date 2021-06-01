package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.BuildConfig
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val REQUEST_LOCATION_PERMISSION_CODE = 25
        private const val DEFAULT_ZOOM = 15f
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val BACKROUND_LOCATION_PERMISSION_INDEX = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 5
    }

    val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var lastKnownLocation: Location


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        //Construct a fusedLocationProviderClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        _viewModel.apply {
            latitude.value = poi.latLng.latitude
            longitude.value = poi.latLng.longitude
            reminderSelectedLocationStr.value = poi.name
        }

        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isEmpty() ||
                (grantResults[LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED) ||
                (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)
            ) {
                Snackbar.make(
                    binding.map,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.LIBRARY_PACKAGE_NAME, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()


            } else {
                checkDeviceLocationSettingsAndStartGeofence()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        if (!checkPermissions()) {

            Timber.i("Check Permission was granted")
            requestPermissions()

            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            checkDeviceLocationSettingsAndStartGeofence()


        }

    }


    private fun checkDeviceLocationSettingsAndStartGeofence(
        resolve: Boolean = true
    ) {

        /*
        * Get the best and most recent location of the device, which may be null in rare
        * cases when a location is not available.
        */
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
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
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        //SET GEO FENCE
        locationSettingsResponseTask.addOnSuccessListener {
            if (locationSettingsResponseTask.isSuccessful) {
                //add geofence
                Timber.i("AddGeoFence")
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkDeviceLocationSettingsAndStartGeofence(false)
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {
            map = googleMap
        }

        enableMyLocation()
        zoomToDeviceLocation()
        setPoiClick(map)
        setMapStyle(map)
        setMapLongClick(map)
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Timber.e(getString(R.string.style_parsing_error))
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(getString(R.string.style_exception), "... ${e.localizedMessage}")
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->

            binding.saveButton.visibility = View.VISIBLE
            binding.saveButton.setOnClickListener {
                onLocationSelected(poi)
            }

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }
    }


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            binding.saveButton.visibility = View.VISIBLE
            binding.saveButton.setOnClickListener { view ->
                _viewModel.apply {
                    longitude.value = it.longitude
                    latitude.value = it.latitude
                    reminderSelectedLocationStr.value = getString(R.string.lat_long_snippet, it.latitude, it.longitude)
                    findNavController().popBackStack()
                }
            }

            val cameraZoom = CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM)
            map.moveCamera(cameraZoom)
            val markerInfo = map.addMarker(
                MarkerOptions()
                    .position(it)
            )
            markerInfo.showInfoWindow()
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomToDeviceLocation() {
        Timber.i("ZoomDeviceLocation")
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val zoomLevel = DEFAULT_ZOOM
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        userLatLng,
                        zoomLevel
                    )
                )
            }
        }
    }


    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState =
            if(runningQOrLater) {
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissionAccessFineLocationApproved = (checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

        Timber.i("Just asked for foreground permissions.." + permissionAccessFineLocationApproved)

        val backgroundLocationPermissionApproved = (checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

        Timber.i(("BackgroundLocation Permission: Just asked..." + backgroundLocationPermissionApproved))

        val shouldProvideRationale =
            permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Timber.i("Display permission rationale to provide additional context")

            Snackbar.make(
                binding.constraintLayoutMaps,
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.permission_ok, View.OnClickListener { // Request permission
                    requestPermissions(
                        requireActivity(), arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_LOCATION_PERMISSION_CODE
                    )
                })
                .show()
        } else {

            Timber.i("Requestion permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        }
    }

}




package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val REQUEST_LOCATION_PERMISSION_CODE = 25
        private const val DEFAULT_ZOOM = 15f
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel by sharedViewModel<SaveReminderViewModel>()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // If the user denied a previous permission request, but didn't check "Don't ask again", these
    // Snackbars provided an explanation for why user should approve, i.e., the additional
    // rationale.
    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            requireView(),
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.permission_ok) {
                requestPermissions(
                    arrayOf(permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
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
                    arrayOf(permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

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

        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    Timber.i(("request was canceled by user"))
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    checkDeviceLocationSettingsAndStartGeofence()
                }
                else -> {
                    // Permission denied.

                    // Notify the user via a SnackBar that they have rejected a core permission for the
                    // app, which makes the Activity useless. In a real app, core permissions would
                    // typically be best requested during a welcome-screen flow.
                    Timber.i(("request was denied"))
//                    requestPermissions(permissions, requestCode)
                    requestPermissionWithRationale(permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE, fineLocationRationalSnackbar)
                }
            }
        }
    }


    private fun enableMyLocation() {

        if (context?.isPermissionGranted() == true) {
            requestPermissions()

        } else {
            Timber.i("Fine Permission was denied ")
            requestPermissionWithRationale(
                permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar
            )
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
                    reminderSelectedLocationStr.value =
                        getString(R.string.lat_long_snippet, it.latitude, it.longitude)
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

    @SuppressLint("MissingPermission")
    private fun requestPermissions() {

        val permissionAccessFineLocationApproved =
            context?.hasPermission(permission.ACCESS_FINE_LOCATION)
        Timber.i("Forground Permission:..." + permissionAccessFineLocationApproved)

        val backgroundLocationPermissionApproved =
            context?.hasPermission(permission.ACCESS_BACKGROUND_LOCATION)

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        Timber.i("BackgroundLocation:..." + backgroundLocationPermissionApproved)

        val shouldProvideRationale =
            permissionAccessFineLocationApproved == true &&
                      backgroundLocationPermissionApproved == true

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            checkDeviceLocationSettingsAndStartGeofence()

        } else {

            Timber.i("Requestion permission denied")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".

            if(permissionAccessFineLocationApproved == false){

            requestPermissionWithRationale(
                permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar
            )

            }

            if(backgroundLocationPermissionApproved == false){
                requestPermissionWithRationale(
                    permission.ACCESS_BACKGROUND_LOCATION,
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                    backgroundRationalSnackbar
                )

            }

        }
    }
}





package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    //to check if api is Q or above
    private val runningQOrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q

    private lateinit var contxt: Context
    private var title: String? = null
    private var description: String? = null
    private var lat: Double? = null
    private var long: Double? = null
    private var location: String? = null
    private lateinit var id: String
    private var radius = 500f

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        //connected it with GeofenceBroadcastReceiver
        val intent = Intent(this.contxt as Activity, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            this.contxt,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        //geofencingClient is main entry point for interacting with geofenceAPIs
        geofencingClient = LocationServices.getGeofencingClient(this.contxt as Activity)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            title = _viewModel.reminderTitle.value
            description = _viewModel.reminderDescription.value
            location = _viewModel.reminderSelectedLocationStr.value
            lat = _viewModel.latitude.value
            long = _viewModel.longitude.value
            id = UUID.randomUUID().toString()


            // add some code for make sure that title and .... not empty
            if (title == null || description == null || lat == null || long == null) {
                Snackbar.make(binding.root, getString(R.string.save_error), Snackbar.LENGTH_SHORT).show()
            } else {
                checkPermissionsAndStartGeofencing()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check if requestCode equal to REQUEST_TURN_DEVICE_LOCATION_ON
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            //checkDeviceLocationSettingsAndStartGeofence and pass to it false
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    @TargetApi(29)
    //check if permissions are granted and if not ask for correct permissions
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                //check if ACCESS_FINE_LOCATION permission is granted
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            contxt,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        //if devices runningQOrHigher check if access background location permission is granted
        //return true if devices is running lower than Q that is we don't need permission to access location in background
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.contxt, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        //return combination of foregroundLocationApproved and backgroundPermissionApproved
        //this should be true if permission is granted and false if not
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                //if ACCESS_FINE_LOCATION permission false return to checkPermissions
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing() }
                //if ACCESS_COARSE_LOCATION permission false return to checkPermissions
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()                }
                //if ACCESS_BACKGROUND_LOCATION permission false return to checkPermissions
                permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                    checkPermissionsAndStartGeofencing()                }
                else -> {
                    Log.i("Permission: ", "Denied")
                    Toast.makeText(
                        context,
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("request", "onRequestPermissionResult")
        // when have any problem with any permission is denied
        if (
        // if grant result array empty then the interaction was as erupted and permission request canceled
            grantResults.isEmpty() ||
            //if has PERMISSION_DENIED at location permission index
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            //if requestCode was of type request foreground and background permission requestCode
            //and BACKGROUND_LOCATION_PERMISSION_INDEX is denied as well
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            //show in snackBar that You need to grant location permission and go to setting to granted permissions
            //present snackBar explaining to user that they need location permission
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            // if permissions are granted check device location settings and then add new geofence
            checkDeviceLocationSettingsAndStartGeofence(true)
        }
    }
    //3 check device location and then add new geofence
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationSettingsResponseTask =
            checkDeviceLocationSettings(resolve)
        //if locationSettingsResponseTask does complete check is successful and if so add new geofence
        locationSettingsResponseTask?.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence()
                val reminderDataItem =
                    ReminderDataItem(title, description, location, lat, long, id = id)
                _viewModel.saveReminder(reminderDataItem)
            }
        }
    }

    //check device location setting enable or not before start to add new geofence
    private fun checkDeviceLocationSettings(
        resolve: Boolean
    ): Task<LocationSettingsResponse>? {
        //create locationRequest
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        //create LocationSettingsRequest.Builder
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        //use location services to get settings client and create locationSettingsResponseTask
        //to check location settings
        val settingsClient = this.activity?.let { LocationServices.getSettingsClient(it) }
        val locationSettingsResponseTask =
            settingsClient?.checkLocationSettings(builder.build())
        locationSettingsResponseTask?.addOnFailureListener { exception ->
            //check exception is a ResolvableApiException
            if (exception is ResolvableApiException && resolve) {
                try {
                    //if so turn on location
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                    Log.d("error", "Error getting location settings resolution: " + sendEx.message)
                }
            }
            //if exception is not of type ResolvableApiException
            // add SnackBar tell user that location needs to be enable
            else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings(true)
                }.show()
            }
        }
        return locationSettingsResponseTask
    }

    //adding new geofence
    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        val geofence = lat?.let {
            long?.let { it1 ->
                Geofence.Builder()
                    .setCircularRegion(it, it1, radius)
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(1000)
                    .build()
            }
        }
        //build geofenceRequest passing in geofenceing Initial trigger and geofence build
        val geofenceRequest = geofence?.let {
            GeofencingRequest.Builder()
                .addGeofence(it)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()
        }

        geofencingClient.addGeofences(geofenceRequest!!, geofencePendingIntent).run {
            addOnSuccessListener {
                //if adding geofence successful tell user by toast that Geofence added successfully
                Toast.makeText(
                    contxt,
                    contxt.getString(R.string.added_geofence_successfully),
                    Toast.LENGTH_LONG
                ).show()
            }
            addOnFailureListener {
                //if adding geofence fail tell user by toast that that has error
                Toast.makeText(
                    contxt,
                    "An exception occurred: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29


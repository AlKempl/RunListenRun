package com.alkempl.rlr.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alkempl.rlr.GeofenceBroadcastReceiver
import com.alkempl.rlr.LocationUpdatesBroadcastReceiver
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.utils.GeofencingConstants
import com.alkempl.rlr.utils.hasPermission
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

private const val TAG = "MyLocationManager"

class LocationManager private constructor(private val context: Context) {
    private val _receivingLocationUpdates: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)

    /**
     * Status of location updates, i.e., whether the app is actively subscribed to location changes.
     */
    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    // globally declare LocationCallback
//    private lateinit var locationCallback: LocationCallback

    // Stores parameters for requests to the FusedLocationProviderApi.
    private val locationRequest: LocationRequest = LocationRequest().apply {
        // Sets the desired interval for active location updates. This interval is inexact. You
        // may not receive updates at all if no location sources are available, or you may
        // receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        //
        // IMPORTANT NOTE: Apps running on "O" devices (regardless of targetSdkVersion) may
        // receive updates less frequently than this interval when the app is no longer in the
        // foreground.
//        interval = TimeUnit.SECONDS.toMillis(60)
        interval = 200
        interval = TimeUnit.SECONDS.toMillis(20)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        fastestInterval = 100
//        fastestInterval = TimeUnit.SECONDS.toMillis(30)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
//        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
//        maxWaitTime = TimeUnit.SECONDS.toMillis(0)
        maxWaitTime = TimeUnit.SECONDS.toMillis(5)

//        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    /**
     * Creates default PendingIntent for location changes.
     *
     * Note: We use a BroadcastReceiver because on API level 26 and above (Oreo+), Android places
     * limits on Services.
     */
    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    fun setActiveGeofence(currentGeofenceData: GeofenceEntry){
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(currentGeofenceData.id)
            // Set the circular region of this geofence.
            .setCircularRegion(
                currentGeofenceData.location.latitude,
                currentGeofenceData.location.longitude,
                currentGeofenceData.radius,
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(currentGeofenceData.expires_in)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
//                        Toast.makeText(this@HuntMainActivity, getString(R.string.geofences_added) + " " + GEOFENCE_VERSION,
//                            Toast.LENGTH_SHORT)
//                            .show()
                        Log.d("${TAG}/ADD", geofence.requestId)
                        // Tell the viewmodel that we've reached the end of the game and
                        // activated the last "geofence" --- by removing the Geofence.
//                        viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        // Failed to add geofences.
//                        Toast.makeText(this@HuntMainActivity, R.string.geofences_not_added,
//                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.e("${TAG}/ADD", it.message!!)
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
//        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
//            addOnSuccessListener {
//                // Geofences removed
//                Log.d(TAG, getString(R.string.geofences_removed))
//                Toast.makeText(context, R.string.geofences_removed, Toast.LENGTH_SHORT)
//                    .show()
//            }
//            addOnFailureListener {
//                // Failed to remove geofences
//                Log.d(TAG, getString(R.string.geofences_not_removed))
//            }
//        }
    }


    /**
     * Uses the FusedLocationProvider to start location updates if the correct fine locations are
     * approved.
     *
     * @throws SecurityException if ACCESS_FINE_LOCATION permission is removed before the
     * FusedLocationClient's requestLocationUpdates() has been completed.
     */
    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            _receivingLocationUpdates.value = true
            // If the PendingIntent is the same as the last request (which it always is), this
            // request will replace any requestLocationUpdates() called before.
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)

        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false

            // Exception only occurs if the user revokes the FINE location permission before
            // requestLocationUpdates() is finished executing (very rare).
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.value = false
//        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    companion object {
        @Volatile
        private var INSTANCE: LocationManager? = null


        fun getInstance(context: Context): LocationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationManager(context).also { INSTANCE = it }
            }
        }
    }

//    private fun isAppInForeground(context: Context): Boolean {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses = activityManager.runningAppProcesses ?: return false
//
//        appProcesses.forEach { appProcess ->
//            if (appProcess.importance ==
//                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
//                appProcess.processName == context.packageName) {
//                return true
//            }
//        }
//        return false
//    }
}
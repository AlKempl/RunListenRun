package com.alkempl.rlr.services

import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.alkempl.rlr.LocationUpdatesBroadcastReceiver
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.db.LocationEntity
import com.alkempl.rlr.hasPermission
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.Executors
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
//            locationCallback = object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult?) {
//                    locationResult ?: return
//
//                    if (locationResult.locations.isNotEmpty()) {
//                        val locations = locationResult.locations.map { location ->
//                            LocationEntity(
//                                latitude = location.latitude,
//                                longitude = location.longitude,
//                                foreground = isAppInForeground(context),
//                                date = Date(location.time)
//                            )
//                        }
//                        if (locations.isNotEmpty()) {
//                            LocationRepository.getInstance(
//                                context,
//                                Executors.newSingleThreadExecutor()
//                            )
//                                .addLocations(locations)
//                        }
//                    }
//                }
//            }
//            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
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
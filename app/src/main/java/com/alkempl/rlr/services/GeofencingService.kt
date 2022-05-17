package com.alkempl.rlr.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.alkempl.rlr.GeofenceBroadcastReceiver
import com.alkempl.rlr.R
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.utils.GeofencingConstants
import com.alkempl.rlr.utils.hasPermission
import com.alkempl.rlr.viewmodel.GeofenceViewModel
import com.google.android.gms.location.*
import java.util.concurrent.Executors


class GeofencingService : Service() {

    var geofencingEnabled = false

    private val binder = LocalBinder()
    public var landmarkData = ArrayList<GeofenceEntry>()

    private val locationRepository = LocationRepository.getInstance(
        this,
        Executors.newSingleThreadExecutor()
    )

    inner class LocalBinder : Binder() {
        fun getService(): GeofencingService = this@GeofencingService
    }

    /*
        * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
        * method should be called after the user has granted the location permission.  If there are
        * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
        * is now "active."
        */
    @SuppressLint("MissingPermission")
    public fun addGeofenceForClue() {
        if (!applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        val currentGeofenceData = landmarkData[0]
        locationRepository.addGeofence(currentGeofenceData)
    }



    override fun onBind(arg0: Intent?): IBinder {
        Log.d(TAG, "onBindCommand")
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    override fun onCreate() {
        Log.d(TAG, "onCreate")
        startForeground(NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this))
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }


    companion object {

        private const val TAG = "Geofencing"
    }
}
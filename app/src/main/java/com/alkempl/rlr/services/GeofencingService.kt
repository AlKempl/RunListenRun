package com.alkempl.rlr.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.alkempl.rlr.data.GeofencingRepository
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.utils.hasPermission
import java.util.ArrayList
import java.util.concurrent.Executors


class GeofencingService : Service() {

    var geofencingEnabled = false

    private val binder = LocalBinder()

    private val locationRepository = LocationRepository.getInstance(
        this,
        Executors.newSingleThreadExecutor()
    )

    private val geofencingRepository = GeofencingRepository.getInstance(
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

        geofencingRepository.processNext()
    }

    fun getStored(): ArrayList<GeofenceEntry> {
        return geofencingRepository.getStored()
    }

    fun storeGeofence(entry: GeofenceEntry){
        geofencingRepository.storeGeofence(entry)
    }

    fun storeGeofences(entries: List<GeofenceEntry>){
        geofencingRepository.storeGeofences(entries)
    }

    fun getActiveIdx(): Int {
        return geofencingRepository.getActiveIdx()
    }

    fun getActiveEntry(): GeofenceEntry?{
        return geofencingRepository.getActiveEntry()
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
        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )
        super.onCreate()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        geofencingRepository.reset()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        geofencingRepository.reset()
        super.onDestroy()
    }


    companion object {
        private const val TAG = "Geofencing"
    }
}
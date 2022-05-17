package com.alkempl.rlr.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import com.google.android.gms.location.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference


class LocationService : Service() {

    var locationEnabled = false

    private val binder = LocalBinder()

    private val locationRepository = LocationRepository.getInstance(
        this,
        Executors.newSingleThreadExecutor()
    )

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onBind(arg0: Intent?): IBinder {
        Log.d(TAG, "onBindCommand")
        locationRepository.wipeOldLocations()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = sharedPref.getBoolean("bg_location_updates", false)

        if(enabled){
            locationRepository.startLocationUpdates()
        }

        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        locationRepository.wipeOldLocations()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = sharedPref.getBoolean("bg_location_updates", false)

        if(enabled){
            locationRepository.startLocationUpdates()
        }
        return START_STICKY
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        startForeground(NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this))
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        locationRepository.stopLocationUpdates()
    }

    companion object {
        private const val TAG = "GPS"
        private const val LOCATION_INTERVAL = 1000
        private const val LOCATION_DISTANCE = 100
    }
}
package com.alkempl.rlr.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import com.alkempl.rlr.data.LocationRepository
import java.util.concurrent.Executors


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
        val enabled = sharedPref.getBoolean("bg_location_updates", true)

        if(enabled){
            locationRepository.startLocationUpdates()
        }

        return binder
    }

/*    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Log.d(TAG, "onStartCommand")
//        super.onStartCommand(intent, flags, startId)
//        locationRepository.wipeOldLocations()
//        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
//        val enabled = sharedPref.getBoolean("bg_location_updates", true)
//
//        if(enabled){
//            locationRepository.startLocationUpdates()
//        }
//        return START_STICKY
    }*/

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
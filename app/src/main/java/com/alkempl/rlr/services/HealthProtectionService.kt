package com.alkempl.rlr.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import com.google.android.gms.location.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.alkempl.rlr.GeofenceBroadcastReceiver
import com.alkempl.rlr.utils.GeofencingConstants
import com.alkempl.rlr.utils.GeofencingConstants.GEOFENCE_VERSION
import com.alkempl.rlr.viewmodel.GeofenceViewModel


class HealthProtectionService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): HealthProtectionService = this@HealthProtectionService
    }

    private var timers: ArrayList<CountDownTimer> = ArrayList()


    override fun onBind(arg0: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        startPhysicalProtectionTimer()
        return START_STICKY
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )
    }

    private fun startPhysicalProtectionTimer() {
        val timer = object :
            CountDownTimer(DEFAULT_TRAINING_TIMEOUT_MINS * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                Log.d(TAG, "physical protection fired, shutting down")
                firePhysicalProtection()
            }
        }
        timers.add(timer)
        timer.start()
    }

    private fun firePhysicalProtection() {
        val intent = Intent("shutdownScenarioServicePlease")
        val bundle = Bundle()
//        bundle.putString("json", json)
//        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        for (timer in timers) {
            timer.cancel()
        }

        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HealthProtection"
        private const val DEFAULT_TRAINING_TIMEOUT_MINS = 1;
//        private const val DEFAULT_TRAINING_TIMEOUT_MINS = 30;
    }
}
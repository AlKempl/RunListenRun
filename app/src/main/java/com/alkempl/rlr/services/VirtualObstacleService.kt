package com.alkempl.rlr.services

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*


class VirtualObstacleService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): VirtualObstacleService = this@VirtualObstacleService
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
        val intent = Intent("scenarioShutdownHealth")
//        val bundle = Bundle()
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
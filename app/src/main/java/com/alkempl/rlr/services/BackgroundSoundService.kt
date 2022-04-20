package com.alkempl.rlr.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent

import android.os.IBinder

import android.app.Service
import android.content.Context
import android.graphics.Color

import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alkempl.rlr.R

class BackgroundSoundService : Service() {
    private var player: MediaPlayer? = null
    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()

        startForeground(NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this))

        player = MediaPlayer.create(this, R.raw.groovin)
        player!!.isLooping = true // Set looping
        player!!.setVolume(50f, 50f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        player!!.start()
        return START_STICKY
    }

   /* override fun onStart(intent: Intent?, startId: Int) {
        // TO DO
    }*/

    fun onUnBind(arg0: Intent?): IBinder? {
        // TO DO Auto-generated method
        return null
    }

    fun onStop() {}
    fun onPause() {}
    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        player!!.stop()
        player!!.release()
    }

    override fun onLowMemory() {}

    companion object {
        private val TAG: String = "BG_MUSIC_SVC"
    }
}

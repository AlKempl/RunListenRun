package com.alkempl.rlr.services

import android.content.Intent

import android.os.IBinder

import android.app.Service

import android.media.MediaPlayer
import android.os.Binder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alkempl.rlr.R

class SoundService : Service() {
    private var player = MediaPlayer()
    private val binder = LocalBinder()

    override fun onBind(arg0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
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
        this.player.stop()
        this.player.reset()
        this.player.release()
    }

    fun playTrack(track_name: String) {
        val resId = application.resources.getIdentifier(track_name, "raw", packageName)
        val resFd = application.resources.openRawResourceFd(resId) ?: return

        if (this.player.isPlaying) {
            this.player.stop()
            this.player.reset()
        }

        this.player.setDataSource(resFd.fileDescriptor, resFd.startOffset, resFd.length)
        this.player.prepare()
        this.player.isLooping = true // Set looping
        this.player.setVolume(50f, 50f)
        this.player.start()
    }

    override fun onLowMemory() {}

    inner class LocalBinder : Binder() {
        fun getService(): SoundService = this@SoundService
    }

    companion object {
        private val TAG: String = "BG_MUSIC_SVC"
    }
}

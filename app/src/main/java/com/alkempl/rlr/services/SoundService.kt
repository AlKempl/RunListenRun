package com.alkempl.rlr.services

import android.content.Intent

import android.os.IBinder

import android.app.Service

import android.media.MediaPlayer
import android.os.Binder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.alkempl.rlr.R

class SoundService : Service() {
    private var player = MediaPlayer()
    private val binder = LocalBinder()

    override fun onBind(arg0: Intent?): IBinder {
        Log.d(TAG, "onBindCommand")
        return binder
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        this.player.stop()
        this.player.reset()
        this.player.release()
        return super.onUnbind(intent)
    }

    fun onStop() {}
    fun onPause() {}
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
    }

    fun playTrack(track_name: String) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val musicEnabled = sharedPref.getBoolean("enable_music_pref_value", true)

        if(musicEnabled){
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
        }else{
            Log.d(TAG, "Cant play [$track_name]: music is disabled from preferences")
        }
    }

    override fun onLowMemory() {}

    inner class LocalBinder : Binder() {
        fun getService(): SoundService = this@SoundService
    }

    companion object {
        private val TAG: String = "Sound"
    }
}

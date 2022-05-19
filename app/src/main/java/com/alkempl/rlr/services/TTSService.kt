package com.alkempl.rlr.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.alkempl.rlr.R
import java.util.*


class TTSService : Service() {

    private val binder = LocalBinder()

    private var ttsManager: TTSManager? = null

    inner class LocalBinder : Binder() {
        fun getService(): TTSService = this@TTSService
    }


    fun speak(text: String) {
//        if(ttsManager.is){
//            ttsManager!!.speak(text)
//        }else{
//            Log.e(TAG, "speak: tts not enabled 1")
//        }
    }

    override fun onBind(arg0: Intent?): IBinder {
        Log.d(TAG, "onBindCommand")
        ttsManager = TTSManager.getInstance(baseContext)
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
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TTS"
    }
}
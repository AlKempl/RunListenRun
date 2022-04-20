package com.alkempl.rlr.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.concurrent.TimeUnit

class MyService : Service() {

    val LOG_TAG = "myLogs"

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")

        startForeground(NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand")
        someTask()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy")
    }

    fun someTask() {
        Thread {
            for (i in 1..20) {
                Log.d(LOG_TAG, "i = $i")
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            stopSelf()
        }.start()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(LOG_TAG, "onBind")
        return null
    }
}
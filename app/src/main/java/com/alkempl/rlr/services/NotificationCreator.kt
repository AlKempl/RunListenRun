package com.alkempl.rlr.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService

class NotificationCreator {
    companion object{
        private val NOTIFICATION_ID = 1094
        private var CHANNEL_ID: String? = null
        private var notification: Notification? = null

        fun getNotification(context: Context): Notification? {
            if(CHANNEL_ID == null){
                CHANNEL_ID =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel("my_service", "My Background Service", context)
                    } else {
                        // If earlier version channel ID is not used
                        // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                        ""
                    }
            }

            if (notification == null) {
                notification = NotificationCompat.Builder(context, CHANNEL_ID!!)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle("Try Foreground Service")
                    .setContentText("Yuhu..., I'm trying foreground service")
//                .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
            }
            return notification
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(channelId: String, channelName: String, context: Context): String{
            val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
        }

        fun getNotificationId(): Int {
            return NOTIFICATION_ID
        }
    }

}
/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alkempl.rlr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.alkempl.rlr.data.GeofencingRepository
import com.alkempl.rlr.services.TTSManager
import com.alkempl.rlr.utils.errorMessage
import com.alkempl.rlr.utils.vibrate
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.concurrent.Executors

/*
 * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
 * ID from the first Geofence, and locate it within the registered landmark data in our
 * GeofencingConstants within GeofenceUtils, which is a linear string search. If we had  very large
 * numbers of Geofence possibilities, it might make sense to use a different data structure.  We
 * then pass the Geofence index into the notification, which allows us to have a custom "found"
 * message associated with each Geofence.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private lateinit var ttsManager: TTSManager


    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            Log.d(TAG, "ACTION_GEOFENCE_EVENT!")
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, context.getString(R.string.geofence_entered))
//                Toast.makeText(context, context.getString(R.string.geofence_entered),
//                    Toast.LENGTH_LONG)
//                    .show()

                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                val geofencingRepository =
                    GeofencingRepository.getInstance(context, Executors.newSingleThreadExecutor())

                // Check geofence against the constants listed in GeofenceUtil.kt to see if the
                // user has entered any of the locations we track for geofences.
                val foundIndex = geofencingRepository.getStored().indexOfFirst {
                    it.id == fenceId
                }

                // Unknown Geofences aren't helpful to us
                if (-1 == foundIndex) {
                    Log.e(TAG, "Unknown Geofence: Abort Mission")
                    return
                }

                if (geofencingRepository.getActiveIdx() != foundIndex) {
                    Log.e(TAG, "Not Active Geofence: Abort Mission")
                    return
                }

                ttsManager = TTSManager.getInstance(context)

//                val notificationManager = ContextCompat.getSystemService(
//                    context,
//                    NotificationManager::class.java
//                ) as NotificationManager
//
//                notificationManager.sendGeofenceEnteredNotification(
//                    context, foundIndex
//                )

                val enteredGeofence = geofencingRepository.getActiveEntry()
                Log.d(TAG, "Entered: $enteredGeofence")

                val enteredDesc = "Ура! Локация ${enteredGeofence?.name} открыта!"
                geofencingRepository.processNext()

                val nextGeofence = geofencingRepository.getActiveEntry()

                val nextDesc = if (nextGeofence != null)
                    "Следующая локация – ${nextGeofence.name}. ${nextGeofence.hint}"
                     else "Глава завершена!"

                val pattern = longArrayOf(0, 200, 100, 300)
                vibrate(context,pattern)

                ttsManager.speak("$enteredDesc $nextDesc")

                Toast.makeText(
                    context,
//                    context.getString(R.string.geofence_entered)
                    "$enteredDesc: $nextDesc",
                    Toast.LENGTH_LONG
                ).show()

                //TODO Stop scenario on chapter finish
            }
        }
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT =
            "com.alkempl.rlr.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val TAG = "GeofenceReceiver"

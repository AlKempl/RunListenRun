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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alkempl.rlr.data.GeofencingManager
import com.alkempl.rlr.data.model.TextContentType
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.services.ActionsManager
import com.alkempl.rlr.services.ScenarioManager
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
    private lateinit var actionsManager: ActionsManager
    private lateinit var scenarioManager: ScenarioManager
    private lateinit var geofencingManager: GeofencingManager

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

                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                geofencingManager =
                    GeofencingManager.getInstance(context, Executors.newSingleThreadExecutor())

                // Check geofence against the constants listed in GeofenceUtil.kt to see if the
                // user has entered any of the locations we track for geofences.
                val foundIndex = geofencingManager.getStored().indexOfFirst {
                    it.id == fenceId
                }

                // Unknown Geofences aren't helpful to us
                if (-1 == foundIndex) {
                    Log.e(TAG, "Unknown Geofence: Abort Mission")
                    return
                }

                if (geofencingManager.getActiveIdx() != foundIndex) {
                    Log.e(TAG, "Not Active Geofence: Abort Mission")
                    return
                }

                ttsManager = TTSManager.getInstance(context)
                actionsManager = ActionsManager.getInstance(context)
                scenarioManager = ScenarioManager.getInstance(context)

                val enteredGeofence = geofencingManager.getActiveEntry()
                Log.d(TAG, "Entered: $enteredGeofence")
                geofencingManager.processNext()
                val nextGeofence = geofencingManager.getActiveEntry()

                enteredGeofence?.let {
                    onGoodGeofenceEntered(it, nextGeofence, context)
                }

                if (nextGeofence == null) {
                    onChapterFinished(context)
                }
            }
        }
    }

    private fun onChapterFinished(context: Context) {
        ttsManager.speak("Ура! Глава завершена!", true)

        scenarioManager.finishChapter()
        actionsManager.clear()

        val intent22 = Intent("scenarioShutdownChapterEnd")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent22)

    }

    private fun onGoodGeofenceEntered(
        enteredGeofence: GeofenceEntry,
        nextGeofence: GeofenceEntry?,
        context: Context
    ) {
        val ttsEnteredDesc = enteredGeofence.getEnteredText(TextContentType.VOICE)
        val enteredDesc = enteredGeofence.getEnteredText(TextContentType.ONSCREEN)
        val nextDesc = nextGeofence?.getTargetedText(TextContentType.ONSCREEN) ?: ""
        val ttsNextDesc = nextGeofence?.getTargetedText(TextContentType.VOICE) ?: ""

        val pattern = longArrayOf(0, 200, 100, 300)
        vibrate(context, pattern)

        ttsManager.speak("$ttsEnteredDesc .. $ttsNextDesc")

        geofencingManager.setStatusHint("$enteredDesc: $nextDesc")

        Toast.makeText(
            context,
            "$enteredDesc: $nextDesc",
            Toast.LENGTH_LONG
        ).show()

        //        TODO: process events on entering
        actionsManager.clearGeofenceTimers()

        enteredGeofence.events?.forEach { event ->
            event.actions?.forEach { action ->
                actionsManager.processEventAction(event, action, true)
            }
        }
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT =
            "com.alkempl.rlr.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val TAG = "GeofenceReceiver"

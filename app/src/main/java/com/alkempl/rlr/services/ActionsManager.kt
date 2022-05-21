package com.alkempl.rlr.services

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.data.model.scenario.ChapterEvent
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ChapterEventType


private const val TAG = "ActionsManager"

class ActionsManager private constructor(private val context: Context) {
    private var generalTimerActions: ArrayList<CountDownTimer> = ArrayList()
    private var currentGeofenceTimerActions: ArrayList<CountDownTimer> = ArrayList()

    init {
        Log.d("${TAG}/BasicSetup", "OK")
    }

    internal fun processEventAction(
        event: ChapterEvent,
        action: ChapterEventAction,
        fromGeofence: Boolean = false
    ) {
        val time = when (event.type) {
            ChapterEventType.INITIAL -> 0
            ChapterEventType.TIME_BASED -> event.time!!
            ChapterEventType.RANDOM -> (0..890).random()
        }
        scheduleAction(time * 1000, event, action, fromGeofence)

        /*when (action.type) {
            EventActionType.PLAY_SOUND -> {
                randomActions.push(
                    Pair<String, ChapterEventAction>(UUID.randomUUID(), action)
                )
            }

            EventActionType.GENERATE_OBSTACLE -> {
                randomActions.push(
                    Pair<String, ChapterEventAction>(UUID.randomUUID(), action)
                )
                randomActions.push(
                    Pair<String, ChapterEventAction>(UUID.randomUUID(), action)
                )
            }
        }*/
    }

    private fun scheduleAction(
        millisInFuture: Number,
        @Suppress("UNUSED_PARAMETER")
        event: ChapterEvent,
        action: ChapterEventAction,
        fromGeofence: Boolean = false
    ) {
        val actionStartTimer = action.initTimer(millisInFuture, context)
        if (fromGeofence) {
            currentGeofenceTimerActions.add(actionStartTimer)
        } else {
            generalTimerActions.add(actionStartTimer)
        }
        actionStartTimer.start()

        val eventFinishTimer = action.finishTimer(millisInFuture, context)
        eventFinishTimer?.let {
            if (fromGeofence) {
                currentGeofenceTimerActions.add(it)
            } else {
                generalTimerActions.add(it)
            }
            it.start()
        }
    }

    fun clear() {
        Log.d(TAG, "clear")
        clearGeneralTimers()
        clearGeofenceTimers()
    }

    fun clearGeofenceTimers() {
        Log.d(TAG, "clearGeofenceTimers")
        for (timer in currentGeofenceTimerActions) {
            timer.cancel()
        }
        currentGeofenceTimerActions.clear()
    }

    fun clearGeneralTimers() {
        Log.d(TAG, "clearGeofenceTimers")
        for (timer in generalTimerActions) {
            timer.cancel()
        }
        generalTimerActions.clear()
    }


    companion object {
        @Volatile
        private var INSTANCE: ActionsManager? = null

        fun getInstance(context: Context): ActionsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActionsManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
package com.alkempl.rlr.services

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.alkempl.rlr.data.model.scenario.ChapterEvent
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ChapterEventType
import java.util.concurrent.TimeUnit


private const val TAG = "HealthProtectionManager"
private val DEFAULT_TRAINING_TIMEOUT = 60


class HealthProtectionManager private constructor(private val context: Context) {

    private var timers: ArrayList<CountDownTimer> = ArrayList()

    fun startPhysicalProtectionTimer() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val settVal = sharedPref.getString("max_running_time_value",
            DEFAULT_TRAINING_TIMEOUT.toString()
        )
        val timeout = TimeUnit.MINUTES.toMillis(settVal?.toLong() ?: DEFAULT_TRAINING_TIMEOUT.toLong())
        val timer = object :
            CountDownTimer(timeout, 1000) {
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    init {
        Log.d("${TAG}/BasicSetup", "OK")
    }

    fun clear() {
        Log.d(TAG, "clear")
        for (timer in timers) {
            timer.cancel()
        }
        timers.clear()
    }


    companion object {
        @Volatile
        private var INSTANCE: HealthProtectionManager? = null

        fun getInstance(context: Context): HealthProtectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HealthProtectionManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
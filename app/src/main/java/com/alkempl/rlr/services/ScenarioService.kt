package com.alkempl.rlr.services

import Scenario
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.Tag
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.alkempl.rlr.data.model.scenario.ChapterEvent
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ChapterEventType
import com.alkempl.rlr.data.model.scenario.EventActionType
import com.google.gson.Gson
import java.io.File
import java.io.Reader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


class ScenarioService : Service() {
    private var isRunning = false

    private var scenario: Scenario? = null

    private val binder = LocalBinder()

    private var soundService: SoundService? = null
    private var soundServiceBounded = false
    private val soundServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as SoundService.LocalBinder
            soundService = binder.getService()
            soundServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            soundServiceBounded = false
            soundService = null
        }
    }

    private var timerActions: ArrayList<CountDownTimer> = ArrayList()
    private var fixedRateTimer: Timer? = null
    private var randomActions: Stack<ChapterEventAction> = Stack()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )
        this.isRunning = true;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(Companion.TAG, "onStartCommand")

        val soundServiceIntent = Intent(applicationContext, SoundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(Companion.TAG, "start Sound ForegroundService")
            this.startForegroundService(soundServiceIntent)
        } else {
            Log.d(Companion.TAG, "start Sound Service")
            this.startService(soundServiceIntent)
        }

        // Bind to LocalService
        Intent(application, SoundService::class.java).also { ssintent ->
            bindService(ssintent, soundServiceConnection, Context.BIND_AUTO_CREATE)
        }

        loadScenarioFromRawResource("demo_scenario")
        Log.d(Companion.TAG, "json scenario loaded")
        processChapters()
        Log.d(Companion.TAG, "scenario parsed")
        initRandomTickActionTimer()
        Log.d(Companion.TAG, "random tick parsed")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun processChapters() {
        this.scenario?.let {
            it.chapters?.let { chapters ->
                for (chapter in chapters) {
                    chapter.events?.let { events ->
                        for (event in events) {
                            event.actions?.let {
                                for (action in event.actions) {
                                    processEventAction(event, action)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processEventAction(event: ChapterEvent, action: ChapterEventAction){
        when (event.type) {
            ChapterEventType.TIME_BASED -> {
                scheduleAction(event.time!! * 1000, event, action)
            }

            ChapterEventType.RANDOM -> {
                randomActions.push(action)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val soundServiceIntent = Intent(applicationContext, SoundService::class.java)
        this.stopService(soundServiceIntent)
        unbindService(soundServiceConnection)

        fixedRateTimer?.cancel()

        for (timer in timerActions) {
            timer.cancel()
        }

        this.isRunning = false
        Log.d(Companion.TAG, "onDestroy")
    }

    private fun runEventAction(tag: String, action: ChapterEventAction, event: ChapterEvent? = null){
        val desc = when (action.type) {
            EventActionType.PLAY_SOUND -> {
                val track = action.attributes.getOrDefault("track_name", "groovin")
                if (soundServiceBounded) {
                    Log.d(tag, "soundServiceBounded")
                    soundService?.playTrack(track)
                }
                "playing track $track"
            }

            EventActionType.GENERATE_OBSTACLE -> {
                "obstacle generation"
            }
        }
        Log.d(tag, "action done: $desc")
    }

    private fun initRandomTickActionTimer() {

        fixedRateTimer = fixedRateTimer("random_tick_action", false, 0L, 5 * 1000) {
            if (!randomActions.empty()) {
                val action = randomActions.pop()
                runEventAction("RTA", action)
            }
        }
    }

    private fun scheduleAction(
        millisInFuture: Number,
        event: ChapterEvent,
        action: ChapterEventAction
    ) {
        val timer = object : CountDownTimer(millisInFuture.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("CDT-" + event.hashCode(), "seconds remaining: " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                runEventAction("CDT-" + event.hashCode(), action, event)
            }
        }
        timerActions.add(timer)
        timer.start()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(Companion.TAG, "onBind")
        return binder
    }

    fun loadScenarioFromFile(filename: String) {
        val reader = File(filename).bufferedReader()
        _loadScenario(reader)
    }

    fun loadScenarioFromRawResource(res_name: String) {
        Log.d(Companion.TAG, "loadScenarioFromRawResource: resId")
        val resId = this.resources.getIdentifier(res_name, "raw", packageName)
        Log.d(Companion.TAG, "loadScenarioFromRawResource: reader")
        val reader = resources.openRawResource(resId).bufferedReader()
        Log.d(Companion.TAG, "loadScenarioFromRawResource: _loadScenario")
        _loadScenario(reader)
    }

    private fun _loadScenario(reader: Reader) {
        Log.d(Companion.TAG, "_loadScenario: gson")
        val gson = Gson()
        Log.d(Companion.TAG, "_loadScenario: fromJson")
        this.scenario = gson.fromJson(reader, Scenario::class.java)
    }

    fun isRunning(): Boolean {
        return this.isRunning
    }

    inner class LocalBinder : Binder() {
        fun getService(): ScenarioService = this@ScenarioService
    }

    companion object {
        private const val TAG = "SCS"
    }
}
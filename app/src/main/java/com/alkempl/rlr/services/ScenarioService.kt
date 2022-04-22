package com.alkempl.rlr.services

import Scenario
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.model.obstacle.ObstacleFactory
import com.alkempl.rlr.data.model.obstacle.ObstacleType
import com.alkempl.rlr.data.model.scenario.ChapterEvent
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ChapterEventType
import com.alkempl.rlr.data.model.scenario.EventActionType
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.io.Reader
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread


class ScenarioService : Service() {
    private var isRunning = false

    private var scenario: Scenario? = null

    private val binder = LocalBinder()

    private var soundService: SoundService? = null
    private var locationService: LocationService? = null
    private var soundServiceBounded = false
    private var locationServiceBounded = false
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

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            locationServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            locationServiceBounded = false
            locationService = null
        }
    }

    private var timerActions: ArrayList<CountDownTimer> = ArrayList()
    private var fixedRateTimer: Timer? = null
    private var randomActions: Stack<Pair<UUID, ChapterEventAction>> = Stack()
    /*
    * stack: {
    * "uuid_{type}" : Action
    * }
    * */

    private val obstacleRepository = ObstacleRepository.getInstance(
        this,
        Executors.newSingleThreadExecutor()
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )

        startPhysicalProtection()
    }

    private fun startPhysicalProtection() {
        val TIME_LIMIT_MINUTES = 1
//        val TIME_LIMIT_MINUTES = 30
        val timer = object : CountDownTimer(TIME_LIMIT_MINUTES*60*1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(
                    "PPT",
                    "seconds remaining: " + millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                Log.d("PPT", "physical protection fired, shutting down")
                firePhysicalProtection()
            }
        }
        timerActions.add(timer)
        timer.start()
    }

    private fun firePhysicalProtection() {
        notifyFragment("")
    }

    private fun notifyFragment(json: String){
        val intent = Intent("shutdownScenarioServicePlease");
        val bundle = Bundle();
//        bundle.putString("json", json)
//        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        this.isRunning = true

        val soundServiceIntent = Intent(applicationContext, SoundService::class.java)
        val locationServiceIntent = Intent(applicationContext, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "start Sound ForegroundService")
            this.startForegroundService(soundServiceIntent)
            this.startForegroundService(locationServiceIntent)
        } else {
            Log.d(TAG, "start Sound Service")
            this.startService(soundServiceIntent)
            this.startService(locationServiceIntent)
        }

        // Bind to LocalService
        Intent(application, SoundService::class.java).also { ssintent ->
            bindService(ssintent, soundServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Intent(application, LocationService::class.java).also { ssintent ->
            bindService(ssintent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }

        loadScenarioFromRawResource("demo_scenario")
        Log.d(TAG, "json scenario loaded")
        processChapters()
        Log.d(TAG, "scenario parsed")
//        initRandomTickActionTimer()
//        Log.d(TAG, "random tick parsed")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun processChapters() {
        this.scenario?.let { scenario ->
            scenario.chapters?.forEach { chapter ->
                chapter.events?.forEach { event ->
                    event.actions?.forEach { action ->
                        processEventAction(event, action)
                    }
                }
            }
        }
    }

    private fun processEventAction(event: ChapterEvent, action: ChapterEventAction) {
        val time = when (event.type) {
            ChapterEventType.TIME_BASED -> {
                event.time!!
            }

            ChapterEventType.RANDOM -> {
                (0..890).random()
            }
        }
        scheduleAction(time * 1000, event, action)

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

    override fun onDestroy() {
        this.isRunning = false
        super.onDestroy()

        val soundServiceIntent = Intent(applicationContext, SoundService::class.java)
        this.stopService(soundServiceIntent)
        unbindService(soundServiceConnection)

        val locationServiceIntent = Intent(applicationContext, LocationService::class.java)
        this.stopService(locationServiceIntent)
        unbindService(locationServiceConnection)

        fixedRateTimer?.cancel()

        for (timer in timerActions) {
            timer.cancel()
        }

        Log.d(TAG, "onDestroy")
    }

  /*  private fun initRandomTickActionTimer() {
        fixedRateTimer = fixedRateTimer("random_tick_action", true, 0L, 5 * 1000) {
            if (!randomActions.empty()) {
                val (uuid, action) = randomActions.pop()
                when (action.type) {
                    EventActionType.PLAY_SOUND -> {
                        val track = action.attributes.getOrDefault("track_name", "groovin")
                        if (soundServiceBounded) {
                            Log.d("RTA", "soundServiceBounded")
                            soundService?.playTrack(track)
                        }
                        val desc = "playing track $track"
                        Log.d("RTA", "action done: $desc")
                    }

                    EventActionType.GENERATE_OBSTACLE -> {
                        val type = ObstacleType.from(action.attributes.getOrDefault("type", "dogs"))
                        val duration = action.attributes.getOrDefault("duration", "20").toInt()

                        val obstacle = ObstacleFactory.buildObstacle(type!!, duration)

                        val desc = "obstacle generation"
                        Log.d("RTA", "action done: $desc")

                        val obstacleFinishTimer = object : CountDownTimer(
                            obstacle.duration.toLong() * 1000,
                            1000
                        ) {
                            override fun onTick(millisUntilFinished: Long) {
                                Log.d(
                                    "RTA",
                                    "seconds remaining: " + millisUntilFinished / 1000
                                )
                            }

                            override fun onFinish() {
                                val desc = "obstacle finalization"
                                Log.d("RTA", "action done: $desc")
                                obstacle.onFinish()
                            }
                        }

                        obstacle.onStart()
                        timerActions.add(obstacleFinishTimer)
                        obstacleFinishTimer.start()
                    }
                }
            }
        }
    }*/

    private fun scheduleAction(
        millisInFuture: Number,
        event: ChapterEvent,
        action: ChapterEventAction
    ) {
        when (action.type) {
            EventActionType.PLAY_SOUND -> {
                val musicPlayTimer = object : CountDownTimer(millisInFuture.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(
                            "CDT-" + event.hashCode(),
                            "seconds remaining: " + millisUntilFinished / 1000
                        )
                    }

                    override fun onFinish() {
                        val track = action.attributes.getOrDefault("track_name", "groovin")
                        if (soundServiceBounded) {
                            Log.d("CDT-" + event.hashCode(), "soundServiceBounded")
                            soundService?.playTrack(track)
                        }
                        val desc = "playing track $track"
                        Log.d("CDT-" + event.hashCode(), "action done: $desc")
                    }
                }
                timerActions.add(musicPlayTimer)
                musicPlayTimer.start()
            }

            EventActionType.GENERATE_OBSTACLE -> {
                val type = ObstacleType.from(action.attributes.getOrDefault("type", "dogs"))
                val duration = action.attributes.getOrDefault("duration", "20").toInt()

                val obstacle = ObstacleFactory.buildObstacle(type!!, application, duration)

                val obstacleStartTimer = object : CountDownTimer(millisInFuture.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(
                            "CDT-" + event.hashCode(),
                            "seconds remaining: " + millisUntilFinished / 1000
                        )
                    }

                    override fun onFinish() {
                        val desc = "obstacle generation"
                        Log.d("CDT-" + event.hashCode(), "action done: $desc")
                        obstacle.onStart()
                    }
                }
                timerActions.add(obstacleStartTimer)
                obstacleStartTimer.start()

                val obstacleFinishTimer = object :
                    CountDownTimer(millisInFuture.toLong() + duration.toLong() * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(
                            "CDT-" + event.hashCode(),
                            "seconds remaining: " + millisUntilFinished / 1000
                        )
                    }

                    override fun onFinish() {
                        val desc = "obstacle finalization"
                        Log.d("CDT-" + event.hashCode(), "action done: $desc")
                        obstacle.onFinish()
                    }
                }
                timerActions.add(obstacleFinishTimer)
                obstacleFinishTimer.start()
            }
        }

    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    fun loadScenarioFromFile(filename: String) {
        val reader = File(filename).bufferedReader()
        _loadScenario(reader)
    }

    fun loadScenarioFromRawResource(res_name: String) {
        Log.d(TAG, "loadScenarioFromRawResource: resId")
        val resId = this.resources.getIdentifier(res_name, "raw", packageName)
        Log.d(TAG, "loadScenarioFromRawResource: reader")
        val reader = resources.openRawResource(resId).bufferedReader()
        Log.d(TAG, "loadScenarioFromRawResource: _loadScenario")
        _loadScenario(reader)
    }

    private fun _loadScenario(reader: Reader) {
        Log.d(TAG, "_loadScenario: gson")
        val gson = Gson()
        Log.d(TAG, "_loadScenario: fromJson")
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
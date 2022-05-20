package com.alkempl.rlr.services

import Scenario
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.annotation.UiThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.model.obstacle.ObstacleFactory
import com.alkempl.rlr.data.model.obstacle.ObstacleType
import com.alkempl.rlr.data.model.scenario.ChapterEvent
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ChapterEventType
import com.alkempl.rlr.data.model.scenario.EventActionType
import com.google.gson.Gson
import java.io.File
import java.io.Reader
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class ScenarioService : Service() {
    private var isRunning = false

    private var scenario: Scenario? = null

    private val binder = LocalBinder()

    private var soundService: SoundService? = null
    private var locationService: LocationService? = null
    private var geofencingService: GeofencingService? = null
    private var healthService: HealthProtectionService? = null

    private var soundServiceBounded = false
    private var locationServiceBounded = false
    private var geofencingServiceBounded = false
    private var healthServiceBounded = false

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

    private val geofencingServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as GeofencingService.LocalBinder
            geofencingService = binder.getService()
            geofencingServiceBounded = true
            Log.d("$TAG/GEO1", "geofencingServiceConnection onServiceConnected")
            processGeofences()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            geofencingServiceBounded = false
            geofencingService = null
            Log.d("$TAG/GEO9", "geofencingServiceConnection onServiceDisconnected")
        }
    }

    private val healthServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HealthProtectionService.LocalBinder
            healthService = binder.getService()
            healthServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            healthServiceBounded = false
            healthService = null
        }
    }

    private lateinit var ttsManager: TTSManager

    private fun ttsTest() {
        Log.d("$TAG/HELLO", "Привет, приложение запущено!")
        ttsManager.speak("Привет, приложение запущено!", true)
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

        this.isRunning = true

        Log.d(TAG, "onStartCommand")

        ttsManager = TTSManager.getInstance(baseContext)
        ttsManager.speak("Привет")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val scenario_prefix = sharedPref.getString("scenario_pref_value", "demo")
        Log.d(TAG, "Loading [\"${scenario_prefix}_scenario\"] scenario json")

        loadScenarioFromRawResource("${scenario_prefix}_scenario")
        Log.d(TAG, "json scenario loaded")
        processChapters()
        Log.d(TAG, "scenario parsed")

        // Bind to LocalService
        Intent(application, SoundService::class.java).also { ssintent ->
            bindService(ssintent, soundServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(application, LocationService::class.java).also { ssintent ->
            bindService(ssintent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(application, GeofencingService::class.java).also { ssintent ->
            bindService(ssintent, geofencingServiceConnection, Context.BIND_AUTO_CREATE)
        }
        Log.d("$TAG/GEO3", "bindService geofencingServiceConnection")

        Intent(application, HealthProtectionService::class.java).also { ssintent ->
            bindService(ssintent, healthServiceConnection, Context.BIND_AUTO_CREATE)
        }

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )

        someTask()
    }


    private fun notifyFragment(json: String) {
        val intent = Intent("scenarioShutdownHealth")
        val bundle = Bundle()
//        bundle.putString("json", json)
//        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


//        initRandomTickActionTimer()
//        Log.d(TAG, "random tick parsed")
        Log.d(TAG, "onStartCommand End")
        return super.onStartCommand(intent, flags, startId)
    }

    @UiThread
    private fun someTask() {
        Thread {
            ttsTest()
            stopSelf()
        }.start()
    }

    private fun processChapters() {
        this.scenario?.let { scenario ->
            scenario.chapters?.forEach { chapter ->

                chapter.initial_event?.let { initial_event ->
                    initial_event.actions?.forEach { action ->
                        processEventAction(initial_event, action)
                    }
                }

                chapter.events?.forEach { event ->
                    event.actions?.forEach { action ->
                        processEventAction(event, action)
                    }
                }
            }
        }
    }

    private fun processGeofences() {
        if (geofencingServiceBounded) {
            this.scenario?.let { scenario ->
                scenario.chapters?.forEach { chapter ->
                    chapter.geofencing?.let {
                        geofencingService?.storeGeofences(it)
                        Log.d("$TAG/ADD_GEOFENCES", "Len: ${chapter.geofencing.size}")
                    }
                }
            }
            geofencingService?.processNext()
        }
    }

    private fun processEventAction(event: ChapterEvent, action: ChapterEventAction) {
        val time = when (event.type) {
            ChapterEventType.INITIAL -> 0
            ChapterEventType.TIME_BASED -> event.time!!
            ChapterEventType.RANDOM -> (0..890).random()
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

        if (soundServiceBounded) {
            val soundServiceIntent = Intent(applicationContext, SoundService::class.java)
            this.stopService(soundServiceIntent)
            unbindService(soundServiceConnection)
        }

        if (locationServiceBounded) {
            val locationServiceIntent = Intent(applicationContext, LocationService::class.java)
            this.stopService(locationServiceIntent)
            unbindService(locationServiceConnection)
        }

        if (geofencingServiceBounded) {
            val geofencingServiceIntent = Intent(applicationContext, GeofencingService::class.java)
            this.stopService(geofencingServiceIntent)
            unbindService(geofencingServiceConnection)
        }

        if (healthServiceBounded) {
            val healthServiceIntent =
                Intent(applicationContext, HealthProtectionService::class.java)
            this.stopService(healthServiceIntent)
            unbindService(healthServiceConnection)
        }

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
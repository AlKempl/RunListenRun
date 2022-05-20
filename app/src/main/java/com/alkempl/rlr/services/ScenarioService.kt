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

    private val binder = LocalBinder()

    private var locationService: LocationService? = null
    private var geofencingService: GeofencingService? = null
    private var healthService: HealthProtectionService? = null

    private var locationServiceBounded = false
    private var geofencingServiceBounded = false
    private var healthServiceBounded = false

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
//            processGeofences()
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
    private lateinit var soundManager: SoundManager
    private lateinit var scenarioManager: ScenarioManager
    private lateinit var actionsManager: ActionsManager

    private fun ttsTest() {
        Log.d("$TAG/HELLO", "Привет, приложение запущено!")
        ttsManager.speak("Привет, приложение запущено!", true)
    }

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

        soundManager = SoundManager.getInstance(baseContext)
        scenarioManager = ScenarioManager.getInstance(baseContext)
        actionsManager = ActionsManager.getInstance(baseContext)


        // Bind to LocalService
        Intent(application, LocationService::class.java).also { ssintent ->
            bindService(ssintent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(application, GeofencingService::class.java).also { ssintent ->
            bindService(ssintent, geofencingServiceConnection, Context.BIND_AUTO_CREATE)
        }

        Intent(application, HealthProtectionService::class.java).also { ssintent ->
            bindService(ssintent, healthServiceConnection, Context.BIND_AUTO_CREATE)
        }

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )

        scenarioManager.initialize()
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


//
//    private fun processGeofences() {
//        if (geofencingServiceBounded) {
//            this.scenario?.let { scenario ->
//                scenario.chapters?.forEach { chapter ->
//                    chapter.geofencing?.let {
//                        geofencingService?.storeGeofences(it)
//                        Log.d("$TAG/ADD_GEOFENCES", "Len: ${chapter.geofencing.size}")
//                    }
//                }
//            }
//            geofencingService?.processNext()
//        }
//    }



    override fun onDestroy() {
        this.isRunning = false

        soundManager.clear()
        scenarioManager.clear()
        actionsManager.clear()

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
        super.onDestroy()

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



    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
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
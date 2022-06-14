package com.alkempl.rlr.services

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.annotation.UiThread
import com.alkempl.rlr.data.GeofencingManager
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ScenarioService : Service() {
    private var isRunning = false

    private val binder = LocalBinder()

    private var locationService: LocationService? = null

    private var locationServiceBounded = false

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

    private lateinit var ttsManager: TTSManager
    private lateinit var soundManager: SoundManager
    private lateinit var scenarioManager: ScenarioManager
    private lateinit var actionsManager: ActionsManager
    private lateinit var geofencingManager: GeofencingManager
    private lateinit var obstaclesManager: ObstaclesManager

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
        obstaclesManager = ObstaclesManager.getInstance(baseContext)
        geofencingManager =
            GeofencingManager.getInstance(baseContext, Executors.newSingleThreadExecutor())


        // Bind to LocalService
        Intent(application, LocationService::class.java).also { ssintent ->
            bindService(ssintent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }

        startForeground(
            NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this)
        )

        scenarioManager.initialize()
        scenarioManager.initializeCurrentChapter()

        object : CountDownTimer(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(1)) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                scenarioManager.runScenario()
                someTask()
            }
        }.start()
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


    override fun onDestroy() {
        this.isRunning = false

        soundManager.clear()
        scenarioManager.clear()
        actionsManager.clear()
        geofencingManager.reset()
        ttsManager.stop()
        obstaclesManager.clear()

        if (locationServiceBounded) {
            val locationServiceIntent = Intent(applicationContext, LocationService::class.java)
            this.stopService(locationServiceIntent)
            unbindService(locationServiceConnection)
        }

        fixedRateTimer?.cancel()
        super.onDestroy()

        Log.d(TAG, "onDestroy")
    }


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
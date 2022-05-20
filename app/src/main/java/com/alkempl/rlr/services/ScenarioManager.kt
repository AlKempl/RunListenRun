package com.alkempl.rlr.services

import Scenario
import android.content.Context
import android.content.SharedPreferences.Editor
import android.util.Log
import androidx.preference.PreferenceManager
import com.alkempl.rlr.data.GeofencingRepository
import com.alkempl.rlr.data.model.scenario.ChapterEventAction
import com.alkempl.rlr.data.model.scenario.ScenarioChapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.isharipov.gson.adapters.PolymorphDeserializer
import java.io.File
import java.io.Reader
import java.util.concurrent.Executors


private const val TAG = "ScenarioManager"

class ScenarioManager private constructor(private val context: Context) {

    private var scenario: Scenario? = null

    private var currentChapter: ScenarioChapter? = null
    private var currentChapterId: String? = null
    private var currentChapterIdx: Int? = null

    private var _scenarioInitialized = false

    private lateinit var soundManager: SoundManager
    private lateinit var actionsManager: ActionsManager

    private val geofencingRepository = GeofencingRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

    /**
     * Status of scenario
     */
    val scenarioInitialized: Boolean
        get() = _scenarioInitialized

    fun initialize() {
        soundManager = SoundManager.getInstance(context)
        actionsManager = ActionsManager.getInstance(context)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val scenario_prefix = sharedPref.getString("scenario_pref_value", "demo")

        if (scenario_prefix == "custom") {
            val filename = "dummy_test_filename.json"
            Log.d(TAG, "loading scenario from custom [$filename] file")
            loadScenarioFromFile(filename)
        } else {
            val resName = "${scenario_prefix}_scenario"
            Log.d(TAG, "loading scenario from bundled [$resName] file")
            loadScenarioFromRawResource("${scenario_prefix}_scenario")
        }
        Log.d(TAG, "scenario parsed")

        initializeCurrentChapter()
    }

    private fun loadScenarioFromRawResource(res_name: String) {
        val resId = context.resources.getIdentifier(res_name, "raw", context.packageName)
        val reader = context.resources.openRawResource(resId).bufferedReader()
        loadScenario(reader)
    }

    private fun loadScenarioFromFile(filename: String) {
        val reader = File(filename).bufferedReader()
        loadScenario(reader)
    }

    private fun loadScenario(reader: Reader) {
//        val gson = Gson()

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(
                ChapterEventAction::class.java,
                PolymorphDeserializer<ChapterEventAction>()
            )
            .create()

        scenario = gson.fromJson(reader, Scenario::class.java)
    }

    private fun initializeCurrentChapter() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentChapterIdPref = sharedPref.getString("internal_current_chapter_id", null)

        currentChapterIdx = scenario
            ?.chapters
            ?.indexOfFirst { it.id == currentChapterIdPref }
            .takeIf { it!! >= 0 }
            ?: 0

        currentChapter = scenario?.chapters?.get(currentChapterIdx!!)

        if (currentChapter == null) {
            Log.e(TAG, "No chapter found")
            return
        }

        currentChapterId = currentChapter!!.id

        currentChapter!!.initial_event?.let { initial_event ->
            initial_event.actions?.forEach { action ->
                actionsManager.processEventAction(initial_event, action)
            }
        }

        currentChapter!!.events?.forEach { event ->
            event.actions?.forEach { action ->
                actionsManager.processEventAction(event, action)
            }
        }

        currentChapter!!.geofencing?.forEach { geofence ->
            geofencingRepository.storeGeofence(geofence)
            Log.d("${TAG}/ADD_GEOFENCES", "Len: ${currentChapter!!.geofencing?.size}")
        }

        _scenarioInitialized = true
    }

    fun clear() {
        Log.d(TAG, "clear")

        _scenarioInitialized = false
        scenario = null
        currentChapter = null
        currentChapterId = null
        currentChapterIdx = null
    }

    fun finishChapter() {
        Log.d(TAG, "finishChapter")

        //TODO: inc chapter idx
        scenario!!.chapters
            ?.get(currentChapterIdx!! + 1)
            ?.id
            .let {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                val editor: Editor = sharedPref.edit()
                editor.putString("internal_current_chapter_id", it)
                editor.apply()
            }
        clear()
    }

    fun onScenarioChange() {
        Log.d(TAG, "onScenarioChange")
        //TODO: clear progress or implement inter-chaptered storage for progress
        clear()
    }

    init {
        Log.d("${TAG}/BasicSetup", "OK")
    }

    companion object {
        @Volatile
        private var INSTANCE: ScenarioManager? = null

        fun getInstance(context: Context): ScenarioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScenarioManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
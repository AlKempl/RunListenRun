package com.alkempl.rlr.services

import Scenario
import android.content.Context
import android.content.SharedPreferences.Editor
import android.util.Log
import androidx.preference.PreferenceManager
import com.alkempl.rlr.data.GeofencingManager
import com.alkempl.rlr.data.model.scenario.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader
import java.io.File
import java.util.concurrent.Executors


private const val TAG = "ScenarioManager"

class ScenarioManager private constructor(private val context: Context) {

    var scenarioPrefix: String? = null
        private set

    var scenario: Scenario? = null
        private set

    private var currentChapter: ScenarioChapter? = null
    private var currentChapterId: String? = null
    private var currentChapterIdx: Int? = null

    var scenarioInitialized = false
        private set

    var scenarioParsed = false
        private set

    private lateinit var soundManager: SoundManager
    private lateinit var actionsManager: ActionsManager

    private val geofencingManager = GeofencingManager.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

    fun runScenario(){
        if(scenarioParsed && scenarioInitialized){
            geofencingManager.processNext()
        }else{
            Log.e(TAG, "runScenario: not scenarioParsed && scenarioInitialized")
        }
    }

    fun initialize() {
        soundManager = SoundManager.getInstance(context)
        actionsManager = ActionsManager.getInstance(context)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        scenarioPrefix = sharedPref.getString("scenario_pref_value", "mmcs")

        if (scenarioPrefix == "custom") {
            val filename = "dummy_test_filename.json"
            Log.d(TAG, "loading scenario from custom [$filename] file")
            loadScenarioFromFile(filename)
        } else {
            val resName = "${scenarioPrefix}_scenario"
            Log.d(TAG, "loading scenario from bundled [$resName] file")
            loadScenarioFromRawResource("${scenarioPrefix}_scenario")
        }
        scenarioParsed = true
        Log.d(TAG, "scenario parsed")
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

    private fun loadScenario(reader: BufferedReader) {
//        val gson = Gson()

        val allText = reader.use(BufferedReader::readText)

        val moshi = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(ChapterEventAction::class.java, "action")
                    .withSubtype(MusicChapterEventAction::class.java, "sound")
                    .withSubtype(GenerateObstacleChapterEventAction::class.java, "obstacle")
            )
            .add(KotlinJsonAdapterFactory())
//            .add(EnumJsonAdapter.create(ChapterEventType::class.java).withUnknownFallback(ChapterEventType.INITIAL))
//            .add(EnumJsonAdapter.create(EventActionType::class.java))
            .build()

        val jsonAdapter = moshi.adapter(Scenario::class.java)

        scenario = jsonAdapter.fromJson(allText)
        //                val gson: Gson = GsonBuilder()
//            .registerTypeAdapter(
//                ChapterEventAction::class.java,
//                PolymorphDeserializer<ChapterEventAction>()
//            )
//            .create()

//                        scenario = gson . fromJson (reader, Scenario::class.java)
    }

    fun initializeCurrentChapter() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentChapterIdPref = sharedPref.getString("internal_current_chapter_id", null)

        Log.d(TAG, "currentChapterIdPref $currentChapterIdPref")

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
        Log.d(TAG, "currentChapterId $currentChapterId")


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
            geofencingManager.storeGeofence(geofence)
            Log.d("${TAG}/ADD_GEOFENCE", geofence.toString())
        }

        scenarioInitialized = true
        Log.d(TAG, "current chapter initialized")
    }

    fun clear() {
        Log.d(TAG, "clear")

        scenarioInitialized = false
        scenarioParsed = false
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
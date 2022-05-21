package com.alkempl.rlr

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alkempl.rlr.data.model.scenario.GenerateObstacleChapterEventAction
import com.alkempl.rlr.data.model.scenario.MusicChapterEventAction
import com.alkempl.rlr.services.ScenarioManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScenarioParsingTest {
    lateinit var appContext: Context

    @Test
    fun testScenarioParses() {
        val scenarioManager = ScenarioManager.getInstance(appContext)
        scenarioManager.initialize()
        val scenario = scenarioManager.scenario
        assertEquals(true, scenarioManager.scenarioParsed)
        scenarioManager.clear()
    }

    @Test
    fun testScenarioLoadsMMCS() {
        val scenarioManager = ScenarioManager.getInstance(appContext)
        scenarioManager.initialize()
        assertEquals("mmcs", scenarioManager.scenarioPrefix)
        scenarioManager.clear()
    }

    @Test
    fun testScenarioHasPolymorphActions() {
        val scenarioManager = ScenarioManager.getInstance(appContext)
        scenarioManager.initialize()
        assertEquals(true, scenarioManager.scenarioParsed)
        val scenario = scenarioManager.scenario
        assert(scenario != null)
        scenario?.let {
            assert(scenario.chapters != null)
            it.chapters?.let {
                assert(it.isNotEmpty())
                val firstChapter = it[0]
                firstChapter.let {
                    assert(it.initial_event != null)
                    it.initial_event?.let {
                        assert(it.actions != null)
                        it.actions?.let {
                            assert(it.size > 1)
                            assert(
                                (it[0] is GenerateObstacleChapterEventAction)
                                        or (it[0] is MusicChapterEventAction)
                            )
                            assert(
                                (it[1] is GenerateObstacleChapterEventAction)
                                        or (it[1] is MusicChapterEventAction)
                            )
                        }
                    }
                }
            }
        }
        scenarioManager.clear()
    }

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }
}
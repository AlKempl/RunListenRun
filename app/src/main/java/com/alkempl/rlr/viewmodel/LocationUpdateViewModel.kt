package com.alkempl.rlr.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.alkempl.rlr.data.GeofencingManager
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.services.ObstaclesManager
import com.alkempl.rlr.services.ScenarioManager
import com.alkempl.rlr.services.ScenarioService
import java.util.*
import java.util.concurrent.Executors

/**
 * Allows Fragment to observer {@link MyLocation} database, follow the state of location updates,
 * and start/stop receiving location updates.
 */
class LocationUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    private val scenarioManager = ScenarioManager.getInstance(
        application.applicationContext,
    )

    private val geofencingManager = GeofencingManager.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    private val obstaclesManager = ObstaclesManager.getInstance(
        application.applicationContext,
    )

    val progressMax = geofencingManager.maxIdx
    val progressNow = geofencingManager.activeIdx
    val geofenceStatus = geofencingManager.statusHint

    val chapterName = scenarioManager.currentChapterName
    val scenarioRunning = scenarioManager.scenarioRunning

    val obstacleImg = obstaclesManager.currentObstacleImage

    val receivingLocationUpdates: LiveData<Boolean> = locationRepository.receivingLocationUpdates

    val locationListLiveData = locationRepository.getLocations()

    fun startLocationUpdates() = locationRepository.startLocationUpdates()

    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}

package com.alkempl.rlr.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alkempl.rlr.data.ObstacleRepository
import java.util.concurrent.Executors

/**
 * Allows Fragment to observer {@link MyLocation} database, follow the state of location updates,
 * and start/stop receiving location updates.
 */
class ObstacleUpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val obstacleRepository = ObstacleRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    val obstacleListLiveData = obstacleRepository.getAll()
}

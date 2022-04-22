package com.alkempl.rlr.data.model.obstacle

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import java.util.concurrent.Executors

abstract class Obstacle(
    var context: Context,
    var duration: Int = 0,
) {
    internal val obstacleRepository = ObstacleRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

    internal val locationRepository = LocationRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

    var entity = ObstacleEntity(
        type = ObstacleType.ABSTRACT,
        status = ObstacleStatus.ONGOING
    )

    abstract fun onStart()
    abstract fun onFinish()
    abstract fun onSuccess()
    abstract fun onFailure()
}
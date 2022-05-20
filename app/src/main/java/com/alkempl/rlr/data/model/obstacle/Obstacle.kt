package com.alkempl.rlr.data.model.obstacle

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.services.TTSManager
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import java.util.concurrent.Executors

open class Obstacle(
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

    internal val ttsManager: TTSManager = TTSManager.getInstance(context)

    var entity = ObstacleEntity(
        type = ObstacleType.ABSTRACT,
        status = ObstacleStatus.ONGOING
    )

    open val name = "Непонятное препятствие"
    open val hint = "Непонятное препятствие"

    open fun onStart() {
        ttsManager.speak(getTTSText())
    }
    open fun onFinish() {}
    open fun onSuccess() {}
    open fun onFailure() {}

    fun getTTSText(): String {
        return "Обнаружено препятствие: $name! $hint."
    }
}
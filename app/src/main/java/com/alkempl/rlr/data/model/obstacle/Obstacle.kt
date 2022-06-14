package com.alkempl.rlr.data.model.obstacle

import android.content.Context
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.services.TTSManager
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
    open val failureText = "Препятствие не пройдено. "
    open val successText = "Препятствие пройдено. "
    open val image = "man"

    open fun onStart() {
        ttsManager.speak(getTTSIntroText())
    }

    open fun onFinish() {}

    open fun onSuccess() {
        ttsManager.speak(successText)
    }
    open fun onFailure() {
        ttsManager.speak(failureText)
    }

    fun getTTSIntroText(): String {
        return "Обнаружено препятствие: $name! $hint."
    }

}
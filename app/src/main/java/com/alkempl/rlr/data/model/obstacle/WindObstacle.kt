package com.alkempl.rlr.data.model.obstacle

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.services.SoundService
import java.util.*
import java.util.stream.IntStream

class WindObstacle(context: Context, duration: Int) : Obstacle(context, duration) {
    private val TAG: String = "OBS_WIND"

    override fun onStart() {
        Log.i(TAG, "onStart")
        this.entity.type = ObstacleType.WIND
        this.obstacleRepository.addObstacle(this.entity)
    }

    private fun isDecreasing(a: List<Float>): Boolean {
        return 0 === IntStream.range(1, a.size)
            .reduce(0) { acc, e -> acc + if (a[e - 1] >= a[e]) 0 else 1 }
    }

    override fun onFinish() {
        Log.i(TAG, "onFinish")
        val locationsLD = this.locationRepository.getLocationsByPeriod(this.entity.date, Date())

        // fallback
        this.entity.status = ObstacleStatus.UNKNOWN
        locationsLD.observeForever(androidx.lifecycle.Observer { locations ->
            val speeds = locations.map { locationEntity -> locationEntity.speed }
            if (isDecreasing(speeds)) {
                this.entity.status = ObstacleStatus.SUCCEEDED
            } else {
                this.entity.status = ObstacleStatus.FAILED
            }
        })

        this.obstacleRepository.updateObstacle(this.entity)
    }

    override fun onSuccess() {
        Log.i(TAG, "onSuccess")
//        TODO("Not yet implemented")
    }

    override fun onFailure() {
        Log.i(TAG, "onFailure")
//        TODO("Not yet implemented")
    }

}
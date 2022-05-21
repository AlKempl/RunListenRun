package com.alkempl.rlr.services

import android.content.Context
import android.util.Log
import com.alkempl.rlr.data.model.obstacle.Obstacle


private const val TAG = "ObstaclesManager"

class ObstaclesManager private constructor(private val context: Context) {
    var obstacles: HashMap<Int, Obstacle> = HashMap()
        private set

    var currentObstacle: Obstacle? = null
        private set

    init {
        Log.d("${TAG}/BasicSetup", "OK")
    }

    fun clear() {
        Log.d(TAG, "clear")
        cleanCurrentObstacle()
        cleanObstacles()
    }

    fun cleanObstacles() {
        Log.d(TAG, "cleanObstacles")
        obstacles.clear()
    }

    fun cleanCurrentObstacle() {
        Log.d(TAG, "cleanCurrentObstacle")
        currentObstacle = null
    }

    fun setCurrent(obstacle: Obstacle, hashCode: Int) {
        if (currentObstacle == null) {
            currentObstacle
            Log.d("$TAG/setCurrent", "obstacle [$hashCode] OK")
            obstacle.onStart()
            obstacles[hashCode] = obstacle
        } else {
            Log.e("$TAG/setCurrent", "ERR for obstacle [$hashCode]: currentObstacle not empty")
        }
    }

    fun finalize(hashCode: Int) {
        val obstacle = obstacles[hashCode]

        if (obstacle == null) {
            Log.e("$TAG/finalize", "obstacle [$hashCode] not found")
            return
        }

        obstacle.onFinish()
    }


    companion object {
        @Volatile
        private var INSTANCE: ObstaclesManager? = null

        fun getInstance(context: Context): ObstaclesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ObstaclesManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
package com.alkempl.rlr.data.model.obstacle

import android.content.Context

class ObstacleFactory {
    companion object {
        fun buildObstacle(type: ObstacleType, context: Context, duration: Int): Obstacle {
            return when (type) {
                ObstacleType.WIND -> {
                    WindObstacle(context, duration)
                }

                ObstacleType.DOGS -> {
                    DogsObstacle(context, duration)
                }
                ObstacleType.ABSTRACT -> TODO()
            }
        }
    }
}
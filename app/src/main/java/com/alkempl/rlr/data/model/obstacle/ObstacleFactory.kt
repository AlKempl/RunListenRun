package com.alkempl.rlr.data.model.obstacle

class ObstacleFactory {
    companion object {
        fun buildObstacle(type: ObstacleType, duration: Int): Obstacle {
            return when (type) {
                ObstacleType.WIND -> {
                    WindObstacle(duration)
                }

                ObstacleType.DOGS -> {
                    DogsObstacle(duration)
                }
            }
        }
    }
}
package com.alkempl.rlr.data.db

import androidx.room.TypeConverter
import com.alkempl.rlr.data.model.obstacle.ObstacleStatus
import java.util.*

class ObstacleTypeConverters {
    @TypeConverter
    fun fromObstacleStatus(obstacleStatus: ObstacleStatus): String {
        return obstacleStatus.status
    }

    @TypeConverter
    fun toObstacleStatus(obstacleStatus: String): ObstacleStatus {
        return ObstacleStatus.from(obstacleStatus)
    }
}
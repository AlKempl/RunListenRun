package com.alkempl.rlr.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alkempl.rlr.data.model.obstacle.ObstacleStatus
import com.alkempl.rlr.data.model.obstacle.ObstacleType
import com.alkempl.rlr.getDateTime
import com.google.android.gms.location.FusedLocationProviderClient
import java.text.DateFormat
import java.util.Date
import java.util.UUID

@Entity(tableName = "obstacle")
data class ObstacleEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var type: ObstacleType,
    var date: Date = Date(),
    var status: ObstacleStatus?
) {

    fun getPrefix(): String{
        return when(status){
            ObstacleStatus.ONGOING -> "ONG"
            ObstacleStatus.FAILED -> "FAL"
            ObstacleStatus.SUCCEEDED -> "SUC"
            else -> "UNK"
        }
    }
    override fun toString(): String {
        val appState = getPrefix()
        return "$appState $type ( ${getDateTime(date)} )\n"
    }
}


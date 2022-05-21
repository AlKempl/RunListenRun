package com.alkempl.rlr.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alkempl.rlr.utils.getDateTime
import com.google.android.gms.location.FusedLocationProviderClient
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Data class for Location related data (only takes what's needed from
 * {@link android.location.Location} class).
 */
@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var accuracy: Float = 0.0f,
    var speed: Float = 0.0f,
    var altitude: Double = 0.0,
    var bearing: Float = 0.0f,
    var foreground: Boolean = true,
    var checked: Boolean = false,
    var mock: Boolean = false,
    var provider: String = "",
    var date: Date = Date()
) {

    fun getPrefix(): String{
        return if (this.foreground) {
            "[APP]"
        } else {
            "[BG]"
        }
    }
    override fun toString(): String {
        val appState = getPrefix()
        return "$provider $mock $latitude, $longitude ( ${getDateTime(date)} )\n"
    }
}


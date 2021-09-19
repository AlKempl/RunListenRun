package com.alkempl.rlr.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alkempl.rlr.getDateTime
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
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val speed: Float = 0.0f,
    val altitude: Double = 0.0,
    val bearing: Float = 0.0f,
    val foreground: Boolean = true,
    val mock: Boolean = false,
    val provider: String = "",
    val date: Date = Date()
) {

    override fun toString(): String {
        val appState = if (foreground) {
            "[APP]"
        } else {
            "[BG]"
        }

        return "$appState $latitude, $longitude ( ${getDateTime(date)} )\n"
    }
}

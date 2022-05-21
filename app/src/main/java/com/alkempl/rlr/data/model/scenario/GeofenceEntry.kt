package com.alkempl.rlr.data.model.scenario

import com.alkempl.rlr.data.model.TextContentType
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json
import java.util.concurrent.TimeUnit

data class GeofenceEntry(
    @Json(name = "id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @Json(name = "name")
    val name: String,
    @Json(name = "hint")
    val hint: String,
    @Json(name = "location")
    val location: LatLng,
    /**
     * In meters
     * */
    @Json(name = "radius")
    val _radius: Float?,
    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    @Json(name = "expires_in")
    val _expires_in: Long?,
    /**
    * Actions to run on geofence entered
    */
    @Json(name = "events")
    val events: List<ChapterEvent>?,
){
    override fun toString(): String {
        return "GeofenceEntry(id='$id', name='$name', location=$location, events=${events?.size})"
    }

    fun getEnteredText(type: TextContentType): String {
        return when (type){
            TextContentType.VOICE -> "Ура! Локация.  ${name}. открыта!. ";
            TextContentType.ONSCREEN -> "Ура! Локация ${name} открыта!.";
        }
    }

    fun getTargetedText(type: TextContentType): String {
        return when (type){
            TextContentType.VOICE -> "Следующая локация. ${name}..  ${hint}.. ";
            TextContentType.ONSCREEN -> "Следующая локация – ${name}. ${hint}.";
        }
    }

    /**
     * In meters
     * */
    val radius: Float
        get() = _radius ?: 20f;

    val expires_in: Long
        get() = _expires_in ?: TimeUnit.HOURS.toMillis(1);
}
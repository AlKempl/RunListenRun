package com.alkempl.rlr.data.model.scenario

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit

data class GeofenceEntry(
    @SerializedName("id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @SerializedName("name")
    val name: String,
    @SerializedName("hint")
    val hint: String,
    @SerializedName("location")
    val location: LatLng,
    /**
     * In meters
     * */
    @SerializedName("radius")
    private val _radius: Float?,
    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    @SerializedName("expires_in")
    private val _expires_in: Long?,
    @SerializedName("actions")
    val actions: List<ChapterEventAction>?,
){
    override fun toString(): String {
        return "GeofenceEntry(id='$id', name='$name', hint='$hint', location=$location, actions=$actions)"
    }

    /**
     * In meters
     * */
    val radius: Float
        get() = _radius ?: 20f;

    val expires_in: Long
        get() = _expires_in ?: TimeUnit.HOURS.toMillis(1);
}
package com.alkempl.rlr.data.model.scenario

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class GeofenceEntry(
    @SerializedName("id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @SerializedName("name")
    val name: String,
    @SerializedName("hint")
    val hint: String,
    @SerializedName("location")
    val location: LatLng,
    @SerializedName("actions")
    val actions: List<ChapterEventAction>?,
){
    override fun toString(): String {
        return "GeofenceEntry(id='$id', name='$name', hint='$hint', location=$location, actions=$actions)"
    }
}
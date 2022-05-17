package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

data class ScenarioChapter(
    @SerializedName("audio")
    val audio: ChapterAudio,
    @SerializedName("description")
    val description: String, // The One Where Monica Gets a Roommate
    @SerializedName("initial_event")
    val initial_event: ChapterEvent?,
    @SerializedName("events")
    val events: List<ChapterEvent>?,
    @SerializedName("geofencing")
    val geofencing: List<GeofenceEntry>?,
    @SerializedName("id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @SerializedName("is_final")
    val isFinal: Boolean, // false
    @SerializedName("name")
    val name: String // Chapter 1
)
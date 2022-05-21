package com.alkempl.rlr.data.model.scenario

import com.squareup.moshi.Json

data class ScenarioChapter(
    @Json(name = "audio")
    val audio: ChapterAudio?,
    @Json(name = "description")
    val description: String?, // The One Where Monica Gets a Roommate
    @Json(name = "initial_event")
    val initial_event: ChapterEvent?,
    @Json(name = "events")
    val events: List<ChapterEvent>?,
    @Json(name = "geofencing")
    val geofencing: List<GeofenceEntry>?,
    @Json(name = "id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @Json(name = "name")
    val name: String // Chapter 1
)
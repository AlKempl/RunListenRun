package com.alkempl.rlr.data.model.scenario

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class ChapterEvent(
    @Json(name = "actions")
    val actions: List<ChapterEventAction>?,
    @Json(name = "id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @Json(name = "time")
    val time: Int?, // null
    @Json(name = "type")
    val type: ChapterEventType // Random
)
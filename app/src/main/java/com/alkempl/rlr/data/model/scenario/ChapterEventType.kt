package com.alkempl.rlr.data.model.scenario

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class ChapterEventType() {
    @Json(name = "init")
    INITIAL,
    @Json(name = "timer")
    TIME_BASED,
    @Json(name = "random")
    RANDOM
}
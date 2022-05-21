package com.alkempl.rlr.data.model.scenario

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class EventActionType {
    @Json(name = "sound")
    PLAY_SOUND,

    @Json(name = "obstacle")
    GENERATE_OBSTACLE
}
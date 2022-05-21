package com.alkempl.rlr.data.model.obstacle

import com.squareup.moshi.Json

enum class ObstacleType(val type: String) {
    @Json(name = "dogs")
    DOGS("dogs"),
    @Json(name = "wind")
    WIND("wind"),
    @Json(name = "abstract")
    ABSTRACT("abstract");

    companion object {
        fun from(s: String): ObstacleType? = values().find { it.type == s }
    }
}
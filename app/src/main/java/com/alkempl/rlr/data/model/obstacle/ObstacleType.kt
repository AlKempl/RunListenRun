package com.alkempl.rlr.data.model.obstacle

import com.google.gson.annotations.SerializedName

enum class ObstacleType(val type: String) {
    DOGS("dogs"),
    WIND("wind");

    companion object {
        fun from(s: String): ObstacleType? = values().find { it.type == s }
    }
}
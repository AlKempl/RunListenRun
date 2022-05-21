package com.alkempl.rlr.data.model.obstacle

enum class ObstacleStatus(val status: String) {
    UNKNOWN(""),
    ONGOING("new"),
    FAILED("failed"),
    SUCCEEDED("succeeded");

    companion object {
        fun from(s: String): ObstacleStatus = values().find { it.status == s } ?: UNKNOWN
    }
}
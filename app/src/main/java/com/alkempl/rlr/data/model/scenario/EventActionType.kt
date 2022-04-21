package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

enum class EventActionType() {
    @SerializedName("sound")
    PLAY_SOUND,
    @SerializedName("obstacle")
    GENERATE_OBSTACLE
}
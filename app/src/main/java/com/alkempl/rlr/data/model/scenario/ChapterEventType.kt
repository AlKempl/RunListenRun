package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

enum class ChapterEventType() {
    @SerializedName("init")
    INITIAL,
    @SerializedName("timer")
    TIME_BASED,
    @SerializedName("random")
    RANDOM
}
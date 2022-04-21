package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

data class ChapterEvent(
    @SerializedName("actions")
    val actions: List<ChapterEventAction>?,
    @SerializedName("id")
    val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
    @SerializedName("time")
    val time: Int?, // null
    @SerializedName("type")
    val type: ChapterEventType // Random
)
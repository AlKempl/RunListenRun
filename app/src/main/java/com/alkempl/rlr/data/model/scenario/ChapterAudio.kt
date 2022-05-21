package com.alkempl.rlr.data.model.scenario

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class ChapterAudio(
    @Json(name = "music")
    val music: List<String>,
    @Json(name = "radio")
    val radio: List<String>
)

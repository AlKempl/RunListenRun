package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

data class ChapterAudio(
    @SerializedName("music")
    val music: List<String>,
    @SerializedName("radio")
    val radio: List<String>
)

package com.alkempl.rlr.data.model.scenario

import com.google.gson.annotations.SerializedName

data class ChapterEventAction(
    @SerializedName("action")
    val type: EventActionType,
    @SerializedName("attributes")
    val attributes: Map<String, String>
){
    fun processEvent(){

    }
}


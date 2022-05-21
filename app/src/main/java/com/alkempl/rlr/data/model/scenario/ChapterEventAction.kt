package com.alkempl.rlr.data.model.scenario

import android.content.Context
import android.os.CountDownTimer
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


open class ChapterEventAction(
    @Json(name = "action")
    val action: EventActionType,
) {
    @Json(ignore = true)
    internal open fun initTimer(millisInFuture: Number, context: Context) : CountDownTimer{
        throw NotImplementedError()
    }
    @Json(ignore = true)
    internal open fun finishTimer(millisInFuture: Number, context: Context) : CountDownTimer?{
        throw NotImplementedError()
    }
}


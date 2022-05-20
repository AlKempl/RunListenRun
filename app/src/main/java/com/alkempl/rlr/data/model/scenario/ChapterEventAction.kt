package com.alkempl.rlr.data.model.scenario

import android.content.Context
import android.os.CountDownTimer
import com.alkempl.rlr.utils.JsonSubtype
import com.alkempl.rlr.utils.JsonType
import com.google.gson.annotations.SerializedName

@JsonType(
    property = "action",
    subtypes = [
        JsonSubtype(MusicChapterEventAction::class, "music")
        , JsonSubtype( GenerateObstacleChapterEventAction::class, "obstacle")
    ]
)
abstract class ChapterEventAction(
    @SerializedName("action")
    val action: EventActionType,
) {
    internal abstract fun initTimer(millisInFuture: Number, context: Context) : CountDownTimer
    internal abstract fun finishTimer(millisInFuture: Number, context: Context) : CountDownTimer?
}


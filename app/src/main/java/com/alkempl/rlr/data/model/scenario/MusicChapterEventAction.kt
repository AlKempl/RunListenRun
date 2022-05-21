package com.alkempl.rlr.data.model.scenario

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.services.SoundManager
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class MusicChapterEventAction(
    @Json(name = "track_name")
    val track_name: String,
) : ChapterEventAction(EventActionType.PLAY_SOUND) {

    @Json(ignore = true)
    override fun initTimer(millisInFuture: Number, context: Context): CountDownTimer {
        return object : CountDownTimer(millisInFuture.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(
                    "CDT-" + this.hashCode(),
                    "seconds remaining: " + millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                val soundManager = SoundManager.getInstance(context)

                if (soundManager.mediaEnabled) {
                    soundManager.playTrack(track_name)
                }

                val desc = "playing track $track_name"
                Log.d("CDT-" + this.hashCode(), "action done: $desc")
            }
        }
    }

    @Json(ignore = true)
    override fun finishTimer(millisInFuture: Number, context: Context): CountDownTimer? {
        return null
    }


}

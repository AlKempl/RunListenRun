package com.alkempl.rlr.data.model.scenario

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.data.model.obstacle.Obstacle
import com.alkempl.rlr.data.model.obstacle.ObstacleFactory
import com.alkempl.rlr.data.model.obstacle.ObstacleType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class GenerateObstacleChapterEventAction(
    @Json(name = "type")
    val type: ObstacleType,
    @Json(name = "duration")
    val _duration: Int?,
) :
    ChapterEventAction(EventActionType.GENERATE_OBSTACLE) {

    @Json(ignore = true)
    lateinit var obstacle: Obstacle

    @Json(ignore = true)
    val duration: Int = _duration ?: 20

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
                obstacle = ObstacleFactory.buildObstacle(type, context, duration)
                val desc = "obstacle generation"
                Log.d("CDT-" + this.hashCode(), "action done: $desc")
                obstacle.onStart()
            }
        }
    }

    @Json(ignore = true)
    override fun finishTimer(millisInFuture: Number, context: Context): CountDownTimer? {
        return object : CountDownTimer(millisInFuture.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(
                    "CDT-" + this.hashCode(),
                    "seconds remaining: " + millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                val desc = "obstacle finalization"
                Log.d("CDT-" + this.hashCode(), "action done: $desc")
                obstacle.onFinish()
            }
        }
    }


}

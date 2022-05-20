package com.alkempl.rlr.data.model.scenario

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.data.model.obstacle.Obstacle
import com.alkempl.rlr.data.model.obstacle.ObstacleFactory
import com.alkempl.rlr.data.model.obstacle.ObstacleType
import com.google.gson.annotations.SerializedName

class GenerateObstacleChapterEventAction(
    @SerializedName("type")
    val type: ObstacleType,
    @SerializedName("duration")
    val _duration: Int?,
) :
    ChapterEventAction(EventActionType.GENERATE_OBSTACLE) {

    lateinit var obstacle: Obstacle
    val duration: Int = _duration ?: 20

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

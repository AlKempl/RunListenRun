package com.alkempl.rlr.data.model.obstacle

import android.os.CountDownTimer
import android.util.Log
import com.alkempl.rlr.services.SoundService

class WindObstacle(override var duration: Int) : Obstacle() {
    private val TAG: String = "OBS_WIND"

    override fun onStart() {
        Log.i(TAG, "onStart")
    }

    override fun onFinish() {
        Log.i(TAG, "onFinish")
//        TODO("Not yet implemented")
    }

    override fun onSuccess() {
        Log.i(TAG, "onSuccess")
//        TODO("Not yet implemented")
    }

    override fun onFailure() {
        Log.i(TAG, "onFailure")
//        TODO("Not yet implemented")
    }

}
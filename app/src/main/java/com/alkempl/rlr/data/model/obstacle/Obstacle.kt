package com.alkempl.rlr.data.model.obstacle

import android.os.CountDownTimer

abstract class Obstacle(
    open var duration: Int = 0,
) {
    abstract fun onStart()
    abstract fun onFinish()
    abstract fun onSuccess()
    abstract fun onFailure()
}
package com.alkempl.rlr.utils

import android.Manifest
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.*

const val TAG = "Utils"

class Utils {

}

fun getDateTime(date: Date): String {
    val formatter = DateFormat.getDateTimeInstance()
    return formatter.format(date)
}

fun getDateTime(milliseconds: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = milliseconds
    return getDateTime(cal.time)
}

/*
Vibrates with pattern
e.g. val pattern = longArrayOf(0, 200, 100, 300)
or takes second value to use
 */
fun vibrate(context: Context, pattern: LongArray){
    if (pattern.size < 2){
        Log.e(TAG, "vibrate: pattern less than 2")
        return
    }

    val vib = context.getSystemService(VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }else{
        vib.vibrate(pattern[1])
    }
}


/**
 * Helper functions to simplify permission checks/requests.
 */
fun Context.hasPermission(permission: String): Boolean {

    // Background permissions didn't exit prior to Q, so it's approved by default.
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

/**
 * Requests permission and if the user denied a previous request, but didn't check
 * "Don't ask again", we provide additional rationale.
 *
 * Note: The Snackbar should have an action to request the permission.
 */
fun Fragment.requestPermissionWithRationale(
    permission: String,
    requestCode: Int,
    snackbar: Snackbar
) {
    val provideRationale = shouldShowRequestPermissionRationale(permission)

    if (provideRationale) {
        snackbar.show()
    } else {
        requestPermissions(arrayOf(permission), requestCode)
    }
}
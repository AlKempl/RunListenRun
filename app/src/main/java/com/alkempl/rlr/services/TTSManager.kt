package com.alkempl.rlr.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alkempl.rlr.GeofenceBroadcastReceiver
import com.alkempl.rlr.LocationUpdatesBroadcastReceiver
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.utils.hasPermission
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import com.alkempl.rlr.R
import java.util.*

private const val TAG = "TTSManager"

class TTSManager private constructor( private val context: Context) {
        private var textToSpeech: TextToSpeech? = null

        private var _ttsEnabled = false

        /**
         * Status of TTS
         */
        val ttsEnabled: Boolean
            get() = _ttsEnabled

        fun setupTTS() {
            textToSpeech = TextToSpeech(context) { status: Int ->
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("$TAG/SSS", "1")
                    if (textToSpeech!!.isLanguageAvailable(
                            Locale(
                                Locale.getDefault().language
                            )
                        )
                        == TextToSpeech.LANG_AVAILABLE
                    ) {
                        textToSpeech!!.language = Locale(
                            Locale.getDefault().language
                        )
                    } else {
                        textToSpeech!!.language = Locale.US
                    }
                    Log.d("$TAG/SSS", "2")

                    textToSpeech!!.setPitch(1.0f)
                    Log.d("$TAG/SSS", "3")

                    textToSpeech!!.setSpeechRate(1f)
                    Log.d("$TAG/SSS", "4")

                    _ttsEnabled = true
                    Log.d("${TAG}/SETUP", "OK")
                } else if (status == TextToSpeech.ERROR) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.tts_initialization_error),
                        Toast.LENGTH_LONG
                    ).show()
                    _ttsEnabled = false
                    Log.e("${TAG}/SETUP", context.getString(R.string.tts_initialization_error))
                }
            }
        }

        fun speak(text: String) {
//        if(_ttsEnabled){
            Log.d("$TAG/BBBB", "1")

            this.textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_ADD,
                null,
                Math.random().toString() + ""
            )
            Log.d("$TAG/BBBB", "2")

//        }else{
//            Log.e(TAG, "speak: tts not enabled 2")
//        }
        }

    companion object {
        @Volatile
        private var INSTANCE: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TTSManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
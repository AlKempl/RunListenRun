package com.alkempl.rlr.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.alkempl.rlr.R
import java.util.*

private const val TAG = "TTSManager"

class TTSManager private constructor(private val context: Context) {
    private lateinit var textToSpeech: TextToSpeech

    private var _ttsEnabled = false

    /**
     * Status of TTS
     */
    val ttsEnabled: Boolean
        get() = _ttsEnabled

    fun speak(text: String) {
        if (_ttsEnabled) {
            this.textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_ADD,
                null,
                Math.random().toString() + ""
            )
        } else {
            Log.e(TAG, "speak: tts not enabled 2")
        }
    }

    init {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                if (textToSpeech.isLanguageAvailable(
                        Locale(
                            Locale.getDefault().language
                        )
                    )
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    textToSpeech.language = Locale(
                        Locale.getDefault().language
                    )
                } else {
                    textToSpeech.language = Locale.US
                }
                textToSpeech.setPitch(1.0f)
                textToSpeech.setSpeechRate(1f)
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
package com.alkempl.rlr.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.FLAG_AUDIBILITY_ENFORCED
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
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

    /*
    * https://stackoverflow.com/questions/3043595/fade-in-and-out-music-while-speaking-a-text
    * */
    fun speak(text: String) {
        if (_ttsEnabled) {
//            val mPlaybackAttributes = AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                .setFlags(FLAG_AUDIBILITY_ENFORCED) //VERY IMPORTANT
//                .build()
//            this.textToSpeech.setAudioAttributes(mPlaybackAttributes)
////            this.textToSpeech.speak(textToSay, TextToSpeech.QUEUE_FLUSH, null, textToSay)
//
//            this.textToSpeech.speak(
//                text,
//                TextToSpeech.QUEUE_FLUSH,
////                TextToSpeech.QUEUE_ADD,
//                null,
//                text
////                Math.random().toString() + ""
//            )
            val mPlaybackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH) //add this below flag if you need the TTS to speak in a louder volume or TTS volume be heard for sure at any cost
                .setFlags(FLAG_AUDIBILITY_ENFORCED)
                .build()
            textToSpeech.setAudioAttributes(mPlaybackAttributes)
            val mFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .build()
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            am!!.requestAudioFocus(mFocusRequest)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
//            val ttsSpeak = Handler()
//            val checkTTSRunning: Runnable = object : Runnable {
//                override fun run() {
//                    if (tts.isSpeaking()) {
//                        ttsSpeak.postDelayed(this, 1000)
//                    } else am!!.abandonAudioFocusRequest(mFocusRequest)
//                }
//            }
//            ttsSpeak.postDelayed(checkTTSRunning, 3000)
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
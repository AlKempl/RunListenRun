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
import androidx.preference.PreferenceManager
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
    fun speak(text: String, flush: Boolean = false) {
        val queueMode = if(flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val ttsPrefEnabled = sharedPref.getBoolean("enable_tts_pref_value", true)

        if (!_ttsEnabled) {
            Log.e(TAG, "speak: tts disabled hard-way")
            return
        }

        if (!ttsPrefEnabled) {
            Log.d(TAG, "speak: tts disabled in preferences")
            return
        }

        Log.d("$TAG/Speak", text)
        textToSpeech.speak(text, queueMode, null, text)
    }

    fun stop(){
        textToSpeech.stop()
    }

    init {
        textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                if (
                    textToSpeech.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    textToSpeech.language = Locale(Locale.getDefault().language)
                } else {
                    textToSpeech.language = Locale.US
                }
                textToSpeech.setPitch(1.0f)
                textToSpeech.setSpeechRate(1f)
                _ttsEnabled = true
                Log.d("${TAG}/BasicSetup", "OK")
            } else if (status == TextToSpeech.ERROR) {
                Toast.makeText(
                    context,
                    context.getString(R.string.tts_initialization_error),
                    Toast.LENGTH_LONG
                ).show()
                _ttsEnabled = false
                Log.e("${TAG}/BasicSetup", context.getString(R.string.tts_initialization_error))
            }
        }

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
        Log.d("${TAG}/PrioritySetup", "OK")
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
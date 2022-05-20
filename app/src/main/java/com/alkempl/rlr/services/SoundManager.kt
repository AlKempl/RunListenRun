package com.alkempl.rlr.services

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.preference.PreferenceManager


private const val TAG = "SoundManager"

class SoundManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer
    private var _mediaEnabled = false

    /**
     * Status of TTS
     */
    val mediaEnabled: Boolean
        get() = _mediaEnabled

    init {
        mediaPlayer = MediaPlayer()
        _mediaEnabled = true
        Log.d("${TAG}/BasicSetup", "OK")
    }

    fun destroy() {
        Log.d(TAG, "destroy")
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
    }

    fun playTrack(
        track_name: String,
        looping: Boolean = true,
        volume: Pair<Float, Float> = Pair(50f, 50f)
    ) {
        if (!_mediaEnabled) {
            Log.e(TAG, "playTrack: Cant play [$track_name]: mediaplayer disabled hard-way")
            return
        }

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val musicPrefEnabled = sharedPref.getBoolean("enable_music_pref_value", true)

        if (!musicPrefEnabled) {
            Log.d(TAG, "playTrack: Cant play [$track_name]: mediaplayer disabled in preferences")
            return
        }

        val resId = context.resources.getIdentifier(track_name, "raw", context.packageName)
        val resFd = context.resources.openRawResourceFd(resId) ?: return

        stop()

        mediaPlayer.setDataSource(resFd.fileDescriptor, resFd.startOffset, resFd.length)
        mediaPlayer.prepare()
        mediaPlayer.isLooping = looping // Set looping
        mediaPlayer.setVolume(volume.first, volume.second)
        mediaPlayer.start()
    }

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}
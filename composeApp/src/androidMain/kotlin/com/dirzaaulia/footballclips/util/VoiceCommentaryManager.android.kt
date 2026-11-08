package com.dirzaaulia.footballclips.util

import android.content.Context
import android.media.AudioAttributes
import android.media.ToneGenerator
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class VoiceCommentaryManager(
    private val context: Context,
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    var isEnabled: Boolean = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.UK)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    val highQualityVoice = engine.voices?.firstOrNull { v ->
                        v.locale.language == "en" &&
                            v.quality >= Voice.QUALITY_HIGH &&
                            !v.isNetworkConnectionRequired
                    }
                    if (highQualityVoice != null) {
                        engine.voice = highQualityVoice
                    }
                }
                engine.setPitch(0.98f)
                engine.setSpeechRate(1.05f)
                engine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                isInitialized = true
            }
        }
    }

    fun speakPlay(clock: String, text: String, isGoal: Boolean = false) {
        if (!isEnabled || !isInitialized || text.isBlank()) return

        if (isGoal) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
            } catch (_: Exception) {
            }
        }

        val speechText = if (clock.isNotBlank()) "$clock. $text" else text
        tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "PLAY_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

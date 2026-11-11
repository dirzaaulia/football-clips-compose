package com.dirzaaulia.footballclips.util

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

actual class VoiceCommentaryManager(
    private val context: Context,
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    actual var isEnabled: Boolean = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.UK)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    val naturalVoice = engine.voices
                        ?.filter { it.locale.language.startsWith("en") }
                        ?.sortedWith(
                            compareByDescending<Voice> { it.quality }
                                .thenByDescending { !it.isNetworkConnectionRequired },
                        )
                        ?.firstOrNull()
                    if (naturalVoice != null) {
                        engine.voice = naturalVoice
                    }
                }
                engine.setPitch(1.0f)
                engine.setSpeechRate(1.0f)
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

    actual fun speakPlay(clock: String, text: String, isGoal: Boolean) {
        if (!isEnabled || !isInitialized || text.isBlank()) return

        val speechText = if (isGoal) {
            "Goal! $text"
        } else if (clock.isNotBlank()) {
            "$clock. $text"
        } else {
            text
        }
        tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "PLAY_${System.currentTimeMillis()}")
    }

    actual fun stop() {
        tts?.stop()
    }

    actual fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

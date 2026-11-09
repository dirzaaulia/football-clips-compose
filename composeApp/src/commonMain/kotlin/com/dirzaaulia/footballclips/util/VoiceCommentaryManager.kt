package com.dirzaaulia.footballclips.util

expect class VoiceCommentaryManager {
    var isEnabled: Boolean
    fun speakPlay(clock: String, text: String, isGoal: Boolean = false)
    fun stop()
    fun shutdown()
}

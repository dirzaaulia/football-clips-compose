package com.dirzaaulia.footballclips.domain.model

data class PitchPlay(
    val id: String,
    val clock: String,
    val period: Int = 1,
    val type: String,
    val typeText: String,
    val text: String,
    val teamName: String? = null,
    val player: String? = null,
    val assistPlayer: String? = null,
    val xPercent: Float? = null,
    val yPercent: Float? = null,
    val targetXPercent: Float? = null,
    val targetYPercent: Float? = null,
) {
    val isGoal: Boolean
        get() = type.contains("goal", ignoreCase = true) || typeText.contains("goal", ignoreCase = true)

    val isShot: Boolean
        get() = type.contains("shot", ignoreCase = true) || typeText.contains("shot", ignoreCase = true)

    val isCorner: Boolean
        get() = type.contains("corner", ignoreCase = true) || typeText.contains("corner", ignoreCase = true)

    val isFoul: Boolean
        get() = type.contains("foul", ignoreCase = true) || typeText.contains("foul", ignoreCase = true)

    val isCard: Boolean
        get() = type.contains("card", ignoreCase = true) || typeText.contains("card", ignoreCase = true)

    val isSub: Boolean
        get() = type.contains("sub", ignoreCase = true) || typeText.contains("sub", ignoreCase = true)
}

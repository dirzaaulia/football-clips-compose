package com.dirzaaulia.footballclips.util

import androidx.compose.ui.graphics.Color

/**
 * Ensures team kit hex colors remain legible and vibrant against dark backgrounds (#101418 / #0F172A).
 */
fun String?.toLegibleColor(fallback: Color): Color {
    val raw = this?.trim()?.removePrefix("#") ?: return fallback
    val fullHex = if (raw.length == 3) raw.map { "$it$it" }.joinToString("") else raw
    if (fullHex.length != 6) return fallback

    val colorLong = fullHex.toLongOrNull(16) ?: return fallback
    val r = ((colorLong shr 16) and 0xFF).toInt()
    val g = ((colorLong shr 8) and 0xFF).toInt()
    val b = (colorLong and 0xFF).toInt()

    val luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
    return if (luminance < 0.22f) {
        val nr = (r * 2.4f + 55).toInt().coerceIn(65, 255)
        val ng = (g * 2.4f + 55).toInt().coerceIn(65, 255)
        val nb = (b * 2.4f + 55).toInt().coerceIn(65, 255)
        Color(red = nr, green = ng, blue = nb)
    } else {
        Color(
            red = r,
            green = g,
            blue = b,
        )
    }
}

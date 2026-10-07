package com.dirzaaulia.footballclips.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    return null
}

@Composable
actual fun SetSystemBarsStyle(darkTheme: Boolean) {}

@Composable
actual fun rememberThemeCapture(): (Offset) -> Unit {
    return { _ -> }
}


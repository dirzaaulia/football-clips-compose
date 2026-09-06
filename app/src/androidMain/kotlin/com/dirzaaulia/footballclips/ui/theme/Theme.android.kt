package com.dirzaaulia.footballclips.ui.theme

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        null
    }
}

@Composable
actual fun SetSystemBarsStyle(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
}

@Composable
actual fun rememberThemeCapture(): (Offset) -> Unit {
    val view = LocalView.current
    val dropletState = LocalDropletThemeState.current

    return { center ->
        if (view.width > 0 && view.height > 0) {
            try {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)

                val actualCenter = if (center == Offset.Zero) {
                    Offset(view.width * 0.55f, view.height * 0.08f)
                } else center

                dropletState.start(
                    screenshot = bitmap.asImageBitmap(),
                    center = actualCenter,
                    onFinish = {
                        bitmap.recycle()
                    }
                )
            } catch (e: Exception) {
                // In case of memory constraints, fallback gracefully without droplet animation
            }
        }
    }
}


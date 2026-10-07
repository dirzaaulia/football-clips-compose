package com.dirzaaulia.footballclips.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.hypot
import kotlin.math.max

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
expect fun SetSystemBarsStyle(darkTheme: Boolean)

@Composable
expect fun rememberThemeCapture(): (Offset) -> Unit

class DropletThemeState {
    var isTransitioning by mutableStateOf(false)
    var dropletCenter by mutableStateOf(Offset.Zero)
    var previousScreenshot by mutableStateOf<ImageBitmap?>(null)
    var transitionKey by mutableStateOf(0)
    var onFinishCallback: (() -> Unit)? = null

    fun start(screenshot: ImageBitmap?, center: Offset, onFinish: (() -> Unit)? = null) {
        previousScreenshot = screenshot
        dropletCenter = center
        onFinishCallback = onFinish
        transitionKey++
        isTransitioning = true
    }

    fun finish() {
        isTransitioning = false
        previousScreenshot = null
        onFinishCallback?.invoke()
        onFinishCallback = null
    }
}

val LocalDropletThemeState = staticCompositionLocalOf { DropletThemeState() }

// Kinetic Editorial Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = DarkCobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkCobaltContainer,
    onPrimaryContainer = DarkOnCobaltContainer,
    secondary = DarkSlateSecondary,
    onSecondary = DarkBaseBackground,
    secondaryContainer = DarkSurfaceCard,
    onSecondaryContainer = DarkTextHigh,
    tertiary = DarkIcyCyan,
    onTertiary = Color.Black,
    background = DarkBaseBackground,
    onBackground = DarkTextHigh,
    surface = DarkSurfaceLow,
    onSurface = DarkTextHigh,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

// Kinetic Editorial Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = LightCobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = LightCobaltContainer,
    onPrimaryContainer = LightOnCobaltContainer,
    secondary = LightSlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceLow,
    onSecondaryContainer = LightTextHigh,
    tertiary = LightIcyCyan,
    onTertiary = Color.White,
    background = LightBaseBackground,
    onBackground = LightTextHigh,
    surface = LightSurfaceCard,
    onSurface = LightTextHigh,
    surfaceVariant = LightSurfaceLow,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorder
)

private val DropletEasing = FastOutSlowInEasing

@Composable
fun FootballClipsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserves Kinetic Editorial design system
    content: @Composable () -> Unit
) {
    SetSystemBarsStyle(darkTheme = darkTheme)

    val dynamicLight = if (dynamicColor) getDynamicColorScheme(false) else null
    val dynamicDark = if (dynamicColor) getDynamicColorScheme(true) else null
    val lightScheme = remember(dynamicLight) { dynamicLight ?: LightColorScheme }
    val darkScheme = remember(dynamicDark) { dynamicDark ?: DarkColorScheme }

    val colorScheme = if (darkTheme) darkScheme else lightScheme
    val dropletState = remember { DropletThemeState() }

    CompositionLocalProvider(LocalDropletThemeState provides dropletState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes
        ) {
            DropletThemeWrapper(content = content)
        }
    }
}

@Composable
fun DropletThemeWrapper(content: @Composable () -> Unit) {
    val dropletState = LocalDropletThemeState.current
    val screenshot = dropletState.previousScreenshot

    if (dropletState.isTransitioning && screenshot != null) {
        val animatable = remember(dropletState.transitionKey) { Animatable(0f) }

        LaunchedEffect(dropletState.transitionKey) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = DropletEasing
                )
            )
            dropletState.finish()
        }

        val progress = animatable.value
        val center = dropletState.dropletCenter

        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 0: Old theme snapshot (underneath)
            Image(
                bitmap = screenshot,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Layer 1: New live theme expanding outwards like a circular droplet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density
                            ): Outline {
                                val maxRadius = hypot(
                                    max(center.x, size.width - center.x),
                                    max(center.y, size.height - center.y)
                                )
                                val path = Path().apply {
                                    addOval(Rect(center = center, radius = maxRadius * progress))
                                }
                                return Outline.Generic(path)
                            }
                        }
                    }
            ) {
                content()
            }
        }
    } else {
        content()
    }
}

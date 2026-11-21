package com.dirzaaulia.footballclips.ui.components.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.dirzaaulia.footballclips.ui.theme.LocalThemeIsDark

@Composable
fun rememberShimmerBrush(
    isDark: Boolean = LocalThemeIsDark.current,
    baseColor: Color = if (isDark) Color(0xFF1E222B) else Color(0xFFE2E8F0),
    highlightColor: Color = if (isDark) Color(0xFF333D4F) else Color.White.copy(alpha = 0.85f),
    targetOffset: Float = 1200f,
    durationMillis: Int = 1200,
): Brush {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerTranslate",
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor,
        ),
        start = Offset(translateAnim - 350f, translateAnim - 350f),
        end = Offset(translateAnim, translateAnim),
    )
}

fun Modifier.shimmerEffect(
    shimmerColor: Color = Color.Unspecified,
    baseColor: Color = Color.Unspecified,
): Modifier = composed {
    val isDark = LocalThemeIsDark.current
    val resolvedBase = if (baseColor != Color.Unspecified) {
        baseColor
    } else if (isDark) {
        Color(0xFF1E222B)
    } else {
        Color(0xFFE2E8F0)
    }
    val resolvedShimmer = if (shimmerColor != Color.Unspecified) {
        shimmerColor
    } else if (isDark) {
        Color(0xFF333D4F)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "ShimmerEffectTransition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerEffectOffsetX",
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                resolvedBase,
                resolvedShimmer,
                resolvedBase,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
        ),
    ).onGloballyPositioned {
        size = it.size
    }
}

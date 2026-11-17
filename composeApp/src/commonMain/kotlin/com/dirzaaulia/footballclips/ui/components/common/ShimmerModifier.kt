package com.dirzaaulia.footballclips.ui.components.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
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

fun Modifier.shimmerEffect(
    shimmerColor: Color = Color.Unspecified,
    baseColor: Color = Color.Unspecified,
): Modifier = composed {
    val resolvedBase = if (baseColor != Color.Unspecified) {
        baseColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val resolvedShimmer = if (shimmerColor != Color.Unspecified) {
        shimmerColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
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

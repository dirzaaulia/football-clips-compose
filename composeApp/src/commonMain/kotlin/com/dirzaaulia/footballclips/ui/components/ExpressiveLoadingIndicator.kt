package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Loading Indicator specification.
 * Features a circular track backdrop, thick stroke, and elastic morphing motion.
 */
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
    strokeWidth: Dp = 4.5.dp
) {
    val transition = rememberInfiniteTransition(label = "M3ExpressiveLoading")
    
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "Rotation"
    )
    
    val sweep by transition.animateFloat(
        initialValue = 30f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sweep"
    )

    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        
        // 1. M3 Expressive Track Layer (Full 360 ring)
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        
        // 2. M3 Expressive Active Arc
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

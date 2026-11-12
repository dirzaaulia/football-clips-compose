package com.dirzaaulia.footballclips.ui.score.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.PitchPlay
import kotlin.math.hypot

@Composable
internal fun PitchCanvas(
    plays: List<PitchPlay>,
    selectedPlay: PitchPlay?,
    homeColor: Color?,
    awayColor: Color?,
    onSelectPlay: (PitchPlay) -> Unit,
    viewMode: PitchViewMode = PitchViewMode.REPLAY,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val pulseRadius by transition.animateFloat(
        initialValue = 6f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val ballProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Canvas(
        modifier = modifier.pointerInput(plays) {
            detectTapGestures { tapOffset ->
                var closest: PitchPlay? = null
                var minDist = Float.MAX_VALUE
                plays.forEach { p ->
                    val px = (p.xPercent ?: 50f) / 100f * size.width
                    val py = (p.yPercent ?: 50f) / 100f * size.height
                    val d = hypot(px - tapOffset.x, py - tapOffset.y)
                    if (d < minDist) {
                        minDist = d
                        closest = p
                    }
                }
                if (closest != null && minDist < 80f) {
                    onSelectPlay(closest)
                }
            }
        },
    ) {
        drawFmPitchMarkings()
        when (viewMode) {
            PitchViewMode.SHOTS -> drawShotMap(plays, selectedPlay, homeColor, awayColor)
            PitchViewMode.HEATMAP -> drawHeatmap(plays, homeColor, awayColor)
            PitchViewMode.REPLAY -> {
                drawPlayNodes(plays, selectedPlay, homeColor, awayColor)
                drawSelectedFmPlay(
                    play = selectedPlay,
                    pulseRadius = pulseRadius,
                    pulseAlpha = pulseAlpha,
                    ballProgress = ballProgress,
                    homeColor = homeColor,
                    awayColor = awayColor,
                )
            }
        }
    }
}

private fun DrawScope.drawPlayNodes(
    plays: List<PitchPlay>,
    selected: PitchPlay?,
    homeColor: Color?,
    awayColor: Color?,
) {
    val hColor = homeColor ?: Color(0xFF7098F8)
    val aColor = awayColor ?: Color(0xFF4FC3F7)
    for (p in plays) {
        if (p.id == selected?.id) continue
        val px = p.xPercent
        val py = p.yPercent
        if (px != null && py != null) {
            val isGK = (px < 12f || px > 88f) && (py in 25f..45f)
            val baseColor = if (px < 50f) hColor else aColor
            drawSquadKitJersey(
                center = Offset((px / 100f) * size.width, (py / 100f) * size.height),
                color = if (isGK) Color(0xFFF59E0B) else baseColor,
                scale = 0.75f,
            )
        }
    }
}

private fun DrawScope.drawSelectedFmPlay(
    play: PitchPlay?,
    pulseRadius: Float,
    pulseAlpha: Float,
    ballProgress: Float,
    homeColor: Color?,
    awayColor: Color?,
) {
    play ?: return
    val px = (play.xPercent ?: return).coerceIn(6.5f, 95.5f)
    val py = (play.yPercent ?: return).coerceIn(5.5f, 62.5f)
    val cx = (px / 100f) * size.width
    val cy = (py / 100f) * size.height
    val ringColor = resolveMarkerColor(play)
    val tokenColor = if (cx <= size.width / 2f) (homeColor ?: Color(0xFF7098F8)) else (awayColor ?: Color(0xFF4FC3F7))

    val rawTx = play.targetXPercent ?: if (play.isShot || play.isGoal) (if (cx <= size.width / 2f) 97f else 3f) else null
    val tx = rawTx?.coerceIn(3f, 97f)
    val ty = play.targetYPercent?.coerceIn(16f, 52f) ?: if (play.isShot || play.isGoal) 50f else null

    if (tx != null && ty != null) {
        val targetX = (tx / 100f) * size.width
        val targetY = (ty / 100f) * size.height

        if (play.isShot || play.isGoal) {
            drawShootingDangerCone(cx, cy, targetX)
        }

        drawLine(
            color = ringColor.copy(alpha = 0.8f),
            start = Offset(cx, cy),
            end = Offset(targetX, targetY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )

        val ballX = cx + (targetX - cx) * ballProgress
        val ballY = cy + (targetY - cy) * ballProgress
        drawAnimatedBall(ballX, ballY)
    }

    drawCircle(
        color = tokenColor.copy(alpha = pulseAlpha),
        radius = pulseRadius * density,
        center = Offset(cx, cy),
    )
    drawSquadKitJersey(
        center = Offset(cx, cy),
        color = tokenColor,
        scale = 1.25f,
    )
}

private fun DrawScope.drawSquadKitJersey(
    center: Offset,
    color: Color,
    scale: Float = 1f,
) {
    val s = 4.dp.toPx() * scale
    val jerseyPath = Path().apply {
        moveTo(center.x - s * 0.9f, center.y - s)
        lineTo(center.x - s * 0.4f, center.y - s * 1.2f)
        lineTo(center.x - s * 0.2f, center.y - s * 0.95f)
        lineTo(center.x + s * 0.2f, center.y - s * 0.95f)
        lineTo(center.x + s * 0.4f, center.y - s * 1.2f)
        lineTo(center.x + s * 0.9f, center.y - s)
        lineTo(center.x + s * 1.2f, center.y - s * 0.4f)
        lineTo(center.x + s * 0.8f, center.y - s * 0.3f)
        lineTo(center.x + s * 0.8f, center.y + s)
        lineTo(center.x - s * 0.8f, center.y + s)
        lineTo(center.x - s * 0.8f, center.y - s * 0.3f)
        lineTo(center.x - s * 1.2f, center.y - s * 0.4f)
        close()
    }
    drawPath(
        path = jerseyPath,
        color = Color.Black.copy(alpha = 0.5f),
        style = Stroke(width = 1.2.dp.toPx()),
    )
    drawPath(
        path = jerseyPath,
        color = color,
    )
    drawPath(
        path = jerseyPath,
        color = Color.White,
        style = Stroke(width = 0.8.dp.toPx()),
    )
}

private fun DrawScope.drawShootingDangerCone(
    cx: Float,
    cy: Float,
    targetX: Float,
) {
    val conePath = Path().apply {
        moveTo(cx, cy)
        lineTo(targetX, size.height * 0.35f)
        lineTo(targetX, size.height * 0.65f)
        close()
    }
    drawPath(
        path = conePath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF59E0B).copy(alpha = 0.40f),
                Color(0xFFFBBF24).copy(alpha = 0.08f),
            ),
            start = Offset(cx, cy),
            end = Offset(targetX, size.height * 0.5f),
        ),
    )
    drawPath(
        path = conePath,
        color = Color(0xFFFBBF24).copy(alpha = 0.75f),
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
        ),
    )
    drawCircle(
        color = Color.White,
        radius = 4.2.dp.toPx(),
        center = Offset(targetX, size.height * 0.5f),
    )
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 3.5.dp.toPx(),
        center = Offset(targetX, size.height * 0.5f),
    )
}

private fun DrawScope.drawAnimatedBall(
    ballX: Float,
    ballY: Float,
) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.55f),
        radius = 3.8.dp.toPx(),
        center = Offset(ballX, ballY + 1.dp.toPx()),
    )
    drawCircle(
        color = Color.White,
        radius = 3.2.dp.toPx(),
        center = Offset(ballX, ballY),
    )
    drawCircle(
        color = Color(0xFFE2E8F0),
        radius = 2.4.dp.toPx(),
        center = Offset(ballX, ballY),
        style = Stroke(width = 0.7.dp.toPx()),
    )
}


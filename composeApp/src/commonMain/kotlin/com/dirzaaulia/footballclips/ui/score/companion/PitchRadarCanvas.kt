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
        drawPlayNodes(plays, selectedPlay)
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

private fun DrawScope.drawPlayNodes(plays: List<PitchPlay>, selected: PitchPlay?) {
    for (p in plays) {
        if (p.id == selected?.id) continue
        val px = p.xPercent
        val py = p.yPercent
        if (px != null && py != null) {
            drawCircle(
                color = resolveMarkerColor(p).copy(alpha = 0.65f),
                radius = 2.5.dp.toPx(),
                center = Offset((px / 100f) * size.width, (py / 100f) * size.height),
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
    val px = play.xPercent ?: return
    val py = play.yPercent ?: return
    val cx = (px / 100f) * size.width
    val cy = (py / 100f) * size.height
    val ringColor = resolveMarkerColor(play)
    val tokenColor = if (cx > size.width / 2f) (homeColor ?: Color(0xFF3B82F6)) else (awayColor ?: Color(0xFFEC4899))

    val tx = play.targetXPercent ?: if (play.isShot || play.isGoal) (if (cx > size.width / 2f) 98f else 2f) else null
    val ty = play.targetYPercent ?: if (play.isShot || play.isGoal) 50f else null

    if (tx != null && ty != null) {
        val targetX = (tx / 100f) * size.width
        val targetY = (ty / 100f) * size.height

        // Danger Shooting Cone
        if (play.isShot || play.isGoal) {
            val conePath = Path().apply {
                moveTo(cx, cy)
                lineTo(targetX, size.height * 0.35f)
                lineTo(targetX, size.height * 0.65f)
                close()
            }
            drawPath(conePath, Color(0xFFEF4444).copy(alpha = 0.15f))

            // Defending Goalkeeper token
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 3.5.dp.toPx(),
                center = Offset(targetX, size.height * 0.5f),
            )
        }

        // Trajectory arc
        drawLine(
            color = ringColor.copy(alpha = 0.8f),
            start = Offset(cx, cy),
            end = Offset(targetX, targetY),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )

        // Animated Ball
        val ballX = cx + (targetX - cx) * ballProgress
        val ballY = cy + (targetY - cy) * ballProgress
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(ballX, ballY),
        )
    }

    drawCircle(
        color = tokenColor.copy(alpha = pulseAlpha),
        radius = pulseRadius * density,
        center = Offset(cx, cy),
    )
    drawCircle(
        color = Color.White,
        radius = 5.5.dp.toPx(),
        center = Offset(cx, cy),
    )
    drawCircle(
        color = tokenColor,
        radius = 4.2.dp.toPx(),
        center = Offset(cx, cy),
    )
}

private fun resolveMarkerColor(play: PitchPlay): Color = when {
    play.isGoal -> Color(0xFF22C55E)
    play.isShot -> Color(0xFF38BDF8)
    play.isCard -> Color(0xFFEAB308)
    play.isCorner -> Color(0xFFA855F7)
    play.isSub -> Color(0xFFF97316)
    else -> Color(0xFF94A3B8)
}

private fun DrawScope.drawFmPitchMarkings() {
    val w = size.width
    val h = size.height

    // Mowing turf lawn stripes
    val stripes = 8
    val stripeW = w / stripes
    for (i in 0 until stripes) {
        drawRect(
            color = if (i % 2 == 0) Color(0xFF0B2213) else Color(0xFF0F2C18),
            topLeft = Offset(i * stripeW, 0f),
            size = Size(stripeW, h),
        )
    }

    val lineStroke = Stroke(width = 1.2.dp.toPx())
    val lineColor = Color.White.copy(alpha = 0.35f)

    drawRect(color = lineColor, style = lineStroke)
    drawLine(color = lineColor, start = Offset(w / 2f, 0f), end = Offset(w / 2f, h), strokeWidth = lineStroke.width)
    drawCircle(color = lineColor, radius = h * 0.16f, center = Offset(w / 2f, h / 2f), style = lineStroke)

    val boxW = w * 0.165f
    val boxH = h * 0.54f
    drawRect(color = lineColor, topLeft = Offset(0f, h * 0.23f), size = Size(boxW, boxH), style = lineStroke)
    drawRect(color = lineColor, topLeft = Offset(w - boxW, h * 0.23f), size = Size(boxW, boxH), style = lineStroke)
}

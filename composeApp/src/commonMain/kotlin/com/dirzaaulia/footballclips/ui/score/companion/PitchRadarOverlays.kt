package com.dirzaaulia.footballclips.ui.score.companion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.PitchPlay

internal fun resolveMarkerColor(play: PitchPlay): Color = when {
    play.isGoal -> Color(0xFF22C55E)
    play.isShot -> Color(0xFF38BDF8)
    play.isCard -> Color(0xFFEAB308)
    play.isCorner -> Color(0xFFA855F7)
    play.isSub -> Color(0xFFF97316)
    else -> Color(0xFF94A3B8)
}

internal fun DrawScope.drawFmPitchMarkings() {
    val w = size.width
    val h = size.height

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

internal fun DrawScope.drawShotMap(
    plays: List<PitchPlay>,
    selected: PitchPlay?,
    homeColor: Color? = null,
    awayColor: Color? = null,
) {
    val hColor = homeColor ?: Color(0xFF7098F8)
    val aColor = awayColor ?: Color(0xFF4FC3F7)
    val shots = plays.filter { it.isShot || it.isGoal }
    for (p in shots) {
        val safePx = (p.xPercent ?: 50f).coerceIn(6.5f, 95.5f)
        val safePy = (p.yPercent ?: 50f).coerceIn(5.5f, 62.5f)
        val px = (safePx / 100f) * size.width
        val py = (safePy / 100f) * size.height
        val rawTx = p.targetXPercent ?: if (px > size.width / 2f) 97f else 3f
        val txPercent = rawTx.coerceIn(3f, 97f)
        val tyPercent = (p.targetYPercent ?: 50f).coerceIn(16f, 52f)
        val tx = (txPercent / 100f) * size.width
        val ty = (tyPercent / 100f) * size.height

        val teamCol = if (px < size.width / 2f) hColor else aColor
        val col = if (p.isGoal) Color(0xFF22C55E) else teamCol
        val isSelected = p.id == selected?.id
        drawLine(
            color = col.copy(alpha = if (isSelected) 1f else 0.5f),
            start = Offset(px, py),
            end = Offset(tx, ty),
            strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )
        drawCircle(
            color = col,
            radius = if (p.isGoal) 4.5.dp.toPx() else 3.dp.toPx(),
            center = Offset(px, py),
        )
    }
}

internal fun DrawScope.drawHeatmap(
    plays: List<PitchPlay>,
    homeColor: Color? = null,
    awayColor: Color? = null,
) {
    val hColor = homeColor ?: Color(0xFF7098F8)
    val aColor = awayColor ?: Color(0xFF4FC3F7)
    for (p in plays) {
        val px = (p.xPercent ?: 50f) / 100f * size.width
        val py = (p.yPercent ?: 50f) / 100f * size.height
        val teamColor = if (px < size.width / 2f) hColor else aColor
        val col = if (p.isGoal || p.isShot) Color(0xFFEF4444) else teamColor
        drawCircle(
            color = col.copy(alpha = 0.25f),
            radius = 16.dp.toPx(),
            center = Offset(px, py),
        )
    }
}

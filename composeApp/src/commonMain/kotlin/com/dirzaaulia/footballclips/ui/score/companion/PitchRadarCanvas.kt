package com.dirzaaulia.footballclips.ui.score.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.PitchPlay

@Composable
fun PitchRadarDeck(
    plays: List<PitchPlay>,
    modifier: Modifier = Modifier,
    onPlaySelected: ((PitchPlay) -> Unit)? = null,
) {
    var selectedPlay by remember(plays) { mutableStateOf(plays.lastOrNull()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PitchCanvas(
            selectedPlay = selectedPlay,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.54f),
        )

        selectedPlay?.let { play ->
            PitchActionBanner(play = play)
        }

        PitchEventChips(
            plays = plays,
            selectedPlay = selectedPlay,
            onSelect = {
                selectedPlay = it
                onPlaySelected?.invoke(it)
            },
        )
    }
}

@Composable
private fun PitchCanvas(
    selectedPlay: PitchPlay?,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val pulseRadius by transition.animateFloat(
        initialValue = 6f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Canvas(modifier = modifier) {
        drawPitchMarkings()
        selectedPlay?.let { play ->
            val px = play.xPercent
            val py = play.yPercent
            if (px != null && py != null) {
                val cx = (px / 100f) * size.width
                val cy = (py / 100f) * size.height
                val ringColor = when {
                    play.isGoal -> Color(0xFF22C55E)
                    play.isShot -> Color(0xFF38BDF8)
                    play.isCard -> Color(0xFFEAB308)
                    else -> Color(0xFFF97316)
                }
                drawCircle(
                    color = ringColor.copy(alpha = pulseAlpha),
                    radius = pulseRadius * density,
                    center = Offset(cx, cy),
                )
                drawCircle(
                    color = ringColor,
                    radius = 5.dp.toPx(),
                    center = Offset(cx, cy),
                )
            }
        }
    }
}

private fun DrawScope.drawPitchMarkings() {
    val w = size.width
    val h = size.height

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF132A1C), Color(0xFF193B26)),
        ),
        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
    )

    val lineStroke = Stroke(width = 1.5.dp.toPx())
    val lineColor = Color.White.copy(alpha = 0.35f)

    drawRect(color = lineColor, style = lineStroke)
    drawLine(color = lineColor, start = Offset(w / 2f, 0f), end = Offset(w / 2f, h), strokeWidth = lineStroke.width)
    drawCircle(color = lineColor, radius = h * 0.16f, center = Offset(w / 2f, h / 2f), style = lineStroke)

    val boxW = w * 0.165f
    val boxH = h * 0.54f
    drawRect(color = lineColor, topLeft = Offset(0f, h * 0.23f), size = Size(boxW, boxH), style = lineStroke)
    drawRect(color = lineColor, topLeft = Offset(w - boxW, h * 0.23f), size = Size(boxW, boxH), style = lineStroke)
}

@Composable
private fun PitchActionBanner(play: PitchPlay) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = play.clock,
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = play.text.ifBlank { "${play.teamName.orEmpty()} ${play.typeText}" },
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PitchEventChips(
    plays: List<PitchPlay>,
    selectedPlay: PitchPlay?,
    onSelect: (PitchPlay) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(plays.takeLast(10).reversed()) { play ->
            val isSelected = play.id == selectedPlay?.id
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                modifier = Modifier.clickable { onSelect(play) },
            ) {
                Text(
                    text = "${play.clock} ${play.typeText.take(6)}",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

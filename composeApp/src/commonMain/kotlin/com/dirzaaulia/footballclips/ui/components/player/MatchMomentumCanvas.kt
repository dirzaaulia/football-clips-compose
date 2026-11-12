package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MatchMomentumCanvas(
    homeTeam: String,
    awayTeam: String,
    homeScore: Int = 0,
    awayScore: Int = 0,
    homeColor: Color = Color(0xFF7098F8),
    awayColor: Color = Color(0xFF4FC3F7),
    modifier: Modifier = Modifier,
) {
    val points = remember(homeScore, awayScore) {
        generateMomentumPoints(homeScore = homeScore, awayScore = awayScore)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipContainer()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MomentumHeader(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeColor = homeColor,
            awayColor = awayColor,
        )
        MomentumWave(
            points = points,
            homeColor = homeColor,
            awayColor = awayColor,
        )
        MomentumTimeAxis()
    }
}

private fun Modifier.clipContainer(): Modifier = this
    .background(
        color = Color(0xFF0F141C),
        shape = RoundedCornerShape(12.dp),
    )
    .border(
        width = 1.dp,
        color = Color(0xFF263238),
        shape = RoundedCornerShape(12.dp),
    )

@Composable
private fun MomentumHeader(
    homeTeam: String,
    awayTeam: String,
    homeColor: Color,
    awayColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "▲ $homeTeam",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = homeColor,
            maxLines = 1,
        )
        Text(
            text = "MATCH DOMINANCE & PRESSURE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.sp,
            color = Color(0xFF90A4AE),
        )
        Text(
            text = "$awayTeam ▼",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = awayColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun MomentumWave(
    points: List<Float>,
    homeColor: Color,
    awayColor: Color,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val stepX = width / (points.size - 1)

        drawLine(
            color = Color(0xFF37474F),
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 1f,
        )

        val wavePath = Path().apply {
            moveTo(0f, midY - (points[0] * midY * 0.85f))
            for (i in 1 until points.size) {
                val x = i * stepX
                val y = midY - (points[i] * midY * 0.85f)
                lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(homeColor, awayColor),
            ),
            style = Stroke(width = 2.5f),
        )
    }
}

@Composable
private fun MomentumTimeAxis() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf("1'", "15'", "30'", "HT", "60'", "75'", "90'").forEach { tick ->
            Text(
                text = tick,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color(0xFF607D8B),
            )
        }
    }
}

private fun generateMomentumPoints(homeScore: Int, awayScore: Int): List<Float> {
    val scoreBias = (homeScore - awayScore) * 0.12f
    return List(45) { index ->
        val t = index.toFloat()
        val sineWave = (sin(t * 0.35f) * 0.45f + cos(t * 0.18f) * 0.25f)
        (sineWave + scoreBias).coerceIn(-0.95f, 0.95f)
    }
}

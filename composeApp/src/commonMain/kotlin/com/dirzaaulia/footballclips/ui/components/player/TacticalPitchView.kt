package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.StarterPlayer

private val PitchGrassDark = Color(0xFF0C381E)
private val PitchGrassLight = Color(0xFF0E4023)
private val PitchLineColor = Color.White.copy(alpha = 0.35f)

@Composable
fun TacticalPitchView(
    formation: String?,
    starters: List<StarterPlayer>,
    isHome: Boolean = true,
    teamColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    var selectedPlayer by remember { mutableStateOf<StarterPlayer?>(null) }
    val lineCounts = remember(formation) { parseFormationLines(formation) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PitchGrassDark,
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()

            PitchCanvas(modifier = Modifier.fillMaxSize())

            PitchPlayersOverlay(
                starters = starters,
                lineCounts = lineCounts,
                isHome = isHome,
                teamColor = teamColor,
                selectedPlayer = selectedPlayer,
                onPlayerClick = { selectedPlayer = if (selectedPlayer == it) null else it },
                widthPx = widthPx,
                heightPx = heightPx,
            )
        }
    }
}

@Composable
private fun PitchCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawGrassStripes()
        drawPitchMarkings()
    }
}

private fun DrawScope.drawGrassStripes() {
    val stripeHeight = size.height / 6
    for (i in 1 until 6 step 2) {
        drawRect(color = PitchGrassLight, topLeft = Offset(0f, i * stripeHeight), size = Size(size.width, stripeHeight))
    }
}

private fun DrawScope.drawPitchMarkings() {
    val stroke = Stroke(width = 1.5.dp.toPx())
    val margin = 8.dp.toPx()
    val bw = size.width * 0.58f
    val bh = size.height * 0.22f

    drawRoundRect(
        color = PitchLineColor,
        topLeft = Offset(margin, margin),
        size = Size(size.width - (margin * 2), size.height - (margin * 2)),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        style = stroke,
    )
    drawCircle(color = PitchLineColor, radius = size.width * 0.18f, center = Offset(size.width / 2f, margin), style = stroke)
    drawRect(color = PitchLineColor, topLeft = Offset((size.width - bw) / 2f, size.height - margin - bh), size = Size(bw, bh), style = stroke)
    val sbw = size.width * 0.28f
    val sbh = size.height * 0.08f
    drawRect(color = PitchLineColor, topLeft = Offset((size.width - sbw) / 2f, size.height - margin - sbh), size = Size(sbw, sbh), style = stroke)
    drawCircle(color = PitchLineColor, radius = 2.5f.dp.toPx(), center = Offset(size.width / 2f, size.height - margin - (bh * 0.65f)))
}

@Composable
private fun PitchPlayersOverlay(
    starters: List<StarterPlayer>,
    lineCounts: List<Int>,
    isHome: Boolean,
    teamColor: Color?,
    selectedPlayer: StarterPlayer?,
    onPlayerClick: (StarterPlayer) -> Unit,
    widthPx: Float,
    heightPx: Float,
) {
    val totalLines = lineCounts.size
    var playerIdx = 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Forward lines at top down to GK at bottom
        for (lineIndex in (totalLines - 1) downTo 0) {
            val countInLine = lineCounts[lineIndex]
            val linePlayers = starters.drop(playerIdx).take(countInLine)
            playerIdx += countInLine

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                linePlayers.forEach { player ->
                    TacticalPlayerNode(
                        player = player,
                        isHome = isHome,
                        teamColor = teamColor,
                        isSelected = selectedPlayer == player,
                        onClick = { onPlayerClick(player) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TacticalPlayerNode(
    player: StarterPlayer,
    isHome: Boolean,
    teamColor: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isGK = player.position.equals("GK", ignoreCase = true) || player.position.equals("G", ignoreCase = true)
    val baseColor = teamColor ?: if (isHome) Color(0xFF7098F8) else Color(0xFF4FC3F7)
    val jerseyColor = if (isGK) Color(0xFFF59E0B) else baseColor
    val shortName = remember(player.name) {
        val parts = player.name.trim().split(" ")
        if (parts.size > 1) parts.last() else player.name
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(1.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(26.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val s = 9.dp.toPx()
                val path = Path().apply {
                    moveTo(cx - s * 0.9f, cy - s * 1.0f)
                    lineTo(cx - s * 0.4f, cy - s * 1.2f)
                    lineTo(cx - s * 0.2f, cy - s * 0.95f)
                    lineTo(cx + s * 0.2f, cy - s * 0.95f)
                    lineTo(cx + s * 0.4f, cy - s * 1.2f)
                    lineTo(cx + s * 0.9f, cy - s * 1.0f)
                    lineTo(cx + s * 1.2f, cy - s * 0.35f)
                    lineTo(cx + s * 0.8f, cy - s * 0.25f)
                    lineTo(cx + s * 0.8f, cy + s * 1.0f)
                    lineTo(cx - s * 0.8f, cy + s * 1.0f)
                    lineTo(cx - s * 0.8f, cy - s * 0.25f)
                    lineTo(cx - s * 1.2f, cy - s * 0.35f)
                    close()
                }
                drawPath(path = path, color = if (isSelected) Color.White else jerseyColor)
                drawPath(
                    path = path,
                    color = if (isSelected) jerseyColor else Color.White,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            Text(
                text = player.jersey.ifBlank { "?" },
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.Black else Color.White,
            )
        }
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(4.dp),
            border = if (isSelected) BorderStroke(1.dp, jerseyColor) else null,
            modifier = Modifier.padding(top = 1.dp),
        ) {
            Text(
                text = shortName,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
    }
}

private fun parseFormationLines(formation: String?): List<Int> {
    val parts = formation?.split("-")?.mapNotNull { it.trim().toIntOrNull() }
    return if (parts.isNullOrEmpty()) listOf(1, 4, 3, 3) else listOf(1) + parts
}

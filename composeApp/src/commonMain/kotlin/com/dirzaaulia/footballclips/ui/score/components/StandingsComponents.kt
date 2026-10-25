package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun OfficialJerseyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B18)),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFD4AF37).copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Official Jersey",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Official Club Jerseys 2024/25",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Get authentic kits & official merchandise with exclusive discounts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Shop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StandingsTableHeader(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
            Text("Club", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("P", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("W", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("D", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("L", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("GD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
            Text("PTS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
        }
    }
}

@Composable
fun FormBadges(form: String?, modifier: Modifier = Modifier) {
    if (form.isNullOrBlank()) return
    val outcomes = form.replace(",", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .flatMap { token -> if (token.length > 1 && token.all { it in "WDLwdl" }) token.map { it.toString() } else listOf(token) }
        .map { it.uppercase().trim() }
        .filter { it in listOf("W", "D", "L") }
        .takeLast(5)

    if (outcomes.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        outcomes.forEach { outcome ->
            val (bgColor, text) = when (outcome) {
                "W" -> Color(0xFF34C759) to "W"
                "D" -> Color(0xFF8E8E93) to "D"
                "L" -> Color(0xFFFF3B30) to "L"
                else -> Color.Transparent to outcome
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
fun StandingRow(standing: StandingDto, totalTeams: Int, modifier: Modifier = Modifier) {
    val isUclZone = standing.position in 1..4
    val isRelegationZone = totalTeams > 4 && standing.position > (totalTeams - 3)

    val posBgColor = when {
        isUclZone -> Color(0xFF007AFF).copy(alpha = 0.2f)
        isRelegationZone -> Color(0xFFFF3B30).copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val posTextColor = when {
        isUclZone -> Color(0xFF0A84FF)
        isRelegationZone -> Color(0xFFFF453A)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(posBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "${standing.position}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = posTextColor)
            }
            Spacer(modifier = Modifier.width(8.dp))

            if (!standing.teamCrest.isNullOrEmpty()) {
                GlassLogoBadge(
                    model = standing.teamCrest.toProxyUrl(),
                    contentDescription = standing.teamName,
                    size = 24.dp,
                    padding = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = standing.teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!standing.form.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    FormBadges(form = standing.form)
                }
            }

            Text("${standing.playedGames}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("${standing.won}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("${standing.draw}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("${standing.lost}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
            Text("${standing.goalDifference}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
            Text("${standing.points}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
        }
    }
}

@Composable
fun StandingsLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF0A84FF)))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Champions League (1-4)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF453A)))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Relegation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

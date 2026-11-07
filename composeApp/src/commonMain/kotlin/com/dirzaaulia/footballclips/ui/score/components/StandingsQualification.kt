package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

data class QualificationZone(
    val label: String,
    val color: Color,
    val isPositionInZone: (pos: Int, totalTeams: Int) -> Boolean,
)

fun getQualificationZones(competitionId: String?, totalTeams: Int): List<QualificationZone> {
    val code = competitionId?.uppercase() ?: "PL"
    return when (code) {
        "PL", "PD", "SA" -> listOf(
            QualificationZone("Champions League (1-4)", Color(0xFF007AFF)) { pos, _ -> pos in 1..4 },
            QualificationZone("Europa League (5)", Color(0xFFF59E0B)) { pos, _ -> pos == 5 },
            QualificationZone("Conference League (6)", Color(0xFF10B981)) { pos, _ -> pos == 6 },
            QualificationZone("Relegation", Color(0xFFFF3B30)) { pos, total -> total > 4 && pos > (total - 3) },
        )
        "BL1" -> listOf(
            QualificationZone("Champions League (1-4)", Color(0xFF007AFF)) { pos, _ -> pos in 1..4 },
            QualificationZone("Europa League (5)", Color(0xFFF59E0B)) { pos, _ -> pos == 5 },
            QualificationZone("Conference League (6)", Color(0xFF10B981)) { pos, _ -> pos == 6 },
            QualificationZone("Relegation Playoff (16)", Color(0xFFE11D48)) { pos, _ -> pos == 16 },
            QualificationZone("Relegation (17-18)", Color(0xFFFF3B30)) { pos, _ -> pos in 17..18 },
        )
        "FL1" -> listOf(
            QualificationZone("Champions League (1-3)", Color(0xFF007AFF)) { pos, _ -> pos in 1..3 },
            QualificationZone("UCL Qualifiers (4)", Color(0xFF06B6D4)) { pos, _ -> pos == 4 },
            QualificationZone("Europa League (5)", Color(0xFFF59E0B)) { pos, _ -> pos == 5 },
            QualificationZone("Conference League (6)", Color(0xFF10B981)) { pos, _ -> pos == 6 },
            QualificationZone("Relegation Playoff (16)", Color(0xFFE11D48)) { pos, _ -> pos == 16 },
            QualificationZone("Relegation (17-18)", Color(0xFFFF3B30)) { pos, _ -> pos in 17..18 },
        )
        "DED" -> listOf(
            QualificationZone("Champions League (1-2)", Color(0xFF007AFF)) { pos, _ -> pos in 1..2 },
            QualificationZone("UCL Qualifiers (3)", Color(0xFF06B6D4)) { pos, _ -> pos == 3 },
            QualificationZone("Europa League (4)", Color(0xFFF59E0B)) { pos, _ -> pos == 4 },
            QualificationZone("UECL Playoff (5-8)", Color(0xFF10B981)) { pos, _ -> pos in 5..8 },
            QualificationZone("Relegation Playoff (16)", Color(0xFFE11D48)) { pos, _ -> pos == 16 },
            QualificationZone("Relegation (17-18)", Color(0xFFFF3B30)) { pos, _ -> pos in 17..18 },
        )
        "PPL" -> listOf(
            QualificationZone("Champions League (1)", Color(0xFF007AFF)) { pos, _ -> pos == 1 },
            QualificationZone("UCL Qualifiers (2)", Color(0xFF06B6D4)) { pos, _ -> pos == 2 },
            QualificationZone("Europa League (3)", Color(0xFFF59E0B)) { pos, _ -> pos == 3 },
            QualificationZone("UECL Qualifiers (4-5)", Color(0xFF10B981)) { pos, _ -> pos in 4..5 },
            QualificationZone("Relegation Playoff (16)", Color(0xFFE11D48)) { pos, _ -> pos == 16 },
            QualificationZone("Relegation (17-18)", Color(0xFFFF3B30)) { pos, _ -> pos in 17..18 },
        )
        "SPL" -> listOf(
            QualificationZone("AFC Champions League Elite (1-3)", Color(0xFF007AFF)) { pos, _ -> pos in 1..3 },
            QualificationZone("Relegation", Color(0xFFFF3B30)) { pos, total -> total > 3 && pos > (total - 3) },
        )
        "MLS" -> listOf(
            QualificationZone("MLS Cup Playoffs (1-7)", Color(0xFF007AFF)) { pos, _ -> pos in 1..7 },
            QualificationZone("Wild Card (8-9)", Color(0xFF8B5CF6)) { pos, _ -> pos in 8..9 },
        )
        "IDN" -> listOf(
            QualificationZone("AFC Champions League Two (1)", Color(0xFF007AFF)) { pos, _ -> pos == 1 },
            QualificationZone("AFC Challenge League (2)", Color(0xFF10B981)) { pos, _ -> pos == 2 },
            QualificationZone("Relegation", Color(0xFFFF3B30)) { pos, total -> total > 3 && pos > (total - 3) },
        )
        "BRA" -> listOf(
            QualificationZone("Copa Libertadores (1-4)", Color(0xFF007AFF)) { pos, _ -> pos in 1..4 },
            QualificationZone("Libertadores Qualifiers (5-6)", Color(0xFF06B6D4)) { pos, _ -> pos in 5..6 },
            QualificationZone("Copa Sudamericana (7-12)", Color(0xFFF59E0B)) { pos, _ -> pos in 7..12 },
            QualificationZone("Relegation (17-20)", Color(0xFFFF3B30)) { pos, total -> total >= 20 && pos in 17..20 },
        )
        "ARG" -> listOf(
            QualificationZone("Copa Libertadores (1-3)", Color(0xFF007AFF)) { pos, _ -> pos in 1..3 },
            QualificationZone("Copa Sudamericana (4-9)", Color(0xFFF59E0B)) { pos, _ -> pos in 4..9 },
            QualificationZone("Relegation", Color(0xFFFF3B30)) { pos, total -> total > 2 && pos > (total - 2) },
        )
        "CL", "EL", "ECL" -> listOf(
            QualificationZone("Round of 16 (1-8)", Color(0xFF007AFF)) { pos, _ -> pos in 1..8 },
            QualificationZone("Knockout Play-offs (9-24)", Color(0xFF8B5CF6)) { pos, _ -> pos in 9..24 },
        )
        else -> listOf(
            QualificationZone("Top Tier (1-4)", Color(0xFF007AFF)) { pos, _ -> pos in 1..4 },
            QualificationZone("Relegation", Color(0xFFFF3B30)) { pos, total -> total > 4 && pos > (total - 3) },
        )
    }
}

fun getZoneForPosition(competitionId: String?, position: Int, totalTeams: Int): QualificationZone? {
    return getQualificationZones(competitionId, totalTeams).firstOrNull { it.isPositionInZone(position, totalTeams) }
}

@Composable
fun StandingRow(
    standing: StandingDto,
    totalTeams: Int,
    modifier: Modifier = Modifier,
    competitionId: String? = null,
) {
    val zone = remember(competitionId, standing.position, totalTeams) {
        getZoneForPosition(competitionId, standing.position, totalTeams)
    }

    val posBgColor = zone?.color?.copy(alpha = 0.2f) ?: Color.Transparent
    val posTextColor = zone?.color ?: MaterialTheme.colorScheme.onSurface

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(posBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${standing.position}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = posTextColor,
                )
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
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = standing.teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StandingsLegend(
    competitionId: String? = null,
    totalTeams: Int = 20,
    modifier: Modifier = Modifier,
) {
    val zones = remember(competitionId, totalTeams) {
        getQualificationZones(competitionId, totalTeams)
    }

    FlowRow(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        zones.forEach { zone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(zone.color),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = zone.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

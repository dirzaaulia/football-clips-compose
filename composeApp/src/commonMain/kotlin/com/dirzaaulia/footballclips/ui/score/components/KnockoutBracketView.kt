package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.domain.model.Match

private val ROUND_ORDER = listOf(
    "round of 32",
    "fourth round",
    "knockout round playoffs",
    "round of 16",
    "cup round",
    "quarterfinals",
    "quarter-finals",
    "semifinals",
    "semi-finals",
    "final"
)

private fun getRoundWeight(roundName: String?): Int {
    if (roundName == null) return 50
    val lower = roundName.lowercase().trim()
    for (i in ROUND_ORDER.indices) {
        if (lower.contains(ROUND_ORDER[i])) return i + 1
    }
    return 50
}

private fun formatRoundDisplay(roundName: String?): String {
    if (roundName == null) return "Knockout Round"
    val lower = roundName.lowercase()
    return when {
        lower.contains("final") && !lower.contains("semi") && !lower.contains("quarter") -> "Final"
        lower.contains("semi") -> "Semi-finals"
        lower.contains("quarter") -> "Quarter-finals"
        lower.contains("round of 16") || lower.contains("cup round") -> "Round of 16"
        lower.contains("playoff") -> "Knockout Play-offs"
        else -> roundName
    }
}

@Composable
fun KnockoutBracketView(
    matches: List<Match>,
    onMatchClick: (Match) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val knockoutMatches = matches.filter {
        val r = (it.roundName ?: "").lowercase()
        !r.contains("league phase")
    }

    if (knockoutMatches.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Knockout Stage Pending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Knockout fixtures will populate once qualified clubs are drawn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Group by normalized round
    val groupedRounds = knockoutMatches
        .groupBy { formatRoundDisplay(it.roundName) }
        .entries
        .sortedBy { getRoundWeight(it.key) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedRounds.forEach { (roundTitle, roundMatches) ->
            val isFinal = roundTitle.equals("Final", ignoreCase = true)

            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Round Header Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isFinal) Color(0xFFD4AF37).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                        .border(
                            1.dp,
                            if (isFinal) Color(0xFFD4AF37).copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = roundTitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFinal) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${roundMatches.size} ${if (roundMatches.size == 1) "Match" else "Ties"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Match Cards Column
                roundMatches.forEach { match ->
                    KnockoutMatchCard(
                        match = match,
                        isFinal = isFinal,
                        onClick = { onMatchClick(match) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KnockoutMatchCard(
    match: Match,
    isFinal: Boolean,
    onClick: () -> Unit
) {
    val isFinished = match.isFinished
    val isLive = match.isLive
    val homeWinner = isFinished && (match.homeScore ?: 0) > (match.awayScore ?: 0)
    val awayWinner = isFinished && (match.awayScore ?: 0) > (match.homeScore ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinal) Color(0xFF181C20) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isFinal) Color(0xFFD4AF37).copy(alpha = 0.4f)
                else if (isLive) Color(0xFF10B981)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.dateOnly.ifBlank { "TBD" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                    }
                } else if (isFinished) {
                    Text(
                        text = "FT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Home Team
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (homeWinner) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = match.homeTeamCrest,
                        contentDescription = match.homeTeamName,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = match.homeTeamName,
                        fontSize = 12.sp,
                        fontWeight = if (homeWinner) FontWeight.Bold else FontWeight.Medium,
                        color = if (homeWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = match.homeScore?.toString() ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (homeWinner) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )
            }

            // Away Team
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (awayWinner) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = match.awayTeamCrest,
                        contentDescription = match.awayTeamName,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = match.awayTeamName,
                        fontSize = 12.sp,
                        fontWeight = if (awayWinner) FontWeight.Bold else FontWeight.Medium,
                        color = if (awayWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = match.awayScore?.toString() ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (awayWinner) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )
            }

            // Highlight chip if available
            if (match.hasHighlight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Watch Highlights",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

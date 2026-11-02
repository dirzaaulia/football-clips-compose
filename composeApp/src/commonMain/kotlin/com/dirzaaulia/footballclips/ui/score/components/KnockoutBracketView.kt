package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    "final",
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
    modifier: Modifier = Modifier,
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
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = "Knockout Stage Pending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Knockout fixtures will populate once qualified clubs are drawn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val groupedRounds = knockoutMatches
        .groupBy { formatRoundDisplay(it.roundName) }
        .entries
        .sortedBy { getRoundWeight(it.key) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        groupedRounds.forEach { (roundTitle, roundMatches) ->
            val isFinal = roundTitle.equals("Final", ignoreCase = true)

            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isFinal) {
                                Color(0xFFD4AF37).copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                        )
                        .border(
                            1.dp,
                            if (isFinal) {
                                Color(0xFFD4AF37).copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = roundTitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFinal) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp,
                    )
                    Text(
                        text = "${roundMatches.size} ${if (roundMatches.size == 1) "Match" else "Ties"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                roundMatches.forEach { match ->
                    KnockoutMatchCard(
                        match = match,
                        isFinal = isFinal,
                        onClick = { onMatchClick(match) },
                    )
                }
            }
        }
    }
}

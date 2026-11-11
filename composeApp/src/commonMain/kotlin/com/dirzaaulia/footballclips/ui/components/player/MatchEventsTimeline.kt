package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.MatchEvent

@Composable
fun MatchEventsTimeline(
    events: List<MatchEvent>?,
    homeName: String,
    awayName: String,
    isSpoilerFree: Boolean = false,
    isLive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (events.isNullOrEmpty()) {
        TimelineEmptyState("No key match events recorded yet.", modifier)
        return
    }

    var isRevealed by remember { mutableStateOf(!isSpoilerFree) }
    val filteredEvents = remember(events) {
        events.filter { e ->
            val t = (e.type ?: "").lowercase()
            val text = e.text.lowercase()
            t.contains("goal") || t.contains("card") || t.contains("sub") ||
                t.contains("penalty") || text.contains("goal") || text.contains("card")
        }
    }

    if (filteredEvents.isEmpty()) {
        TimelineEmptyState("No goals or cards recorded for this match.", modifier)
        return
    }

    if (isSpoilerFree && !isRevealed && !isLive) {
        MatchSpoilerNoticeCard(
            onRevealClick = { isRevealed = true },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        filteredEvents.forEach { event ->
            TimelineEventRow(
                event = event,
                homeName = homeName,
                awayName = awayName,
            )
        }
    }
}

@Composable
private fun TimelineEmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineEventRow(
    event: MatchEvent,
    homeName: String,
    awayName: String,
) {
    val isGoal = event.scoringPlay ||
        (event.type?.contains("goal", ignoreCase = true) == true) ||
        event.text.contains("goal", ignoreCase = true)
    val isRed = event.type?.contains("red-card", ignoreCase = true) == true
    val isYellow = event.type?.contains("yellow-card", ignoreCase = true) == true && !isRed
    val isSub = event.type?.contains("sub", ignoreCase = true) == true
    val isHome = event.side == "home"

    val badgeColor = when {
        isGoal -> Color(0xFF10B981)
        isRed -> Color(0xFFEF4444)
        isYellow -> Color(0xFFF59E0B)
        isSub -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val badgeIcon = when {
        isGoal -> "⚽"
        isRed -> "🟥"
        isYellow -> "🟨"
        isSub -> "🔄"
        else -> "•"
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = badgeIcon, fontSize = 14.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (isSub) {
                    SubstitutionDetails(event = event)
                } else {
                    GeneralEventDetails(event = event, isGoal = isGoal)
                }

                Text(
                    text = if (isHome) homeName else awayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = event.clock?.ifBlank { "" } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SubstitutionDetails(event: MatchEvent) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BadgeTag(text = "IN", color = Color(0xFF10B981))
        Text(
            text = event.player.ifBlank { "Player" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    if (!event.assist.isNullOrBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(top = 2.dp),
        ) {
            BadgeTag(text = "OUT", color = Color(0xFFF97316))
            Text(
                text = event.assist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GeneralEventDetails(event: MatchEvent, isGoal: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = event.player.ifBlank { "Player" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (isGoal) {
            BadgeTag(text = "GOAL", color = Color(0xFF10B981))
        }
    }

    if (isGoal && !event.assist.isNullOrBlank()) {
        Text(
            text = "Assist: ${event.assist}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BadgeTag(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}

package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.MatchEvent

@Composable
fun MatchEventsTimeline(
    events: List<MatchEvent>?,
    homeName: String,
    awayName: String,
    homeColor: Color? = null,
    awayColor: Color? = null,
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

    val effectiveHomeColor = homeColor ?: Color(0xFF7098F8)
    val effectiveAwayColor = awayColor ?: Color(0xFF4FC3F7)

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimelineMilestoneBadge("0' • Kickoff")
        filteredEvents.forEachIndexed { idx, event ->
            CentralSpineEventRow(
                event = event,
                homeName = homeName,
                awayName = awayName,
                homeColor = effectiveHomeColor,
                awayColor = effectiveAwayColor,
                isLast = idx == filteredEvents.lastIndex,
            )
        }
        TimelineMilestoneBadge(if (isLive) "LIVE IN PLAY" else "Full Time")
    }
}

@Composable
private fun CentralSpineEventRow(
    event: MatchEvent,
    homeName: String,
    awayName: String,
    homeColor: Color,
    awayColor: Color,
    isLast: Boolean,
) {
    val isHome = event.side == "home"
    val clubColor = if (isHome) homeColor else awayColor
    val clockText = event.clock?.ifBlank { "•" } ?: "•"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f).padding(end = 6.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (isHome) {
                SpineBroadcastCard(event = event, clubColor = clubColor, teamName = homeName, isHome = true)
            }
        }

        SpineCenterAxis(clockText = clockText, clubColor = clubColor, isLast = isLast)

        Box(
            modifier = Modifier.weight(1f).padding(start = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (!isHome) {
                SpineBroadcastCard(event = event, clubColor = clubColor, teamName = awayName, isHome = false)
            }
        }
    }
}

@Composable
private fun SpineCenterAxis(clockText: String, clubColor: Color, isLast: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(42.dp),
    ) {
        Box(
            modifier = Modifier.width(2.dp).height(12.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.5.dp, clubColor),
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = clockText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = clubColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (!isLast) {
            Box(
                modifier = Modifier.width(2.dp).height(12.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
private fun SpineBroadcastCard(
    event: MatchEvent,
    clubColor: Color,
    teamName: String,
    isHome: Boolean,
) {
    val isGoal = event.scoringPlay || (event.type?.contains("goal", ignoreCase = true) == true)
    val isRed = event.type?.contains("red-card", ignoreCase = true) == true
    val isYellow = event.type?.contains("yellow-card", ignoreCase = true) == true && !isRed
    val isSub = event.type?.contains("sub", ignoreCase = true) == true

    val badgeIcon = when {
        isGoal -> "⚽"
        isRed -> "🟥"
        isYellow -> "🟨"
        isSub -> "🔄"
        else -> "•"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, clubColor.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = if (isHome) Alignment.End else Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!isHome) Text(text = badgeIcon, fontSize = 12.sp)
                Text(
                    text = event.player.ifBlank { teamName },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isHome) Text(text = badgeIcon, fontSize = 12.sp)
            }
            if (isGoal && !event.assist.isNullOrBlank()) {
                Text(
                    text = "Ast: ${event.assist}",
                    style = MaterialTheme.typography.labelSmall,
                    color = clubColor,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
            }
            if (isSub && !event.assist.isNullOrBlank()) {
                Text(
                    text = "Out: ${event.assist}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TimelineMilestoneBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun TimelineEmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

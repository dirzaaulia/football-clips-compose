package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun HighlightTeamsSection(
    match: Match,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    onRevealClick: (() -> Unit)?,
) {
    val isFinishedOrLive = match.isFinished || match.isLive
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HighlightTeamRow(
            teamName = match.homeTeamName,
            teamCrest = match.homeTeamCrest,
            score = match.homeScore,
            penalties = match.homePenalties,
            isFinishedOrLive = isFinishedOrLive,
            isLive = match.isLive,
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            kickoffTime = if (!isFinishedOrLive) match.formattedLocalKickoff else null,
            onRevealClick = onRevealClick,
        )
        HighlightTeamRow(
            teamName = match.awayTeamName,
            teamCrest = match.awayTeamCrest,
            score = match.awayScore,
            penalties = match.awayPenalties,
            isFinishedOrLive = isFinishedOrLive,
            isLive = match.isLive,
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            kickoffTime = null,
            onRevealClick = onRevealClick,
        )
    }
}

@Composable
internal fun HighlightTeamRow(
    teamName: String,
    teamCrest: String?,
    score: Int?,
    penalties: Int?,
    isFinishedOrLive: Boolean,
    isLive: Boolean,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    kickoffTime: String?,
    onRevealClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            if (!teamCrest.isNullOrEmpty()) {
                GlassLogoBadge(
                    model = teamCrest.toProxyUrl(),
                    contentDescription = teamName,
                    size = 24.dp,
                    padding = 2.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = teamName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HighlightScoreColumn(
            score = score,
            penalties = penalties,
            isFinishedOrLive = isFinishedOrLive,
            isLive = isLive,
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            kickoffTime = kickoffTime,
            onRevealClick = onRevealClick,
        )
    }
}

@Composable
internal fun HighlightScoreColumn(
    score: Int?,
    penalties: Int?,
    isFinishedOrLive: Boolean,
    isLive: Boolean,
    isSpoilerFree: Boolean,
    isRevealed: Boolean,
    kickoffTime: String?,
    onRevealClick: (() -> Unit)?,
) {
    if (isFinishedOrLive) {
        if (isSpoilerFree && !isRevealed && !isLive) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.then(if (onRevealClick != null) Modifier.clickable { onRevealClick() } else Modifier),
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${score ?: 0}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                )
                if (penalties != null) {
                    Text(
                        text = "($penalties)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 3.dp),
                    )
                }
            }
        }
    } else if (kickoffTime != null) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                text = kickoffTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

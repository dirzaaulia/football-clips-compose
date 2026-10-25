package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
fun MatchCentreHero(
    match: Match,
    isSpoilerFree: Boolean = false,
    isScoreRevealed: Boolean = !isSpoilerFree,
    onRevealClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isFinished = match.isFinished
    val isLive = match.isLive
    val isHidden = isSpoilerFree && !isScoreRevealed && isFinished

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MatchCentreHeroHeader(
                match = match,
                isLive = isLive,
                isFinished = isFinished,
            )

            MatchCentreTeamsFaceOff(
                match = match,
                isSpoilerFree = isSpoilerFree,
                isHidden = isHidden,
                onRevealClick = onRevealClick,
            )

            if (isLive) {
                MatchCentreLiveStreamBanner(match = match)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            MatchCentreHeroFooter(
                match = match,
                isFinished = isFinished,
            )

            if (isFinished && match.highlightVideoId.isNullOrBlank()) {
                MatchCentreEmbargoBanner()
            }
        }
    }
}

@Composable
private fun MatchCentreHeroFooter(
    match: Match,
    isFinished: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = match.formattedLocalKickoff,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isFinished && match.highlightVideoId.isNullOrBlank()) {
            Text(
                text = "Embargo Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B),
            )
        }
    }
}

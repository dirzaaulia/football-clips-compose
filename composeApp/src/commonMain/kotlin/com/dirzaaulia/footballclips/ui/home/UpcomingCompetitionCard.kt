package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge

@Composable
fun UpcomingCompetitionCard(
    league: BigLeague,
    selectedSeason: Int,
    onSeeAll: () -> Unit,
    onSwitchSeason: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val subtitle = getCompetitionStatusMessage(league.id, selectedSeason)

    Box(
        modifier = modifier
            .width(280.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onSeeAll)
            .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            UpcomingCompetitionHeader(
                league = league,
                subtitle = subtitle,
            )
            Spacer(modifier = Modifier.height(10.dp))
            UpcomingCompetitionActions(
                selectedSeason = selectedSeason,
                onSeeAll = onSeeAll,
                onSwitchSeason = onSwitchSeason,
            )
        }
    }
}

@Composable
private fun UpcomingCompetitionHeader(
    league: BigLeague,
    subtitle: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlassLogoBadge(
            logoUrl = league.logo.ifBlank { league.emblemUrl },
            contentDescription = league.name,
            size = 36.dp,
            padding = 4.dp,
            shape = CircleShape,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = league.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun UpcomingCompetitionActions(
    selectedSeason: Int,
    onSeeAll: () -> Unit,
    onSwitchSeason: ((Int) -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onSeeAll,
            label = { Text("Fixtures", style = MaterialTheme.typography.labelSmall) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ),
        )
        if (selectedSeason == 2026 && onSwitchSeason != null) {
            AssistChip(
                onClick = { onSwitchSeason(2025) },
                label = { Text("2025 Archive", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                ),
            )
        }
    }
}

private fun getCompetitionStatusMessage(compId: String, season: Int): String {
    return when (compId.uppercase()) {
        "CIT" -> if (season >= 2026) "Round of 16 kicks off Dec 1, 2026" else "Coppa Italia tournament action"
        "LIB" -> if (season >= 2026) "Final Nov 28, 2026 • Stages in prep" else "CONMEBOL Libertadores highlights"
        "SUD" -> if (season >= 2026) "Final Nov 21, 2026 • Stages in prep" else "CONMEBOL Sudamericana highlights"
        "REC" -> "Annual Continental Champion clash"
        else -> "Upcoming fixtures available for Season $season"
    }
}

package com.dirzaaulia.footballclips.ui.components.filter

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun BigLeaguesQuickFilterBar(
    selectedLeagueId: Int? = null,
    selectedCompetitionId: String? = null,
    onLeagueSelected: (Int?, String?) -> Unit,
    modifier: Modifier = Modifier,
    dynamicCompetitions: List<BigLeague>? = null,
    showAll: Boolean = true,
    isCompact: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    BoxWithConstraints(modifier = modifier.padding(vertical = 4.dp)) {
        val isWeb = maxWidth > 840.dp
        val leagues = remember(dynamicCompetitions, showAll) {
            val comps = dynamicCompetitions ?: emptyList()
            val list = mutableListOf<BigLeague>()
            if (showAll && comps.none { it.leagueId == null && it.competitionId.isEmpty() }) {
                list.add(
                    BigLeague(
                        id = "",
                        name = "All Leagues",
                        leagueId = null,
                        countryCode = null,
                        emblemUrl = null,
                    ),
                )
            }
            list.addAll(comps.filter { if (showAll) true else (it.leagueId != null || it.competitionId.isNotEmpty()) })
            list
        }

        val itemSpacing = if (isCompact) 8.dp else if (isWeb) 16.dp else 10.dp

        LazyRow(
            modifier = Modifier.wrapContentWidth(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(leagues) { league ->
                val isSelected = if (selectedLeagueId != null) {
                    league.leagueId == selectedLeagueId
                } else if (selectedCompetitionId != null) {
                    league.competitionId == selectedCompetitionId
                } else {
                    league.leagueId == null && league.competitionId.isEmpty()
                }

                LeagueModernFilterItem(
                    name = league.name,
                    logo = league.logo,
                    isSelected = isSelected,
                    isWeb = isWeb,
                    isCompact = isCompact,
                    onClick = { onLeagueSelected(league.leagueId, league.competitionId) },
                )
            }
        }
    }
}

@Composable
fun LeagueModernFilterItem(
    name: String,
    logo: String,
    isSelected: Boolean,
    isWeb: Boolean = false,
    isCompact: Boolean = false,
    onClick: () -> Unit,
) {
    val isALL = logo.isEmpty()

    val contentColor by animateColorAsState(
        if (isSelected && isALL) MaterialTheme.colorScheme.onPrimary
        else if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
    )

    val itemWidth = if (isCompact) 44.dp else if (isWeb) 68.dp else 52.dp
    val circleSize = if (isCompact) 40.dp else if (isWeb) 64.dp else 48.dp
    val logoPadding = if (isCompact) 6.dp else if (isWeb) 10.dp else 8.dp

    Column(
        modifier = Modifier
            .width(itemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val frostedGradient = Brush.verticalGradient(
            colors = if (isSelected) {
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.12f),
                )
            },
        )
        val borderColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.White.copy(alpha = 0.25f)
        }

        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(frostedGradient)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape,
                )
                .padding(logoPadding),
            contentAlignment = Alignment.Center,
        ) {
            if (logo.isNotEmpty()) {
                AsyncImage(
                    model = logo.toProxyUrl(),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = "ALL",
                    style = if (isCompact) MaterialTheme.typography.labelSmall else if (isWeb) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                )
            }
        }
    }
}

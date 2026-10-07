package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun BigLeaguesQuickFilterBar(
    selectedLeagueId: Int? = null,
    selectedCompetitionId: String? = null,
    onLeagueSelected: (Int?, String?) -> Unit,
    modifier: Modifier = Modifier,
    showAll: Boolean = true,
    isCompact: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    BoxWithConstraints(modifier = modifier.padding(vertical = 4.dp)) {
        val isWeb = maxWidth > 840.dp
        val leagues = remember(showAll) {
            if (showAll) BigLeaguesConstants.leagues
            else BigLeaguesConstants.leagues.filter { it.leagueId != null || it.competitionId != null }
        }

        val itemSpacing = if (isCompact) 8.dp else if (isWeb) 16.dp else 10.dp

        LazyRow(
            modifier = Modifier.wrapContentWidth(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(leagues) { league ->
                val isSelected = if (selectedLeagueId != null) {
                    league.leagueId == selectedLeagueId
                } else if (selectedCompetitionId != null) {
                    league.competitionId == selectedCompetitionId
                } else {
                    league.leagueId == null && league.competitionId == null
                }

                LeagueModernFilterItem(
                    name = league.name,
                    logo = league.logo,
                    isSelected = isSelected,
                    isWeb = isWeb,
                    isCompact = isCompact,
                    onClick = { onLeagueSelected(league.leagueId, league.competitionId) }
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
    onClick: () -> Unit
) {
    val isALL = logo.isEmpty()
    
    val backgroundColor by animateColorAsState(
        if (isSelected && isALL) MaterialTheme.colorScheme.primary 
        else if (isALL) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else Color.White.copy(alpha = 0.95f) // Professional clean backdrop for logos
    )
    
    val contentColor by animateColorAsState(
        if (isSelected && isALL) MaterialTheme.colorScheme.onPrimary
        else if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
    )

    val itemWidth = if (isCompact) 44.dp else if (isWeb) 68.dp else 52.dp
    val circleSize = if (isCompact) 40.dp else if (isWeb) 64.dp else 48.dp
    val logoPadding = if (isCompact) 6.dp else if (isWeb) 10.dp else 8.dp

    Column(
        modifier = Modifier
            .width(itemWidth)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .background(backgroundColor, CircleShape)
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(logoPadding),
            contentAlignment = Alignment.Center
        ) {
            if (logo.isNotEmpty()) {
                AsyncImage(
                    model = logo.toProxyUrl(),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "ALL",
                    style = if (isCompact) MaterialTheme.typography.labelSmall else if (isWeb) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = contentColor
                )
            }
        }
    }
}

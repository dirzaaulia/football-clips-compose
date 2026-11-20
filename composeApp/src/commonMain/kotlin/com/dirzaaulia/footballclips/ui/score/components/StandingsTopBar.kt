package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.home.MyClubsActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsTopBar(
    isLarge: Boolean,
    isLandscape: Boolean,
    horizontalPadding: Dp,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    favoriteClubsCount: Int,
    onShowMyClubs: () -> Unit,
    selectedCompetitionId: String?,
    dynamicCompetitions: List<BigLeague>?,
    onLeagueSelected: (Int?, String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        val statusBarInset = if (isLandscape) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        TopAppBar(
            modifier = if (isLarge) Modifier else Modifier.height(50.dp + statusBarInset),
            title = {
                StandingsTitleRow(
                    isLarge = isLarge,
                    selectedSeason = selectedSeason,
                    onSeasonSelected = onSeasonSelected,
                )
            },
            actions = {
                StandingsActionsRow(
                    isLarge = isLarge,
                    isLandscape = isLandscape,
                    horizontalPadding = horizontalPadding,
                    favoriteClubsCount = favoriteClubsCount,
                    onShowMyClubs = onShowMyClubs,
                    selectedCompetitionId = selectedCompetitionId,
                    dynamicCompetitions = dynamicCompetitions,
                    onLeagueSelected = onLeagueSelected,
                    onNavigateToSettings = onNavigateToSettings,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        if (!isLarge && !isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LeagueDropdownSelector(
                    selectedCompetitionId = selectedCompetitionId ?: "PL",
                    dynamicCompetitions = dynamicCompetitions,
                    onLeagueSelected = onLeagueSelected,
                    showAll = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StandingsTitleRow(
    isLarge: Boolean,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Standings",
            style = if (isLarge) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = if (isLarge) 1.sp else (-0.5).sp,
        )
        SeasonDropdownSelector(
            selectedSeason = selectedSeason,
            onSeasonSelected = onSeasonSelected,
            isCompact = true,
        )
    }
}

@Composable
private fun StandingsActionsRow(
    isLarge: Boolean,
    isLandscape: Boolean,
    horizontalPadding: Dp,
    favoriteClubsCount: Int,
    onShowMyClubs: () -> Unit,
    selectedCompetitionId: String?,
    dynamicCompetitions: List<BigLeague>?,
    onLeagueSelected: (Int?, String?) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    if (isLarge || isLandscape) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(end = horizontalPadding),
        ) {
            MyClubsActionButton(
                favoriteClubsCount = favoriteClubsCount,
                onShowMyClubs = onShowMyClubs,
            )
            LeagueDropdownSelector(
                selectedCompetitionId = selectedCompetitionId ?: "PL",
                dynamicCompetitions = dynamicCompetitions,
                onLeagueSelected = onLeagueSelected,
                showAll = false,
            )
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 4.dp),
        ) {
            MyClubsActionButton(
                favoriteClubsCount = favoriteClubsCount,
                onShowMyClubs = onShowMyClubs,
            )
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

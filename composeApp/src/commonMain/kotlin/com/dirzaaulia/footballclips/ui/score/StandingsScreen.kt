package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.score.components.KnockoutBracketView
import com.dirzaaulia.footballclips.ui.score.components.LeagueStandingsTable
import com.dirzaaulia.footballclips.ui.score.components.StandingsTableSkeleton
import com.dirzaaulia.footballclips.ui.score.components.StandingsTopBar

@Composable
fun StandingsScreen(
    state: StandingsState,
    isPremium: Boolean,
    selectedCompetitionId: String? = null,
    dynamicCompetitions: List<BigLeague>? = null,
    selectedSeason: Int = 2026,
    favoriteClubsCount: Int = 0,
    onShowMyClubs: () -> Unit = {},
    onLeagueSelected: (Int?, String?) -> Unit = { _, _ -> },
    onSeasonSelected: (Int) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hybridStage by remember(selectedCompetitionId) { mutableStateOf("league") }
    val currentComp = dynamicCompetitions?.firstOrNull { it.id.equals(selectedCompetitionId, ignoreCase = true) }
    val isCup = currentComp?.isCup == true || selectedCompetitionId in setOf("FAC", "EFL", "CDR", "DFB", "CIT")
    val isHybrid = currentComp?.isHybrid == true || selectedCompetitionId in setOf("CL", "EL", "ECL")

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isLarge = maxWidth >= 600.dp && !isLandscape
        val bottomLegendPadding = if (isLarge || isLandscape) 16.dp else 90.dp
        val horizontalPadding = if (maxWidth >= 840.dp) 32.dp else if (isLarge) 24.dp else 16.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                StandingsTopBar(
                    isLarge = isLarge,
                    isLandscape = isLandscape,
                    horizontalPadding = horizontalPadding,
                    selectedSeason = selectedSeason,
                    onSeasonSelected = onSeasonSelected,
                    favoriteClubsCount = favoriteClubsCount,
                    onShowMyClubs = onShowMyClubs,
                    selectedCompetitionId = selectedCompetitionId,
                    dynamicCompetitions = dynamicCompetitions,
                    onLeagueSelected = onLeagueSelected,
                    onNavigateToSettings = onNavigateToSettings,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (!isPremium) {
                    BannerAdItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 6.dp),
                        style = NativeAdStyle.MATCH,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    when (state) {
                        is StandingsState.Loading -> {
                            StandingsTableSkeleton(
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        is StandingsState.Error -> {
                            EmptyState(
                                title = "Failed to Load Standings",
                                description = state.message,
                                onActionClick = onRetry,
                                actionText = "Try Again",
                            )
                        }
                        is StandingsState.Success -> {
                            StandingsContent(
                                state = state,
                                isCup = isCup,
                                isHybrid = isHybrid,
                                hybridStage = hybridStage,
                                onHybridStageChange = { hybridStage = it },
                                isPremium = isPremium,
                                horizontalPadding = horizontalPadding,
                                bottomLegendPadding = bottomLegendPadding,
                                selectedCompetitionId = selectedCompetitionId,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsContent(
    state: StandingsState.Success,
    isCup: Boolean,
    isHybrid: Boolean,
    hybridStage: String,
    onHybridStageChange: (String) -> Unit,
    isPremium: Boolean,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    bottomLegendPadding: androidx.compose.ui.unit.Dp,
    selectedCompetitionId: String?,
) {
    if (isCup) {
        if (state.knockoutMatches.isEmpty()) {
            EmptyState(
                title = "No Cup Matches Available",
                description = "Cup fixtures and bracket data are currently unavailable.",
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
            ) {
                KnockoutBracketView(
                    matches = state.knockoutMatches,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                if (!isPremium) {
                    BannerAdItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        style = NativeAdStyle.MATCH,
                    )
                }
            }
        }
    } else if (isHybrid) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = if (hybridStage == "league") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                Tab(
                    selected = hybridStage == "league",
                    onClick = { onHybridStageChange("league") },
                    text = { Text("League Phase (36 Teams)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                )
                Tab(
                    selected = hybridStage == "knockout",
                    onClick = { onHybridStageChange("knockout") },
                    text = { Text("Knockout Bracket", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                )
            }

            if (hybridStage == "knockout") {
                KnockoutBracketView(
                    matches = state.knockoutMatches,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                if (!isPremium) {
                    BannerAdItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        style = NativeAdStyle.MATCH,
                    )
                }
            } else {
                LeagueStandingsTable(
                    standings = state.standings,
                    bottomLegendPadding = bottomLegendPadding,
                    selectedCompetitionId = selectedCompetitionId,
                    isAdsRemoved = isPremium,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
        ) {
            LeagueStandingsTable(
                standings = state.standings,
                bottomLegendPadding = bottomLegendPadding,
                selectedCompetitionId = selectedCompetitionId,
                isAdsRemoved = isPremium,
            )
        }
    }
}

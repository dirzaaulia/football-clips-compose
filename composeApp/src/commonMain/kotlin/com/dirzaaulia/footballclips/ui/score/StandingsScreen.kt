package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.score.components.KnockoutBracketView
import com.dirzaaulia.footballclips.ui.score.components.LeagueStandingsTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    state: StandingsState,
    isPremium: Boolean,
    selectedCompetitionId: String? = null,
    dynamicCompetitions: List<BigLeague>? = null,
    selectedSeason: Int = 2026,
    onLeagueSelected: (Int?, String?) -> Unit = { _, _ -> },
    onSeasonSelected: (Int) -> Unit = {},
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val statusBarInset = if (isLandscape) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    TopAppBar(
                        modifier = if (isLarge) Modifier else Modifier.height(50.dp + statusBarInset),
                        title = {
                            Text(
                                text = "Standings",
                                style = if (isLarge) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = if (isLarge) 1.sp else (-0.5).sp,
                            )
                        },
                        actions = {
                            if (isLarge || isLandscape) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(end = horizontalPadding),
                                ) {
                                    SeasonDropdownSelector(
                                        selectedSeason = selectedSeason,
                                        onSeasonSelected = onSeasonSelected,
                                        isCompact = false,
                                    )
                                    LeagueDropdownSelector(
                                        selectedCompetitionId = selectedCompetitionId ?: "PL",
                                        dynamicCompetitions = dynamicCompetitions,
                                        onLeagueSelected = onLeagueSelected,
                                        showAll = false,
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        ),
                    )
                    if (!isLarge && !isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected,
                                isCompact = true,
                            )
                            LeagueDropdownSelector(
                                selectedCompetitionId = selectedCompetitionId ?: "PL",
                                dynamicCompetitions = dynamicCompetitions,
                                onLeagueSelected = onLeagueSelected,
                                showAll = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    is StandingsState.Loading -> {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 5.dp,
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
                                            style = NativeAdStyle.STANDINGS,
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
                                        onClick = { hybridStage = "league" },
                                        text = { Text("League Phase (36 Teams)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    )
                                    Tab(
                                        selected = hybridStage == "knockout",
                                        onClick = { hybridStage = "knockout" },
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
                                            style = NativeAdStyle.STANDINGS,
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
                }
            }
        }
    }
}
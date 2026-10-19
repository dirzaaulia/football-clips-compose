package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.score.components.StandingRow
import com.dirzaaulia.footballclips.ui.score.components.StandingsLegend
import com.dirzaaulia.footballclips.ui.score.components.StandingsTableHeader

import com.dirzaaulia.footballclips.data.constants.BigLeague

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.ui.score.components.KnockoutBracketView

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
    modifier: Modifier = Modifier
) {
    var hybridStage by remember(selectedCompetitionId) { mutableStateOf("league") }
    val isHybrid = BigLeaguesConstants.isEuropeanHybrid(selectedCompetitionId)
    val isCup = BigLeaguesConstants.isKnockoutCup(selectedCompetitionId)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isWeb = maxWidth > 840.dp && !isLandscape
        val bottomLegendPadding = if (isWeb || isLandscape) 16.dp else 90.dp
        val horizontalPadding = if (isWeb) 32.dp else if (isLandscape) 24.dp else 16.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Standings",
                                style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = if (isWeb) 1.sp else (-0.5).sp
                            )
                            SeasonDropdownSelector(
                                selectedSeason = selectedSeason,
                                onSeasonSelected = onSeasonSelected,
                                isCompact = !isWeb
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = horizontalPadding)
                        ) {
                            LeagueDropdownSelector(
                                selectedCompetitionId = selectedCompetitionId ?: "PL",
                                dynamicCompetitions = dynamicCompetitions,
                                onLeagueSelected = onLeagueSelected,
                                showAll = false
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is StandingsState.Loading -> {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 5.dp
                        )
                    }
                    is StandingsState.Error -> {
                        EmptyState(
                            title = "Failed to Load Standings",
                            description = state.message,
                            onActionClick = onRetry,
                            actionText = "Try Again"
                        )
                    }
                    is StandingsState.Success -> {
                        if (isCup) {
                            if (state.knockoutMatches.isEmpty()) {
                                EmptyState(
                                    title = "No Cup Matches Available",
                                    description = "Cup fixtures and bracket data are currently unavailable."
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = horizontalPadding)
                                ) {
                                    KnockoutBracketView(
                                        matches = state.knockoutMatches,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                }
                            }
                        } else if (isHybrid) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = horizontalPadding)
                            ) {
                                PrimaryTabRow(
                                    selectedTabIndex = if (hybridStage == "league") 0 else 1,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Tab(
                                        selected = hybridStage == "league",
                                        onClick = { hybridStage = "league" },
                                        text = { Text("League Phase (36 Teams)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                    )
                                    Tab(
                                        selected = hybridStage == "knockout",
                                        onClick = { hybridStage = "knockout" },
                                        text = { Text("Knockout Bracket", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                    )
                                }

                                if (hybridStage == "knockout") {
                                    KnockoutBracketView(
                                        matches = state.knockoutMatches,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                } else {
                                    if (state.standings.isEmpty()) {
                                        EmptyState(
                                            title = "No Standings Available",
                                            description = "League phase standings data is currently unavailable."
                                        )
                                    } else {
                                        StandingsTableHeader(modifier = Modifier.padding(vertical = 6.dp))

                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            contentPadding = PaddingValues(bottom = 8.dp)
                                        ) {
                                            items(
                                                items = state.standings,
                                                key = { "${it.competitionId}-${it.teamId}-${it.position}" }
                                            ) { standing ->
                                                StandingRow(standing = standing, totalTeams = state.standings.size)
                                            }
                                        }

                                        StandingsLegend(modifier = Modifier.padding(top = 4.dp, bottom = bottomLegendPadding))
                                    }
                                }
                            }
                        } else {
                            if (state.standings.isEmpty()) {
                                EmptyState(
                                    title = "No Standings Available",
                                    description = "Standings data is currently unavailable for this league."
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = horizontalPadding)
                                ) {
                                    // Fixed Table Header
                                    StandingsTableHeader(modifier = Modifier.padding(vertical = 6.dp))

                                    // Scrollable Club Items List taking full available height
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        contentPadding = PaddingValues(bottom = 8.dp)
                                    ) {
                                        items(
                                            items = state.standings,
                                            key = { "${it.competitionId}-${it.teamId}-${it.position}" }
                                        ) { standing ->
                                            StandingRow(standing = standing, totalTeams = state.standings.size)
                                        }
                                    }

                                    // Fixed Legend at bottom
                                    StandingsLegend(modifier = Modifier.padding(top = 4.dp, bottom = bottomLegendPadding))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
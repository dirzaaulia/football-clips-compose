package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.ui.score.components.KnockoutBracketView
import com.dirzaaulia.footballclips.ui.score.components.StandingRow
import com.dirzaaulia.footballclips.ui.score.components.StandingsLegend
import com.dirzaaulia.footballclips.ui.score.components.StandingsTableHeader

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
            topBar = {
                TopAppBar(
                    title = {
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
                                isCompact = !isLarge,
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = horizontalPadding),
                        ) {
                            LeagueDropdownSelector(
                                selectedCompetitionId = selectedCompetitionId ?: "PL",
                                dynamicCompetitions = dynamicCompetitions,
                                onLeagueSelected = onLeagueSelected,
                                showAll = false,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    ),
                )
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
                                } else {
                                    LeagueStandingsTable(
                                        standings = state.standings,
                                        bottomLegendPadding = bottomLegendPadding,
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
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LeagueStandingsTable(
    standings: List<StandingDto>,
    bottomLegendPadding: Dp,
) {
    if (standings.isEmpty()) {
        EmptyState(
            title = "No Standings Available",
            description = "Standings data is currently unavailable for this league.",
        )
    } else {
        StandingsTableHeader(modifier = Modifier.padding(vertical = 6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(
                items = standings,
                key = { "${it.competitionId}-${it.teamId}-${it.position}" },
            ) { standing ->
                StandingRow(standing = standing, totalTeams = standings.size)
            }
        }

        StandingsLegend(modifier = Modifier.padding(top = 4.dp, bottom = bottomLegendPadding))
    }
}
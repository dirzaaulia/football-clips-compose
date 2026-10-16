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
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.filter.LeagueDropdownSelector
import com.dirzaaulia.footballclips.ui.score.components.StandingRow
import com.dirzaaulia.footballclips.ui.score.components.StandingsLegend
import com.dirzaaulia.footballclips.ui.score.components.StandingsTableHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    state: StandingsState,
    isPremium: Boolean,
    selectedCompetitionId: String? = null,
    onLeagueSelected: (Int?, String?) -> Unit = { _, _ -> },
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        Text(
                            text = "Standings",
                            style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    },
                    actions = {
                        LeagueDropdownSelector(
                            selectedCompetitionId = selectedCompetitionId ?: "PL",
                            onLeagueSelected = onLeagueSelected,
                            showAll = false,
                            modifier = Modifier.padding(end = horizontalPadding)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
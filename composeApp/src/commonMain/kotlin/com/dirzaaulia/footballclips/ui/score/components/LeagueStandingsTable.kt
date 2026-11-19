package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.common.EmptyState

@Composable
fun ColumnScope.LeagueStandingsTable(
    standings: List<StandingDto>,
    bottomLegendPadding: Dp,
    selectedCompetitionId: String? = null,
    isAdsRemoved: Boolean = false,
) {
    if (standings.isEmpty()) {
        EmptyState(
            title = "No Standings Available",
            description = "Standings data is currently unavailable for this league.",
        )
    } else {
        var sortCol by remember { mutableStateOf("POS") }
        var isAsc by remember { mutableStateOf(true) }

        val sortedList = remember(standings, sortCol, isAsc) {
            when (sortCol) {
                "W" -> if (isAsc) standings.sortedBy { it.won } else standings.sortedByDescending { it.won }
                "L" -> if (isAsc) standings.sortedBy { it.lost } else standings.sortedByDescending { it.lost }
                "GD" -> if (isAsc) standings.sortedBy { it.goalDifference } else standings.sortedByDescending { it.goalDifference }
                "PTS" -> if (isAsc) standings.sortedBy { it.points } else standings.sortedByDescending { it.points }
                else -> if (isAsc) standings.sortedBy { it.position } else standings.sortedByDescending { it.position }
            }
        }

        StandingsTableHeader(
            modifier = Modifier.padding(vertical = 6.dp),
            activeSort = sortCol,
            isAsc = isAsc,
            onSortClick = { clicked ->
                if (sortCol == clicked) {
                    isAsc = !isAsc
                } else {
                    sortCol = clicked
                    isAsc = (clicked == "POS")
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(
                items = sortedList,
                key = { "${it.competitionId}-${it.teamId}-${it.position}" },
            ) { standing ->
                StandingRow(
                    standing = standing,
                    totalTeams = standings.size,
                    competitionId = selectedCompetitionId,
                )
            }
        }

        if (!isAdsRemoved && standings.isNotEmpty()) {
            BannerAdItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                style = NativeAdStyle.STANDINGS,
            )
        }

        StandingsLegend(
            competitionId = selectedCompetitionId,
            totalTeams = standings.size,
            modifier = Modifier.padding(top = 4.dp, bottom = bottomLegendPadding),
        )
    }
}

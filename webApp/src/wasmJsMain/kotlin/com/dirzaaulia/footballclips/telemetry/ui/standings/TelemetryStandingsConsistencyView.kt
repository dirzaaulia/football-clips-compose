package com.dirzaaulia.footballclips.telemetry.ui.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStandingRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import kotlinx.coroutines.launch

@Composable
fun TelemetryStandingsConsistencyView(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedComp by remember { mutableStateOf("PL") }
    var selectedSeason by remember { mutableStateOf(2026) }
    var standings by remember { mutableStateOf<List<TelemetryStandingRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val competitions = listOf(
        "PL" to "Premier League",
        "PD" to "La Liga",
        "SA" to "Serie A",
        "BL1" to "Bundesliga",
        "FL1" to "Ligue 1",
        "CL" to "Champions League",
        "SPL" to "Saudi Pro League",
        "MLS" to "MLS",
        "DED" to "Eredivisie",
        "PPL" to "Liga Portugal",
    )

    val loadStandings: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.getStandings(selectedComp, selectedSeason)
                .onSuccess { data ->
                    standings = data.distinctBy { it.teamId ?: it.id }
                    isLoading = false
                }
                .onFailure { err ->
                    errorMessage = err.message ?: "Failed to fetch standings from edge proxy"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(selectedComp, selectedSeason) {
        loadStandings()
    }

    val discrepancies = remember(standings) {
        standings.filter { team ->
            val expectedPts = (team.won * 3) + team.draw
            val expectedGd = team.goalsFor - team.goalsAgainst
            team.points != expectedPts || team.goalDifference != expectedGd
        }
    }

    val isConsistent = discrepancies.isEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StandingsMetricCards(
            isConsistent = isConsistent,
            discrepanciesCount = discrepancies.size,
            standingsCount = standings.size,
        )

        StandingsFilterAndAuditBar(
            competitions = competitions,
            selectedComp = selectedComp,
            onCompSelect = { selectedComp = it },
            selectedSeason = selectedSeason,
            onSeasonToggle = { selectedSeason = if (selectedSeason == 2026) 2025 else 2026 },
            onAuditClick = loadStandings,
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = StitchPrimary, modifier = Modifier.size(24.dp))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchError, RoundedCornerShape(8.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(errorMessage ?: "", color = StitchError, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else if (standings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("No standings records found for this league", color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            TelemetryStandingsTable(standings = standings)
        }
    }
}

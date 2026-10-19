package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import kotlinx.coroutines.launch

@Composable
fun TelemetryCatalogView(
    repository: TelemetryRepository,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<DatabaseMatchRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCompetition by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var highlightsOnly by remember { mutableStateOf(false) }

    val loadMatches: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.getSyncedMatches(limit = 150)
                .onSuccess { data ->
                    matches = data
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Failed to fetch matches from edge proxy"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        loadMatches()
    }

    val competitions = listOf(
        "ALL", "PL", "PD", "SA", "BL1", "FL1", "CL", "EL", "ECL",
        "FAC", "EFL", "CDR", "DFB", "CIT", "SPL", "MLS", "DED", "PPL",
    )
    val statuses = listOf("ALL", "FINISHED", "IN_PLAY", "TIMED", "SCHEDULED")

    val filteredMatches = remember(matches, searchQuery, selectedCompetition, selectedStatus, highlightsOnly) {
        matches.filter { match ->
            val compMatches = if (selectedCompetition == "ALL") true else {
                match.competitionId.equals(selectedCompetition, ignoreCase = true) ||
                    match.competitionName.contains(selectedCompetition, ignoreCase = true)
            }
            val statusMatches = if (selectedStatus == "ALL") true else {
                match.status.equals(selectedStatus, ignoreCase = true) ||
                    (selectedStatus == "IN_PLAY" && match.status in listOf("IN_PLAY", "PAUSED", "LIVE")) ||
                    (selectedStatus in listOf("TIMED", "SCHEDULED") && match.status in listOf("TIMED", "SCHEDULED"))
            }
            val highlightMatches = if (!highlightsOnly) true else {
                !match.highlightVideoId.isNullOrBlank()
            }
            val q = searchQuery.trim().lowercase()
            val searchMatches = if (q.isBlank()) true else {
                match.homeTeamName.lowercase().contains(q) ||
                    match.awayTeamName.lowercase().contains(q) ||
                    match.competitionName.lowercase().contains(q) ||
                    match.id.toString().contains(q) ||
                    (match.highlightVideoId?.lowercase()?.contains(q) == true)
            }
            compMatches && statusMatches && highlightMatches && searchMatches
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TelemetryCatalogStats(
            totalCount = matches.size,
            withHighlightCount = matches.count { !it.highlightVideoId.isNullOrBlank() },
            finishedCount = matches.count { it.status == "FINISHED" },
            inPlayCount = matches.count { it.status in listOf("IN_PLAY", "PAUSED", "LIVE") },
        )

        TelemetryCatalogFilters(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedCompetition = selectedCompetition,
            onCompetitionSelect = { selectedCompetition = it },
            competitions = competitions,
            selectedStatus = selectedStatus,
            onStatusSelect = { selectedStatus = it },
            statuses = statuses,
            highlightsOnly = highlightsOnly,
            onHighlightsOnlyToggle = { highlightsOnly = !highlightsOnly },
            onRefresh = loadMatches,
        )

        if (isLoading) {
            CatalogLoadingBox()
        } else if (errorMessage != null) {
            CatalogErrorBox(message = errorMessage ?: "Unknown Error", onRetry = loadMatches)
        } else if (filteredMatches.isEmpty()) {
            CatalogEmptyBox()
        } else {
            CatalogTableContent(
                matches = filteredMatches,
                onOpenVideo = onOpenVideo,
            )
        }
    }
}

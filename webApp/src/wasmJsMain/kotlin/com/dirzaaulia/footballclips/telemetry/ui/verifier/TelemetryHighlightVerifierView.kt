package com.dirzaaulia.footballclips.telemetry.ui.verifier

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import kotlinx.coroutines.launch

@Composable
fun TelemetryHighlightVerifierView(
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
    var filterMode by remember { mutableStateOf("LINKED") }

    val loadMatches: () -> Unit = {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val hasHl = when (filterMode) {
                "LINKED" -> true
                "MISSING" -> false
                else -> null
            }
            val st = if (filterMode == "MISSING") "FINISHED" else null
            repository.getSyncedMatches(limit = 150, hasHighlight = hasHl, status = st)
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

    LaunchedEffect(filterMode) {
        loadMatches()
    }

    val competitions = listOf(
        "ALL", "PL", "PD", "SA", "BL1", "FL1", "CL", "EL", "ECL",
        "FAC", "EFL", "CDR", "DFB", "CIT", "SPL", "MLS", "DED", "PPL",
    )

    val filteredHighlights = remember(matches, searchQuery, selectedCompetition, filterMode) {
        matches.filter { match ->
            val statusPass = when (filterMode) {
                "LINKED" -> !match.highlightVideoId.isNullOrBlank()
                "MISSING" -> match.status == "FINISHED" && match.highlightVideoId.isNullOrBlank()
                else -> true
            }
            if (!statusPass) return@filter false

            val compPass = if (selectedCompetition == "ALL") true else {
                match.competitionId.equals(selectedCompetition, ignoreCase = true) ||
                    match.competitionName.contains(selectedCompetition, ignoreCase = true)
            }
            if (!compPass) return@filter false

            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) true else {
                match.homeTeamName.lowercase().contains(q) ||
                    match.awayTeamName.lowercase().contains(q) ||
                    match.competitionName.lowercase().contains(q) ||
                    (match.highlightVideoId?.lowercase()?.contains(q) == true)
            }
        }
    }

    val totalScraped = matches.count { !it.highlightVideoId.isNullOrBlank() }
    val totalFinished = matches.count { it.status == "FINISHED" }
    val coveragePct = if (totalFinished > 0) (totalScraped * 100 / totalFinished) else 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TelemetryVerifierStats(
            totalScraped = totalScraped,
            totalFinished = totalFinished,
            coveragePct = coveragePct,
        )

        TelemetryVerifierFilterBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            filterMode = filterMode,
            onToggleFilterMode = {
                filterMode = when (filterMode) {
                    "LINKED" -> "MISSING"
                    "MISSING" -> "ALL"
                    else -> "LINKED"
                }
            },
            selectedCompetition = selectedCompetition,
            onCompetitionSelect = { selectedCompetition = it },
            competitions = competitions,
            onRefresh = loadMatches,
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
        } else if (filteredHighlights.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("No highlight records match active criteria", color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VERIFYING ${filteredHighlights.size} MATCH RECORD(S)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                filteredHighlights.forEach { match ->
                    HighlightVerificationCard(
                        match = match,
                        onOpenVideo = onOpenVideo,
                    )
                }
            }
        }
    }
}

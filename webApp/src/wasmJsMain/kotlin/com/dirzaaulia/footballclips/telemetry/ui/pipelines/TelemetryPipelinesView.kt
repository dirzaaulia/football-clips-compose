package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.model.PipelineCategory
import com.dirzaaulia.footballclips.telemetry.model.PipelineStepStage
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics
import com.dirzaaulia.footballclips.telemetry.ui.catalog.TelemetryFixtureListView
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetryPipelinesView(
    logs: List<LogStreamItem>,
    highlightMatches: List<ParsedMatch> = emptyList(),
    metrics: TelemetryMetrics,
    onInspectStep: (LogStreamItem) -> Unit,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf(PipelineCategory.HIGHLIGHTS) }
    var selectedLeague by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val allSteps = remember(logs) { TelemetryPipelineMapper.extractPipelineSteps(logs) }
    val retentionSummary = remember(logs) { TelemetryPipelineMapper.extractRetentionSummary(logs) }

    val filteredSteps = remember(allSteps, selectedCategory, selectedLeague, searchQuery) {
        allSteps.filter { step ->
            val matchesCategory = when (selectedCategory) {
                PipelineCategory.ALL -> true
                PipelineCategory.HIGHLIGHTS -> step.stage in listOf(
                    PipelineStepStage.SEARCHING, PipelineStepStage.CANDIDATES,
                    PipelineStepStage.AI_EVALUATION, PipelineStepStage.MATCHED,
                    PipelineStepStage.LINKED, PipelineStepStage.NOT_FOUND,
                    PipelineStepStage.QUOTA_ERROR,
                )
                PipelineCategory.FIXTURES -> step.id.startsWith("match_") || step.stage in listOf(
                    PipelineStepStage.FIXTURE_INGESTED, PipelineStepStage.NEW_FIXTURE,
                    PipelineStepStage.LIVE_GOAL, PipelineStepStage.MATCH_FINISHED,
                )
                PipelineCategory.RETENTION -> step.stage in listOf(
                    PipelineStepStage.RETENTION_CLEAN, PipelineStepStage.RETENTION_PURGED,
                )
            }

            val matchesLeague = selectedLeague == "ALL" || step.league.equals(selectedLeague, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                step.fixtureName.contains(searchQuery, ignoreCase = true) ||
                step.detailMessage.contains(searchQuery, ignoreCase = true) ||
                step.league.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesLeague && matchesSearch
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PipelineHeaderSection(eventCount = filteredSteps.size)

        if (selectedCategory == PipelineCategory.RETENTION || selectedCategory == PipelineCategory.ALL) {
            RetentionMonitorCard(summary = retentionSummary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PipelineCategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
            )

            PipelineSearchField(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
            )
        }

        if (selectedCategory == PipelineCategory.FIXTURES || selectedCategory == PipelineCategory.ALL) {
            val leagues = listOf("ALL", "Premier League", "LaLiga", "Serie A", "Bundesliga", "Ligue 1", "Champions League")
            PipelineLeagueFilterRow(
                leagues = leagues,
                selectedLeague = selectedLeague,
                onLeagueSelect = { selectedLeague = it },
            )
        }

        when (selectedCategory) {
            PipelineCategory.HIGHLIGHTS -> {
                TelemetryHighlightPipelineView(
                    matches = highlightMatches,
                    searchQuery = searchQuery,
                )
            }
            PipelineCategory.FIXTURES -> {
                TelemetryFixtureListView(
                    logs = logs,
                    searchQuery = searchQuery,
                    selectedLeague = selectedLeague,
                    onOpenVideo = onOpenVideo,
                )
            }
            PipelineCategory.RETENTION, PipelineCategory.ALL -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(580.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredSteps, key = { it.id }) { step ->
                        PipelineStepCard(
                            step = step,
                            onInspect = { step.rawLogItem?.let(onInspectStep) },
                            onOpenVideo = onOpenVideo,
                        )
                    }
                    if (filteredSteps.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AccountTree, null, tint = StitchPrimary, modifier = Modifier.size(28.dp))
                                    Text("No pipeline activity found matching the selected filters.", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text("Activity will log as worker cycles scrape highlights and sync schedules.", color = StitchTextMuted, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

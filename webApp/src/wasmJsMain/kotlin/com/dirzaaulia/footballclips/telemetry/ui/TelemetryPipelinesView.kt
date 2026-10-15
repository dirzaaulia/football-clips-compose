package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.*

@Composable
fun TelemetryPipelinesView(
    logs: List<LogStreamItem>,
    highlightMatches: List<ParsedMatch> = emptyList(),
    metrics: TelemetryMetrics,
    onInspectStep: (LogStreamItem) -> Unit,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier
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
                    PipelineStepStage.QUOTA_ERROR
                )
                PipelineCategory.FIXTURES -> step.id.startsWith("match_") || step.stage in listOf(
                    PipelineStepStage.FIXTURE_INGESTED, PipelineStepStage.NEW_FIXTURE,
                    PipelineStepStage.LIVE_GOAL, PipelineStepStage.MATCH_FINISHED
                )
                PipelineCategory.RETENTION -> step.stage in listOf(
                    PipelineStepStage.RETENTION_CLEAN, PipelineStepStage.RETENTION_PURGED
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Clip & Match Pipeline Execution Monitor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Granular step-by-step trace of YouTube scraping, AI reranking, fixture ingestion, and 30-day retention purges.",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchPrimary.copy(alpha = 0.15f))
                    .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("${filteredSteps.size} Pipeline Events", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // 30-Day Retention Policy Monitor (visible in Retention & All views)
        if (selectedCategory == PipelineCategory.RETENTION || selectedCategory == PipelineCategory.ALL) {
            RetentionMonitorCard(summary = retentionSummary)
        }

        // Sub-Pipeline Category Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PipelineCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StitchPrimary.copy(alpha = 0.15f) else StitchSurface)
                            .border(1.dp, if (isSelected) StitchPrimary else StitchBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StitchPrimary else StitchTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter fixture or club...", fontSize = 11.sp, color = StitchTextMuted) },
                    singleLine = true,
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = StitchTextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    } else null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )
            }
        }

        // League Filter Pills (for Fixtures & All tabs)
        if (selectedCategory == PipelineCategory.FIXTURES || selectedCategory == PipelineCategory.ALL) {
            val leagues = listOf("ALL", "Premier League", "LaLiga", "Serie A", "Bundesliga", "Ligue 1", "Champions League")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(leagues) { l ->
                    val isSel = selectedLeague == l
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) StitchSecondary.copy(alpha = 0.15f) else StitchSurfaceContainerHigh)
                            .border(1.dp, if (isSel) StitchSecondary else StitchBorder, RoundedCornerShape(4.dp))
                            .clickable { selectedLeague = l }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(l, fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) StitchSecondary else StitchTextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Sub-Pipeline View Switching
        when (selectedCategory) {
            PipelineCategory.HIGHLIGHTS -> {
                TelemetryHighlightPipelineView(
                    matches = highlightMatches,
                    searchQuery = searchQuery
                )
            }
            PipelineCategory.FIXTURES -> {
                TelemetryFixtureListView(
                    logs = logs,
                    searchQuery = searchQuery,
                    selectedLeague = selectedLeague,
                    onOpenVideo = onOpenVideo
                )
            }
            PipelineCategory.RETENTION, PipelineCategory.ALL -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(580.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSteps, key = { it.id }) { step ->
                        PipelineStepCard(
                            step = step,
                            onInspect = { step.rawLogItem?.let(onInspectStep) },
                            onOpenVideo = onOpenVideo
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

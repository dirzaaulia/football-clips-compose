package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun PipelinePicker(state: MonitorState, action: (MonitorAction) -> Unit) {
    val pipelines = listOf("fixtures", "youtube", "youtube_upload")
    val selectedIndex = pipelines.indexOf(state.filter.pipeline).coerceAtLeast(0)
    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
    ) {
        pipelines.forEachIndexed { index, pipeline ->
            Tab(
                selected = selectedIndex == index,
                onClick = { action(MonitorAction.Filter(state.filter.copy(pipeline = pipeline))) },
                text = {
                    Text(
                        text = label(pipeline),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                    )
                },
            )
        }
    }
}

@Composable
internal fun CycleList(state: MonitorState, action: (MonitorAction) -> Unit) {
    Panel(label("cycles")) {
        if (state.cycles.items.isEmpty()) Text(label("no_cycles"))
        state.cycles.items.forEach { cycle ->
            TextButton(onClick = { action(MonitorAction.Navigate("pipelines")) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                        Text(
                            label(cycle.pipeline) + " #" + cycle.sequence,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Badge(cycle.mode)
                    }
                    Text(displayTime(cycle.time), style = MaterialTheme.typography.bodySmall)
                    if (cycle.error.isNotBlank()) Text(cycle.error, color = MaterialTheme.colorScheme.error)
                }
                Badge(cycle.pending)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
internal fun MatchList(state: MonitorState, action: (MonitorAction) -> Unit) {
    Panel(label(if (state.catalogMode.isNotBlank()) "matches_result" else "matches")) {
        if (state.matches.items.isEmpty()) Text(label("no_matches"))
        state.matches.items.filter { state.match == null || it.matchId == state.match.matchId }.forEach { match ->
            TextButton(onClick = { action(MonitorAction.SelectMatch(match)) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
                    Text(match.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        match.competition + " · " + displayTime(match.date),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(match.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Badge(match.outcome)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

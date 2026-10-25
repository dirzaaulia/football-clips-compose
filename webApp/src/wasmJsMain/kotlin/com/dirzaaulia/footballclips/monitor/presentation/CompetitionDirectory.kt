package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dirzaaulia.footballclips.monitor.domain.Competition
import com.dirzaaulia.footballclips.monitor.domain.SourceChannel

@Composable
internal fun CompetitionDirectory(state: MonitorState, action: (MonitorAction) -> Unit, modifier: Modifier) {
    var format by remember { mutableStateOf("") }
    val competitions = state.competitions.filter {
        (format.isBlank() || it.format == format) && (
            state.filter.competition.isBlank() ||
                it.name.contains(state.filter.competition, true) || it.code.equals(state.filter.competition, true)
            )
    }
    Column(modifier) {
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            listOf("", "league", "cup", "hybrid", "unknown").forEach { value ->
                FilterChip(format == value, { format = value }, label = {
                    Text(
                        workspaceLabel(
                            if (value.isBlank()) {
                                "all_formats"
                            } else if (value == "unknown") {
                                "unknown_format"
                            } else {
                                value
                            },
                        ),
                    )
                })
            }
        }
        val scroll =
            workspaceScroll(contextKey(state) + ":competitions:" + format, competitions.map { it.code }, state.loading)
        if (state.loading && state.competitions.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(Space.medium))
        } else if (competitions.isEmpty()) {
            Panel("Competitions Directory") {
                Text(
                    text =
                    if (format.isNotBlank()) {
                        "No competitions found matching '$format' format."
                    } else {
                        "No competitions configured."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                state = scroll,
                verticalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                items(competitions, key = { it.code }) { competition -> CompetitionCard(competition, state, action) }
            }
        }
    }
}

@Composable
private fun CompetitionCard(competition: Competition, state: MonitorState, action: (MonitorAction) -> Unit) {
    val id = "source:" + competition.code
    Panel(competition.name) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
            RemoteMark(competition.emblem, competition.name, Modifier.size(Space.touch))
            Column(Modifier.weight(1f)) {
                Badge(
                    workspaceLabel("format") + " · " +
                        workspaceLabel(if (competition.format == "unknown") "unknown_format" else competition.format),
                )
                Text(
                    competition.formatDescription + " · " + competition.formatSeason,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(displayTime(competition.lastSync), style = MaterialTheme.typography.labelMedium)
            }
            Badge(label(if (competition.approved) "approved" else "pending"))
        }
        TechnicalDetails(competition)
        TextButton(onClick = { action(MonitorAction.Expand(id)) }) {
            Chevron(id in state.expanded)
            Text(workspaceLabel("sources_directory"))
        }
        if (id in state.expanded) SourceRows(competition.code, state)
    }
}

@Composable
private fun TechnicalDetails(competition: Competition) {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = !open }) {
        Chevron(open)
        Text(workspaceLabel("technical"))
    }
    if (open) {
        Text(competition.code + " · " + competition.provider, style = MaterialTheme.typography.bodySmall)
        Text(competition.formatEvidence, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SourceRows(code: String, state: MonitorState) {
    Text(workspaceLabel("approved_sources_only"), style = MaterialTheme.typography.bodySmall)
    state.sourceChannels[code]?.groupBy { it.kind }?.forEach { (kind, sources) ->
        Text(kind.replace('_', ' '), style = MaterialTheme.typography.titleSmall)
        sources.forEach { SourceRow(it) }
    }
    state.branchErrors["source:" + code]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun SourceRow(source: SourceChannel) {
    NestedSurface(1) {
        Text(source.name + " · " + source.handle, style = MaterialTheme.typography.titleSmall)
        Detail(workspaceLabel("source_identity"), source.identity)
        Detail(workspaceLabel("source_approval"), label(if (source.approved) "approved" else "pending"))
        Detail(
            workspaceLabel("highlight_rights"),
            if (source.rights ==
                "unknown"
            ) {
                workspaceLabel("rights_unknown")
            } else {
                source.rights
            },
        )
        if (source.territories.isNotBlank()) Detail(workspaceLabel("territories"), source.territories)
        if (source.evidence.isNotBlank()) Detail(workspaceLabel("recorded_evidence"), source.evidence)
        Text(source.mapping, style = MaterialTheme.typography.labelMedium)
        if (source.verified.isNotBlank()) Text(displayTime(source.verified))
    }
}

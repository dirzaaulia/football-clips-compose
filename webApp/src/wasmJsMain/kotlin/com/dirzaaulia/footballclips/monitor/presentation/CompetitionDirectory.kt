package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.small)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            listOf("", "league", "cup", "hybrid", "unknown").forEach { value ->
                val labelText = if (value.isBlank()) "All Formats" else value.replaceFirstChar { it.uppercase() }
                FilterChip(
                    selected = format == value,
                    onClick = { format = value },
                    label = { Text(labelText) },
                )
            }
        }
        val scroll = workspaceScroll(
            contextKey(state) + ":competitions:" + format,
            competitions.map { it.code },
            state.loading,
        )
        if (state.loading && state.competitions.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(Space.medium))
        } else if (competitions.isEmpty()) {
            Panel("Competitions Directory") {
                Text(
                    text = if (format.isNotBlank()) "No competitions found matching '$format' format." else "No competitions configured.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scroll,
                verticalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                items(competitions, key = { it.code }) { competition ->
                    CompetitionCard(competition, state, action)
                }
            }
        }
    }
}

@Composable
private fun CompetitionCard(competition: Competition, state: MonitorState, action: (MonitorAction) -> Unit) {
    val id = "source:" + competition.code
    val formatColor = when (competition.format.lowercase()) {
        "league" -> Color(0xFF3B82F6)
        "cup" -> Color(0xFFF59E0B)
        "hybrid" -> Color(0xFF8B5CF6)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(Space.medium),
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.medium),
            ) {
                RemoteMark(competition.emblem, competition.name, Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = competition.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = competition.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Text(
                        text = "${competition.formatDescription} · ${competition.formatSeason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(formatColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, formatColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = competition.format.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = formatColor,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Last sync: " + displayTime(competition.lastSync),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (competition.approved) Color(0xFF22C55E).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (competition.approved) "Approved" else "Pending Review",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (competition.approved) Color(0xFF22C55E) else Color(0xFFF59E0B),
                    )
                }
            }

            TechnicalDetails(competition)

            TextButton(
                onClick = { action(MonitorAction.Expand(id)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Chevron(id in state.expanded)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Channels & Rights Sources (${competition.sources})")
            }
            if (id in state.expanded) SourceRows(competition.code, state)
        }
    }
}

@Composable
private fun TechnicalDetails(competition: Competition) {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = !open }) {
        Chevron(open)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Technical Mapping & Data Provider")
    }
    if (open) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                .padding(Space.small),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Code: ${competition.code} · Provider: ${competition.provider}", style = MaterialTheme.typography.bodySmall)
            if (competition.formatEvidence.isNotBlank()) {
                Text(competition.formatEvidence, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SourceRows(code: String, state: MonitorState) {
    state.sourceChannels[code]?.groupBy { it.kind }?.forEach { (kind, sources) ->
        Text(kind.replace('_', ' ').uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        sources.forEach { SourceRow(it) }
    }
    state.branchErrors["source:$code"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun SourceRow(source: SourceChannel) {
    NestedSurface(1) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(source.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Badge(source.handle)
        }
        Detail("Approval", if (source.approved) "Approved" else "Pending")
        Detail("Highlight Rights", if (source.rights == "unknown") "Not Verified" else source.rights)
        if (source.territories.isNotBlank()) Detail("Territories", source.territories)
        if (source.evidence.isNotBlank()) Detail("Evidence", source.evidence)
        if (source.verified.isNotBlank()) Text(displayTime(source.verified), style = MaterialTheme.typography.labelSmall)
    }
}

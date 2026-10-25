package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.MatchRun

@Composable
internal fun MatchHeader(match: MatchRun, state: MonitorState, action: (MonitorAction) -> Unit) {
    val id = "match:" + match.id.ifBlank { "skip:" + match.matchId }
    val isExpanded = id in state.expanded
    Card(
        onClick = { action(MonitorAction.SelectMatch(match)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Chevron(isExpanded)
            MatchHeaderInfo(match = match, modifier = Modifier.weight(1f))
            MatchOutcomeBadge(match.outcome)
            val matchLogToCopy = "${match.title} [${match.competition}]\nKickoff: ${displayTime(
                match.date,
            )}\nStatus/Outcome: ${match.outcome}\nEvaluation/Reason: ${match.reason}\nNext Check: ${match.next}"
            CopyButton(textToCopy = matchLogToCopy)
        }
    }
}

@Composable
private fun MatchHeaderInfo(match: MatchRun, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SignalIcon(kind = match.outcome, modifier = Modifier.size(16.dp))
            Text(
                text = match.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            CompetitionChip(match.competition)
        }
        Text(
            text = displayTime(match.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (match.reason.isNotBlank()) {
            Text(
                text = match.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompetitionChip(competition: String) {
    if (competition.isBlank()) return
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text(
            text = competition.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun MatchOutcomeBadge(outcome: String) {
    val cleanOutcome = outcome.replace('_', ' ').trim()
    EventStatusBadge(kind = cleanOutcome, text = cleanOutcome.ifBlank { "Recorded" })
}

@Composable
internal fun MatchSummary(match: MatchRun, state: MonitorState, action: (MonitorAction) -> Unit) {
    if (match.reason.isNotBlank()) {
        MatchEvaluationTraceBox(match.reason, match.outcome)
    }
    val isRejected = match.outcome.contains("reject", true) || match.reason.contains("reject", true)
    val coverage = state.coverages[match.matchId]
    if (!isRejected && coverage != null && coverage.countries.isNotEmpty()) {
        CoveragePanel(coverage, match.matchId)
    }
    state.correction?.takeIf { it.first == match.matchId }?.let { correction ->
        TextButton(onClick = {
            action(MonitorAction.Correct(correction.second, true, match.matchId))
        }) {
            Text(label("undo"))
        }
    }
}

@Composable
private fun MatchEvaluationTraceBox(reason: String, outcome: String) {
    val isRejected = outcome.contains("reject", true) || reason.contains("reject", true)
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Scraper Evaluation Trace & Decision Reason",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isRejected) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

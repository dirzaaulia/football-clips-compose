package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.LogLine

@Composable
internal fun CycleHeader(cycle: Cycle, expanded: Boolean, action: (MonitorAction) -> Unit) {
    val hasAttention = cycle.error.isNotBlank() || cycle.indicators.any {
        (it.kind == "error" || it.kind == "warning") && (it.count.toIntOrNull() ?: 0) > 0
    }

    Card(
        onClick = { action(MonitorAction.SelectCycle(cycle)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAttention) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
        border = BorderStroke(
            1.dp,
            if (hasAttention) Color(0xFFEF4444).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Chevron(expanded)
            CycleSummaryInfo(
                cycle = cycle,
                hasAttention = hasAttention,
                modifier = Modifier.weight(1f),
            )
            SignalIndicators(cycle.indicators, cycle.error)
            val logToCopy =
                "Cycle #${cycle.sequence} [${cycle.pipeline}] ID: ${cycle.id}\nStarted: ${displayTime(
                    cycle.time,
                )}\nMode: ${cycle.mode}\nPending: ${cycle.pending}\nError: ${cycle.error}\nIndicators: " +
                    cycle.indicators.joinToString { "${it.kind}: ${it.count}" }
            CopyButton(textToCopy = logToCopy)
        }
    }
}

@Composable
private fun CycleSummaryInfo(cycle: Cycle, hasAttention: Boolean, modifier: Modifier) {
    val isError = cycle.error.isNotBlank() || cycle.indicators.any {
        it.kind.contains("error", true) && (it.count.toIntOrNull() ?: 0) > 0
    }
    val isWarn = cycle.indicators.any {
        (it.kind.contains("warn", true) || it.kind.contains("reject", true)) && (it.count.toIntOrNull() ?: 0) > 0
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Icon(
                imageVector = if (isError) {
                    Icons.Rounded.Error
                } else if (isWarn) {
                    Icons.Rounded.Warning
                } else {
                    Icons.Rounded.CheckCircle
                },
                contentDescription = if (isError) {
                    "Error"
                } else if (isWarn) {
                    "Warning"
                } else {
                    "Success"
                },
                tint = if (isError) {
                    Color(0xFFEF4444)
                } else if (isWarn) {
                    Color(0xFFF59E0B)
                } else {
                    Color(0xFF22C55E)
                },
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "${label(cycle.pipeline)} #${cycle.sequence}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            if (hasAttention) {
                AttentionAlertPill(text = if (cycle.error.isNotBlank()) "Error" else "Issues")
            }
        }
        Text(
            text = displayTime(cycle.time),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AttentionAlertPill(text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Attention Needed",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFEF4444),
        )
    }
}

@Composable
internal fun Chevron(expanded: Boolean) {
    Icon(
        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
        contentDescription = workspaceLabel(if (expanded) "collapse" else "expand"),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun NestedSurface(depth: Int, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(start = Space.child * depth),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(Space.line, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(Space.small),
            verticalArrangement = Arrangement.spacedBy(Space.small),
            content = content,
        )
    }
}

@Composable
internal fun PipelineEventRow(event: LogLine) {
    val signalKind = event.indicator.ifBlank { event.code }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.small),
        verticalAlignment = Alignment.Top,
    ) {
        SignalIcon(kind = signalKind, modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                Text(
                    text = displayTime(event.time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = event.code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        val eventLogToCopy = "[${displayTime(event.time)}] [${event.level}] [${event.code}]: ${event.message}"
        CopyButton(textToCopy = eventLogToCopy)
    }
}

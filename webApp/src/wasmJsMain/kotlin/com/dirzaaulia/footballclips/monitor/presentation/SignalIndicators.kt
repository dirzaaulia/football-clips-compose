package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.SignalCount
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun SignalIndicators(signals: ImmutableList<SignalCount>, cycleError: String = "") {
    val activeSignals = signals.filter { it.count.toLongOrNull()?.let { count -> count > 0 } == true }
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (activeSignals.isEmpty()) {
            val isErr = cycleError.isNotBlank()
            EventStatusBadge(
                kind = if (isErr) "error" else "up_to_date",
                text = if (isErr) "Error: ${cycleError.take(24)}" else "Up to date (0 modifications)",
            )
        } else {
            activeSignals.forEach { signal ->
                val cleanKind = signal.kind.replace('_', ' ').trim()
                val text = "${formatSignalTitle(cleanKind)} ${signal.count}"
                EventStatusBadge(kind = signal.kind, text = text)
            }
        }
    }
}

private fun formatSignalTitle(kind: String): String {
    val clean = kind.lowercase()
    return when {
        clean.contains("error") || clean.contains("failed") -> "Error"
        clean.contains("warn") -> "Warn"
        clean.contains("reject") -> "Match Rejected"
        clean.contains("covered") -> "Already Covered"
        clean.contains("match found") || clean.contains("found") || clean.contains("linked") -> "Match Found"
        clean.contains("new fixture") || clean.contains("fixture") -> "New Fixture"
        clean.contains("live") || clean.contains("score") -> "Live Score"
        else -> kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Composable
internal fun SignalIcon(
    kind: String,
    description: String = workspaceLabel(kind + "_signal"),
    modifier: Modifier = Modifier.size(16.dp),
) {
    val (icon, color) = resolveSignalIconAndColor(kind) ?: return
    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = color,
        modifier = modifier,
    )
}

@Composable
internal fun EventStatusBadge(kind: String, text: String, modifier: Modifier = Modifier) {
    val (icon, color) = resolveSignalIconAndColor(kind)
        ?: (Icons.Rounded.Info to MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
internal fun resolveSignalIconAndColor(kind: String): Pair<ImageVector, Color>? {
    val clean = kind.lowercase().trim().removeSurrounding("[", "]").replace('_', ' ')
    return when {
        clean.contains("error") || clean.contains("failed") || clean.contains("fail") ||
            clean.contains("rejected") || clean.contains("reject") || clean.contains("critical") ||
            clean.contains("not found") -> Icons.Rounded.Cancel to Color(0xFFEF4444)

        clean.contains("warning") || clean.contains("warn") || clean.contains("alert") ||
            clean.contains("retry") || clean.contains("stale") || clean.contains("missing") ||
            clean.contains("blocked") || clean.contains("fallback") -> Icons.Rounded.Warning to Color(0xFFF59E0B)

        clean.contains("success") || clean.contains("linked") || clean.contains("accepted") ||
            clean.contains("verified") || clean.contains("passed") || clean.contains("full time") ||
            clean.contains("up to date") ->
            Icons.Rounded.CheckCircle to Color(0xFF22C55E)

        clean.contains("match found") || clean.contains("found") || clean.contains("candidate") ||
            clean.contains("discovery") || clean.contains("new fixture") || clean.contains("video") ->
            Icons.Rounded.PlayCircle to Color(0xFF0EA5E9)

        clean.contains("live") || clean.contains("score") || clean.contains("in play") ||
            clean.contains("kickoff") || clean.contains("active") ->
            Icons.Rounded.Radio to Color(0xFFF43F5E)

        clean.contains("scheduled") || clean.contains("standby") || clean.contains("cooldown") ||
            clean.contains("pending") || clean.contains("status update") ->
            Icons.Rounded.Schedule to Color(0xFF94A3B8)

        clean.contains("ai") || clean.contains("gemini") || clean.contains("process") ||
            clean.contains("info") ->
            Icons.Rounded.Info to Color(0xFF06B6D4)

        else -> null
    }
}

package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem

@Composable
fun TelemetryCycleStreamCard(
    cycle: CycleHistoryItem,
    logs: List<LogStreamItem>,
    onInspectCycle: (CycleHistoryItem) -> Unit,
    onInspectLog: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val hasErrors = cycle.exceptions > 0
    val statusColor = if (hasErrors) StitchError else StitchSuccess
    val isFullSync = cycle.executionMode.contains("FULL", true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, if (hasErrors) StitchError.copy(alpha = 0.5f) else StitchBorder, RoundedCornerShape(6.dp))
    ) {
        // Cycle Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurfaceContainer)
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Badges
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(7.dp).clip(CircleShape).background(statusColor)
                )
                Text(
                    text = "CYCLE #${cycle.id}",
                    style = TelemetryTypography.titleSmall.copy(color = StitchTextPrimary)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isFullSync) StitchSecondary.copy(alpha = 0.15f) else StitchBorder)
                        .border(1.dp, if (isFullSync) StitchSecondary.copy(alpha = 0.4f) else StitchBorderSubtle, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = cycle.executionMode,
                        style = TelemetryTypography.labelSmall.copy(color = if (isFullSync) StitchSecondary else StitchTextSecondary)
                    )
                }
                Text(
                    text = cycle.timestamp,
                    style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted)
                )
            }

            // Right Actions & Stats
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cycle.highlightsHarvested > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(StitchSuccess.copy(alpha = 0.15f))
                            .border(1.dp, StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${cycle.highlightsHarvested} LINKED",
                            style = TelemetryTypography.labelSmall.copy(color = StitchSuccess)
                        )
                    }
                }
                Text(
                    text = "${cycle.ingestedMatches} fixtures",
                    style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted)
                )

                Button(
                    onClick = { onInspectCycle(cycle) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchSurfaceContainerHigh,
                        contentColor = StitchPrimary
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                ) {
                    Text("Inspect Run", style = TelemetryTypography.labelSmall)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(13.dp))
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Toggle",
                        tint = StitchTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Expanded Console Lines
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchTerminalBg)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No log lines matching active filter in this cycle.",
                        style = TelemetryTypography.codeStream.copy(color = StitchTextMuted),
                        modifier = Modifier.padding(6.dp)
                    )
                } else {
                    logs.forEach { item ->
                        CycleLogLine(item = item, onInspect = { onInspectLog(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleLogLine(
    item: LogStreamItem,
    onInspect: () -> Unit
) {
    val subColor = TelemetryUtils.getSubsystemBadgeColor(item.subsystem)
    val msgColor = TelemetryUtils.getEventLineColor(item.cleanMessage)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onInspect)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = item.timestamp,
            style = TelemetryTypography.labelMedium.copy(color = StitchTextMuted)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(subColor.copy(alpha = 0.12f))
                .border(1.dp, subColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(item.subsystem, style = TelemetryTypography.labelMedium.copy(color = subColor))
        }
        Text(
            text = item.cleanMessage,
            style = TelemetryTypography.codeStream.copy(color = msgColor),
            softWrap = true,
            modifier = Modifier.weight(1f)
        )
        if (item.extractedUrl != null) {
            IconButton(
                onClick = { TelemetryUtils.openUrlInNewTab(item.extractedUrl) },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open", tint = StitchPrimary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

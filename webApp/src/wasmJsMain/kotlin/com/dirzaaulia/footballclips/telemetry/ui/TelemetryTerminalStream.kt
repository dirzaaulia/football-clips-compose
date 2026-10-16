package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem

@Composable
fun TelemetryTerminalStream(
    cycles: List<CycleHistoryItem>,
    logs: List<LogStreamItem>,
    isLoading: Boolean = false,
    searchQuery: String = "",
    onClearSearch: () -> Unit = {},
    onInspectCycle: (CycleHistoryItem) -> Unit,
    onInspectLog: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val logsByCycle = remember(logs) { logs.groupBy { it.cycleId } }
    val activeCycles = remember(cycles, logsByCycle, searchQuery) {
        if (searchQuery.isBlank()) cycles
        else cycles.filter { (logsByCycle[it.id]?.isNotEmpty() == true) }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchTerminalBg)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
    ) {
        // Window Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurface)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                }

                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Folder, null, tint = StitchTextMuted, modifier = Modifier.size(13.dp))
                Text(
                    text = "/dev/fc-worker/stream.log (Grouped by Cycle)",
                    style = TelemetryTypography.labelSmall.copy(color = StitchTextSecondary)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(StitchPrimary.copy(alpha = 0.12f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text("CYCLE STREAM", style = TelemetryTypography.labelSmall.copy(color = StitchPrimary))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(StitchSecondary.copy(alpha = 0.15f))
                            .border(1.dp, StitchSecondary.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SYNCING...",
                            style = TelemetryTypography.labelSmall.copy(color = StitchSecondary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    text = "${activeCycles.size} Cycles \u2022 ${logs.size} Events",
                    style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted)
                )
                Icon(Icons.Default.Block, null, tint = StitchTextMuted, modifier = Modifier.size(13.dp))
            }
        }

        // Monospace Console Rows Grouped by Cycle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(640.dp)
                .background(StitchTerminalBg)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeCycles, key = { it.id }) { cycle ->
                    val cycleLogs = (logsByCycle[cycle.id] ?: emptyList()).sortedBy { it.id }
                    TelemetryCycleStreamCard(
                        cycle = cycle,
                        logs = cycleLogs,
                        onInspectCycle = onInspectCycle,
                        onInspectLog = onInspectLog
                    )
                }

                if (activeCycles.isEmpty()) {
                    item {
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 56.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        strokeWidth = 3.dp,
                                        color = StitchPrimary
                                    )
                                    Text(
                                        text = "LOADING WORKER TELEMETRY & CYCLES...",
                                        style = TelemetryTypography.titleMedium.copy(
                                            color = StitchPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Connecting to Google Cloud Storage telemetry stream and parsing execution cycles...",
                                        style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = StitchPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No log events matching \"$searchQuery\"" else "No telemetry cycles or events found.",
                                        style = TelemetryTypography.titleMedium.copy(color = StitchTextPrimary)
                                    )
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No recorded worker cycles contain this search query." else "Events will appear once worker cycles start streaming.",
                                        style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted)
                                    )
                                if (searchQuery.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StitchPrimary.copy(alpha = 0.15f))
                                            .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .clickable { onClearSearch() }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Clear Search Filter",
                                            style = TelemetryTypography.labelSmall.copy(
                                                color = StitchPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Window Bottom Status Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurface)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                Text(
                    text = "Live Cycle Stream \u2022 Click 'Inspect Run' to view step-by-step accordions",
                    style = TelemetryTypography.labelSmall.copy(color = StitchTextSecondary)
                )
            }
            Text(
                text = "${cycles.size} Cycles Active",
                style = TelemetryTypography.labelSmall.copy(color = StitchPrimary)
            )
        }
    }
}

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem

@Composable
fun TelemetryCyclesView(
    cycles: List<CycleHistoryItem>,
    onInspectCycle: (CycleHistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "Sync Cycles & Pipeline Executions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Chronological record of worker execution cycles. Click any cycle to inspect its stages.",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchPrimary.copy(alpha = 0.12f))
                    .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("${cycles.size} Recorded Cycles", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Cycle Cards List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(620.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cycles, key = { it.id }) { cycle ->
                CycleExecutionCard(cycle = cycle, onClick = { onInspectCycle(cycle) })
            }
            if (cycles.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No execution cycles found.", color = StitchTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleExecutionCard(
    cycle: CycleHistoryItem,
    onClick: () -> Unit
) {
    val isFull = cycle.executionMode.contains("FULL", true)
    val modeColor = if (isFull) StitchSecondary else StitchTextSecondary
    val statusColor = if (cycle.exceptions > 0) StitchError else StitchSuccess

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Meta Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                Text(
                    text = "Cycle #${cycle.id}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(modeColor.copy(alpha = 0.15f))
                        .border(1.dp, modeColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(cycle.executionMode, fontSize = 8.sp, color = modeColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(cycle.timestamp, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = StitchTextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CycleStatPill("Ingested Matches", "${cycle.ingestedMatches} fixtures", StitchPrimary, Modifier.weight(1f))
            CycleStatPill("Harvested Clips", "${cycle.highlightsHarvested} videos", StitchSuccess, Modifier.weight(1f))
            CycleStatPill("API Quota Cost", "${cycle.apiQuotaCost} units", StitchWarning, Modifier.weight(1f))
            CycleStatPill("Database Status", cycle.dbStatus, StitchTextPrimary, Modifier.weight(1.2f))
        }

        // Pipeline Stages Mini Timeline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(StitchSurfaceContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StageIndicator("1. Fixtures Sync", StitchSuccess)
            Text("\u2192", color = StitchBorder, fontSize = 10.sp)
            StageIndicator("2. Highlight Scraper", StitchPrimary)
            Text("\u2192", color = StitchBorder, fontSize = 10.sp)
            StageIndicator("3. Standings", StitchSuccess)
            Text("\u2192", color = StitchBorder, fontSize = 10.sp)
            StageIndicator("4. Maintenance", StitchSuccess)
        }
    }
}

@Composable
private fun CycleStatPill(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 7.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 9.sp, color = valueColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
    }
}

@Composable
private fun StageIndicator(label: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotColor))
        Text(label, fontSize = 8.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
    }
}

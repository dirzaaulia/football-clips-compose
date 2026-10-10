package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem

@Composable
fun CycleInspectorBody(item: CycleHistoryItem) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cycle Execution Metadata", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorMetaRow("Execution Mode", item.executionMode, StitchSecondary)
            InspectorMetaRow("Timestamp (WIB)", item.timestamp, StitchTextPrimary)
            InspectorMetaRow("Ingested Matches", "${item.ingestedMatches} fixtures", StitchPrimary)
            InspectorMetaRow("Highlights Harvested", "${item.highlightsHarvested} video clips", StitchSuccess)
            InspectorMetaRow("API Quota Consumed", "${item.apiQuotaCost} units", StitchWarning)
            InspectorMetaRow("Database Status", item.dbStatus, StitchSuccess)
            InspectorMetaRow("Exceptions / Errors", "${item.exceptions} err", if (item.exceptions > 0) StitchError else StitchSuccess)
        }

        Text("Active Pipeline Stages Summary", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StageRowCard("1. Schedule & Team Sync", "Football-Data.org Ingested: ${item.ingestedMatches} fixtures", "OK", StitchSuccess)
            StageRowCard("2. YouTube Highlight Scraper", "Scraped ${item.highlightsHarvested} clips (Quota: ${item.apiQuotaCost} units)", "DISCOVERED", StitchPrimary)
            StageRowCard("3. League Standings Sync", "Synced top 5 European competitions", "OK", StitchSuccess)
            StageRowCard("4. Maintenance & Cache Purge", "Retention clean (<30 days active)", "CLEAN", StitchSuccess)
        }
    }
}

@Composable
fun LogInspectorBody(item: LogStreamItem) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Structured Event Attributes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorMetaRow("Event ID", item.id.toString(), StitchTextPrimary)
            InspectorMetaRow("Thread Worker", item.threadId, StitchSecondary)
            InspectorMetaRow("Subsystem", item.subsystem, StitchPrimary)
            InspectorMetaRow("Action Tag", item.action, StitchTextPrimary)
            InspectorMetaRow("Timestamp (WIB)", item.timestamp, StitchTextPrimary)
            InspectorMetaRow("Cycle Run ID", item.cycleId.toString(), StitchTextMuted)
        }

        item.extractedUrl?.let { url ->
            Text("Harvested YouTube Video", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(url, fontSize = 9.sp, color = StitchPrimary, fontFamily = FontFamily.Monospace, maxLines = 1, modifier = Modifier.weight(1f))
                Button(
                    onClick = { TelemetryUtils.openUrlInNewTab(url) },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary, contentColor = Color(0xFF002D6B)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Open Video", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open", modifier = Modifier.size(10.dp))
                }
            }
        }

        Text("Raw Execution Trace", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchTerminalBg).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(10.dp)
        ) {
            Text(
                text = item.rawText,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
fun InspectorMetaRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 9.sp, color = valueColor, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun StageRowCard(title: String, subtitle: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(4.dp)).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            Text(subtitle, fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(statusColor.copy(alpha = 0.12f)).border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(status, fontSize = 7.sp, color = statusColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

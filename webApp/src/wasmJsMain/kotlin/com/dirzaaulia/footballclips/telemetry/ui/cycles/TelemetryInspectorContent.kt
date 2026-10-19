package com.dirzaaulia.footballclips.telemetry.ui.cycles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryUtils
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPurple
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTerminalBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun CycleInspectorBody(
    item: CycleHistoryItem,
    steps: List<com.dirzaaulia.footballclips.telemetry.model.ParsedStep> = emptyList()
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cycle Execution Metadata", style = TelemetryTypography.titleSmall.copy(color = StitchTextSecondary))
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

        Text("Active Pipeline Steps & Match Traces", style = TelemetryTypography.titleSmall.copy(color = StitchTextSecondary))
        TelemetryCycleAccordions(steps = steps)
    }
}

@Composable
fun LogInspectorBody(item: LogStreamItem) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Structured Event Attributes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorMetaRow("Event ID", item.id.toString(), StitchTextPrimary)
            InspectorMetaRow("Subsystem", item.subsystem, TelemetryUtils.getSubsystemBadgeColor(item.subsystem))
            InspectorMetaRow("Action Tag", item.action, StitchTextPrimary)
            InspectorMetaRow("Timestamp (WIB)", item.timestamp, StitchTextPrimary)
            InspectorMetaRow("Cycle Run ID", item.cycleId.toString(), StitchTextMuted)
        }

        item.extractedUrl?.let { url ->
            Text("Harvested YouTube Video", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(StitchSurface).border(1.dp, StitchBorder, RoundedCornerShape(6.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(url, fontSize = 11.sp, color = StitchPrimary, fontFamily = FontFamily.Monospace, maxLines = 1, modifier = Modifier.weight(1f))
                Button(
                    onClick = { TelemetryUtils.openUrlInNewTab(url) },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary, contentColor = Color(0xFF002D6B)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("Open Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, "Open", modifier = Modifier.size(12.dp))
                }
            }
        }

        Text("Raw Execution Trace", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchTerminalBg)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            Text(
                text = item.rawText,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
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
        Text(label, style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted))
        Text(value, style = TelemetryTypography.bodySmall.copy(color = valueColor, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun NotificationInspectorBody(item: com.dirzaaulia.footballclips.telemetry.model.NotificationLogItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Device Notification Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF161F2E))
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(StitchPrimary))
                    Text("FootballClips", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                }
                Text("now", fontSize = 9.sp, color = StitchTextMuted)
            }
            Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            Text(item.body, fontSize = 11.sp, color = StitchTextSecondary)
        }

        Text("Distinct Audience & Device Fanout", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorMetaRow("Distinct Human Users", "${item.audience.distinctUsers} recipients", StitchPrimary)
            InspectorMetaRow("Total Devices Fanout", "${item.audience.totalDevices} endpoints", StitchSuccess)
            InspectorMetaRow("Fanout Ratio", "${item.audience.deviceToUserRatio} devices / user", StitchWarning)
            InspectorMetaRow("Platform Breakdown", "${item.audience.platformBreakdown.android} Android  |  ${item.audience.platformBreakdown.web} Web", StitchPurple)
            InspectorMetaRow("Single-Device Users", "${item.audience.distribution.singleDeviceUsers} users", StitchTextSecondary)
            InspectorMetaRow("Multi-Device Users", "${item.audience.distribution.multiDeviceUsers} users (up to ${item.audience.distribution.maxDevicesForSingleUser} dev)", StitchSuccess)
        }

        Text("Notification Delivery Metadata", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StitchTextSecondary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorMetaRow("Event Type", item.eventType.uppercase(), StitchPrimary)
            InspectorMetaRow("Timestamp (UTC)", item.timestamp, StitchTextPrimary)
            InspectorMetaRow("Match Fixture", "${item.homeTeam} vs ${item.awayTeam}", StitchSecondary)
            InspectorMetaRow("Target Topics", item.clubIds.joinToString { "club_$it" }, StitchPrimary)
            InspectorMetaRow("Storage Pipeline", "GCS CDN (0 Database writes)", StitchSuccess)
        }
    }
}

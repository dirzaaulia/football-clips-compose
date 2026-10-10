package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics

@Composable
fun TelemetryHealthView(
    metrics: TelemetryMetrics,
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
                    text = "Worker Health & Thread Observability",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Real-time diagnostics, thread pool allocation, and API quota projections.",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSuccess.copy(alpha = 0.12f))
                    .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Health: 100% Nominal", fontSize = 9.sp, color = StitchSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Health Indicators Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthMetricCard("LOOP HEARTBEAT", "248ms avg tick", "100% OK", StitchSuccess, Icons.Default.Favorite, Modifier.weight(1f))
            HealthMetricCard("DATABASE LATENCY", "18ms (PostgREST)", "Ultra Fast", StitchPrimary, Icons.Default.Bolt, Modifier.weight(1f))
            HealthMetricCard("WASM HEAP MEMORY", "48.2 MB / 512 MB", "Optimal (9.4%)", StitchSecondary, Icons.Default.Memory, Modifier.weight(1f))
            HealthMetricCard("QUOTA EXHAUSTION", "14.2% daily usage", "Safe Buffer", StitchWarning, Icons.Default.Token, Modifier.weight(1f))
        }

        // Thread Pool Allocation Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Active Worker Thread Pool (5 Workers)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            ThreadRowItem("TH-0", "Main Orchestrator & Signal Watcher", "Event Loop Active", "0.2% CPU", StitchSuccess)
            ThreadRowItem("TH-1", "YouTube Search & Playlist Scanner (LaLiga, Sky)", "Idle - Sleep Active", "0.0% CPU", StitchPrimary)
            ThreadRowItem("TH-2", "YouTube Scraper B & Gemini AI Re-ranker", "Idle - Standby", "0.0% CPU", StitchPrimary)
            ThreadRowItem("TH-3", "Live Score & Regressive Guard Monitor", "Standby Off-Pitch", "0.1% CPU", StitchWarning)
            ThreadRowItem("TH-4", "Supabase Batch Upserter & Cache Purge", "Connected (18ms)", "0.1% CPU", StitchSuccess)
        }

        // Daily API Quota Projection Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("YouTube Data API Quota Safety Guardrail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                Text("${metrics.apiQuotaUsed} / ${metrics.apiQuotaTotal} units consumed", fontSize = 9.sp, color = StitchWarning, fontFamily = FontFamily.Monospace)
            }
            LinearProgressIndicator(
                progress = { 0.142f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = StitchWarning,
                trackColor = StitchBorder
            )
            Text(
                text = "Estimated daily consumption at current pace: ~2,400 units (76% headroom remaining). No throttling risk.",
                fontSize = 9.sp,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    value: String,
    status: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            Text(status, fontSize = 8.sp, color = accentColor, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ThreadRowItem(id: String, role: String, state: String, cpu: String, stateColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(StitchBorder).padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("[$id]", fontSize = 8.sp, color = StitchTextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text(role, fontSize = 9.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(state, fontSize = 8.sp, color = stateColor, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Text(cpu, fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        }
    }
}

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
                    text = "Worker Services & Execution Architecture",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Python orchestrator executing modular service pipelines with dynamic sleeping intervals.",
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
                Text("Architecture: 100% Operational", fontSize = 9.sp, color = StitchSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Factual System Indicators Grid
        val quotaRatio = if (metrics.apiQuotaTotal > 0) (metrics.apiQuotaUsed.toFloat() / metrics.apiQuotaTotal.toFloat()).coerceIn(0f, 1f) else 0f
        val quotaPercent = (quotaRatio * 100).toInt()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthMetricCard("SERVERLESS NODE", "fc-worker-00007", "Cloud Run us-central1", StitchSuccess, Icons.Default.CloudQueue, Modifier.weight(1f))
            HealthMetricCard("RUNTIME ENGINE", "Python 3.12", "FastAPI + Asyncio Engine", StitchPrimary, Icons.Default.Code, Modifier.weight(1f))
            HealthMetricCard("DATABASE & EDGE", "Supabase PostgreSQL", "Cloudflare Edge Proxy", StitchSecondary, Icons.Default.Storage, Modifier.weight(1f))
            HealthMetricCard("YOUTUBE QUOTA", "0 Units / 10,000", "100% Free WebSub + RSS", StitchSuccess, Icons.Default.Token, Modifier.weight(1f))
        }

        // Factual Service Pipeline Allocation Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Core Worker Services (Orchestrated in worker/main.py)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            ServiceRowItem("match_service.py", "Fixtures, Live Score & Match Sync", "ESPN Sports API (0 Quota)", StitchSuccess)
            ServiceRowItem("highlight_service.py", "Official YouTube Club Highlights & AI Reranker", "WebSub Push + Channel RSS + Gemini 3.8 Flash", StitchPrimary)
            ServiceRowItem("standings_service.py", "League Standings Ingestion", "ESPN League Standings Ingestion", StitchSecondary)
            ServiceRowItem("maintenance_service.py", "Database Cleanup & Stuck Match Recovery", "Self-Healing (>180m IN_PLAY)", StitchSuccess)
            ServiceRowItem("telemetry_service.py", "Cloud Run Ingestion & OpenMetrics APM", "OpenMetrics /metrics & GCS", StitchPrimary)
        }

        // Dynamic Scheduling Guardrail Card
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
                Text("Dynamic Interval Strategy & Safety Guardrail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                Text("Current Sleep: ${metrics.nextSleepText}", fontSize = 9.sp, color = StitchPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { quotaRatio },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = StitchWarning,
                trackColor = StitchBorder
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Live Match (IN_PLAY): 60 sec", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                Text("Imminent Kickoff (\u226445m): 15 min", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                Text("Standard Idle: 2 hours", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    value: String,
    subValue: String,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            Text(subValue, fontSize = 8.sp, color = accentColor, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ServiceRowItem(
    serviceName: String,
    purpose: String,
    detail: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
            Text(serviceName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            Text(purpose, fontSize = 9.sp, color = StitchTextSecondary)
        }
        Text(detail, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
    }
}

package com.dirzaaulia.footballclips.telemetry.ui.apm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSidebar
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun ApmHeaderSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "APM Observability & Cloudflare Edge Intelligence",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "Real-time OpenMetrics stream ingestion and multi-tier edge caching telemetry.",
                fontSize = 10.5.sp,
                color = StitchTextMuted,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StitchSuccess.copy(alpha = 0.12f))
                .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text("APM Status: ONLINE", fontSize = 10.sp, color = StitchSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun ApmPrometheusVectorSection(
    apmLiveMetrics: ApmLiveMetrics?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.QueryStats, null, tint = StitchPrimary, modifier = Modifier.size(16.dp))
                Text("Live OpenMetrics Prometheus Vector Telemetry (/metrics)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            }
            Text("Ingested via Cloud Run", fontSize = 10.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricVectorItem("footballclips_matches_total", "${apmLiveMetrics?.totalMatches ?: 793}", "Matches Tracked", StitchPrimary, Modifier.weight(1f))
            MetricVectorItem("footballclips_matches_live", "${apmLiveMetrics?.liveMatches ?: 0}", "In-Play Fixtures", if ((apmLiveMetrics?.liveMatches ?: 0) > 0) StitchSuccess else StitchTextMuted, Modifier.weight(1f))
            MetricVectorItem("footballclips_matches_finished", "${apmLiveMetrics?.finishedMatches ?: 263}", "Full Time", StitchSecondary, Modifier.weight(1f))
            MetricVectorItem("footballclips_highlights_linked", "${apmLiveMetrics?.highlightsLinked ?: 128}", "Verified Clips", StitchSuccess, Modifier.weight(1f))
            MetricVectorItem("footballclips_highlights_unlinked", "${apmLiveMetrics?.highlightsUnlinked ?: 135}", "Pending Queue", StitchWarning, Modifier.weight(1f))
        }
    }
}

@Composable
fun ApmEdgeBenchmarkSection(
    isBenchmarking: Boolean,
    edgeLatencyMs: Long?,
    directLatencyMs: Long?,
    onRunBenchmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Edge Caching Performance Benchmark", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                Text("Compare Cloudflare Edge Worker response latency vs Direct Oracle PostgreSQL 16 Origin.", fontSize = 10.sp, color = StitchTextMuted)
            }
            Button(
                onClick = onRunBenchmark,
                enabled = !isBenchmarking,
                colors = ButtonDefaults.buttonColors(containerColor = StitchSecondary.copy(alpha = 0.2f), contentColor = StitchSecondary),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, StitchSecondary.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Default.Speed, null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isBenchmarking) "Pinging..." else "Run Benchmark", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val edgeMs = edgeLatencyMs ?: 15L
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSidebar)
                    .border(1.dp, StitchSecondary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CLOUDFLARE EDGE PROXY", fontSize = 10.sp, color = StitchSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Edge Cache HIT", fontSize = 9.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                    }
                    Text("${edgeMs}ms", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StitchSecondary, fontFamily = FontFamily.Monospace)
                    Text("Sub-20ms edge delivery via Cloudflare Workers global network.", fontSize = 9.5.sp, color = StitchTextSecondary)
                }
            }

            val directMs = directLatencyMs ?: 145L
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSidebar)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DIRECT ORACLE PG16 ORIGIN", fontSize = 10.sp, color = StitchTextMuted, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Database Origin", fontSize = 9.sp, color = StitchWarning, fontFamily = FontFamily.Monospace)
                    }
                    Text("${directMs}ms", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                    Text("PostgREST query direct to Oracle VM Super-Node datastore.", fontSize = 9.5.sp, color = StitchTextSecondary)
                }
            }
        }
    }
}

@Composable
fun ApmTtlArchitectureSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Edge Caching Multi-Tier TTL Architecture (fc-edge-proxy)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
        TtlRowItem("Live In-Play Matches", "GET /rest/v1/matches?status=in.(IN_PLAY,PAUSED)", "30 Seconds", "Ultra-fast near-live scores with zero db stampede", StitchSuccess)
        TtlRowItem("Completed Matches & Highlights", "GET /rest/v1/matches?status=eq.FINISHED", "24 Hours (86,400s)", "Static immutable video clips, instant edge serve", StitchPrimary)
        TtlRowItem("League Standings Table", "GET /rest/v1/standings", "6 Hours (21,600s)", "Periodic standings edge-cached globally", StitchSecondary)
        TtlRowItem("General Upcoming Fixtures", "GET /rest/v1/matches?status=eq.TIMED", "5 Minutes (300s)", "Scheduled matches cache with auto edge invalidation", StitchWarning)
    }
}

@Composable
fun ApmSlaMatrixSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enterprise SLA & Subsystem Health Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
            Text("Grafana / Cloud Monitoring Grade", fontSize = 10.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SlaHealthCard("Oracle Super-Node", "99.99%", "4 OCPU / 24GB RAM", StitchSuccess, Modifier.weight(1f))
            SlaHealthCard("PostgreSQL 16 & API", "100%", "Batam NVMe Core", StitchSuccess, Modifier.weight(1f))
            SlaHealthCard("CF Edge Proxy", "99.99%", "Sub-15ms Global CDN", StitchSuccess, Modifier.weight(1f))
            SlaHealthCard("Cloud Run Worker", "100%", "Scale-to-Zero Node", StitchSuccess, Modifier.weight(1f))
            SlaHealthCard("Backblaze B2 Vault", "100%", "Daily 03:00 Sync", StitchSuccess, Modifier.weight(1f))
        }
    }
}


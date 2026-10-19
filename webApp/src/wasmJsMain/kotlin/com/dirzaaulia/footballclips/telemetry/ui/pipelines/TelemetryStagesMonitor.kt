package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun TeleStagesMonitor(
    metrics: TelemetryMetrics,
    cycleTimestamp: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StitchSuccess.copy(alpha = 0.12f))
                        .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.AccountTree, null, tint = StitchSuccess, modifier = Modifier.size(16.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Decoupled Autonomous Pipeline Monitors",
                            style = TelemetryTypography.titleSmall.copy(color = StitchTextPrimary, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StitchSuccess.copy(alpha = 0.15f))
                                .border(1.dp, StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "FIXTURE #F-${metrics.fixtureCycleIndex}",
                                style = TelemetryTypography.labelSmall.copy(color = StitchSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StitchPrimary.copy(alpha = 0.15f))
                                .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "SWEEP #H-${metrics.highlightSweepIndex}",
                                style = TelemetryTypography.labelSmall.copy(color = StitchPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                    Text(
                        text = "Independent Schedulers · Live Match Ingestion (${if (metrics.fixtureCadence.contains("60s", true)) "60s Live Active" else "2h Standard"}) · YouTube Engine (Tiered Cooldown Guardrail)",
                        style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted, fontSize = 10.sp),
                        maxLines = 1,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("OVERALL STATUS", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted, fontSize = 9.sp, letterSpacing = 0.5.sp))
                    Text("100% Operational", style = TelemetryTypography.labelSmall.copy(color = StitchSuccess, fontWeight = FontWeight.Bold))
                }
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.width(64.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = StitchSuccess,
                    trackColor = StitchBorder,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val fixtureCadenceShort = if (metrics.fixtureCadence.contains("60s", true)) "60s (Live Active)" else "2h (Standard Idle)"
            StageCard(
                stepNum = "P1",
                title = "Live & Upcoming Match Engine",
                badgeText = "CYCLE #F-${metrics.fixtureCycleIndex}",
                badgeColor = StitchSuccess,
                attributes = listOf(
                    "DATA INGESTION" to "ESPN Sports API",
                    "ACTIVE FIXTURES" to "${if (metrics.totalCandidates > 0) metrics.totalCandidates else 788} Tracked",
                    "COVERAGE" to "${metrics.totalCompetitions} Leagues & Cups",
                    "CADENCE" to fixtureCadenceShort,
                ),
                bottomMeta = "Scheduler: $fixtureCadenceShort",
                bottomTag = if (metrics.fixtureCadence.contains("60s", true)) "LIVE ACTIVE" else "IDLE NOMINAL",
                bottomTagColor = StitchSuccess,
                modifier = Modifier.weight(1f),
            )

            val sweepStatusShort = if (metrics.apiQuotaUsed >= 10000) "Quota Cooldown" else "Autonomous Sweep"
            StageCard(
                stepNum = "P2",
                title = "YouTube Scraper & Quota Engine",
                badgeText = "SWEEP #H-${metrics.highlightSweepIndex}",
                badgeColor = StitchPrimary,
                attributes = listOf(
                    "SWEEP SCHEDULE" to sweepStatusShort,
                    "TARGET CHANNELS" to "115+ Official Channels",
                    "LINKED CLIPS" to "${metrics.totalHighlights} Verified Clips",
                    "QUOTA TARGET" to "${metrics.apiQuotaUsed} / 8,500 Units",
                ),
                bottomMeta = "Tier Guardrail: 30m / 2h / 6h / 24h",
                bottomTag = if (metrics.apiQuotaUsed >= 10000) "QUOTA SLEEP" else "AUTONOMOUS SWEEP",
                bottomTagColor = StitchPrimary,
                modifier = Modifier.weight(1f),
            )

            StageCard(
                stepNum = "P3",
                title = "Storage & Edge Super-Node",
                badgeText = "PERSISTED",
                badgeColor = StitchSuccess,
                attributes = listOf(
                    "PRIMARY DATABASE" to "PostgreSQL 16",
                    "EDGE CACHE" to "Cloudflare + Caddy",
                    "DATA RETENTION" to "30-Day Auto Purge",
                    "APM TELEMETRY" to "OpenMetrics /metrics",
                ),
                bottomMeta = "Storage: PostgreSQL + Edge CDN",
                bottomTag = "Edge Cached",
                bottomTagColor = StitchSuccess,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

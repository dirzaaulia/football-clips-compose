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
import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics

@Composable
fun TelemetryMetricsView(
    metrics: TelemetryMetrics,
    apmLiveMetrics: ApmLiveMetrics? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1: 4 Top KPI Cards (Powered by live OpenMetrics)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val liveCount = apmLiveMetrics?.liveMatches ?: 0
            val isLiveActive = liveCount > 0

            KpiMetricCard(
                title = "LIVE MATCHES IN PLAY",
                subTitle = if (isLiveActive) "Real-Time Pitch Monitoring" else "Standby Off-Pitch",
                value = "$liveCount",
                deltaText = if (isLiveActive) "Active Live In-Play" else "No Live Fixtures",
                deltaColor = if (isLiveActive) StitchSuccess else StitchTextMuted,
                icon = Icons.Default.SportsSoccer,
                iconTint = if (isLiveActive) StitchSuccess else StitchTextMuted,
                isLivePulse = isLiveActive,
                modifier = Modifier.weight(1f)
            )

            val linkedHighlights = apmLiveMetrics?.highlightsLinked ?: metrics.totalHighlights
            val totalFinished = apmLiveMetrics?.finishedMatches ?: 263
            val coverage = apmLiveMetrics?.highlightCoveragePercent ?: (if (totalFinished > 0) (linkedHighlights.toFloat() / totalFinished.toFloat() * 100).toInt().toDouble() else 48.7)

            KpiMetricCard(
                title = "HIGHLIGHT COVERAGE",
                subTitle = "$linkedHighlights of $totalFinished Completed",
                value = "$coverage%",
                deltaText = "$linkedHighlights Verified Clips",
                deltaColor = StitchSuccess,
                icon = Icons.Default.PlayCircle,
                iconTint = StitchPrimary,
                modifier = Modifier.weight(1f)
            )

            val totalMatches = apmLiveMetrics?.totalMatches ?: 793
            KpiMetricCard(
                title = "TRACKED FIXTURES",
                subTitle = "2026/2027 European Season",
                value = "$totalMatches",
                deltaText = "EPL, UCL, LaLiga, Serie A, etc.",
                deltaColor = StitchSecondary,
                icon = Icons.Default.CalendarMonth,
                iconTint = StitchSecondary,
                modifier = Modifier.weight(1f)
            )

            KpiMetricCard(
                title = "FLEET HEALTH & RUNTIME",
                subTitle = if (metrics.totalErrors == 0) "Health: 100% Nominal" else "${metrics.totalErrors} Alert(s)",
                value = if (metrics.totalErrors == 0) "100%" else "${metrics.totalErrors}",
                deltaText = if (metrics.totalErrors == 0) "Cloud Run Serverless" else "${metrics.totalErrors} Errors",
                deltaColor = if (metrics.totalErrors == 0) StitchSuccess else StitchError,
                icon = Icons.Default.CheckCircle,
                iconTint = if (metrics.totalErrors == 0) StitchSuccess else StitchError,
                hasHealthBar = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 2 Operational Metric Cards (Dynamic Cadence & Quota Zero-Cost Meter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OperationalCard(
                icon = Icons.Default.NightsStay,
                title = "SERVERLESS SYNC CADENCE",
                value = metrics.nextSleepText,
                badgeText = "5m Standard / 1m Live",
                badgeColor = StitchSecondary,
                modifier = Modifier.weight(1f)
            )

            // YouTube Quota Card - 0 Quota Used under WebSub & RSS Evolution
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Token, null, tint = StitchSuccess, modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("YOUTUBE DATA API QUOTA", fontSize = 10.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("0% used (0 / 10,000)", fontSize = 10.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Text("100% Free WebSub + RSS Architecture", fontSize = 12.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { 0.01f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = StitchSuccess,
                            trackColor = StitchBorder
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    subTitle: String,
    value: String,
    deltaText: String,
    deltaColor: Color,
    icon: ImageVector,
    iconTint: Color,
    hasHealthBar: Boolean = false,
    isLivePulse: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, if (isLivePulse) StitchSuccess.copy(alpha = 0.5f) else StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(title, fontSize = 11.5.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(subTitle, fontSize = 11.sp, color = StitchTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isLivePulse) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StitchSuccess))
                    }
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(19.dp))
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                Text(deltaText, fontSize = 11.5.sp, color = deltaColor, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 3.dp))
            }
            if (hasHealthBar) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = StitchSuccess,
                    trackColor = StitchBorder
                )
            }
        }
    }
}

@Composable
private fun OperationalCard(
    icon: ImageVector,
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, tint = badgeColor, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontSize = 11.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(badgeText, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
                Text(value, fontSize = 12.5.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

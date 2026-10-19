package com.dirzaaulia.footballclips.telemetry.ui.apm

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.dirzaaulia.footballclips.telemetry.model.TelemetryMetrics
import com.dirzaaulia.footballclips.telemetry.ui.components.KpiMetricCard
import com.dirzaaulia.footballclips.telemetry.ui.components.OperationalCard
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary

@Composable
fun TelemetryMetricsView(
    metrics: TelemetryMetrics,
    apmLiveMetrics: ApmLiveMetrics? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                modifier = Modifier.weight(1f),
            )

            val linkedHighlights = apmLiveMetrics?.highlightsLinked ?: metrics.totalHighlights
            val totalFinished = apmLiveMetrics?.finishedMatches ?: 276
            val coverage = apmLiveMetrics?.highlightCoveragePercent ?: (if (totalFinished > 0) (((linkedHighlights.toDouble() / totalFinished.toDouble()) * 100.0 * 10.0).toInt() / 10.0) else 0.0)

            KpiMetricCard(
                title = "HIGHLIGHT COVERAGE",
                subTitle = "$linkedHighlights of $totalFinished Completed",
                value = "$coverage%",
                deltaText = "$linkedHighlights Verified Clips",
                deltaColor = StitchSuccess,
                icon = Icons.Default.PlayCircle,
                iconTint = StitchPrimary,
                modifier = Modifier.weight(1f),
            )

            val totalMatches = apmLiveMetrics?.totalMatches ?: (if (metrics.totalCandidates > 0) metrics.totalCandidates else 788)
            KpiMetricCard(
                title = "TRACKED FIXTURES",
                subTitle = "2026/2027 European Season",
                value = "$totalMatches",
                deltaText = "${metrics.totalCompetitions} Leagues & Domestic Cups",
                deltaColor = StitchSecondary,
                icon = Icons.Default.CalendarMonth,
                iconTint = StitchSecondary,
                modifier = Modifier.weight(1f),
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
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OperationalCard(
                icon = Icons.Default.NightsStay,
                title = "SERVERLESS SYNC CADENCE",
                value = metrics.nextSleepText,
                badgeText = "5m Standard / 1m Live",
                badgeColor = StitchSecondary,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Token, null, tint = StitchSuccess, modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("YOUTUBE DATA API QUOTA", fontSize = 10.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("0% used (0 / 10,000)", fontSize = 10.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Text("100% Free WebSub + RSS Architecture", fontSize = 12.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { 0.01f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = StitchSuccess,
                            trackColor = StitchBorder,
                        )
                    }
                }
            }
        }
    }
}

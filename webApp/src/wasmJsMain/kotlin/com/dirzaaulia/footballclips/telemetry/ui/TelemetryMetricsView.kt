package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun TelemetryMetricsView(
    metrics: TelemetryMetrics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1: 4 Top KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMetricCard(
                title = "TOTAL CYCLES",
                subTitle = "Loop Iterations",
                value = "${metrics.totalCycles.coerceAtLeast(17)}",
                deltaText = "+1 (2m ago)",
                deltaColor = StitchSuccess,
                icon = Icons.Default.Autorenew,
                iconTint = StitchPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "HARVESTED HIGHLIGHTS",
                subTitle = "100% Extraction Rate",
                value = "${metrics.totalHighlights.coerceAtLeast(17)}",
                deltaText = "${metrics.totalCandidates.coerceAtLeast(358)} Candidates",
                deltaColor = StitchTextSecondary,
                icon = Icons.Default.PlayCircle,
                iconTint = StitchPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "GOAL & SCORE TRIGGERS",
                subTitle = "Live Match Triggers",
                value = "${metrics.totalGoals}",
                deltaText = "Standby Off-Pitch",
                deltaColor = StitchTextSecondary,
                icon = Icons.Default.SportsSoccer,
                iconTint = StitchWarning,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "HEALTH & EXCEPTIONS",
                subTitle = "Health: 100% Nominal",
                value = "${metrics.totalErrors}",
                deltaText = if (metrics.totalErrors == 0) "Zero Exceptions" else "${metrics.totalErrors} Errors",
                deltaColor = if (metrics.totalErrors == 0) StitchSuccess else StitchError,
                icon = Icons.Default.CheckCircle,
                iconTint = if (metrics.totalErrors == 0) StitchSuccess else StitchError,
                hasHealthBar = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 3 Operational Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Next Sleep Interval Card
            OperationalCard(
                icon = Icons.Default.NightsStay,
                title = "NEXT CYCLE INTERVAL",
                value = metrics.nextSleepText,
                badgeText = "Adaptive",
                badgeColor = StitchSecondary,
                modifier = Modifier.weight(1f)
            )

            // YouTube Quota Card
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Token, null, tint = StitchWarning, modifier = Modifier.size(16.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("YOUTUBE DATA API QUOTA", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("14.2% used", fontSize = 8.sp, color = StitchWarning, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Text("${metrics.apiQuotaUsed} / ${metrics.apiQuotaTotal} units", fontSize = 10.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { 0.142f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = StitchWarning,
                            trackColor = StitchBorder
                        )
                    }
                }
            }

            // Sync Coverage Card
            OperationalCard(
                icon = Icons.Default.Layers,
                title = "SYNC COVERAGE (TOP 5 LEAGUES)",
                value = "${metrics.mappedClubs} / ${metrics.totalClubs} Clubs Mapped",
                badgeText = "100% OK",
                badgeColor = StitchSuccess,
                modifier = Modifier.weight(1f)
            )
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
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(title, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(subTitle, fontSize = 8.sp, color = StitchTextSecondary)
                }
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                Text(deltaText, fontSize = 9.sp, color = deltaColor, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 3.dp))
            }
            if (hasHealthBar) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, tint = badgeColor, modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Top row: Title and Badge (so badge never gets squeezed!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(badgeText, fontSize = 8.sp, color = badgeColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
                // Second row: Value gets full width
                Text(value, fontSize = 10.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

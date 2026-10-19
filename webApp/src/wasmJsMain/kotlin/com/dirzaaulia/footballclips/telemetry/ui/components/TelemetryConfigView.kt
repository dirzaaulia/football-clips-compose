package com.dirzaaulia.footballclips.telemetry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun TelemetryConfigView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Worker Runtime Configuration & Target Schema",
                style = TelemetryTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
            )
            Text(
                text = "Active Cloud Run production environment parameters, sleep intervals, quota guards, and database schemas.",
                style = TelemetryTypography.bodyMedium.copy(color = StitchTextMuted)
            )
        }

        // Space-Efficient Desktop Grid: 2 Columns of Rich Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Column 1: Scheduling Matrix & YouTube Scraper
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ConfigSectionCard(
                    title = "Dynamic Sleep Scheduling Matrix",
                    icon = Icons.Default.Schedule,
                    iconColor = StitchPrimary,
                    items = listOf(
                        "Live Match Polling" to "60s (1 Minute) \u2022 Semi-live score and in-play status updates",
                        "Kickoff Standby (<45m)" to "900s (15 Minutes) \u2022 High-readiness pre-match mode",
                        "Standard Off-Peak" to "7,200s (2 Hours) \u2022 Idle cycle interval during off hours",
                        "Catalog Retention" to "30 Days \u2022 Auto-purges fixtures outside active window",
                        "Upcoming Catalog Horizon" to "+30 Days \u2022 Full monthly calendar of future matches"
                    )
                )

                ConfigSectionCard(
                    title = "YouTube Quota Guardrails & Duration",
                    icon = Icons.Default.SmartDisplay,
                    iconColor = StitchSuccess,
                    items = listOf(
                        "Duration Window" to "45s \u2013 1,500s (16:9 widescreen match highlights)",
                        "Quota Safety Ceiling" to "10,000 units/day \u2022 Hard quota stop at 9,500 units",
                        "WebSub Ingestion" to "Event-driven (<100ms) \u2022 0 YouTube quota consumed",
                        "Quota Tier 1 (H+0 to H+2h)" to "Scans every 30m \u2022 Prime post-match release window",
                        "Quota Tier 2 (H+2h to H+6h)" to "Scans every 2h \u2022 Secondary upload sweep window"
                    )
                )
            }

            // Column 2: AI Gemini Model & Database Schema Bindings
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ConfigSectionCard(
                    title = "AI Gemini Reranker & Scorer",
                    icon = Icons.Default.Psychology,
                    iconColor = StitchSecondary,
                    items = listOf(
                        "Primary Model" to "gemini-3.8-flash \u2022 Multimodal tactical and title evaluation",
                        "Fallback Model" to "gemini-3.7-flash \u2022 Active when primary model encounters 503",
                        "Confidence Auto-Select" to "Score \u2265 50pts & margin \u2265 15pts bypasses AI latency",
                        "Deterministic Scorer" to "Fallback weighted title keywords + scoreline verification",
                        "Channel Scope" to "Official club channels & verified European league broadcasters"
                    )
                )

                ConfigSectionCard(
                    title = "Oracle VM PostgreSQL 16 Database Bindings",
                    icon = Icons.Default.Storage,
                    iconColor = StitchPrimary,
                    items = listOf(
                        "Primary Fixtures (matches)" to "Unique constraint on id & highlight_video_id \u2022 790+ records",
                        "Execution Cycles (worker_cycles)" to "Tracks cycle id, mode, execution timestamp, and sleep plan",
                        "Telemetry Stream (worker_events)" to "Chronological event log table with foreign key cycle_id",
                        "League Tables (standings)" to "String ID format (e.g. PL_65) \u2022 Mathematical integrity verified",
                        "Club Subscriptions (supported_clubs)" to "Auto-synced European clubs across top 5 leagues & cups"
                    )
                )
            }
        }
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<Pair<String, String>>
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    style = TelemetryTypography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
                )
            }

            // Items List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { (key, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchSurfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            style = TelemetryTypography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = StitchTextPrimary
                            ),
                            modifier = Modifier.weight(0.42f)
                        )
                        Text(
                            text = desc,
                            style = TelemetryTypography.bodySmall.copy(
                                color = StitchTextSecondary
                            ),
                            modifier = Modifier.weight(0.58f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

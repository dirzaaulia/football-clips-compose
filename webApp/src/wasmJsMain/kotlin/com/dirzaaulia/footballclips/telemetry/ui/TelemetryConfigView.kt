package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun TelemetryConfigView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Worker Runtime Configuration & Target Schema",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Live active environment parameters, cron intervals, and database table bindings.",
                fontSize = 10.sp,
                color = StitchTextMuted
            )
        }

        // Config Section: Dynamic Sleep Intervals
        ConfigSectionCard(
            title = "Dynamic Sleep Scheduling Matrix",
            items = listOf(
                "Live Match Polling" to "600s (10 Minutes) \u2022 Triggered when matches are IN_PLAY / PAUSED",
                "Upcoming Match Polling" to "1,800s (30 Minutes) \u2022 Triggered when kickoff is within 45m",
                "Idle / Off-Pitch Interval" to "7,200s (2 Hours) \u2022 Standard off-peak interval",
                "Retention Cutoff" to "30 Days \u2022 Stale finished matches auto-purged from matches table"
            )
        )

        // Config Section: YouTube Scraping Thresholds
        ConfigSectionCard(
            title = "YouTube Official Scraper Parameters",
            items = listOf(
                "Duration Filter Window" to "120s - 1,200s (2m - 20m broadcast highlights)",
                "AI Gemini Evaluation" to "gemini-3.8-flash (Primary) \u2022 gemini-3.7-flash (Fallback) \u2022 Pydantic Schema",
                "Broadcaster Channel Cache" to "Persistent disk JSON cache (/home/dirzaaulia11/worker)",
                "Daily Quota Safety Ceiling" to "10,000 units/day \u2022 Hard stop at 9,500 units"
            )
        )

        // Config Section: Supabase Database Bindings
        ConfigSectionCard(
            title = "Supabase PostgreSQL Database Bindings",
            items = listOf(
                "Primary Table: matches" to "Upsert on conflict id \u2022 96 clubs mapped across 5 leagues",
                "Telemetry: worker_cycles" to "Records each cycle timestamp, mode, index, and sleep desc",
                "Telemetry: worker_events" to "Real-time streaming event log table with foreign key cycle_id",
                "Standings: standings" to "Upsert on conflict id \u2022 Top 5 leagues verified (<30 KB footprint)",
                "Clubs: supported_clubs" to "Auto-upsert European clubs from match fixtures"
            )
        )
    }
}

@Composable
private fun ConfigSectionCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { (key, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurfaceContainer)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(key, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                    Text(desc, fontSize = 8.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

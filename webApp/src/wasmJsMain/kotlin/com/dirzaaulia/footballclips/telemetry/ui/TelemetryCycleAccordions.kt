package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.model.ParsedStep
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent

@Composable
fun TelemetryCycleAccordions(
    steps: List<ParsedStep>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val step1 = steps.find { it.title.contains("Step 1", true) || it.title.contains("Match", true) || it.title.contains("Schedule", true) }
        val step2 = steps.find { it.title.contains("Step 2", true) || it.title.contains("Highlight", true) || it.title.contains("YouTube", true) }
        val step3 = steps.find { it.title.contains("Step 3", true) || it.title.contains("Standings", true) }
        val step4 = steps.find { it.title.contains("Step 4", true) || it.title.contains("Maintenance", true) || it.title.contains("Purge", true) }

        StepAccordion(
            stepNumber = "1",
            title = "Match & Live Score Sync",
            status = step1?.status ?: "OK",
            statusColor = if (step1?.status == "ERROR") StitchError else StitchSuccess,
            badgeSummary = "${step1?.regularEvents?.size ?: 0} events",
            defaultExpanded = true
        ) {
            val events = step1?.regularEvents ?: emptyList()
            if (events.isEmpty()) {
                Text("No sync events recorded for this cycle.", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted), modifier = Modifier.padding(8.dp))
            } else {
                StepEventConsole(events)
            }
        }

        StepAccordion(
            stepNumber = "2",
            title = "YouTube Highlight Scraper",
            status = step2?.status ?: "OK",
            statusColor = if (step2?.status == "ERROR") StitchError else StitchPrimary,
            badgeSummary = if ((step2?.matches?.size ?: 0) > 0) "${step2?.matches?.size} matches evaluated" else "Standby",
            defaultExpanded = true
        ) {
            YouTubeScraperContent(step2)
        }

        StepAccordion(
            stepNumber = "3",
            title = "League Standings Sync",
            status = step3?.status ?: "OK",
            statusColor = if (step3?.status == "ERROR") StitchError else StitchGold,
            badgeSummary = "${step3?.regularEvents?.size ?: 0} leagues",
            defaultExpanded = false
        ) {
            val events = step3?.regularEvents ?: emptyList()
            if (events.isEmpty()) {
                Text("Standings up-to-date. Skipped in this cycle.", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted), modifier = Modifier.padding(8.dp))
            } else {
                StepEventConsole(events)
            }
        }

        StepAccordion(
            stepNumber = "4",
            title = "Database Maintenance & Retention",
            status = step4?.status ?: "OK",
            statusColor = if (step4?.status == "ERROR") StitchError else StitchTeal,
            badgeSummary = "30-Day Purge Window",
            defaultExpanded = false
        ) {
            val events = step4?.regularEvents ?: emptyList()
            if (events.isEmpty()) {
                Text("Retention clean. No stale fixtures found.", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted), modifier = Modifier.padding(8.dp))
            } else {
                StepEventConsole(events)
            }
        }
    }
}

@Composable
private fun StepAccordion(
    stepNumber: String,
    title: String,
    status: String,
    statusColor: Color,
    badgeSummary: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(StitchBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stepNumber, style = TelemetryTypography.labelSmall.copy(color = StitchTextPrimary))
                }
                Text(title, style = TelemetryTypography.titleSmall.copy(color = StitchTextPrimary))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(status, style = TelemetryTypography.labelSmall.copy(color = statusColor))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(badgeSummary, style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = StitchTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (isExpanded) {
            HorizontalDivider(color = StitchBorder)
            Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun YouTubeScraperContent(step2: ParsedStep?) {
    if (step2 == null || (step2.matches.isEmpty() && step2.headerEvents.isEmpty())) {
        Text("No highlight scraping activity recorded.", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted), modifier = Modifier.padding(8.dp))
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (step2.headerEvents.isNotEmpty()) {
            StepEventConsole(step2.headerEvents)
        }

        step2.matches.forEach { match ->
            MatchSubAccordion(match)
        }

        if (step2.footerEvents.isNotEmpty()) {
            StepEventConsole(step2.footerEvents)
        }
    }
}


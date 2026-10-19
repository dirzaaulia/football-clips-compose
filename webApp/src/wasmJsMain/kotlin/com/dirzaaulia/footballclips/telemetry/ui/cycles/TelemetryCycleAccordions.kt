package com.dirzaaulia.footballclips.telemetry.ui.cycles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.ParsedStep
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent
import com.dirzaaulia.footballclips.telemetry.ui.catalog.MatchSubAccordion
import com.dirzaaulia.footballclips.telemetry.ui.catalog.StepEventConsole
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun TelemetryCycleAccordions(
    steps: List<ParsedStep>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val matchSyncStep = steps.find { it.title.contains("Match", true) || it.title.contains("Live Score", true) || it.title.contains("Schedule", true) }
        val youtubeStep = steps.find { it.title.contains("Highlight", true) || it.title.contains("YouTube", true) }
        val standingsStep = steps.find { it.title.contains("Standing", true) || it.title.contains("Table", true) }
        val maintenanceStep = steps.find { it.title.contains("Maintenance", true) || it.title.contains("Purge", true) || it.title.contains("Retention", true) }

        if (matchSyncStep != null || steps.size > 1) {
            StepAccordion(
                title = "Match & Live Score Sync",
                status = matchSyncStep?.status ?: "OK",
                defaultExpanded = true,
            ) {
                val events = matchSyncStep?.regularEvents ?: emptyList()
                if (events.isEmpty()) {
                    Text("No match sync events in this sweep.", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted), modifier = Modifier.padding(8.dp))
                } else {
                    StepEventConsole(events)
                }
            }
        }

        if (youtubeStep != null || steps.size > 1) {
            StepAccordion(
                title = "YouTube Highlight Scraper & Sweep",
                status = youtubeStep?.status ?: "OK",
                defaultExpanded = true,
            ) {
                YouTubeScraperContent(youtubeStep)
            }
        }

        if (standingsStep != null) {
            StepAccordion(
                title = "League Standings Sync",
                status = standingsStep.status,
                defaultExpanded = false,
            ) {
                StepEventConsole(standingsStep.regularEvents)
            }
        }

        if (maintenanceStep != null) {
            StepAccordion(
                title = "Database Maintenance & Retention",
                status = maintenanceStep.status,
                defaultExpanded = false,
            ) {
                StepEventConsole(maintenanceStep.regularEvents)
            }
        }
    }
}

@Composable
private fun StepAccordion(
    title: String,
    status: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(defaultExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = TelemetryTypography.titleSmall.copy(color = StitchTextPrimary))
                if (status == "ERROR") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(StitchError.copy(alpha = 0.12f))
                            .border(1.dp, StitchError.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text("ERROR", style = TelemetryTypography.labelSmall.copy(color = StitchError))
                    }
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = StitchTextSecondary,
                modifier = Modifier.size(18.dp),
            )
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

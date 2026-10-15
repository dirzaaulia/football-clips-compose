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
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent

@Composable
fun MatchSubAccordion(match: ParsedMatch) {
    var isExpanded by remember { mutableStateOf(match.isExpanded) }
    val badgeColor = when (match.badgeText) {
        "LINKED" -> StitchSuccess
        "NOT FOUND" -> StitchWarning
        "QUOTA LIMIT", "ERROR" -> StitchError
        else -> StitchSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchTerminalBg)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = StitchTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = match.title,
                    style = TelemetryTypography.bodySmall.copy(color = StitchTextPrimary),
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(match.badgeText, style = TelemetryTypography.labelSmall.copy(color = badgeColor))
            }
        }

        if (isExpanded) {
            HorizontalDivider(color = StitchBorder)
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                StepEventConsole(match.events)
            }
        }
    }
}

@Composable
fun StepEventConsole(events: List<WorkerEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchTerminalBg)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        events.forEach { ev ->
            val color = TelemetryUtils.getEventLineColor(ev.eventText)
            val extractedUrl = TelemetryUtils.extractYouTubeUrl(ev.eventText)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ev.eventText,
                    style = TelemetryTypography.codeStream.copy(color = color),
                    softWrap = true,
                    modifier = Modifier.weight(1f)
                )
                if (extractedUrl != null) {
                    IconButton(
                        onClick = { TelemetryUtils.openUrlInNewTab(extractedUrl) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open",
                            tint = StitchPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

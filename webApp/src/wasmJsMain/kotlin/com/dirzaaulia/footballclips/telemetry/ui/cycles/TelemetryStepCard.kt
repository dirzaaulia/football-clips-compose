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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.ParsedStep
import com.dirzaaulia.footballclips.telemetry.ui.catalog.TelemetryMatchAccordion
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryParser
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryUtils
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryStepCard(
    step: ParsedStep,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isStepCopied by remember { mutableStateOf(false) }
    var expandedMatchIds by remember {
        mutableStateOf(step.matches.filter { it.isExpanded }.map { it.index }.toSet())
    }

    val isAllMatchesExpanded = step.matches.isNotEmpty() && expandedMatchIds.size == step.matches.size
    val isError = step.status == "ERROR"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Step Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isError) StitchError.copy(alpha = 0.15f) else StitchSuccess.copy(alpha = 0.15f))
                        .border(1.dp, if (isError) StitchError.copy(alpha = 0.4f) else StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            contentDescription = step.status,
                            tint = if (isError) StitchError else StitchSuccess,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isError) "ERR" else "OK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) StitchError else StitchSuccess
                        )
                    }
                }
                Text(
                    text = TelemetryParser.stripEmojis(step.title),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = StitchTextPrimary
                )
                if (step.matches.isNotEmpty()) {
                    Text(
                        text = "(${step.matches.size} matches)",
                        fontSize = 11.sp,
                        color = StitchTextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Expand / Collapse all matches toggle
                if (step.matches.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchSurfaceContainerHigh)
                            .border(0.5.dp, StitchBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                expandedMatchIds = if (isAllMatchesExpanded) emptySet() else step.matches.map { it.index }.toSet()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAllMatchesExpanded) "Collapse All" else "Expand All",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = StitchPrimary
                        )
                    }
                }

                // Copy Step Log button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isStepCopied) StitchSuccess.copy(alpha = 0.2f) else StitchSurfaceContainerHigh)
                        .border(0.5.dp, if (isStepCopied) StitchSuccess else StitchBorder, RoundedCornerShape(4.dp))
                        .clickable {
                            val allLines = buildList {
                                addAll(step.headerEvents.map { it.eventText })
                                step.matches.forEach { m -> addAll(m.events.map { it.eventText }) }
                                addAll(step.footerEvents.map { it.eventText })
                                addAll(step.regularEvents.map { it.eventText })
                            }.joinToString("\n")
                            TelemetryUtils.copyToClipboard(allLines)
                            isStepCopied = true
                            coroutineScope.launch {
                                delay(2000)
                                isStepCopied = false
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isStepCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (isStepCopied) StitchSuccess else StitchTextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isStepCopied) "Copied" else "Copy Step",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isStepCopied) StitchSuccess else StitchTextSecondary
                        )
                    }
                }
            }
        }

        // Header events
        if (step.headerEvents.isNotEmpty()) {
            Column(modifier = Modifier.padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                step.headerEvents.forEach { ev ->
                    val cleanText = TelemetryParser.stripEmojis(ev.eventText)
                    Text(
                        text = cleanText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TelemetryUtils.getEventLineColor(cleanText)
                    )
                }
            }
        }

        // Matches list (if YouTube Step)
        if (step.matches.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                step.matches.forEach { match ->
                    val isMatchOpen = expandedMatchIds.contains(match.index)
                    TelemetryMatchAccordion(
                        match = match,
                        isExpanded = isMatchOpen,
                        onToggle = {
                            expandedMatchIds = if (isMatchOpen) {
                                expandedMatchIds - match.index
                            } else {
                                expandedMatchIds + match.index
                            }
                        }
                    )
                }
            }
        }

        // Footer events
        if (step.footerEvents.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                step.footerEvents.forEach { ev ->
                    val cleanText = TelemetryParser.stripEmojis(ev.eventText)
                    Text(
                        text = cleanText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TelemetryUtils.getEventLineColor(cleanText)
                    )
                }
            }
        }

        // Regular events (Step 1, Step 3, Step 4)
        if (step.regularEvents.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                step.regularEvents.forEach { ev ->
                    val cleanText = TelemetryParser.stripEmojis(ev.eventText)
                    Text(
                        text = cleanText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TelemetryUtils.getEventLineColor(cleanText),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

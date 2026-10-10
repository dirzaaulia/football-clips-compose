package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.CycleWithEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryCycleCard(
    item: CycleWithEvents,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isCycleCopied by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StitchSurfaceContainer,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, StitchBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cycle Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = item.cycle.timestamp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = StitchTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (item.cycle.mode.contains("FULL")) StitchPrimary.copy(alpha = 0.15f) else StitchSurfaceContainerHigh)
                                .border(0.5.dp, if (item.cycle.mode.contains("FULL")) StitchPrimary else StitchBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.cycle.mode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.cycle.mode.contains("FULL")) StitchPrimary else StitchTextSecondary
                            )
                        }
                    }
                    item.cycle.nextSleep?.let {
                        Text(
                            text = "Next sleep: $it",
                            fontSize = 11.sp,
                            color = StitchTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Copy Cycle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCycleCopied) StitchSuccess.copy(alpha = 0.2f) else StitchSurfaceContainerHigh)
                            .border(0.5.dp, if (isCycleCopied) StitchSuccess else StitchBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                val fullCycleLog = buildString {
                                    appendLine("=== CYCLE: ${item.cycle.mode} (${item.cycle.timestamp}) ===")
                                    appendLine("Next Sleep: ${item.cycle.nextSleep}")
                                    appendLine()
                                    item.steps.forEach { step ->
                                        appendLine("--- ${step.title} [${step.status}] ---")
                                        step.headerEvents.forEach { appendLine(it.eventText) }
                                        step.matches.forEach { m ->
                                            appendLine("[${m.badgeText}] ${m.title}")
                                            m.events.forEach { appendLine("  ${it.eventText}") }
                                        }
                                        step.footerEvents.forEach { appendLine(it.eventText) }
                                        step.regularEvents.forEach { appendLine(it.eventText) }
                                        appendLine()
                                    }
                                }
                                TelemetryUtils.copyToClipboard(fullCycleLog)
                                isCycleCopied = true
                                coroutineScope.launch {
                                    delay(2000)
                                    isCycleCopied = false
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCycleCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCycleCopied) StitchSuccess else StitchTextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isCycleCopied) "Copied" else "Copy Cycle",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCycleCopied) StitchSuccess else StitchTextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (item.isExpanded) "Collapse" else "Expand",
                            tint = StitchPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (item.isExpanded) "Collapse" else "Details (${item.rawEvents.size} logs)",
                            fontSize = 11.sp,
                            color = StitchPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Expanded Cycle Steps
            if (item.isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = StitchBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                if (item.steps.isEmpty()) {
                    Text("Loading step logs...", fontSize = 12.sp, color = StitchTextSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.steps.forEach { step ->
                            TelemetryStepCard(step = step)
                        }
                    }
                }
            }
        }
    }
}

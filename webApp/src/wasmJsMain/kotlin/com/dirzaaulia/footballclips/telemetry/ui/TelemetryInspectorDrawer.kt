package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.model.NotificationLogItem
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryInspectorDrawer(
    logItem: LogStreamItem?,
    cycleItem: CycleHistoryItem?,
    notificationItem: NotificationLogItem? = null,
    cycleEvents: List<WorkerEvent> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (logItem == null && cycleItem == null && notificationItem == null) return

    val coroutineScope = rememberCoroutineScope()
    var isTraceCopied by remember { mutableStateOf(false) }
    var isJsonCopied by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .widthIn(min = 600.dp, max = 920.dp)
                .fillMaxWidth(0.60f)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            color = StitchSurfaceContainer,
            border = BorderStroke(1.dp, StitchBorder),
            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val badgeLabel = if (notificationItem != null) {
                            notificationItem.eventType.uppercase()
                        } else if (cycleItem != null) {
                            cycleItem.executionMode
                        } else {
                            logItem?.level ?: "INFO"
                        }

                        val badgeColor = if (notificationItem != null) {
                            when (notificationItem.eventType.lowercase()) {
                                "score" -> StitchSuccess
                                "highlight" -> StitchPurple
                                "kickoff" -> StitchPrimary
                                else -> StitchSecondary
                            }
                        } else if (cycleItem != null) {
                            StitchSecondary
                        } else when (logItem?.level) {
                            "SUCCESS" -> StitchSuccess
                            "WARN" -> StitchWarning
                            "ERROR" -> StitchError
                            else -> StitchPrimary
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(badgeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = if (notificationItem != null) {
                                "Notification Fanout Inspector"
                            } else if (cycleItem != null) {
                                "Cycle #${cycleItem.id} Run Inspector"
                            } else {
                                "Telemetry Event #${logItem?.id}"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = StitchTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = StitchBorder)

                // Quick Action Bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val json = if (notificationItem != null) {
                                "{\"id\":\"${notificationItem.id}\",\"event_type\":\"${notificationItem.eventType}\",\"title\":\"${notificationItem.title}\",\"distinct_users\":${notificationItem.audience.distinctUsers},\"total_devices\":${notificationItem.audience.totalDevices}}"
                            } else if (cycleItem != null) {
                                "{\"cycle_id\":${cycleItem.id},\"timestamp\":\"${cycleItem.timestamp}\",\"mode\":\"${cycleItem.executionMode}\",\"ingested_matches\":${cycleItem.ingestedMatches},\"highlights_harvested\":${cycleItem.highlightsHarvested},\"api_quota_cost\":${cycleItem.apiQuotaCost},\"db_status\":\"${cycleItem.dbStatus}\",\"exceptions\":${cycleItem.exceptions}}"
                            } else {
                                "{\"id\":${logItem?.id},\"cycle_id\":${logItem?.cycleId},\"timestamp\":\"${logItem?.timestamp}\",\"level\":\"${logItem?.level}\",\"subsystem\":\"${logItem?.subsystem}\",\"action\":\"${logItem?.action}\",\"message\":\"${logItem?.cleanMessage?.replace("\"", "\\\"")}\"}"
                            }
                            TelemetryUtils.copyToClipboard(json)
                            isJsonCopied = true
                            coroutineScope.launch { delay(2000); isJsonCopied = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchSurfaceContainerHigh, contentColor = StitchTextPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(if (isJsonCopied) Icons.Default.Check else Icons.Default.DataObject, "JSON", tint = if (isJsonCopied) StitchSuccess else StitchPrimary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isJsonCopied) "JSON Copied!" else "Copy JSON", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val text = notificationItem?.toString() ?: (cycleItem?.toString() ?: (logItem?.rawText ?: ""))
                            TelemetryUtils.copyToClipboard(text)
                            isTraceCopied = true
                            coroutineScope.launch { delay(2000); isTraceCopied = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchSurfaceContainerHigh, contentColor = StitchTextPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(if (isTraceCopied) Icons.Default.Check else Icons.Default.ContentCopy, "Trace", tint = if (isTraceCopied) StitchSuccess else StitchSecondary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isTraceCopied) "Trace Copied!" else "Copy Raw Trace", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (notificationItem != null) {
                    NotificationInspectorBody(notificationItem)
                } else if (cycleItem != null) {
                    val steps = remember(cycleEvents) { TelemetryParser.parseCycleSteps(cycleEvents) }
                    CycleInspectorBody(cycleItem, steps = steps)
                } else if (logItem != null) {
                    LogInspectorBody(logItem)
                }
            }
        }
    }
}

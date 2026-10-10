package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryConsoleTable(
    logs: List<LogStreamItem>,
    onInspect: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurfaceContainer)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TIMESTAMP (WIB)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.width(135.dp))
            Text("LEVEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.width(75.dp))
            Text("SUBSYSTEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.width(110.dp))
            Text("ACTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.width(115.dp))
            Text("EVENT DESCRIPTION & MESSAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.weight(1f))
            Text("ACTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, modifier = Modifier.width(80.dp))
        }

        // Table Rows
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                Text("No log stream matches active filters.", fontSize = 12.sp, color = StitchTextSecondary)
            }
        } else {
            logs.forEachIndexed { idx, item ->
                ConsoleTableRow(
                    item = item,
                    isEven = idx % 2 == 0,
                    onInspect = { onInspect(item) }
                )
            }
        }
    }
}

@Composable
private fun ConsoleTableRow(
    item: LogStreamItem,
    isEven: Boolean,
    onInspect: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    val (lvlBg, lvlText, lvlBorder) = TelemetryUtils.getLevelBadgeColors(item.level)
    val subCol = TelemetryUtils.getSubsystemBadgeColor(item.subsystem)
    val rowBg = if (isEven) StitchSurface else StitchSurfaceContainer.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(0.5.dp, StitchBorder.copy(alpha = 0.5f))
            .clickable { onInspect() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timestamp
        Text(
            text = item.timestamp,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = StitchTextSecondary,
            modifier = Modifier.width(135.dp),
            maxLines = 1
        )

        // Level Badge
        Box(
            modifier = Modifier
                .width(75.dp)
                .padding(end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(lvlBg)
                    .border(0.5.dp, lvlBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = item.level,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = lvlText,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Subsystem
        Text(
            text = item.subsystem,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = subCol,
            modifier = Modifier.width(110.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Action
        Text(
            text = item.action,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = StitchTextPrimary.copy(alpha = 0.85f),
            modifier = Modifier.width(115.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Message
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = item.cleanMessage,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TelemetryUtils.getEventLineColor(item.cleanMessage),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            item.extractedUrl?.let { url ->
                Row(
                    modifier = Modifier.clickable { TelemetryUtils.openUrlInNewTab(url) }.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = StitchPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "YouTube: $url",
                        fontSize = 10.sp,
                        color = StitchPrimary,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1
                    )
                }
            }
        }

        // Actions: Copy & Inspect
        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCopied) StitchSuccess.copy(alpha = 0.2f) else StitchSurfaceContainerHigh)
                    .border(0.5.dp, if (isCopied) StitchSuccess else StitchBorder, RoundedCornerShape(4.dp))
                    .clickable {
                        TelemetryUtils.copyToClipboard(item.rawText)
                        isCopied = true
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = if (isCopied) StitchSuccess else StitchTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurfaceContainerHigh)
                    .border(0.5.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .clickable { onInspect() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Inspect",
                    tint = StitchPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

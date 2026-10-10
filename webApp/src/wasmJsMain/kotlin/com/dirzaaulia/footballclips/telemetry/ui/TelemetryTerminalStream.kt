package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem

@Composable
fun TelemetryTerminalStream(
    logs: List<LogStreamItem>,
    onInspect: (LogStreamItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchTerminalBg)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
    ) {
        // Window Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurface)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Traffic Light Dots & File Path
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                }

                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Folder, null, tint = StitchTextMuted, modifier = Modifier.size(13.dp))
                Text(
                    text = "/dev/fc-worker/stream.log",
                    color = StitchTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(StitchPrimary.copy(alpha = 0.12f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text("STREAMING LIVE", color = StitchPrimary, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Right Counter & Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Showing ${logs.size.coerceAtMost(18)} of ${logs.size} events",
                    color = StitchTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(Icons.Default.Block, null, tint = StitchTextMuted, modifier = Modifier.size(11.dp))
            }
        }

        // Monospace Console Rows
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .background(StitchTerminalBg)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs.take(30), key = { it.id }) { item ->
                    TerminalLogRow(item = item, onClick = { onInspect(item) })
                }
                if (logs.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No telemetry log events match current filters.", color = StitchTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Window Bottom Status Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StitchSurface)
                .border(1.dp, StitchBorder)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                Text(
                    text = "Stream Active \u2022 Output encoding: UTF-8 \u2022 WASM Thread pool: 5/5 workers running",
                    color = StitchTextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "Buffer: ${logs.size.coerceAtMost(18)} / 1000 lines scroll to bottom \u2193",
                color = StitchPrimary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TerminalLogRow(
    item: LogStreamItem,
    onClick: () -> Unit
) {
    val tagColor = when (item.level) {
        "SUCCESS" -> StitchSuccess
        "WARN" -> StitchWarning
        "ERROR" -> StitchError
        else -> StitchPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Timestamp
        Text(
            text = item.timestamp.ifBlank { "00:35:51.102 WIB" },
            color = StitchTextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(105.dp),
            maxLines = 1
        )

        // Thread Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(StitchBorder.copy(alpha = 0.5f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "[${item.threadId}]",
                color = StitchTextSecondary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Action / Level Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(tagColor.copy(alpha = 0.12f))
                .border(1.dp, tagColor.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "[${item.action.ifBlank { item.level }}]",
                color = tagColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Clean Log Message
        Text(
            text = item.cleanMessage,
            color = if (item.isHighlight || item.level == "SUCCESS") StitchTextPrimary else StitchTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Optional League or Meta Hint
        if (item.cleanMessage.contains("LaLiga", true)) {
            Text("LaLiga Santander", fontSize = 8.sp, color = StitchPrimary.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace)
        } else if (item.cleanMessage.contains("Clubs Ok")) {
            Text("96 Clubs Ok", fontSize = 8.sp, color = StitchSuccess, fontFamily = FontFamily.Monospace)
        }
    }
}

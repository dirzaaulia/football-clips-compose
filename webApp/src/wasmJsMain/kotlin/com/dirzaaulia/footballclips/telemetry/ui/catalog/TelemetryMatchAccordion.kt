package com.dirzaaulia.footballclips.telemetry.ui.catalog

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryParser
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryUtils
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryMatchAccordion(
    match: ParsedMatch,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    val (badgeBg, badgeTextCol) = TelemetryUtils.getBadgeColorForMatch(match.statusType)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
    ) {
        // Accordion Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = StitchTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeTextCol.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = match.badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextCol
                    )
                }
                Text(
                    text = "${match.index}. ${match.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StitchTextPrimary
                )
            }

            Text(
                text = "${match.events.size} steps",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = StitchTextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurfaceContainerHigh)
                    .border(0.5.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Expanded Body
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Detail Header & Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Match Telemetry Details:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCopied) StitchSuccess.copy(alpha = 0.2f) else StitchSurfaceContainerHigh)
                            .border(1.dp, if (isCopied) StitchSuccess else StitchBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val fullLog = match.events.joinToString("\n") { it.eventText }
                                TelemetryUtils.copyToClipboard(fullLog)
                                isCopied = true
                                coroutineScope.launch {
                                    delay(2000)
                                    isCopied = false
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCopied) StitchSuccess else StitchTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isCopied) "Log Copied" else "Copy Log",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCopied) StitchSuccess else StitchTextSecondary
                            )
                        }
                    }
                }

                // Event Trace Lines
                match.events.forEach { ev ->
                    val cleanText = TelemetryParser.stripEmojis(ev.eventText)
                    val color = TelemetryUtils.getEventLineColor(cleanText)
                    val ytUrl = TelemetryUtils.extractYouTubeUrl(cleanText)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = cleanText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = color,
                            lineHeight = 16.sp
                        )
                        ytUrl?.let { url ->
                            Row(
                                modifier = Modifier
                                    .padding(top = 2.dp, bottom = 4.dp)
                                    .clickable { TelemetryUtils.openUrlInNewTab(url) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Watch on YouTube: $url",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

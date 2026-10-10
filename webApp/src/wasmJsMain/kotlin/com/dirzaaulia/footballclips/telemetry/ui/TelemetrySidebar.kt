package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryNavSection

@Composable
fun TelemetrySidebar(
    selectedSection: TelemetryNavSection,
    onSectionSelected: (TelemetryNavSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(210.dp)
            .fillMaxHeight()
            .background(StitchSidebar)
            .border(width = 1.dp, color = StitchBorder)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Brand & Node Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Node Icon",
                        tint = StitchPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "FC-WORKER",
                            color = StitchTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                    }
                    Text(
                        text = "TELEMETRY NODE 01",
                        color = StitchTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Engine Loop Heartbeat Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENGINE LOOP",
                        color = StitchTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "248ms tick",
                        color = StitchPrimary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = StitchPrimary,
                    trackColor = StitchBorder
                )
            }

            // Navigation Items
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SidebarNavItem(
                    title = "Telemetry Stream",
                    icon = Icons.Default.ViewStream,
                    isSelected = selectedSection == TelemetryNavSection.STREAM,
                    onClick = { onSectionSelected(TelemetryNavSection.STREAM) }
                )
                SidebarNavItem(
                    title = "Sync Cycles",
                    icon = Icons.Default.Refresh,
                    isSelected = selectedSection == TelemetryNavSection.CYCLES,
                    onClick = { onSectionSelected(TelemetryNavSection.CYCLES) }
                )
                SidebarNavItem(
                    title = "Clip Pipelines",
                    icon = Icons.Default.PlayCircle,
                    isSelected = selectedSection == TelemetryNavSection.PIPELINES,
                    onClick = { onSectionSelected(TelemetryNavSection.PIPELINES) }
                )
                SidebarNavItem(
                    title = "Worker Threads",
                    icon = Icons.Default.AccountTree,
                    isSelected = selectedSection == TelemetryNavSection.THREADS,
                    onClick = { onSectionSelected(TelemetryNavSection.THREADS) }
                )
                SidebarNavItem(
                    title = "Failure Alerts",
                    icon = Icons.Default.Warning,
                    isSelected = selectedSection == TelemetryNavSection.ALERTS,
                    onClick = { onSectionSelected(TelemetryNavSection.ALERTS) }
                )
                SidebarNavItem(
                    title = "Configuration",
                    icon = Icons.Default.Tune,
                    isSelected = selectedSection == TelemetryNavSection.CONFIG,
                    onClick = { onSectionSelected(TelemetryNavSection.CONFIG) }
                )
            }
        }

        // Bottom Stats (WASM Heap & Edge Status)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Memory, null, tint = StitchTextMuted, modifier = Modifier.size(12.dp))
                    Text("WASM HEAP", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                }
                Text("48.2 MB", fontSize = 9.sp, color = StitchPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                    Text("EDGE OK", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                }
                Text("v2.14.8", fontSize = 9.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) StitchPrimary.copy(alpha = 0.12f) else Color.Transparent
    val border = if (isSelected) StitchPrimary.copy(alpha = 0.35f) else Color.Transparent
    val tint = if (isSelected) StitchPrimary else StitchTextSecondary
    val fw = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(16.dp))
        Text(text = title, color = tint, fontSize = 11.sp, fontWeight = fw, maxLines = 1)
    }
}

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
            .width(240.dp)
            .fillMaxHeight()
            .background(StitchSidebar)
            .border(width = 1.dp, color = StitchBorder)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Brand & Node Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Node Icon",
                        tint = StitchPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "FC-WORKER",
                            color = StitchTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StitchSuccess))
                    }
                    Text(
                        text = "free-football-worker",
                        color = StitchTextMuted,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Navigation Items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SidebarNavItem(
                    title = "Pipeline Health",
                    icon = Icons.Default.MonitorHeart,
                    isSelected = selectedSection == TelemetryNavSection.STREAM,
                    onClick = { onSectionSelected(TelemetryNavSection.STREAM) }
                )
                SidebarNavItem(
                    title = "Highlight Verifier",
                    icon = Icons.Default.VideoLibrary,
                    isSelected = selectedSection == TelemetryNavSection.HIGHLIGHT_VERIFIER,
                    onClick = { onSectionSelected(TelemetryNavSection.HIGHLIGHT_VERIFIER) }
                )
                SidebarNavItem(
                    title = "Fixture & Live Sync",
                    icon = Icons.Default.Sync,
                    isSelected = selectedSection == TelemetryNavSection.FIXTURE_SYNC,
                    onClick = { onSectionSelected(TelemetryNavSection.FIXTURE_SYNC) }
                )
                SidebarNavItem(
                    title = "Standings Consistency",
                    icon = Icons.Default.EmojiEvents,
                    isSelected = selectedSection == TelemetryNavSection.STANDINGS_AUDIT,
                    onClick = { onSectionSelected(TelemetryNavSection.STANDINGS_AUDIT) }
                )
                SidebarNavItem(
                    title = "Club Notifications Log",
                    icon = Icons.Default.Notifications,
                    isSelected = selectedSection == TelemetryNavSection.NOTIFICATIONS,
                    onClick = { onSectionSelected(TelemetryNavSection.NOTIFICATIONS) }
                )
                SidebarNavItem(
                    title = "Configuration",
                    icon = Icons.Default.Tune,
                    isSelected = selectedSection == TelemetryNavSection.CONFIG,
                    onClick = { onSectionSelected(TelemetryNavSection.CONFIG) }
                )
            }
        }

        // Bottom Stats: Serverless Cloud Run & Edge Fleet Environment
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                    Text("CLOUD RUN", fontSize = 11.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                }
                Text("us-central1", fontSize = 11.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Speed, null, tint = StitchSecondary, modifier = Modifier.size(13.dp))
                    Text("CF EDGE", fontSize = 11.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                }
                Text("Global (Workers)", fontSize = 11.sp, color = StitchSecondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = title, color = tint, fontSize = 13.5.sp, fontWeight = fw, maxLines = 1)
    }
}

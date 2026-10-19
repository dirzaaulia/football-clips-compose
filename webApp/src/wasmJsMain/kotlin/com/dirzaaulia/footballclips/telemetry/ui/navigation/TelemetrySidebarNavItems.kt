package com.dirzaaulia.footballclips.telemetry.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryNavSection
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetrySidebarNavList(
    selectedSection: TelemetryNavSection,
    isExpanded: Boolean,
    onSectionSelected: (TelemetryNavSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SidebarNavItem(
            title = "Pipeline Cycles & Sweeps",
            icon = Icons.Default.SyncAlt,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.PIPELINE_CYCLES ||
                    selectedSection == TelemetryNavSection.CONSOLE_STREAM ||
                    selectedSection == TelemetryNavSection.STREAM,
            onClick = { onSectionSelected(TelemetryNavSection.PIPELINE_CYCLES) },
        )
        SidebarNavItem(
            title = "Highlight Verifier",
            icon = Icons.Default.VideoLibrary,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.HIGHLIGHT_VERIFIER,
            onClick = { onSectionSelected(TelemetryNavSection.HIGHLIGHT_VERIFIER) },
        )
        SidebarNavItem(
            title = "Fixture & Live Sync",
            icon = Icons.Default.Sync,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.FIXTURE_SYNC,
            onClick = { onSectionSelected(TelemetryNavSection.FIXTURE_SYNC) },
        )
        SidebarNavItem(
            title = "Standings Consistency",
            icon = Icons.Default.EmojiEvents,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.STANDINGS_AUDIT,
            onClick = { onSectionSelected(TelemetryNavSection.STANDINGS_AUDIT) },
        )
        SidebarNavItem(
            title = "Club Notifications Log",
            icon = Icons.Default.Notifications,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.NOTIFICATIONS,
            onClick = { onSectionSelected(TelemetryNavSection.NOTIFICATIONS) },
        )
        SidebarNavItem(
            title = "App Versions & Users",
            icon = Icons.Default.Devices,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.USER_VERSIONS,
            onClick = { onSectionSelected(TelemetryNavSection.USER_VERSIONS) },
        )
        SidebarNavItem(
            title = "Crashes & Diagnostics",
            icon = Icons.Default.BugReport,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.CRASHLYTICS,
            onClick = { onSectionSelected(TelemetryNavSection.CRASHLYTICS) },
        )
        SidebarNavItem(
            title = "Configuration",
            icon = Icons.Default.Tune,
            isExpanded = isExpanded,
            isSelected = selectedSection == TelemetryNavSection.CONFIG,
            onClick = { onSectionSelected(TelemetryNavSection.CONFIG) },
        )
    }
}

@Composable
fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
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
            .padding(
                horizontal = if (isExpanded) 10.dp else 0.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.spacedBy(10.dp) else Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        if (isExpanded) {
            Text(
                text = title,
                color = tint,
                fontSize = 12.5.sp,
                fontWeight = fw,
                maxLines = 1,
            )
        }
    }
}

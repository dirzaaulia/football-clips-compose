package com.dirzaaulia.footballclips.telemetry.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryNavSection
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSidebar
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetrySidebar(
    selectedSection: TelemetryNavSection,
    onSectionSelected: (TelemetryNavSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 68.dp,
        animationSpec = tween(durationMillis = 200),
    )

    Column(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(StitchSidebar)
            .border(width = 1.dp, color = StitchBorder)
            .padding(if (isExpanded) 14.dp else 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // TOP SECTION: Header & Scrollable Navigation Menu
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarHeader(
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded },
            )

            TelemetrySidebarNavList(
                selectedSection = selectedSection,
                isExpanded = isExpanded,
                onSectionSelected = onSectionSelected,
                modifier = Modifier.weight(1f),
            )
        }

        // Sticky Pinned Footer
        TelemetrySidebarFooter(isExpanded = isExpanded)
    }
}

@Composable
private fun SidebarHeader(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Center,
    ) {
        if (isExpanded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Node Icon",
                        tint = StitchPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = "FC-WORKER",
                            color = StitchTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                    }
                    Text(
                        text = "free-football-worker",
                        color = StitchTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = "Collapse Sidebar",
                    tint = StitchTextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleExpand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Expand Sidebar",
                        tint = StitchPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(26.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Expand Sidebar",
                        tint = StitchTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

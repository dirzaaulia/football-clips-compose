package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PRIMARY_PAGES = listOf("dashboard", "pipelines", "competitions")

@Composable
internal fun MonitorRail(
    state: MonitorState,
    action: (MonitorAction) -> Unit,
    collapsed: Boolean,
    toggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(if (collapsed) Space.iconRail else Space.rail).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(Space.small),
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            RailHeader(collapsed, toggle)
            Spacer(modifier = Modifier.height(Space.small))
            PRIMARY_PAGES.forEach { page ->
                val isSelected = state.page == page
                Hint(label(page)) {
                    TextButton(
                        onClick = { action(MonitorAction.Navigate(page)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp),
                            ),
                    ) {
                        NavigationLabel(page, collapsed, isSelected)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            TimezoneStatusPill(collapsed)
        }
    }
}

@Composable
private fun RailHeader(collapsed: Boolean, toggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (collapsed) Arrangement.Center else Arrangement.SpaceBetween,
    ) {
        if (!collapsed) {
            Text(
                text = label("app_title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        RailToggle(collapsed, toggle)
    }
}

@Composable
private fun RailToggle(collapsed: Boolean, toggle: () -> Unit) {
    val text = workspaceLabel(if (collapsed) "expand_navigation" else "collapse_navigation")
    Hint(text) {
        IconButton(onClick = toggle) {
            Icon(
                imageVector = if (collapsed) Icons.Rounded.Menu else Icons.AutoMirrored.Rounded.MenuOpen,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimezoneStatusPill(collapsed: Boolean) {
    Hint(workspaceLabel("timezone_detail")) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
        ) {
            if (collapsed) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF22C55E), CircleShape))
                }
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                    Column {
                        Text(
                            "WIB • UTC+7",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            "Asia/Jakarta Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Hint(text: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
        content = content,
    )
}

@Composable
internal fun MonitorHeader(state: MonitorState, action: (MonitorAction) -> Unit, onMenu: (() -> Unit)? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.tiny)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMenu != null) {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Rounded.Menu, workspaceLabel("menu"))
                    }
                }
                Text(
                    text = label(state.page),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                AutoRefreshButton(state.live) { action(MonitorAction.ToggleLive) }
                Button(
                    onClick = { action(MonitorAction.Refresh) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = label("refresh"),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = label("refresh"), fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = label("updated") + " · " + displayTime(state.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AutoRefreshButton(live: Boolean, onToggle: () -> Unit) {
    FilledTonalButton(
        onClick = onToggle,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (live) {
                Color(
                    0xFF22C55E,
                ).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = if (live) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            imageVector = if (live) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (live) "Pause auto-refresh" else "Resume auto-refresh",
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (live) "Auto-refresh: 15s" else "Auto-refresh: Paused",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

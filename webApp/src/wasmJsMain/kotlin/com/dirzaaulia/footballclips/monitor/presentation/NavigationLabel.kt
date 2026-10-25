package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun NavigationLabel(page: String, iconsOnly: Boolean = false, selected: Boolean = false) {
    val icon = when (page) {
        "dashboard" -> Icons.Rounded.Dashboard
        "pipelines" -> Icons.Rounded.AccountTree
        "competitions" -> Icons.Rounded.Public
        else -> Icons.Rounded.Dashboard
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.small),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (iconsOnly) label(page) else null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!iconsOnly) {
            Text(
                text = label(page),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

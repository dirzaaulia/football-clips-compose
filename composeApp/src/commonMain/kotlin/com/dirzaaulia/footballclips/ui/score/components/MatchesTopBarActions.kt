package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchesTopBarMobileActions(
    isFiltersExpanded: Boolean,
    favoriteClubsCount: Int,
    isSpoilerFreeMode: Boolean,
    onToggleFilterExpanded: () -> Unit,
    onOpenMyClubsSheet: () -> Unit,
    onToggleSpoilerFree: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(end = 4.dp),
    ) {
        IconButton(
            onClick = onToggleFilterExpanded,
            modifier = Modifier.size(38.dp),
        ) {
            Icon(
                imageVector = if (isFiltersExpanded) Icons.Default.FilterListOff else Icons.Default.FilterList,
                contentDescription = "Toggle Filters",
                tint = if (isFiltersExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        IconButton(
            onClick = onOpenMyClubsSheet,
            modifier = Modifier.size(38.dp),
        ) {
            BadgedBox(
                badge = {
                    if (favoriteClubsCount > 0) {
                        Badge(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.Black,
                        ) {
                            Text(favoriteClubsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = if (favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "My Clubs",
                    tint = if (favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        IconButton(
            onClick = onToggleSpoilerFree,
            modifier = Modifier.size(38.dp),
        ) {
            Icon(
                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Spoiler-Free Mode",
                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

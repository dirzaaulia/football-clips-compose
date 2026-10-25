package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MatchesTopBarMobileActions(
    liveMatchCount: Int,
    isLiveOnlyActive: Boolean,
    onToggleLiveOnly: () -> Unit,
    favoriteClubsCount: Int,
    isMyClubsActive: Boolean,
    onToggleMyClubs: () -> Unit,
    onOpenMyClubsSheet: () -> Unit,
    isSpoilerFreeMode: Boolean,
    onToggleSpoilerFree: () -> Unit,
    isFiltersExpanded: Boolean,
    onToggleFilterExpanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(end = 4.dp),
    ) {
        if (liveMatchCount > 0) {
            Surface(
                onClick = onToggleLiveOnly,
                shape = RoundedCornerShape(12.dp),
                color = if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    if (isLiveOnlyActive) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.5f),
                ),
                modifier = Modifier.height(28.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLiveOnlyActive) Color.White else Color(0xFFEF4444)),
                    )
                    Text(
                        text = "LIVE ($liveMatchCount)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveOnlyActive) Color.White else Color(0xFFEF4444),
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (favoriteClubsCount == 0) {
                    onOpenMyClubsSheet()
                } else {
                    onToggleMyClubs()
                }
            },
            modifier = Modifier.size(36.dp),
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
                    imageVector = if (isMyClubsActive || favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "My Clubs",
                    tint = if (isMyClubsActive || favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Box {
            IconButton(
                onClick = { showMoreMenu = true },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                modifier = Modifier
                    .width(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    text = {
                        Text(
                            text = "Spoiler-Free Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSpoilerFreeMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    trailingIcon = {
                        Switch(
                            checked = isSpoilerFreeMode,
                            onCheckedChange = null,
                            modifier = Modifier.height(24.dp),
                        )
                    },
                    onClick = {
                        onToggleSpoilerFree()
                        showMoreMenu = false
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFiltersExpanded) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    text = {
                        Text(
                            text = if (isFiltersExpanded) "Hide Filters" else "Show Filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onToggleFilterExpanded()
                        showMoreMenu = false
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

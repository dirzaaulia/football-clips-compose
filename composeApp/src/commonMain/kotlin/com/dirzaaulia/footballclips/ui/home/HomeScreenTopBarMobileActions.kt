package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.util.isWasmTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenTopBarMobileActions(
    isDarkMode: Boolean,
    isPremium: Boolean,
    isSpoilerFreeMode: Boolean,
    favoriteClubsCount: Int,
    isScrolled: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onToggleSpoilerFree: () -> Unit,
    onShowMyClubs: () -> Unit,
    onShowPaywall: () -> Unit,
    onShowFilter: () -> Unit,
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(end = 4.dp),
    ) {
        // 1. Filter Button
        IconButton(
            onClick = onShowFilter,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter Highlights",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        // 2. My Clubs Button
        IconButton(
            onClick = onShowMyClubs,
            modifier = Modifier.size(36.dp),
        ) {
            BadgedBox(
                badge = {
                    if (favoriteClubsCount > 0) {
                        Badge(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.Black,
                        ) {
                            Text(
                                text = favoriteClubsCount.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
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

        // 3. Compact Elegant Premium Diamond Button
        Surface(
            onClick = onShowPaywall,
            shape = RoundedCornerShape(10.dp),
            color = if (isPremium) {
                Color(0xFF4CAF50).copy(alpha = 0.15f)
            } else {
                Color(0xFFD4AF37).copy(alpha = 0.15f)
            },
            border = BorderStroke(
                1.dp,
                if (isPremium) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFD4AF37).copy(alpha = 0.6f),
            ),
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
                    contentDescription = if (isPremium) "Premium Member" else "Upgrade to Premium",
                    tint = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // 4. Unified Overflow More Menu (Spoiler-Free, Theme)
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
                // Spoiler Free Toggle Item
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

                // Theme Toggle Item
                if (!isWasmTarget) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        text = {
                            Text(
                                text = if (isDarkMode) "Light Mode" else "Dark Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onDarkModeToggle(!isDarkMode)
                            showMoreMenu = false
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

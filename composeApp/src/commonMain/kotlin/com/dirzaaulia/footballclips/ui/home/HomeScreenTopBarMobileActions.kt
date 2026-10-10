package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        // Filter Button
        IconButton(
            onClick = onShowFilter,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter Highlights",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // My Clubs Button
        IconButton(
            onClick = onShowMyClubs,
            modifier = Modifier.size(38.dp)
        ) {
            BadgedBox(
                badge = {
                    if (favoriteClubsCount > 0) {
                        Badge(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.Black
                        ) {
                            Text(favoriteClubsCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "My Clubs",
                    tint = if (favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Spoiler Free Mode Button
        IconButton(
            onClick = onToggleSpoilerFree,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Spoiler Free Mode",
                tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Theme Toggle Button
        if (!isWasmTarget) {
            IconButton(
                onClick = { onDarkModeToggle(!isDarkMode) },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Go Premium Button
        if (isScrolled) {
            FilledIconButton(
                onClick = onShowPaywall,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                    contentColor = if (isPremium) Color.White else Color.Black
                ),
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
                    contentDescription = "Premium",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Button(
                onClick = onShowPaywall,
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                    contentColor = if (isPremium) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (isPremium) "PREMIUM" else "GO PREMIUM",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

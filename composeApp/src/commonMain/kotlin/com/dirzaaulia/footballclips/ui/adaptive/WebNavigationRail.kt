package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.dirzaaulia.footballclips.ui.navigation.bottomNavItems
import footballclips.composeapp.generated.resources.Res
import footballclips.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun WebNavigationRail(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<com.dirzaaulia.footballclips.ui.navigation.NavDestination> = bottomNavItems,
    favoriteClubsCount: Int = 0,
    selectedSeason: Int = 2026,
    isSpoilerFree: Boolean = false,
    isPremium: Boolean = false,
    onShowMyClubs: () -> Unit = {},
    onSwitchSeason: ((Int) -> Unit)? = null,
    onToggleSpoilerFree: () -> Unit = {},
    onGoPremium: () -> Unit = {},
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .widthIn(min = 60.dp),
        windowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 6.dp, horizontal = 8.dp)
                    .size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = "Football Highlights & Clips Logo",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavigate(screen.route) },
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }

            WebRailBottomDock(
                favoriteClubsCount = favoriteClubsCount,
                selectedSeason = selectedSeason,
                isSpoilerFree = isSpoilerFree,
                isPremium = isPremium,
                onShowMyClubs = onShowMyClubs,
                onSwitchSeason = onSwitchSeason,
                onToggleSpoilerFree = onToggleSpoilerFree,
                onGoPremium = onGoPremium,
            )
        }
    }
}

@Composable
private fun WebRailBottomDock(
    favoriteClubsCount: Int,
    selectedSeason: Int,
    isSpoilerFree: Boolean,
    isPremium: Boolean,
    onShowMyClubs: () -> Unit,
    onSwitchSeason: ((Int) -> Unit)?,
    onToggleSpoilerFree: () -> Unit,
    onGoPremium: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        )
        Spacer(modifier = Modifier.height(2.dp))

        BadgedBox(
            badge = {
                if (favoriteClubsCount > 0) {
                    Badge(
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color(0xFF4FC3F7),
                    ) {
                        Text(
                            text = favoriteClubsCount.toString(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
            },
        ) {
            IconButton(
                onClick = onShowMyClubs,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "My Clubs",
                    tint = Color(0xFF7098F8),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Surface(
            onClick = {
                val next = if (selectedSeason == 2026) 2025 else 2026
                onSwitchSeason?.invoke(next)
            },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        ) {
            Text(
                text = "'${selectedSeason % 100}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        IconButton(
            onClick = onToggleSpoilerFree,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isSpoilerFree) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isSpoilerFree) "Spoiler-Free ON" else "Spoiler-Free OFF",
                tint = if (isSpoilerFree) Color(0xFF4FC3F7) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
        }

        IconButton(
            onClick = onGoPremium,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                contentDescription = if (isPremium) "Premium Active" else "Go Premium",
                tint = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    favoriteClubsCount: Int = 0,
    showScrollToTop: Boolean = false,
    isFiltersExpanded: Boolean = false,
    onNavigate: (String) -> Unit,
    onShowFilter: () -> Unit = {},
    onToggleFilter: () -> Unit = {},
    onShowMyClubs: () -> Unit = {},
    onScrollToTop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isHomeRoute = currentDestination?.route == com.dirzaaulia.footballclips.ui.navigation.NavDestination.Home.route
    val isFixturesRoute = currentDestination?.route == com.dirzaaulia.footballclips.ui.navigation.NavDestination.Fixtures.route

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .widthIn(min = 64.dp),
        windowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = "FootballClips Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }
        }
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(screen.route) },
                icon = { 
                    Icon(
                        screen.icon, 
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        // My Clubs in the top part of the navigation rail
        NavigationRailItem(
            selected = false,
            onClick = onShowMyClubs,
            icon = { 
                BadgedBox(
                    badge = {
                        if (favoriteClubsCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFFB300),
                                contentColor = Color.Black
                            ) {
                                Text("$favoriteClubsCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "My Clubs",
                        tint = if (favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        Spacer(Modifier.weight(1f))

        // Bottom Left action buttons outside dropdown (Scroll to Top & Filter with distinctive colors)
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + expandVertically() + scaleIn(),
            exit = fadeOut() + shrinkVertically() + scaleOut()
        ) {
            FilledTonalIconButton(
                onClick = onScrollToTop,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isHomeRoute || isFixturesRoute,
            enter = fadeIn() + expandVertically() + scaleIn(),
            exit = fadeOut() + shrinkVertically() + scaleOut()
        ) {
            val isActive = isFixturesRoute && isFiltersExpanded
            FilledTonalIconButton(
                onClick = if (isHomeRoute) onShowFilter else onToggleFilter,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.FilterListOff else Icons.Default.FilterList,
                    contentDescription = if (isHomeRoute) "Filter Highlights" else "Toggle Fixtures Filter",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

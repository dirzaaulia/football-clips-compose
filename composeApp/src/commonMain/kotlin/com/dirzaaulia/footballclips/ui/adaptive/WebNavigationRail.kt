package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
    favoriteClubsCount: Int = 0,
    showScrollToTop: Boolean = false,
    isFiltersExpanded: Boolean = false,
    onShowFilter: () -> Unit = {},
    onToggleFilter: () -> Unit = {},
    onShowMyClubs: () -> Unit = {},
    onScrollToTop: () -> Unit = {},
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
        bottomNavItems.forEach { screen ->
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
}

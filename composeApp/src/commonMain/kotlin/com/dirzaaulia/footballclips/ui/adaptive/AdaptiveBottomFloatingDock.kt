package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination as AndroidNavDestination
import com.dirzaaulia.footballclips.ui.components.common.FloatingPillNavigationBar
import com.dirzaaulia.footballclips.ui.home.HomeScreenFloatingActions
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.score.components.FixturesFloatingActions

@Composable
internal fun BoxScope.AdaptiveBottomFloatingDock(
    currentDestination: AndroidNavDestination?,
    navItems: List<NavDestination>,
    hasLiveMatch: Boolean,
    isHomeScrolled: Boolean,
    isFixturesScrolled: Boolean,
    bottomNavPadding: Dp,
    verticalPadding: Dp,
    onTabSwitch: (String) -> Unit,
    onHomeScrollToTop: () -> Unit,
    onFixturesScrollToTop: () -> Unit,
) {
    val isHomeRoute = currentDestination?.route == NavDestination.Home.route
    val isFixturesRoute = currentDestination?.route == NavDestination.Fixtures.route
    val showFloatingActions = (isHomeRoute && isHomeScrolled) || (isFixturesRoute && isFixturesScrolled)

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = bottomNavPadding)
            .padding(horizontal = 12.dp, vertical = verticalPadding)
            .animateContentSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FloatingPillNavigationBar(
            currentDestination = currentDestination,
            items = navItems,
            hasLiveMatch = hasLiveMatch,
            onNavigate = onTabSwitch,
        )

        AnimatedVisibility(
            visible = showFloatingActions,
            enter = fadeIn(animationSpec = tween(350)) +
                expandHorizontally(animationSpec = tween(350)) +
                scaleIn(animationSpec = tween(350)),
            exit = fadeOut(animationSpec = tween(250)) +
                shrinkHorizontally(animationSpec = tween(250)) +
                scaleOut(animationSpec = tween(250)),
        ) {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                if (isHomeRoute) {
                    HomeScreenFloatingActions(
                        onScrollToTop = onHomeScrollToTop,
                    )
                } else if (isFixturesRoute) {
                    FixturesFloatingActions(
                        onScrollToTop = onFixturesScrollToTop,
                    )
                }
            }
        }
    }
}

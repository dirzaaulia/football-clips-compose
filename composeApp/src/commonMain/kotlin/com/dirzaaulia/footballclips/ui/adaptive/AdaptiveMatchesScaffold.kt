package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dirzaaulia.footballclips.ui.components.common.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.selectVideo
import com.dirzaaulia.footballclips.ui.home.selectVideoById
import com.dirzaaulia.footballclips.ui.home.setDarkMode
import com.dirzaaulia.footballclips.ui.home.setSpoilerFreePreference
import com.dirzaaulia.footballclips.ui.home.toggleFavoriteClub
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.navigation.bottomNavItems
import com.dirzaaulia.footballclips.ui.onboarding.OnboardingScreen
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AdaptiveMatchesScaffold(
    initialMatchId: String? = null,
    onMatchConsumed: (() -> Unit)? = null,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val videoState = remember { MobileVideoPlayerState() }
    val scoreViewModel: ScoreViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()

    val vmSelectedItem by homeViewModel.selectedItem.collectAsState()
    LaunchedEffect(vmSelectedItem) {
        val item = vmSelectedItem
        if (item != null && videoState.selectedItem != item) {
            videoState.onVideoClick(item) { _ -> }
        }
    }

    LaunchedEffect(initialMatchId) {
        if (!initialMatchId.isNullOrBlank()) {
            homeViewModel.selectVideoById(initialMatchId)
            onMatchConsumed?.invoke()
        }
    }

    val navigateToTab: (String) -> Unit = remember(navController) {
        { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val isMerchandiseEnabled by homeViewModel.isMerchandiseEnabled.collectAsState()
    val navItems = remember(isMerchandiseEnabled) {
        if (isMerchandiseEnabled) bottomNavItems else bottomNavItems.filter { it != NavDestination.Store }
    }

    LaunchedEffect(isMerchandiseEnabled, currentDestination?.route) {
        if (!isMerchandiseEnabled && currentDestination?.route == NavDestination.Store.route) {
            navController.navigate(NavDestination.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route?.let { route ->
            homeViewModel.appAnalytics.trackScreenView(screenName = route)
        }
    }

    val isSpoilerFreeConfigured by homeViewModel.isSpoilerFreeConfigured.collectAsState()
    val supportedClubs by homeViewModel.supportedClubs.collectAsState()
    val isDarkMode by homeViewModel.isDarkMode.collectAsState()

    if (isSpoilerFreeConfigured == false) {
        OnboardingScreen(
            clubs = supportedClubs,
            initialDarkMode = isDarkMode,
            onComplete = { isSpoilerFree, isDark, selectedClubs ->
                homeViewModel.setSpoilerFreePreference(isSpoilerFree)
                homeViewModel.setDarkMode(isDark)
                selectedClubs.forEach { homeViewModel.toggleFavoriteClub(it) }
            },
        )
        return
    }

    val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isExpanded = maxWidth >= 840.dp
        val shouldUseNavRail = isLandscape || isExpanded
        val verticalPadding = if (isLandscape) 10.dp else 20.dp

        val favoriteClubs by homeViewModel.favoriteClubs.collectAsState()
        val isSpoilerFreeMode by homeViewModel.isSpoilerFreeMode.collectAsState()
        val selectedSeason by homeViewModel.selectedSeason.collectAsState()
        val isPremium by homeViewModel.isPremium.collectAsState()
        var showMyClubsSheet by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxSize()) {
            if (shouldUseNavRail) {
                WebNavigationRail(
                    currentDestination = currentDestination,
                    items = navItems,
                    favoriteClubsCount = favoriteClubs.size,
                    selectedSeason = selectedSeason,
                    isSpoilerFree = isSpoilerFreeMode,
                    isPremium = isPremium,
                    onShowMyClubs = { showMyClubsSheet = true },
                    onSwitchSeason = { homeViewModel.setSeason(it) },
                    onToggleSpoilerFree = { homeViewModel.setSpoilerFreePreference(!isSpoilerFreeMode) },
                    onGoPremium = { navController.navigate(NavDestination.Info.route) },
                    onNavigate = navigateToTab,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (shouldUseNavRail) {
                            Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        },
                    ),
            ) {
                AdaptiveNavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    scoreViewModel = scoreViewModel,
                    onNavigateToFixtures = {
                        scoreViewModel.selectStatusFilter(MatchStatusFilter.LIVE)
                        navigateToTab(NavDestination.Fixtures.route)
                    },
                    onVideoClick = { item, onDismiss ->
                        homeViewModel.selectVideo(item)
                        videoState.onVideoClick(item, onDismiss)
                    },
                )

                if (!shouldUseNavRail) {
                    val hasLiveMatch by scoreViewModel.hasLiveMatch.collectAsState()
                    val isHomeScrolled by homeViewModel.showScrollToTop.collectAsState()
                    val isFixturesScrolled by scoreViewModel.showScrollToTop.collectAsState()

                    AdaptiveBottomFloatingDock(
                        currentDestination = currentDestination,
                        navItems = navItems,
                        hasLiveMatch = hasLiveMatch,
                        isHomeScrolled = isHomeScrolled,
                        isFixturesScrolled = isFixturesScrolled,
                        bottomNavPadding = bottomNavPadding,
                        verticalPadding = verticalPadding,
                        onTabSwitch = { route ->
                            homeViewModel.appAnalytics.trackTabSwitch(tabName = route)
                            navigateToTab(route)
                        },
                        onHomeScrollToTop = { homeViewModel.triggerScrollToTop() },
                        onFixturesScrollToTop = { scoreViewModel.triggerScrollToTop() },
                    )
                }
            }
        }

        if (showMyClubsSheet) {
            MyClubsBottomSheet(
                selectedClubIds = favoriteClubs,
                onToggleClub = { homeViewModel.toggleFavoriteClub(it) },
                onDismissRequest = { showMyClubsSheet = false },
                clubs = supportedClubs,
            )
        }

        AdaptiveVideoOverlay(
            videoState = videoState,
            homeViewModel = homeViewModel,
            scoreViewModel = scoreViewModel,
            isLandscape = isLandscape,
            bottomPadding = bottomNavPadding,
        )
    }
}

package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dirzaaulia.footballclips.ui.components.common.FloatingPillNavigationBar
import com.dirzaaulia.footballclips.ui.home.HomeScreen
import com.dirzaaulia.footballclips.ui.home.HomeScreenFloatingActions
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.selectVideo
import com.dirzaaulia.footballclips.ui.home.selectVideoById
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.store.ClubStoreScreen
import com.dirzaaulia.footballclips.ui.score.MatchStatusFilter
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.StandingsScreen
import com.dirzaaulia.footballclips.ui.score.components.FixturesFloatingActions
import com.dirzaaulia.footballclips.ui.score.getStandings
import com.dirzaaulia.footballclips.ui.score.selectCompetition
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
            videoState.onVideoClick(item) { }
        }
    }

    LaunchedEffect(initialMatchId) {
        if (!initialMatchId.isNullOrBlank()) {
            homeViewModel.selectVideoById(initialMatchId)
            onMatchConsumed?.invoke()
        }
    }

    val homeContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToFixtures = {
                    scoreViewModel.selectStatusFilter(MatchStatusFilter.LIVE)
                    navController.navigate(NavDestination.Fixtures.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onVideoClick = { item, onDismiss ->
                    homeViewModel.selectVideo(item)
                    videoState.onVideoClick(item, onDismiss)
                },
            )
        }
    }
    val fixturesContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            MatchesAndHighlightsScreen(
                viewModel = scoreViewModel,
                onVideoClick = { item, onDismiss ->
                    homeViewModel.selectVideo(item)
                    videoState.onVideoClick(item, onDismiss)
                },
            )
        }
    }
    val standingsContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            val standingsState by scoreViewModel.standingsState.collectAsState()
            val dynamicCompetitions by scoreViewModel.competitions.collectAsState()
            val selectedCompetitionId by scoreViewModel.selectedCompetitionId.collectAsState()
            val selectedSeason by scoreViewModel.selectedSeason.collectAsState()
            val isPremium by scoreViewModel.isPremium.collectAsState()
            StandingsScreen(
                state = standingsState,
                isPremium = isPremium,
                selectedCompetitionId = selectedCompetitionId,
                dynamicCompetitions = dynamicCompetitions,
                selectedSeason = selectedSeason,
                onLeagueSelected = { _, competitionId ->
                    scoreViewModel.selectCompetition(competitionId ?: "PL")
                },
                onSeasonSelected = { season ->
                    scoreViewModel.setSeason(season)
                },
                onRetry = { scoreViewModel.getStandings() },
            )
        }
    }
    val infoContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            InfoScreen(
                onNavigateToStore = { navController.navigate(NavDestination.Store.route) },
            )
        }
    }
    val storeContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            ClubStoreScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxHeight < 500.dp
            val isExpanded = maxWidth >= 840.dp
            val shouldUseNavRail = isLandscape || isExpanded
            val verticalPadding = if (isLandscape) 10.dp else 20.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (shouldUseNavRail) {
                    WebNavigationRail(
                        currentDestination = currentDestination,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = NavDestination.Home.route,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { it / 16 } },
                        exitTransition = { fadeOut(animationSpec = tween(200)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(350)) },
                        popExitTransition = { fadeOut(animationSpec = tween(200)) },
                    ) {
                        composable(NavDestination.Home.route, content = homeContent)
                        composable(NavDestination.Fixtures.route, content = fixturesContent)
                        composable(NavDestination.Standings.route, content = standingsContent)
                        composable(NavDestination.Store.route, content = storeContent)
                        composable(NavDestination.Info.route, content = infoContent)
                    }

                    // Floating Pill Navigation Bar & Action Controls (Portrait Mode on Phone / Foldable)
                    if (!shouldUseNavRail) {
                        val hasLiveMatch by scoreViewModel.hasLiveMatch.collectAsState()
                        val isHomeScrolled by homeViewModel.showScrollToTop.collectAsState()
                        val isFixturesScrolled by scoreViewModel.showScrollToTop.collectAsState()

                        val isHomeRoute = currentDestination?.route == NavDestination.Home.route
                        val isFixturesRoute = currentDestination?.route == NavDestination.Fixtures.route
                        val showFloatingActions = (isHomeRoute && isHomeScrolled) || (isFixturesRoute && isFixturesScrolled)

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = innerPadding.calculateBottomPadding())
                                .padding(horizontal = 12.dp, vertical = verticalPadding)
                                .animateContentSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FloatingPillNavigationBar(
                                currentDestination = currentDestination,
                                hasLiveMatch = hasLiveMatch,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )

                            AnimatedVisibility(
                                visible = showFloatingActions,
                                enter = fadeIn(animationSpec = tween(350)) + expandHorizontally(animationSpec = tween(350)) + scaleIn(animationSpec = tween(350)),
                                exit = fadeOut(animationSpec = tween(250)) + shrinkHorizontally(animationSpec = tween(250)) + scaleOut(animationSpec = tween(250)),
                            ) {
                                Row(modifier = Modifier.padding(start = 8.dp)) {
                                    if (isHomeRoute) {
                                        HomeScreenFloatingActions(
                                            onScrollToTop = { homeViewModel.triggerScrollToTop() },
                                        )
                                    } else if (isFixturesRoute) {
                                        FixturesFloatingActions(
                                            onScrollToTop = { scoreViewModel.triggerScrollToTop() },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdaptiveVideoOverlay(
                videoState = videoState,
                homeViewModel = homeViewModel,
                scoreViewModel = scoreViewModel,
                isLandscape = isLandscape,
                bottomPadding = innerPadding.calculateBottomPadding(),
            )
        }
    }
}

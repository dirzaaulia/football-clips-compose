package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.FloatingPillNavigationBar
import com.dirzaaulia.footballclips.ui.components.VideoPlayerSheet
import com.dirzaaulia.footballclips.ui.home.HomeScreen
import com.dirzaaulia.footballclips.ui.home.HomeScreenFloatingActions
import com.dirzaaulia.footballclips.ui.home.HomeState
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.StandingsScreen
import com.dirzaaulia.footballclips.ui.score.components.FixturesFloatingActions
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMatchesScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val videoState = remember { MobileVideoPlayerState() }

    val scoreViewModel: ScoreViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()

    val homeContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { HomeScreen(viewModel = homeViewModel, onVideoClick = { item, onDismiss -> videoState.onVideoClick(item, onDismiss) }) }
    }
    val fixturesContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { MatchesAndHighlightsScreen(viewModel = scoreViewModel, onVideoClick = { item, onDismiss -> videoState.onVideoClick(item, onDismiss) }) }
    }
    val standingsContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        {
            val standingsState by scoreViewModel.standingsState.collectAsState()
            val selectedCompetitionId by scoreViewModel.selectedCompetitionId.collectAsState()
            val isPremium by scoreViewModel.isPremium.collectAsState()
            StandingsScreen(
                state = standingsState,
                isPremium = isPremium,
                selectedCompetitionId = selectedCompetitionId,
                onLeagueSelected = { _, competitionId ->
                    scoreViewModel.selectCompetition(competitionId ?: "PL")
                },
                onRetry = { scoreViewModel.getStandings() }
            )
        }
    }
    val infoContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { InfoScreen() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxHeight < 500.dp
            val verticalPadding = if (isLandscape) 10.dp else 20.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isLandscape) {
                    val isHomeRoute = currentDestination?.route == NavDestination.Home.route
                    val isHomeScrolled by homeViewModel.showScrollToTop.collectAsState()
                    val isFixturesScrolled by scoreViewModel.showScrollToTop.collectAsState()
                    val homeFavoriteClubs by homeViewModel.favoriteClubs.collectAsState()
                    val scoreFavoriteClubs by scoreViewModel.favoriteClubs.collectAsState()
                    val isFiltersExpanded by scoreViewModel.isFiltersExpanded.collectAsState()

                    WebNavigationRail(
                        currentDestination = currentDestination,
                        favoriteClubsCount = if (isHomeRoute) homeFavoriteClubs.size else scoreFavoriteClubs.size,
                        showScrollToTop = if (isHomeRoute) isHomeScrolled else isFixturesScrolled,
                        isFiltersExpanded = isFiltersExpanded,
                        onNavigate = { route -> navController.navigate(route) { popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        onShowFilter = { homeViewModel.setFilterSheetVisible(true) },
                        onToggleFilter = { scoreViewModel.toggleFiltersExpanded() },
                        onShowMyClubs = {
                            if (isHomeRoute) homeViewModel.setMyClubsSheetVisible(true)
                            else scoreViewModel.setMyClubsSheetVisible(true)
                        },
                        onScrollToTop = {
                            if (isHomeRoute) homeViewModel.triggerScrollToTop()
                            else scoreViewModel.triggerScrollToTop()
                        }
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
                        popExitTransition = { fadeOut(animationSpec = tween(200)) }
                    ) {
                        composable(NavDestination.Home.route, content = homeContent)
                        composable(NavDestination.Fixtures.route, content = fixturesContent)
                        composable(NavDestination.Standings.route, content = standingsContent)
                        composable(NavDestination.Info.route, content = infoContent)
                    }

                    // Floating Pill Navigation Bar & Action Controls (Portrait Mode)
                    if (!isLandscape) {
                        val hasLiveMatch by scoreViewModel.hasLiveMatch.collectAsState()

                        val isHomeScrolled by homeViewModel.showScrollToTop.collectAsState()
                        val isFixturesScrolled by scoreViewModel.showScrollToTop.collectAsState()

                        val homeFavoriteClubs by homeViewModel.favoriteClubs.collectAsState()

                        val scoreFavoriteClubs by scoreViewModel.favoriteClubs.collectAsState()
                        val isFiltersExpanded by scoreViewModel.isFiltersExpanded.collectAsState()

                        val isHomeRoute = currentDestination?.route == NavDestination.Home.route
                        val isFixturesRoute = currentDestination?.route == NavDestination.Fixtures.route
                        val showFloatingActions = (isHomeRoute && isHomeScrolled) || isFixturesRoute

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = innerPadding.calculateBottomPadding())
                                .padding(horizontal = 12.dp, vertical = verticalPadding)
                                .animateContentSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
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
                                }
                            )

                            AnimatedVisibility(
                                visible = showFloatingActions,
                                enter = fadeIn(animationSpec = tween(350)) + expandHorizontally(animationSpec = tween(350)) + scaleIn(animationSpec = tween(350)),
                                exit = fadeOut(animationSpec = tween(250)) + shrinkHorizontally(animationSpec = tween(250)) + scaleOut(animationSpec = tween(250))
                            ) {
                                Row(modifier = Modifier.padding(start = 8.dp)) {
                                    if (isHomeRoute) {
                                        HomeScreenFloatingActions(
                                            favoriteClubsCount = homeFavoriteClubs.size,
                                            showScrollToTop = isHomeScrolled,
                                            onScrollToTop = { homeViewModel.triggerScrollToTop() },
                                            onShowFilter = { homeViewModel.setFilterSheetVisible(true) },
                                            onShowMyClubs = { homeViewModel.setMyClubsSheetVisible(true) }
                                        )
                                    } else if (isFixturesRoute) {
                                        FixturesFloatingActions(
                                            isFiltersExpanded = isFiltersExpanded,
                                            favoriteClubsCount = scoreFavoriteClubs.size,
                                            showScrollToTop = isFixturesScrolled,
                                            onToggleFilter = { scoreViewModel.toggleFiltersExpanded() },
                                            onScrollToTop = { scoreViewModel.triggerScrollToTop() },
                                            onShowMyClubs = { scoreViewModel.setMyClubsSheetVisible(true) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (videoState.showVideoPlayer && videoState.selectedVideoHtml != null) {
                val selectedItem = videoState.selectedItem
                val relatedItems = remember(selectedItem) {
                    if (selectedItem == null) return@remember emptyList()
                    val leagueName = when(selectedItem) {
                        is HighlightUiItem.SupabaseMatch -> selectedItem.match.competitionName
                        is HighlightUiItem.Highlight -> selectedItem.highlight.leagueName
                        else -> ""
                    }
                    val h = (homeViewModel.uiState.value as? HomeState.Success)?.items ?: emptyList()
                    val s = (scoreViewModel.uiState.value as? ScoreState.Success)?.matches ?: emptyList()
                    val allItems: List<HighlightUiItem> = h + s
                    allItems.distinctBy { 
                        when (it) {
                            is HighlightUiItem.SupabaseMatch -> it.match.id.toString()
                            is HighlightUiItem.Highlight -> it.highlight.title
                            is HighlightUiItem.BannerAd -> it.id
                        }
                    }.filter { 
                        it != selectedItem && 
                        (it is HighlightUiItem.SupabaseMatch && it.match.competitionName == leagueName ||
                         it is HighlightUiItem.Highlight && it.highlight.leagueName == leagueName) &&
                        (it is HighlightUiItem.SupabaseMatch && it.match.highlightVideoId != null ||
                         it is HighlightUiItem.Highlight && it.highlight.embedHtml.isNotEmpty())
                    }.take(10)
                }

                VideoPlayerSheet(
                    item = selectedItem,
                    html = videoState.selectedVideoHtml!!,
                    relatedItems = relatedItems,
                    onItemClick = { item -> videoState.onVideoClick(item) {} },
                    onDismiss = { isExternal -> videoState.onDismiss(isExternal) }
                )
            }
        }
    }
}

private class MobileVideoPlayerState {
    var selectedItem by mutableStateOf<HighlightUiItem?>(null)
    var selectedVideoHtml by mutableStateOf<String?>(null)
    var showVideoPlayer by mutableStateOf(false)
    var onDismissVideo by mutableStateOf<((Boolean) -> Unit)?>(null)

    fun onVideoClick(item: HighlightUiItem, onDismiss: (Boolean) -> Unit) {
        selectedItem = item
        selectedVideoHtml = when (item) {
            is HighlightUiItem.SupabaseMatch -> item.match.highlightVideoId
            is HighlightUiItem.Highlight -> item.highlight.embedHtml
            else -> null
        }
        onDismissVideo = onDismiss
        showVideoPlayer = selectedVideoHtml != null
    }

    fun onDismiss(isExternal: Boolean) {
        showVideoPlayer = false
        onDismissVideo?.invoke(isExternal)
    }
}

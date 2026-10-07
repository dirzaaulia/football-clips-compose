package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.FilterBottomSheet
import com.dirzaaulia.footballclips.ui.components.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.home.HomeScreen
import com.dirzaaulia.footballclips.ui.home.HomeState
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.StandingsScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WebMatchesScreen(
    viewModel: HomeViewModel = koinViewModel(),
    scoreViewModel: ScoreViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val showExternalHighlights by viewModel.showExternalHighlights.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val homeMyClubsSheetVisible by viewModel.showMyClubsSheet.collectAsState()
    val scoreMyClubsSheetVisible by scoreViewModel.showMyClubsSheet.collectAsState()
    val isMyClubsSheetVisible = homeMyClubsSheetVisible || scoreMyClubsSheetVisible

    val onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit = { item, _ ->
        navController.navigate("player/${item.uniqueId}")
    }

    val homeGridState = rememberLazyGridState()
    val fixturesGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            when (currentDestination?.route) {
                NavDestination.Home.route -> {
                    val hasContent = (uiState as? HomeState.Success)?.items?.isNotEmpty() == true
                    hasContent && (homeGridState.firstVisibleItemIndex > 0 || homeGridState.firstVisibleItemScrollOffset > 30)
                }
                NavDestination.Fixtures.route -> {
                    val scoreState = scoreViewModel.uiState.value
                    val hasMatches = (scoreState as? ScoreState.Success)?.matches?.isNotEmpty() == true
                    hasMatches && (fixturesGridState.firstVisibleItemIndex > 0 || fixturesGridState.firstVisibleItemScrollOffset > 30)
                }
                else -> false
            }
        }
    }

    val isPlayerScreen = currentDestination?.route?.startsWith("player") == true

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isPlayerScreen) Color.Black else MaterialTheme.colorScheme.background)
                .onPreviewKeyEvent { 
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyUp && currentDestination?.route?.startsWith("player") == true) {
                        navController.popBackStack()
                        true
                    } else false
                }
        ) {
            if (!isPlayerScreen) {
                val homeFavoriteClubs by viewModel.favoriteClubs.collectAsState()
                val scoreFavoriteClubs by scoreViewModel.favoriteClubs.collectAsState()
                val isFiltersExpanded by scoreViewModel.isFiltersExpanded.collectAsState()

                WebNavigationRail(
                    currentDestination = currentDestination,
                    favoriteClubsCount = homeFavoriteClubs.size.coerceAtLeast(scoreFavoriteClubs.size),
                    showScrollToTop = showScrollToTop,
                    isFiltersExpanded = isFiltersExpanded,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().route ?: route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onShowFilter = { showFilterSheet = true },
                    onToggleFilter = { scoreViewModel.toggleFiltersExpanded() },
                    onShowMyClubs = {
                        scoreViewModel.setMyClubsSheetVisible(true)
                    },
                    onScrollToTop = {
                        coroutineScope.launch {
                            when (currentDestination?.route) {
                                NavDestination.Home.route -> homeGridState.animateScrollToItem(0)
                                NavDestination.Fixtures.route -> fixturesGridState.animateScrollToItem(0)
                            }
                        }
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = NavDestination.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavDestination.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            gridState = homeGridState,
                            onVideoClick = onVideoClick
                        )
                    }
                    composable(NavDestination.Fixtures.route) {
                        MatchesAndHighlightsScreen(
                            onVideoClick = onVideoClick,
                            gridState = fixturesGridState,
                            viewModel = scoreViewModel
                        )
                    }
                    composable(NavDestination.Standings.route) {
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
                    composable(NavDestination.Info.route) {
                        InfoScreen()
                    }

                    composable(
                        route = NavDestination.Player.route,
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.savedStateHandle.get<String>("itemId")
                        LaunchedEffect(itemId) {
                            if (itemId != null) {
                                viewModel.selectVideoById(itemId)
                            }
                        }

                        val selectedItem by viewModel.selectedItem.collectAsState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(100f)
                                .background(Color.Black)
                        ) {
                            MatchTheater(
                                item = selectedItem,
                                onClose = {
                                    navController.navigate(NavDestination.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onItemClick = { clickedItem ->
                                    navController.navigate("player/${clickedItem.uniqueId}") { launchSingleTop = true }
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            state = filterState,
            isLoadingMore = (uiState as? HomeState.Success)?.isLoadingMore == true,
            showLoadMore = showExternalHighlights,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onCountryToggle = viewModel::toggleCountry,
            onLeagueToggle = viewModel::toggleLeague,
            onReset = viewModel::resetFilters,
            onLoadMore = viewModel::loadMore,
            onApply = {
                viewModel.applyFilters()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (isMyClubsSheetVisible) {
        val supportedClubs by scoreViewModel.supportedClubs.collectAsState()
        val favoriteClubs by scoreViewModel.favoriteClubs.collectAsState()
        MyClubsBottomSheet(
            selectedClubIds = favoriteClubs,
            onToggleClub = { scoreViewModel.toggleFavoriteClub(it) },
            onDismissRequest = {
                viewModel.setMyClubsSheetVisible(false)
                scoreViewModel.setMyClubsSheetVisible(false)
            },
            clubs = supportedClubs
        )
    }
}

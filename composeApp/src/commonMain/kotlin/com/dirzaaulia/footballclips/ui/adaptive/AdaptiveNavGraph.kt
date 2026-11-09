package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.home.HomeScreen
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.ui.score.StandingsScreen
import com.dirzaaulia.footballclips.ui.score.getStandings
import com.dirzaaulia.footballclips.ui.score.selectCompetition
import com.dirzaaulia.footballclips.ui.shorts.ShortsRoute
import com.dirzaaulia.footballclips.ui.shorts.ShortsViewModel
import com.dirzaaulia.footballclips.ui.store.ClubStoreScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AdaptiveNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    scoreViewModel: ScoreViewModel,
    onNavigateToFixtures: () -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Home.route,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { it / 16 } },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(350)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) },
    ) {
        addPrimaryNavRoutes(
            navController = navController,
            homeViewModel = homeViewModel,
            scoreViewModel = scoreViewModel,
            onNavigateToFixtures = onNavigateToFixtures,
            onVideoClick = onVideoClick,
        )
        addSecondaryNavRoutes(
            navController = navController,
            scoreViewModel = scoreViewModel,
        )
    }
}

private fun NavGraphBuilder.addPrimaryNavRoutes(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    scoreViewModel: ScoreViewModel,
    onNavigateToFixtures: () -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
) {
    composable(NavDestination.Home.route) {
        HomeScreen(
            viewModel = homeViewModel,
            onNavigateToFixtures = onNavigateToFixtures,
            onNavigateToStore = { navController.navigate(NavDestination.Store.route) },
            onNavigateToSettings = { navController.navigate(NavDestination.Info.route) },
            onVideoClick = onVideoClick,
        )
    }
    composable(NavDestination.Shorts.route) {
        val shortsViewModel: ShortsViewModel = koinViewModel()
        ShortsRoute(viewModel = shortsViewModel)
    }
    composable(NavDestination.Fixtures.route) {
        MatchesAndHighlightsScreen(
            viewModel = scoreViewModel,
            onVideoClick = onVideoClick,
        )
    }
}

private fun NavGraphBuilder.addSecondaryNavRoutes(
    navController: NavHostController,
    scoreViewModel: ScoreViewModel,
) {
    composable(NavDestination.Standings.route) {
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
            onLeagueSelected = { _, compId -> scoreViewModel.selectCompetition(compId ?: "PL") },
            onSeasonSelected = { season -> scoreViewModel.setSeason(season) },
            onRetry = { scoreViewModel.getStandings() },
        )
    }
    composable(NavDestination.Store.route) {
        ClubStoreScreen(onBack = { navController.popBackStack() })
    }
    composable(NavDestination.Info.route) {
        InfoScreen(onNavigateToStore = { navController.navigate(NavDestination.Store.route) })
    }
}

package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedContentScope
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
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.util.extractVideoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMatchesScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val videoState = remember { MobileVideoPlayerState() }

    val homeContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { HomeScreen(onVideoClick = { item, onDismiss -> videoState.onVideoClick(item, onDismiss) }) }
    }
    val fixturesContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { MatchesAndHighlightsScreen(onVideoClick = { item, onDismiss -> videoState.onVideoClick(item, onDismiss) }) }
    }
    val infoContent: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit = remember {
        { InfoScreen() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = NavDestination.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavDestination.Home.route, content = homeContent)
                composable(NavDestination.Fixtures.route, content = fixturesContent)
                composable(NavDestination.Info.route, content = infoContent)
            }

            // Floating Pill Navigation Bar
            FloatingPillNavigationBar(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(innerPadding)
            )

            if (videoState.showVideoPlayer && videoState.selectedVideoHtml != null) {
                VideoPlayerSheet(
                    html = videoState.selectedVideoHtml!!,
                    onDismiss = { isExternal -> videoState.onDismiss(isExternal) }
                )
            }
        }
    }
}

private class MobileVideoPlayerState {
    var selectedVideoHtml by mutableStateOf<String?>(null)
    var showVideoPlayer by mutableStateOf(false)
    var onDismissVideo by mutableStateOf<((Boolean) -> Unit)?>(null)

    fun onVideoClick(item: HighlightUiItem, onDismiss: (Boolean) -> Unit) {
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

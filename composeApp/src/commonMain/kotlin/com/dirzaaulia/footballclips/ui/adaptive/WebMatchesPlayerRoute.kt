package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.selectVideoById
import com.dirzaaulia.footballclips.ui.navigation.NavDestination

@Composable
internal fun WebPlayerRoute(
    backStackEntry: NavBackStackEntry,
    viewModel: HomeViewModel,
    navController: NavController,
) {
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
            .background(Color.Black),
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
            viewModel = viewModel,
        )
    }
}

package com.dirzaaulia.footballclips.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : NavDestination("home", "Highlights", Icons.Default.VideoLibrary)
    data object Fixtures : NavDestination("fixtures", "Fixtures", Icons.Default.EventNote)
    data object Info : NavDestination("info", "Info", Icons.Default.Info)
    data object Player : NavDestination("player/{itemId}", "Player", Icons.Default.VideoLibrary)
}

val bottomNavItems = listOf(
    NavDestination.Home,
    NavDestination.Fixtures,
    NavDestination.Info
)

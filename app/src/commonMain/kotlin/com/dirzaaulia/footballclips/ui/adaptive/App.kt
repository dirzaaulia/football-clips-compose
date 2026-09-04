package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.ui.theme.FootballClipsTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App() {
    val preferenceManager: PreferenceManager = koinInject()
    val isDarkMode by preferenceManager.isDarkMode.collectAsState(initial = true)

    FootballClipsTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val windowWidthSizeClass = when {
                    maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                    maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                    else -> WindowWidthSizeClass.Expanded
                }
                val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded
                
                if (isExpanded) {
                    WebMatchesScreen()
                } else {
                    MobileMatchesScreen()
                }
            }
        }
    }
}

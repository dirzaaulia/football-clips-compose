package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.filter.SeasonDropdownSelector
import com.dirzaaulia.footballclips.util.isDebugBuild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenTopBar(
    isWeb: Boolean,
    isLandscape: Boolean = false,
    isDarkMode: Boolean,
    isPremium: Boolean,
    isSpoilerFreeMode: Boolean,
    favoriteClubsCount: Int = 0,
    isScrolled: Boolean = false,
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    onTitleTap: () -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onToggleSpoilerFree: () -> Unit,
    onShowMyClubs: () -> Unit = {},
    onShowPaywall: () -> Unit,
    onShowFilter: () -> Unit,
) {
    TopAppBar(
        windowInsets = if (isLandscape) WindowInsets(0, 0, 0, 0) else TopAppBarDefaults.windowInsets,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Highlights",
                    style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (isWeb) 1.sp else (-0.5).sp,
                    modifier = Modifier.clickable {
                        if (!isWeb && isDebugBuild) {
                            onTitleTap()
                        }
                    },
                )

                SeasonDropdownSelector(
                    selectedSeason = selectedSeason,
                    onSeasonSelected = onSeasonSelected,
                    isCompact = !isWeb,
                )
            }
        },
        actions = {
            if (isWeb) {
                HomeScreenTopBarWebActions(
                    isDarkMode = isDarkMode,
                    isPremium = isPremium,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    favoriteClubsCount = favoriteClubsCount,
                    onDarkModeToggle = onDarkModeToggle,
                    onToggleSpoilerFree = onToggleSpoilerFree,
                    onShowMyClubs = onShowMyClubs,
                    onShowPaywall = onShowPaywall,
                )
            } else {
                HomeScreenTopBarMobileActions(
                    isDarkMode = isDarkMode,
                    isPremium = isPremium,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    favoriteClubsCount = favoriteClubsCount,
                    isScrolled = isScrolled,
                    onDarkModeToggle = onDarkModeToggle,
                    onToggleSpoilerFree = onToggleSpoilerFree,
                    onShowMyClubs = onShowMyClubs,
                    onShowPaywall = onShowPaywall,
                    onShowFilter = onShowFilter,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        ),
    )
}

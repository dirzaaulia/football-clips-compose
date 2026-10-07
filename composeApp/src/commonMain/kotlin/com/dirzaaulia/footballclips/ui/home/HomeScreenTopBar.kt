package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.util.isDebugBuild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenTopBar(
    isWeb: Boolean,
    isDarkMode: Boolean,
    isPremium: Boolean,
    isSpoilerFreeMode: Boolean,
    favoriteClubsCount: Int = 0,
    isScrolled: Boolean = false,
    onTitleTap: () -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onToggleSpoilerFree: () -> Unit,
    onShowMyClubs: () -> Unit = {},
    onShowPaywall: () -> Unit,
    onShowFilter: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isWeb) "Football Highlights & Clips" else "Highlights",
                style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = if (isWeb) 1.sp else 0.sp,
                modifier = Modifier.clickable {
                    if (!isWeb && isDebugBuild) {
                        onTitleTap()
                    }
                }
            )
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
                    onShowPaywall = onShowPaywall
                )
            } else {
                HomeScreenTopBarMobileActions(
                    isDarkMode = isDarkMode,
                    isPremium = isPremium,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    isScrolled = isScrolled,
                    onDarkModeToggle = onDarkModeToggle,
                    onToggleSpoilerFree = onToggleSpoilerFree,
                    onShowPaywall = onShowPaywall
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    )
}

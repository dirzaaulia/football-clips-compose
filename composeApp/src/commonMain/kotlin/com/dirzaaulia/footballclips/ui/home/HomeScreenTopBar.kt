package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
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
import com.dirzaaulia.footballclips.util.isDeveloperModeAllowed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenTopBar(
    isWeb: Boolean,
    isLandscape: Boolean = false,
    isPremium: Boolean,
    favoriteClubsCount: Int = 0,
    selectedSeason: Int = 2026,
    onSeasonSelected: (Int) -> Unit = {},
    onTitleTap: () -> Unit,
    onShowMyClubs: () -> Unit = {},
    onShowPaywall: () -> Unit,
    onShowFilter: () -> Unit,
) {
    val statusBarInset = if (isLandscape) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    TopAppBar(
        modifier = if (isWeb) Modifier else Modifier.height(50.dp + statusBarInset),
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
                        if (!isWeb && isDeveloperModeAllowed) {
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
                    isPremium = isPremium,
                    onShowPaywall = onShowPaywall,
                )
            } else {
                HomeScreenTopBarMobileActions(
                    isPremium = isPremium,
                    favoriteClubsCount = favoriteClubsCount,
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

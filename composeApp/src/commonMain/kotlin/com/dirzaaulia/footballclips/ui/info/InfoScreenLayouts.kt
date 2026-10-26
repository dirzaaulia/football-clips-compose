package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.Profile

@Composable
internal fun InfoSettingsLandscapeGrid(
    profile: Profile?,
    isPremium: Boolean,
    isDarkMode: Boolean,
    isMerchandiseEnabled: Boolean,
    favoriteCount: Int,
    uriHandler: UriHandler,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenMyClubs: () -> Unit,
    onToggleMerchandise: (Boolean) -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 60.dp, start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AccountProfileCard(
                profile = profile,
                isPremium = isPremium,
                onSignIn = onSignIn,
                onSignOut = onSignOut,
            )
        }
        item {
            ThemeSettingsCard(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
            )
        }
        item {
            MyClubsSettingsCard(
                favoriteCount = favoriteCount,
                onOpenMyClubs = onOpenMyClubs,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            MerchandiseSettingsCard(
                isEnabled = isMerchandiseEnabled,
                isPremium = isPremium,
                onToggle = onToggleMerchandise,
                onShowPaywall = onShowPaywall,
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SupportDeveloperCard(
                initialExpanded = false,
                uriHandler = uriHandler,
            )
        }
        item { DataSourceCard(initialExpanded = false) }
        item { AccountPurchasesCard(initialExpanded = false) }
        if (isMerchandiseEnabled) {
            item { AmazonAffiliateDisclosureCard(initialExpanded = false) }
        }
        item { DisclaimerCard(initialExpanded = false) }
        item { PrivacyPolicyCard(initialExpanded = false) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InfoSettingsWideLayout(
    profile: Profile?,
    isPremium: Boolean,
    isDarkMode: Boolean,
    isMerchandiseEnabled: Boolean,
    favoriteCount: Int,
    scrollState: ScrollState,
    uriHandler: UriHandler,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenMyClubs: () -> Unit,
    onToggleMerchandise: (Boolean) -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        WasmHeroBanner()

        AccountProfileCard(
            profile = profile,
            isPremium = isPremium,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            maxItemsInEachRow = 2,
        ) {
            ThemeSettingsCard(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
            MyClubsSettingsCard(
                favoriteCount = favoriteCount,
                onOpenMyClubs = onOpenMyClubs,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
            MerchandiseSettingsCard(
                isEnabled = isMerchandiseEnabled,
                isPremium = isPremium,
                onToggle = onToggleMerchandise,
                onShowPaywall = onShowPaywall,
                modifier = Modifier.fillMaxWidth(),
            )
            SupportDeveloperCard(
                initialExpanded = false,
                uriHandler = uriHandler,
                modifier = Modifier.fillMaxWidth(),
            )
            DataSourceCard(
                initialExpanded = false,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
            AccountPurchasesCard(
                initialExpanded = false,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
            if (isMerchandiseEnabled) {
                AmazonAffiliateDisclosureCard(
                    initialExpanded = false,
                    modifier = Modifier.weight(1f).widthIn(min = 280.dp),
                )
            }
            DisclaimerCard(
                initialExpanded = false,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
            PrivacyPolicyCard(
                initialExpanded = false,
                modifier = Modifier.weight(1f).widthIn(min = 280.dp),
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
internal fun InfoSettingsPortraitLayout(
    profile: Profile?,
    isPremium: Boolean,
    isDarkMode: Boolean,
    isMerchandiseEnabled: Boolean,
    favoriteCount: Int,
    scrollState: ScrollState,
    uriHandler: UriHandler,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenMyClubs: () -> Unit,
    onToggleMerchandise: (Boolean) -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccountProfileCard(
            profile = profile,
            isPremium = isPremium,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
        )
        ThemeSettingsCard(
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
        )
        MyClubsSettingsCard(
            favoriteCount = favoriteCount,
            onOpenMyClubs = onOpenMyClubs,
        )
        MerchandiseSettingsCard(
            isEnabled = isMerchandiseEnabled,
            isPremium = isPremium,
            onToggle = onToggleMerchandise,
            onShowPaywall = onShowPaywall,
        )
        SupportDeveloperCard(
            initialExpanded = false,
            uriHandler = uriHandler,
        )
        DataSourceCard(initialExpanded = false)
        AccountPurchasesCard(initialExpanded = false)
        if (isMerchandiseEnabled) {
            AmazonAffiliateDisclosureCard(initialExpanded = false)
        }
        DisclaimerCard(initialExpanded = false)
        PrivacyPolicyCard(initialExpanded = false)

        Spacer(modifier = Modifier.height(120.dp))
    }
}

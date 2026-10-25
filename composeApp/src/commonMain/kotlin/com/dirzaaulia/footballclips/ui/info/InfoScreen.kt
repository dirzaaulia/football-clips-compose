package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.ui.components.common.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.components.paywall.PaywallBottomSheet
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.purchasePackage
import com.dirzaaulia.footballclips.ui.home.setDarkMode
import com.dirzaaulia.footballclips.ui.home.signIn
import com.dirzaaulia.footballclips.ui.home.signOut
import com.dirzaaulia.footballclips.ui.home.toggleFavoriteClub
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateToStore: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val preferenceManager: PreferenceManager? = if (!isPreview) koinInject() else null
    val homeViewModel: HomeViewModel? = if (!isPreview) koinViewModel() else null
    val isPremium = homeViewModel?.isPremium?.collectAsState()?.value ?: true
    val isMerchandiseEnabled = preferenceManager?.isMerchandiseEnabled?.collectAsState(initial = true)?.value ?: true
    val isDarkMode = homeViewModel?.isDarkMode?.collectAsState()?.value ?: true
    val favoriteClubs = homeViewModel?.favoriteClubs?.collectAsState()?.value ?: emptySet()
    val supportedClubs = homeViewModel?.supportedClubs?.collectAsState()?.value ?: emptyList()
    val profile = homeViewModel?.currentUserProfile?.collectAsState(initial = null)?.value
    val customerInfo = homeViewModel?.customerInfo?.collectAsState(initial = null)?.value
    val offerings = homeViewModel?.offerings?.collectAsState()?.value
    val isBillingLoading = homeViewModel?.isBillingLoading?.collectAsState()?.value ?: false
    var showPaywall by remember { mutableStateOf(false) }
    var showMyClubsSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val onToggleMerchandise: (Boolean) -> Unit = { enabled ->
        coroutineScope.launch { preferenceManager?.setMerchandiseEnabled(enabled) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isWide = maxWidth >= 600.dp && !isLandscape

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = if (isWide) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (isWide) 1.sp else (-0.5).sp,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    ),
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (isLandscape) {
                    InfoSettingsLandscapeGrid(
                        profile = profile,
                        isPremium = isPremium,
                        isDarkMode = isDarkMode,
                        isMerchandiseEnabled = isMerchandiseEnabled,
                        favoriteCount = favoriteClubs.size,
                        uriHandler = uriHandler,
                        onSignIn = { homeViewModel?.signIn() },
                        onSignOut = { homeViewModel?.signOut() },
                        onToggleDarkMode = { dark -> homeViewModel?.setDarkMode(dark) },
                        onOpenMyClubs = { showMyClubsSheet = true },
                        onToggleMerchandise = onToggleMerchandise,
                        onShowPaywall = { showPaywall = true },
                    )
                } else if (isWide) {
                    InfoSettingsWideLayout(
                        profile = profile,
                        isPremium = isPremium,
                        isDarkMode = isDarkMode,
                        isMerchandiseEnabled = isMerchandiseEnabled,
                        favoriteCount = favoriteClubs.size,
                        scrollState = scrollState,
                        uriHandler = uriHandler,
                        onSignIn = { homeViewModel?.signIn() },
                        onSignOut = { homeViewModel?.signOut() },
                        onToggleDarkMode = { dark -> homeViewModel?.setDarkMode(dark) },
                        onOpenMyClubs = { showMyClubsSheet = true },
                        onToggleMerchandise = onToggleMerchandise,
                        onShowPaywall = { showPaywall = true },
                    )
                } else {
                    InfoSettingsPortraitLayout(
                        profile = profile,
                        isPremium = isPremium,
                        isDarkMode = isDarkMode,
                        isMerchandiseEnabled = isMerchandiseEnabled,
                        favoriteCount = favoriteClubs.size,
                        scrollState = scrollState,
                        uriHandler = uriHandler,
                        onSignIn = { homeViewModel?.signIn() },
                        onSignOut = { homeViewModel?.signOut() },
                        onToggleDarkMode = { dark -> homeViewModel?.setDarkMode(dark) },
                        onOpenMyClubs = { showMyClubsSheet = true },
                        onToggleMerchandise = onToggleMerchandise,
                        onShowPaywall = { showPaywall = true },
                    )
                }
            }
        }

        if (showMyClubsSheet) {
            MyClubsBottomSheet(
                selectedClubIds = favoriteClubs,
                onToggleClub = { clubId -> homeViewModel?.toggleFavoriteClub(clubId) },
                onDismissRequest = { showMyClubsSheet = false },
                clubs = supportedClubs,
            )
        }

        if (showPaywall) {
            PaywallBottomSheet(
                isPremium = isPremium,
                profile = profile,
                customerInfo = customerInfo,
                offerings = offerings,
                isLoading = isBillingLoading,
                onSignInClick = { homeViewModel?.signIn() },
                onPurchaseClick = { pkg -> homeViewModel?.purchasePackage(pkg) },
                onSignOutClick = { homeViewModel?.signOut() },
                onDismiss = { showPaywall = false },
            )
        }
    }
}

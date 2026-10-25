package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.ui.components.paywall.PaywallBottomSheet
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.purchasePackage
import com.dirzaaulia.footballclips.ui.home.signOut
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val preferenceManager: PreferenceManager? = if (!isPreview) koinInject() else null
    val homeViewModel: HomeViewModel? = if (!isPreview) koinViewModel() else null
    val isPremium = homeViewModel?.isPremium?.collectAsState()?.value ?: true
    val isMerchandiseEnabled = preferenceManager?.isMerchandiseEnabled?.collectAsState(initial = true)?.value ?: true
    val profile = homeViewModel?.currentUserProfile?.collectAsState(initial = null)?.value
    val customerInfo = homeViewModel?.customerInfo?.collectAsState(initial = null)?.value
    val offerings = homeViewModel?.offerings?.collectAsState()?.value
    val isBillingLoading = homeViewModel?.isBillingLoading?.collectAsState()?.value ?: false
    var showPaywall by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val onToggleMerchandise: (Boolean) -> Unit = { enabled ->
        coroutineScope.launch {
            preferenceManager?.setMerchandiseEnabled(enabled)
        }
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
                            "Information",
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 60.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            MerchandiseSettingsCard(
                                isEnabled = isMerchandiseEnabled,
                                isPremium = isPremium,
                                onToggle = onToggleMerchandise,
                                onShowPaywall = { showPaywall = true },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SupportDeveloperCard(
                                initialExpanded = true,
                                uriHandler = uriHandler,
                            )
                        }
                        item { DataSourceCard(initialExpanded = false) }
                        item { AccountPurchasesCard(initialExpanded = false) }
                        item { AmazonAffiliateDisclosureCard(initialExpanded = false) }
                        item { DisclaimerCard(initialExpanded = false) }
                        item { PrivacyPolicyCard(initialExpanded = false) }
                    }
                } else if (isWide) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        WasmHeroBanner()

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            maxItemsInEachRow = 2,
                        ) {
                            MerchandiseSettingsCard(
                                isEnabled = isMerchandiseEnabled,
                                isPremium = isPremium,
                                onToggle = onToggleMerchandise,
                                onShowPaywall = { showPaywall = true },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            SupportDeveloperCard(
                                initialExpanded = true,
                                uriHandler = uriHandler,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            DataSourceCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 280.dp),
                            )
                            AccountPurchasesCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 280.dp),
                            )
                            AmazonAffiliateDisclosureCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 280.dp),
                            )
                            DisclaimerCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 280.dp),
                            )
                            PrivacyPolicyCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 280.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MerchandiseSettingsCard(
                            isEnabled = isMerchandiseEnabled,
                            isPremium = isPremium,
                            onToggle = onToggleMerchandise,
                            onShowPaywall = { showPaywall = true },
                        )
                        SupportDeveloperCard(
                            initialExpanded = true,
                            uriHandler = uriHandler,
                        )
                        DataSourceCard(initialExpanded = false)
                        AccountPurchasesCard(initialExpanded = false)
                        AmazonAffiliateDisclosureCard(initialExpanded = false)
                        DisclaimerCard(initialExpanded = false)
                        PrivacyPolicyCard(initialExpanded = false)

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }

        if (showPaywall) {
            PaywallBottomSheet(
                isPremium = isPremium,
                profile = profile,
                customerInfo = customerInfo,
                offerings = offerings,
                isLoading = isBillingLoading,
                onSignInClick = { /* Handled via Settings */ },
                onPurchaseClick = { pkg -> homeViewModel?.purchasePackage(pkg) },
                onSignOutClick = { homeViewModel?.signOut() },
                onDismiss = { showPaywall = false },
            )
        }
    }
}

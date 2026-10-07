package com.dirzaaulia.footballclips.ui.home

import androidx.compose.runtime.Composable
import com.dirzaaulia.footballclips.data.billing.CustomerInfoModel
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.ui.components.DeveloperOptionsBottomSheet
import com.dirzaaulia.footballclips.ui.components.FilterBottomSheet
import com.dirzaaulia.footballclips.ui.components.PaywallBottomSheet

@Composable
internal fun HomeScreenDialogs(
    showPaywall: Boolean,
    showFilterSheet: Boolean,
    showDevScreen: Boolean,
    isPremium: Boolean,
    profile: Profile?,
    customerInfo: CustomerInfoModel?,
    offerings: Any?,
    isBillingLoading: Boolean,
    filterState: FilterState,
    isLoadingMore: Boolean,
    showExternalHighlights: Boolean,
    isDebugPremium: Boolean,
    isForceNonPremium: Boolean,
    viewModel: HomeViewModel,
    onSignIn: () -> Unit,
    onDismissPaywall: () -> Unit,
    onDismissFilter: () -> Unit,
    onDismissDev: () -> Unit
) {
    if (showPaywall) {
        PaywallBottomSheet(
            isPremium = isPremium,
            profile = profile,
            customerInfo = customerInfo,
            offerings = offerings,
            isLoading = isBillingLoading,
            onSignInClick = onSignIn,
            onPurchaseClick = { rcPackage -> viewModel.purchasePackage(rcPackage) },
            onRestoreClick = { viewModel.restorePurchases() },
            onDismiss = onDismissPaywall
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            state = filterState,
            isLoadingMore = isLoadingMore,
            showLoadMore = showExternalHighlights,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onCountryToggle = viewModel::toggleCountry,
            onLeagueToggle = viewModel::toggleLeague,
            onReset = viewModel::resetFilters,
            onLoadMore = viewModel::loadMore,
            onApply = {
                viewModel.applyFilters()
                onDismissFilter()
            },
            onDismiss = onDismissFilter
        )
    }

    if (showDevScreen) {
        DeveloperOptionsBottomSheet(
            isDebugPremium = isDebugPremium,
            onToggleDebugPremium = { enabled -> viewModel.setDebugPremium(enabled) },
            isForceNonPremium = isForceNonPremium,
            onToggleForceNonPremium = { enabled -> viewModel.setForceNonPremium(enabled) },
            onOpenAdInspector = { viewModel.openAdInspector() },
            onDismiss = onDismissDev
        )
    }
}

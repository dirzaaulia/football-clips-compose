package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

internal fun handlePurchasePackage(
    scope: CoroutineScope,
    packageToPurchase: Any,
    billingManager: BillingManager,
    profilesRepository: ProfilesRepository
) {
    billingManager.purchasePackage(packageToPurchase) { isSuccess ->
        if (isSuccess) {
            scope.launch {
                val profile = profilesRepository.profile.firstOrNull()
                if (profile != null) {
                    profilesRepository.updatePremiumStatus(true)
                }
            }
        }
    }
}

internal fun handleRestorePurchases(
    scope: CoroutineScope,
    billingManager: BillingManager,
    profilesRepository: ProfilesRepository,
    onError: suspend (String) -> Unit
) {
    billingManager.restorePurchases { isRestoredPremium ->
        if (isRestoredPremium) {
            scope.launch {
                val profile = profilesRepository.profile.firstOrNull()
                if (profile != null) {
                    profilesRepository.updatePremiumStatus(true)
                } else {
                    onError("Purchases restored on Google Play! Sign in with Google to sync your Premium status across all platforms.")
                }
            }
        }
    }
}

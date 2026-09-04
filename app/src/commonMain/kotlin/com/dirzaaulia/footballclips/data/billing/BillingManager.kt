package com.dirzaaulia.footballclips.data.billing

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isPremium: StateFlow<Boolean>
    val offerings: StateFlow<Any?>
    val customerInfo: StateFlow<CustomerInfoModel?>
    val isLoading: StateFlow<Boolean>
    val errorEvent: SharedFlow<String>
    val messageEvent: SharedFlow<String>

    fun identify(userId: String)
    fun purchasePackage(packageToPurchase: Any)
    fun restorePurchases()
    fun logOut()
}

data class CustomerInfoModel(
    val platform: String,
    val userId: String,
    val isActive: Boolean
)

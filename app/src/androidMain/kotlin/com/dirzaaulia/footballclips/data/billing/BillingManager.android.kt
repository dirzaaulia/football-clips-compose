package com.dirzaaulia.footballclips.data.billing

import android.content.Context
import com.dirzaaulia.footballclips.FootballClipsApplication
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AndroidBillingManager(
    private val context: Context
) : BillingManager {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    override val offerings: StateFlow<Any?> = _offerings.asStateFlow()

    private val _customerInfo = MutableStateFlow<CustomerInfoModel?>(null)
    override val customerInfo: StateFlow<CustomerInfoModel?> = _customerInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    override val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    override val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    init {
        if (Purchases.isConfigured) {
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                updatePremiumStatus(customerInfo)
            }
            
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updatePremiumStatus(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    sendError("Could not check subscription status: ${error.message}")
                }
            })

            loadOfferings()
        }
    }

    override fun identify(userId: String) {
        if (!Purchases.isConfigured) return
        _isLoading.value = true
        Purchases.sharedInstance.logIn(userId, object : LogInCallback {
            override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                updatePremiumStatus(customerInfo)
                _isLoading.value = false
            }

            override fun onError(error: PurchasesError) {
                sendError("Billing login failed: ${error.message}")
                _isLoading.value = false
            }
        })
    }

    override fun logOut() {
        if (!Purchases.isConfigured) return
        if (!Purchases.sharedInstance.isAnonymous) {
            Purchases.sharedInstance.logOut(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    updatePremiumStatus(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    sendError("Billing logout error: ${error.message}")
                }
            })
        }
    }

    private fun updatePremiumStatus(customerInfo: CustomerInfo) {
        val isActive = customerInfo.entitlements["remove_ads"]?.isActive == true
        _isPremium.value = isActive
        _customerInfo.value = CustomerInfoModel(
            platform = "Google Play Store",
            userId = customerInfo.originalAppUserId,
            isActive = isActive
        )
    }

    private fun loadOfferings() {
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _offerings.value = offerings
            }

            override fun onError(error: PurchasesError) {
                sendError("Failed to load in-app packages: ${error.message}")
            }
        })
    }

    override fun purchasePackage(packageToPurchase: Any, onComplete: ((Boolean) -> Unit)?) {
        if (!Purchases.isConfigured) return
        val rcPackage = packageToPurchase as? Package
        if (rcPackage == null) {
            sendError("Invalid purchase package selected. Please try again.")
            onComplete?.invoke(false)
            return
        }

        val activity = FootballClipsApplication.getCurrentActivity()
        if (activity == null) {
            sendError("Unable to open Google Play Store. App window not ready.")
            onComplete?.invoke(false)
            return
        }

        _isLoading.value = true

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, rcPackage).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    updatePremiumStatus(customerInfo)
                    sendMessage("Purchase completed successfully! Premium features unlocked.")
                    _isLoading.value = false
                    val isActive = customerInfo.entitlements["remove_ads"]?.isActive == true
                    onComplete?.invoke(isActive)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    if (userCancelled) {
                        sendMessage("Purchase was cancelled.")
                    } else {
                        sendError("Purchase failed: ${error.message}")
                    }
                    _isLoading.value = false
                    onComplete?.invoke(false)
                }
            }
        )
    }

    override fun restorePurchases(onComplete: ((Boolean) -> Unit)?) {
        if (!Purchases.isConfigured) {
            onComplete?.invoke(false)
            return
        }
        _isLoading.value = true

        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePremiumStatus(customerInfo)
                val isPrem = _isPremium.value
                if (isPrem) {
                    sendMessage("Purchases restored successfully! Premium access is active.")
                } else {
                    sendMessage("No active purchases or subscriptions found on this Google Play account.")
                }
                _isLoading.value = false
                onComplete?.invoke(isPrem)
            }

            override fun onError(error: PurchasesError) {
                sendError("Restore failed: ${error.message}")
                _isLoading.value = false
                onComplete?.invoke(false)
            }
        })
    }

    private fun sendError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _errorEvent.emit(message)
        }
    }

    private fun sendMessage(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _messageEvent.emit(message)
        }
    }
}

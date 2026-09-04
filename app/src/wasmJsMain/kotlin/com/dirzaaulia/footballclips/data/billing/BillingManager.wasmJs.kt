package com.dirzaaulia.footballclips.data.billing

import com.dirzaaulia.footballclips.data.constants.AdConfiguration
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WasmBillingManager : BillingManager {
    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _offerings = MutableStateFlow<Any?>(null)
    override val offerings: StateFlow<Any?> = _offerings.asStateFlow()

    private val _customerInfo = MutableStateFlow<CustomerInfoModel?>(null)
    override val customerInfo: StateFlow<CustomerInfoModel?> = _customerInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    override val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    override val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private var currentUserId: String? = null

    init {
        // Mock offerings for WASM to show the button
        _offerings.value = object {
            val price = "$0.00"
            val rcPackage = "wasm_package"
        }
    }

    override fun identify(userId: String) {
        currentUserId = userId
        _customerInfo.value = CustomerInfoModel(
            platform = "Web (Paddle)",
            userId = userId,
            isActive = _isPremium.value
        )
    }

    override fun logOut() {
        currentUserId = null
        _customerInfo.value = null
    }

    override fun purchasePackage(packageToPurchase: Any) {
        val uid = currentUserId
        if (uid.isNullOrEmpty()) {
            sendError("Please sign in with Google first so your purchase can be linked to your account.")
            return
        }

        _isLoading.value = true

        val checkoutUrl = "${AdConfiguration.SANDBOX_REMOVE_ADS_PURCHASE_URL}/$uid"
        val openedWindow = window.open(checkoutUrl, "_blank")

        if (openedWindow == null) {
            sendError("Checkout popup was blocked by your browser. Please allow popups for this site.")
        } else {
            sendMessage("Opening Paddle checkout window in a new tab...")
        }

        _isLoading.value = false
    }

    override fun restorePurchases() {
        sendMessage("On Web, purchases are automatically linked to your Google account. Sign in to sync your access.")
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

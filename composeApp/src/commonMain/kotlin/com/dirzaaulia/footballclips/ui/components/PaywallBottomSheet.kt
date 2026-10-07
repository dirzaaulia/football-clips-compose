package com.dirzaaulia.footballclips.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.billing.CustomerInfoModel
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.isWasmTarget

enum class PaywallState {
    PREMIUM_ACTIVE,
    VERIFYING,
    NON_PREMIUM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    isPremium: Boolean,
    profile: Profile?,
    customerInfo: CustomerInfoModel?,
    offerings: Any?,
    isLoading: Boolean = false,
    onSignInClick: () -> Unit,
    onPurchaseClick: (Any) -> Unit,
    onRestoreClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayInfo = extractOfferingInfo(offerings)
    val isWasm = isWasmTarget
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasm && isBigScreen

    val paywallState = when {
        isPremium -> PaywallState.PREMIUM_ACTIVE
        isLoading -> PaywallState.VERIFYING
        else -> PaywallState.NON_PREMIUM
    }

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = onDismiss,
            sheetWidth = 480.dp,
            containerColor = Color(0xFF121212)
        ) {
            PaywallSheetContent(
                isSideSheet = true,
                isWasm = isWasm,
                paywallState = paywallState,
                displayInfo = displayInfo,
                profile = profile,
                customerInfo = customerInfo,
                isLoading = isLoading,
                onSignInClick = onSignInClick,
                onPurchaseClick = onPurchaseClick,
                onRestoreClick = onRestoreClick,
                onDismiss = onDismiss
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF121212),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD4AF37)) }
        ) {
            PaywallSheetContent(
                isSideSheet = false,
                isWasm = isWasm,
                paywallState = paywallState,
                displayInfo = displayInfo,
                profile = profile,
                customerInfo = customerInfo,
                isLoading = isLoading,
                onSignInClick = onSignInClick,
                onPurchaseClick = onPurchaseClick,
                onRestoreClick = onRestoreClick,
                onDismiss = onDismiss
            )
        }
    }
}

data class OfferingDisplayInfo(
    val price: String,
    val rcPackage: Any
)

@Composable
expect fun extractOfferingInfo(offerings: Any?): OfferingDisplayInfo?

package com.dirzaaulia.footballclips.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun extractOfferingInfo(offerings: Any?): OfferingDisplayInfo? {
    return OfferingDisplayInfo(
        price = "$0.00",
        rcPackage = "sandbox_package"
    )
}

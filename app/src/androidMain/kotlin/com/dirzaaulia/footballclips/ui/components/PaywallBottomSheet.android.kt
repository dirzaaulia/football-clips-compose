package com.dirzaaulia.footballclips.ui.components

import androidx.compose.runtime.Composable
import com.revenuecat.purchases.Offerings

@Composable
actual fun extractOfferingInfo(offerings: Any?): OfferingDisplayInfo? {
    val rcOfferings = offerings as? Offerings ?: return null
    
    // 1. Try "current" offering
    val currentOffering = rcOfferings.current
    var bestPackage = currentOffering?.monthly 
        ?: currentOffering?.availablePackages?.firstOrNull()

    // 2. Search for any package in any offering if current is null or empty
    if (bestPackage == null) {
        // Flatten all packages from all offerings
        val allPackages = rcOfferings.all.values.flatMap { it.availablePackages }
        
        // Prefer monthly, then anything
        bestPackage = allPackages.firstOrNull { it.identifier.contains("monthly", ignoreCase = true) }
            ?: allPackages.firstOrNull { it.packageType == com.revenuecat.purchases.PackageType.MONTHLY }
            ?: allPackages.firstOrNull { it.identifier.contains("remove_ads", ignoreCase = true) }
            ?: allPackages.firstOrNull()
    }

    return bestPackage?.let { 
        OfferingDisplayInfo(
            price = it.product.price.formatted,
            rcPackage = it
        )
    }
}

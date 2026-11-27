package com.dirzaaulia.footballclips.data.admob

import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.data.analytics.AppAnalytics
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "AdMob_Diagnostics"

data class AdDiagnosticSnapshot(
    val nativeStatus: String = "Idle",
    val bannerStatus: String = "Idle",
    val interstitialStatus: String = "Idle",
    val nativeFillCount: Int = 0,
    val nativeNoFillCount: Int = 0,
    val bannerFallbackCount: Int = 0,
    val lastErrorDetail: String? = null,
)

object AdMobDiagnostics {
    private val analyticsRef = AtomicReference<AppAnalytics?>(null)
    private val _snapshot = MutableStateFlow(AdDiagnosticSnapshot())
    val snapshot: StateFlow<AdDiagnosticSnapshot> = _snapshot.asStateFlow()

    fun attachAnalytics(analytics: AppAnalytics?) {
        analyticsRef.set(analytics)
    }

    fun recordNativeFill(placement: String) {
        _snapshot.update { it.copy(nativeStatus = "Filled", nativeFillCount = it.nativeFillCount + 1) }
        trackAdEvent("ad_filled_native", mapOf("placement" to placement, "format" to "native"))
    }

    fun recordNativeNoFill(placement: String, reason: String) {
        _snapshot.update {
            it.copy(
                nativeStatus = "NO_FILL",
                nativeNoFillCount = it.nativeNoFillCount + 1,
                lastErrorDetail = reason,
            )
        }
        trackAdEvent("ad_no_fill", mapOf("placement" to placement, "format" to "native", "reason" to reason))
    }

    fun recordBannerFallback(placement: String, sizeLabel: String) {
        _snapshot.update { it.copy(bannerStatus = "Active ($sizeLabel)", bannerFallbackCount = it.bannerFallbackCount + 1) }
        trackAdEvent("ad_fallback_banner", mapOf("placement" to placement, "size" to sizeLabel))
    }

    fun recordBannerFailed(placement: String, reason: String) {
        _snapshot.update { it.copy(bannerStatus = "Failed", lastErrorDetail = reason) }
        trackAdEvent("ad_no_fill", mapOf("placement" to placement, "format" to "banner_fallback", "reason" to reason))
    }

    fun recordInterstitialState(status: String, shown: Boolean = false) {
        _snapshot.update { it.copy(interstitialStatus = status) }
        if (shown) {
            trackAdEvent("ad_interstitial_shown", mapOf("format" to "interstitial"))
        }
    }

    fun openAdInspector(onResult: (String?) -> Unit = {}) {
        try {
            MobileAds.openAdInspector { error ->
                if (error != null) {
                    Log.e(TAG, "Ad Inspector closed with error: ${error.message}")
                    onResult(error.message)
                } else {
                    onResult(null)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to open Ad Inspector: ${t.message}")
            onResult(t.message)
        }
    }

    private fun trackAdEvent(name: String, props: Map<String, String>) {
        analyticsRef.get()?.trackEvent(name, props)
    }
}

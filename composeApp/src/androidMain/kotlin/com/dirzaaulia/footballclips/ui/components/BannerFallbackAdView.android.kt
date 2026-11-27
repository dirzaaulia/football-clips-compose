package com.dirzaaulia.footballclips.ui.components.ad

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.data.admob.AdMobDiagnostics
import com.dirzaaulia.footballclips.data.admob.AdMobInitializer
import com.dirzaaulia.footballclips.data.admob.formatAdError
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.isDebugBuild
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

private const val TAG = "AdMob_Banner"

@Composable
internal fun BannerFallbackAdView(
    style: NativeAdStyle,
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bannerUnitId = remember {
        if (isDebugBuild) {
            "ca-app-pub-3940256099942544/9214589741"
        } else {
            getBuildConfigString("ADMOB_BANNER_ID", "")
        }
    }

    if (bannerUnitId.isBlank()) {
        onAdFailed("ADMOB_BANNER_ID not configured")
        return
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(style, widthDp) {
            resolveBannerAdSize(context, style, widthDp)
        }
        val adView = remember(bannerUnitId, adSize) { AdView(context) }

        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    addView(
                        adView,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER,
                        ),
                    )
                    loadBannerIntoAdView(
                        context = ctx,
                        adView = adView,
                        adUnitId = bannerUnitId,
                        adSize = adSize,
                        placement = style.name.lowercase(),
                        onAdLoaded = onAdLoaded,
                        onAdFailed = onAdFailed,
                    )
                }
            },
        )
    }
}

@Suppress("DEPRECATION")
private fun resolveBannerAdSize(
    context: Context,
    style: NativeAdStyle,
    widthDp: Int,
): AdSize {
    return if (style == NativeAdStyle.SHORTS) {
        AdSize.MEDIUM_RECTANGLE
    } else {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
    }
}

private fun loadBannerIntoAdView(
    context: Context,
    adView: AdView,
    adUnitId: String,
    adSize: AdSize,
    placement: String,
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
) {
    AdMobInitializer.runWhenInitialized(context) {
        val request = BannerAdRequest.Builder(adUnitId, adSize).build()
        val sizeLabel = if (adSize == AdSize.MEDIUM_RECTANGLE) "300x250" else "adaptive"
        Log.d(TAG, "🔄 FALLBACK BANNER REQUEST ($sizeLabel) for $placement: $adUnitId")
        adView.loadAd(
            request,
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    Log.d(TAG, "🟢 FALLBACK BANNER LOADED ($sizeLabel) for $placement")
                    AdMobDiagnostics.recordBannerFallback(placement, sizeLabel)
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, formatAdError(adError, "BANNER FALLBACK ($placement)"))
                    AdMobDiagnostics.recordBannerFailed(placement, "Code ${adError.code}: ${adError.message}")
                    onAdFailed(adError.message)
                }
            },
        )
    }
}

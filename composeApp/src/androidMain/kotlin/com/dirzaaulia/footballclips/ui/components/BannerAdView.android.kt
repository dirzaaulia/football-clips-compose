package com.dirzaaulia.footballclips.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.data.admob.NativeAdCache
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.isDebugBuild
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

private const val TAG = "BannerAdView"

@Composable
actual fun BannerAdView(
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    modifier: Modifier,
    style: NativeAdStyle
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(style) {
        if (activity != null) {
            val adUnitId = if (isDebugBuild) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                getBuildConfigString("ADMOB_NATIVE_ID", "ca-app-pub-6717632447198427/5222531302").ifEmpty { "ca-app-pub-3940256099942544/2247696110" }
            }
            val cachedAd = NativeAdCache.getAd(adUnitId)
            if (cachedAd != null) {
                Log.d(TAG, "⚡ USING PRELOADED NATIVE AD [$style]")
                nativeAdState = cachedAd
                isAdLoaded = true
                onAdLoaded()
                NativeAdCache.preloadAd(activity.applicationContext, adUnitId)
            } else {
                loadNativeAd(
                    context = activity.applicationContext,
                    adUnitId = adUnitId,
                    style = style,
                    onAdLoaded = { ad ->
                        nativeAdState = ad
                        isAdLoaded = true
                        onAdLoaded()
                        NativeAdCache.preloadAd(activity.applicationContext, adUnitId)
                    },
                    onAdFailed = onAdFailed
                )
            }
        } else {
            onAdFailed("No Activity context available")
        }
    }

    if (isAdLoaded && nativeAdState != null && activity != null) {
        Box(modifier = modifier) {
            AndroidView(
                factory = { ctx ->
                    val nativeAdView = NativeAdView(ctx)
                    nativeAdState?.let { ad ->
                        bindAdView(ctx, nativeAdView, ad, style)
                    }
                    nativeAdView
                },
                update = { nativeAdView ->
                    nativeAdState?.let { ad ->
                        bindAdView(nativeAdView.context, nativeAdView, ad, style)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun loadNativeAd(
    context: android.content.Context,
    adUnitId: String,
    style: NativeAdStyle,
    onAdLoaded: (NativeAd) -> Unit,
    onAdFailed: (String) -> Unit
) {
    try {
        Log.d(TAG, "📡 AD REQUEST INITIATED [$style]: Unit ID = $adUnitId, Debug = $isDebugBuild")

        NativeAdCache.preloadAd(context, adUnitId)
        val ad = NativeAdCache.getAd(adUnitId)
        if (ad != null) {
            onAdLoaded(ad)
        } else {
            onAdFailed("Ad not cached")
        }
    } catch (t: Throwable) {
        Log.e(TAG, "💥 NATIVE AD EXCEPTION [$style]: ${t.message}", t)
        onAdFailed(t.message ?: "Native ad load exception")
    }
}

package com.dirzaaulia.footballclips.ui.components.ad

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
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
            NativeAdCache.fetchOrLoadAd(
                context = activity.applicationContext,
                adUnitId = adUnitId,
                onAdLoaded = { ad ->
                    nativeAdState = ad
                    isAdLoaded = true
                    onAdLoaded()
                },
                onAdFailed = { error ->
                    onAdFailed(error)
                }
            )
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

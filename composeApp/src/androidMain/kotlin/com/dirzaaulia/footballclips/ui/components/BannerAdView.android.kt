package com.dirzaaulia.footballclips.ui.components.ad

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.data.admob.NativeAdCache
import com.dirzaaulia.footballclips.ui.home.findActivity
import com.dirzaaulia.footballclips.util.ActivityHolder
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.isDebugBuild
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

@Composable
actual fun BannerAdView(
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    modifier: Modifier,
    style: NativeAdStyle,
) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: ActivityHolder.currentActivity
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    var isNativeLoaded by remember { mutableStateOf(false) }
    var useBannerFallback by remember { mutableStateOf(false) }

    LaunchedEffect(style) {
        val appContext = activity?.applicationContext ?: context.applicationContext
        val adUnitId = if (isDebugBuild) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            getBuildConfigString("ADMOB_NATIVE_ID", "ca-app-pub-6717632447198427/5222531302").ifEmpty {
                "ca-app-pub-6717632447198427/5222531302"
            }
        }
        NativeAdCache.fetchOrLoadAd(
            context = appContext,
            adUnitId = adUnitId,
            placement = style.name.lowercase(),
            onAdLoaded = { ad ->
                nativeAdState = ad
                isNativeLoaded = true
                onAdLoaded()
            },
            onAdFailed = {
                useBannerFallback = true
            },
        )
    }

    if (useBannerFallback) {
        BannerFallbackAdView(
            style = style,
            onAdLoaded = onAdLoaded,
            onAdFailed = onAdFailed,
            modifier = modifier,
        )
    } else if (isNativeLoaded && nativeAdState != null) {
        Box(modifier = modifier) {
            AndroidView(
                factory = { ctx ->
                    NativeAdView(ctx).apply {
                        tag = nativeAdState
                        nativeAdState?.let { ad -> bindAdView(ctx, this, ad, style) }
                    }
                },
                update = { nativeAdView ->
                    if (nativeAdView.tag != nativeAdState) {
                        nativeAdView.tag = nativeAdState
                        nativeAdState?.let { ad -> bindAdView(nativeAdView.context, nativeAdView, ad, style) }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

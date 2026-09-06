package com.dirzaaulia.footballclips.ui.components

import android.app.Activity
import android.graphics.Color
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.data.constants.AdConfiguration
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Composable
actual fun BannerAdView(onAdLoaded: () -> Unit, onAdFailed: (String) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    BoxWithConstraints(modifier = modifier) {
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.density
        val screenDpWidth = (displayMetrics.widthPixels / density).toInt()
        val rawWidth = this.maxWidth.value.toInt()
        val adWidth = if (rawWidth in 1..screenDpWidth) rawWidth else if (screenDpWidth > 0) screenDpWidth else 320

        activity?.let { act ->
            AndroidView(
                factory = {
                    val adView = AdView(act).apply {
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(act, adWidth)
                    val adRequest = BannerAdRequest.Builder(
                        com.dirzaaulia.footballclips.BuildConfig.ADMOB_BANNER_ID,
                        adSize
                    ).build()
                    try {
                        adView.loadAd(adRequest, object : AdLoadCallback<BannerAd> {
                            override fun onAdLoaded(ad: BannerAd) {
                                println("BannerAdView loaded successfully for width $adWidth")
                                onAdLoaded()
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                println("BannerAdView failed to load: ${adError.message} (Code: ${adError.code})")
                                onAdFailed(adError.message)
                            }
                        })
                    } catch (t: Throwable) {
                        println("BannerAdView load exception: ${t.message}")
                        onAdFailed(t.message ?: "Ad load error")
                    }
                    adView
                },
                modifier = Modifier.fillMaxWidth(),
                onRelease = { adView -> adView.destroy() }
            )
        }
    }
}

package com.dirzaaulia.footballclips.ui.components

import android.app.Activity
import android.util.Log
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
        val adWidth = maxWidth.value.toInt()
        activity?.let { act ->
            AndroidView(
                factory = {
                    val adView = AdView(act).apply {
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    }
                    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(act, adWidth)
                    val adRequest = BannerAdRequest.Builder(
                        AdConfiguration.ADMOB_BANNER_ID,
                        adSize
                    ).build()
                    adView.loadAd(adRequest, object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            onAdLoaded()
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            onAdFailed(adError.message)
                        }
                    })
                    adView
                },
                modifier = Modifier.fillMaxWidth(),
                onRelease = { adView -> adView.destroy() }
            )
        }
    }
}

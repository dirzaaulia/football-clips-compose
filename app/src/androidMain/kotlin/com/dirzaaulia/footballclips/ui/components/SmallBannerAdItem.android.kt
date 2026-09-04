package com.dirzaaulia.footballclips.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Composable
actual fun SmallBannerAdItem(modifier: Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val adWidth = maxWidth.value.toInt()
        activity?.let { act ->
            AndroidView(
                factory = {
                    val adView = AdView(act)
                    // Menggunakan Anchored Adaptive Banner (lebih ramping untuk footer)
                    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(act, adWidth)
                    val adRequest = BannerAdRequest.Builder("ca-app-pub-3940256099942544/9214589741", adSize).build()
                    adView.loadAd(adRequest, object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {}
                        override fun onAdFailedToLoad(adError: LoadAdError) {}
                    })
                    adView
                },
                onRelease = { adView -> adView.destroy() }
            )
        }
    }
}

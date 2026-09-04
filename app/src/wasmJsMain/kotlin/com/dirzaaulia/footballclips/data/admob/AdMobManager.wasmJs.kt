package com.dirzaaulia.footballclips.data.admob

actual class AdMobManager {
    actual fun showInterstitial(onAdDismissed: () -> Unit) {
        onAdDismissed()
    }
}

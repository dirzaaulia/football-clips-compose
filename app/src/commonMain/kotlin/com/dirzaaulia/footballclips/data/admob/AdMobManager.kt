package com.dirzaaulia.footballclips.data.admob

expect class AdMobManager {
    fun showInterstitial(onAdDismissed: () -> Unit)
}

package com.dirzaaulia.footballclips.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.FootballClipsApplication
import com.dirzaaulia.footballclips.data.constants.AdConfiguration
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

actual class AdMobManager(private val context: Context) {

    companion object {
        private var isInitialized = false

        fun initializeMobileAds(context: Context) {
            if (!isInitialized) {
                isInitialized = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        MobileAds.initialize(
                            context.applicationContext,
                            InitializationConfig.Builder(AdConfiguration.ADMOB_APP_ID).build()
                        )
                        Log.d("AdMobManager", "MobileAds initialized on demand")
                    } catch (t: Throwable) {
                        Log.e("AdMobManager", "MobileAds initialization failed: ${t.message}")
                    }
                }
            }
        }
    }

    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialTime: Long = 0
    private val interstitialCooldown = TimeUnit.MINUTES.toMillis(3)

    init {
        initializeMobileAds(context)
        loadInterstitial()
    }

    private fun loadInterstitial() {
        // Using Google Test Interstitial ID for reliable testing
        val adRequest = AdRequest.Builder("ca-app-pub-3940256099942544/1033173712").build()
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdMobManager", "Interstitial Ad Loaded Successfully")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    Log.e("AdMobManager", "Failed to load Interstitial: ${adError.message} (Code: ${adError.code})")
                }
            }
        )
    }

    actual fun showInterstitial(onAdDismissed: () -> Unit) {
        val activity = FootballClipsApplication.getCurrentActivity()
        if (activity != null) {
            showInterstitial(activity, onAdDismissed)
        } else {
            onAdDismissed()
        }
    }
    
    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val canShowAd = interstitialAd != null && (currentTime - lastInterstitialTime >= interstitialCooldown)
        
        Log.d("AdMobManager", "Attempting to show Interstitial. Ad available: ${interstitialAd != null}, Cooldown passed: ${currentTime - lastInterstitialTime >= interstitialCooldown}")

        if (canShowAd) {
            interstitialAd?.adEventCallback = object : InterstitialAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMobManager", "Interstitial Ad Dismissed")
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    loadInterstitial()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                    Log.e("AdMobManager", "Interstitial Ad Failed to Show: ${error.message}")
                    interstitialAd = null
                    loadInterstitial()
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            if (interstitialAd == null) {
                loadInterstitial() // Reload if null
            }
            onAdDismissed()
        }
    }
}

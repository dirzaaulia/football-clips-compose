package com.dirzaaulia.footballclips.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.util.ActivityHolder
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.isDebugBuild
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import java.util.concurrent.TimeUnit

private const val TAG_INTERSTITIAL = "AdMob_Interstitial"

actual class AdMobManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialTime: Long = 0
    private val interstitialCooldown = TimeUnit.MINUTES.toMillis(3)
    private var isInitialized = false

    init {
        initializeAndLoad()
    }

    private fun initializeAndLoad() {
        if (isInitialized) {
            loadInterstitial()
            preloadNativeAd()
            return
        }

        try {
            val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            val admobAppId = getBuildConfigString("ADMOB_APP_ID", "ca-app-pub-6717632447198427~3285794491")
            Log.d(TAG_INTERSTITIAL, "🚀 INITIALIZING ADMOB SDK (App ID: $admobAppId)")
            MobileAds.initialize(
                context.applicationContext,
                InitializationConfig.Builder(admobAppId).build()
            ) {
                isInitialized = true
                Log.d(TAG_INTERSTITIAL, "✅ ADMOB SDK INITIALIZED SUCCESSFULLY")
                loadInterstitial()
                preloadNativeAd()
            }
        } catch (t: Throwable) {
            Log.e(TAG_INTERSTITIAL, "💥 ADMOB INITIALIZATION EXCEPTION: ${t.message}", t)
        }
    }

    private fun loadInterstitial() {
        if (!isInitialized) {
            initializeAndLoad()
            return
        }

        val interstitialId = getBuildConfigString("ADMOB_INTERSTITIAL_ID", "ca-app-pub-6717632447198427/9984137502")
        Log.d(TAG_INTERSTITIAL, "🔄 LOADING INTERSTITIAL AD (Unit ID: $interstitialId)")
        val adRequest = AdRequest.Builder(interstitialId).build()
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG_INTERSTITIAL, "🟢 INTERSTITIAL LOADED SUCCESSFULLY")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG_INTERSTITIAL, formatAdError(adError, "INTERSTITIAL"))
                }
            }
        )
    }

    private fun preloadNativeAd() {
        val nativeAdUnitId = if (isDebugBuild) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            getBuildConfigString("ADMOB_NATIVE_ID", "ca-app-pub-6717632447198427/5222531302").ifEmpty { "ca-app-pub-3940256099942544/2247696110" }
        }
        NativeAdCache.preloadAd(context.applicationContext, nativeAdUnitId)
    }

    actual fun showInterstitial(onAdDismissed: () -> Unit) {
        val activity = ActivityHolder.currentActivity
        if (activity != null) {
            showInterstitial(activity, onAdDismissed)
        } else {
            Log.w(TAG_INTERSTITIAL, "⚠️ SHOW CANCELLED: No current Activity available")
            onAdDismissed()
        }
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val cooldownPassed = currentTime - lastInterstitialTime >= interstitialCooldown
        val canShowAd = interstitialAd != null && cooldownPassed

        Log.d(TAG_INTERSTITIAL, "📊 SHOW REQUEST: Ad Available = ${interstitialAd != null}, Cooldown Passed = $cooldownPassed (${(currentTime - lastInterstitialTime) / 1000}s / ${interstitialCooldown / 1000}s)")

        if (canShowAd) {
            interstitialAd?.adEventCallback = object : InterstitialAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG_INTERSTITIAL, "🚪 INTERSTITIAL DISMISSED")
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    loadInterstitial()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    Log.e(TAG_INTERSTITIAL, "🔴 INTERSTITIAL SHOW FAILED: ${fullScreenContentError.message} (Code: ${fullScreenContentError.code})")
                    interstitialAd = null
                    loadInterstitial()
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            if (interstitialAd == null) {
                Log.d(TAG_INTERSTITIAL, "ℹ️ INTERSTITIAL NOT READY: Fetching new ad for next time")
                loadInterstitial()
            } else if (!cooldownPassed) {
                Log.d(TAG_INTERSTITIAL, "⏳ COOLDOWN ACTIVE: Skipping ad display until cooldown expires")
            }
            onAdDismissed()
        }
    }

    actual fun openAdInspector() {
        try {
            Log.d(TAG_INTERSTITIAL, "🔍 OPENING AD INSPECTOR")
            MobileAds.openAdInspector { adInspectorError ->
                if (adInspectorError != null) {
                    Log.e(TAG_INTERSTITIAL, "🔴 AD INSPECTOR ERROR: ${adInspectorError.message} (Code: ${adInspectorError.code})")
                } else {
                    Log.d(TAG_INTERSTITIAL, "🟢 AD INSPECTOR CLOSED")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG_INTERSTITIAL, "💥 AD INSPECTOR EXCEPTION: ${t.message}", t)
        }
    }
}

package com.dirzaaulia.footballclips.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.BuildConfig
import com.dirzaaulia.footballclips.FootballClipsApplication
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

private const val TAG_NATIVE = "AdMob_Native"
private const val TAG_INTERSTITIAL = "AdMob_Interstitial"

/**
 * Human-readable error formatter for NextGen GMA SDK LoadAdError
 */
fun formatAdError(adError: LoadAdError, adType: String = "NATIVE"): String {
    val errorReason = when (adError.code) {
        LoadAdError.ErrorCode.INTERNAL_ERROR -> "INTERNAL_ERROR (Ad server returned an invalid response)"
        LoadAdError.ErrorCode.INVALID_REQUEST -> "INVALID_REQUEST (Invalid ad unit ID, missing parameter, or configuration issue)"
        LoadAdError.ErrorCode.NETWORK_ERROR -> "NETWORK_ERROR (Network connection failed or timed out)"
        LoadAdError.ErrorCode.NO_FILL -> "NO_FILL (Request successful, but no ad inventory was returned by AdMob network)"
        else -> "ERROR (${adError.code})"
    }
    val adSourceDetails = adError.responseInfo?.adSourceResponses?.joinToString("\n  - ") { adapter ->
        "${adapter.adapterClassName} (${adapter.name}): ${adapter.adError?.message ?: "OK"}"
    } ?: "Direct AdMob"

    return """
        ==================================================
        🔴 ADMOB $adType LOAD FAILED 🔴
        Error Code : ${adError.code}
        Reason     : $errorReason
        Message    : ${adError.message}
        Response ID: ${adError.responseInfo?.responseId ?: "N/A"}
        Ad Sources : 
          - $adSourceDetails
        ==================================================
    """.trimIndent()
}

/**
 * In-memory Native Ad Cache adhering to Google AdMob Best Practices:
 * 1. Precaching up to 3 ads max to limit memory footprint.
 * 2. Cache Expiration: Automatically clears and evicts ads older than 1 hour (60 minutes).
 * 3. Proper Cleanup: Calls ad.destroy() on evicted/replaced ads to prevent memory leaks.
 */
object NativeAdCache {
    private const val MAX_CACHE_SIZE = 5
    private const val CACHE_EXPIRATION_MS = 60 * 60 * 1000L // 1 hour

    private class CachedNativeAd(
        val ad: NativeAd,
        val loadedTimestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - loadedTimestamp >= CACHE_EXPIRATION_MS
    }

    private val cache = ConcurrentHashMap<String, Queue<CachedNativeAd>>()

    fun getAd(adUnitId: String): NativeAd? {
        val queue = cache[adUnitId] ?: return null
        while (queue.isNotEmpty()) {
            val cached = queue.poll() ?: continue
            if (cached.isExpired()) {
                Log.d(TAG_NATIVE, "🗑️ EVICTING EXPIRED AD: NativeAd '${cached.ad.headline}' is older than 1 hour")
                cached.ad.destroy()
            } else {
                Log.d(TAG_NATIVE, "🟢 CACHE HIT: Serving preloaded NativeAd '${cached.ad.headline}' for unit $adUnitId")
                return cached.ad
            }
        }
        Log.d(TAG_NATIVE, "⚪ CACHE MISS: No preloaded NativeAd available in pool for unit $adUnitId")
        return null
    }

    fun putAd(adUnitId: String, ad: NativeAd) {
        val queue = cache.getOrPut(adUnitId) { ConcurrentLinkedQueue() }
        if (queue.size < MAX_CACHE_SIZE) {
            queue.offer(CachedNativeAd(ad))
            Log.d(TAG_NATIVE, "📥 CACHED AD: NativeAd '${ad.headline}' stored in pool for $adUnitId (Pool size: ${queue.size}/$MAX_CACHE_SIZE)")
        } else {
            Log.d(TAG_NATIVE, "⚠️ CACHE FULL: Pool limit ($MAX_CACHE_SIZE) reached for $adUnitId, destroying extra ad")
            ad.destroy()
        }
    }

    fun preloadAd(context: Context, adUnitId: String) {
        val queue = cache.getOrPut(adUnitId) { ConcurrentLinkedQueue() }
        val needed = MAX_CACHE_SIZE - queue.size
        if (needed <= 0) {
            Log.d(TAG_NATIVE, "ℹ️ PRELOAD SKIPPED: Cache pool already full (${queue.size}/$MAX_CACHE_SIZE)")
            return
        }

        Log.d(TAG_NATIVE, "🔄 PRELOADING ADS: Requesting $needed native ads for $adUnitId (Current pool: ${queue.size}/$MAX_CACHE_SIZE)")
        val types = listOf(NativeAd.NativeAdType.NATIVE)
        val request = NativeAdRequest.Builder(adUnitId, types).build()
        try {
            NativeAdLoader.load(request, needed, object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    Log.d(TAG_NATIVE, "🟢 PRELOAD SUCCESS: NativeAd '${nativeAd.headline}' pre-fetched into pool")
                    putAd(adUnitId, nativeAd)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG_NATIVE, formatAdError(adError, "NATIVE PRELOAD"))
                }

                override fun onAdLoadingCompleted() {
                    Log.d(TAG_NATIVE, "✅ PRELOAD BATCH COMPLETED: Pool size is now ${cache[adUnitId]?.size ?: 0}/$MAX_CACHE_SIZE")
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG_NATIVE, "💥 PRELOAD EXCEPTION: ${t.message}", t)
        }
    }

    fun clear() {
        cache.values.forEach { queue ->
            while (queue.isNotEmpty()) {
                queue.poll()?.ad?.destroy()
            }
        }
        cache.clear()
        Log.d(TAG_NATIVE, "🧹 CACHE CLEARED: All cached NativeAds destroyed")
    }
}

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

            Log.d(TAG_INTERSTITIAL, "🚀 INITIALIZING ADMOB SDK (App ID: ${BuildConfig.ADMOB_APP_ID})")
            MobileAds.initialize(
                context.applicationContext,
                InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build()
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

        Log.d(TAG_INTERSTITIAL, "🔄 LOADING INTERSTITIAL AD (Unit ID: ${BuildConfig.ADMOB_INTERSTITIAL_ID})")
        val adRequest = AdRequest.Builder(BuildConfig.ADMOB_INTERSTITIAL_ID).build()
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
        val nativeAdUnitId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            BuildConfig.ADMOB_NATIVE_ID.ifEmpty { "ca-app-pub-3940256099942544/2247696110" }
        }
        NativeAdCache.preloadAd(context.applicationContext, nativeAdUnitId)
    }

    actual fun showInterstitial(onAdDismissed: () -> Unit) {
        val activity = FootballClipsApplication.getCurrentActivity()
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

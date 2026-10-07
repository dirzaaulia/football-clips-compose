package com.dirzaaulia.footballclips.data.admob

import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private const val TAG_NATIVE = "AdMob_Native"

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

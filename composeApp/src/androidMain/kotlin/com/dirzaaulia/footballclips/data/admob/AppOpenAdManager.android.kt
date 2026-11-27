package com.dirzaaulia.footballclips.data.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.isDebugBuild
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import java.util.concurrent.TimeUnit

private const val TAG = "AdMob_AppOpen"

internal class AppOpenAdManager(private val context: Context) {

    private var appOpenAd: AppOpenAd? = null
    private var loadTimeMs: Long = 0L
    private var lastShownMs: Long = 0L
    private var isLoading: Boolean = false
    private var isShowing: Boolean = false

    private val expirationMs = TimeUnit.HOURS.toMillis(4)
    private val showCooldownMs = TimeUnit.MINUTES.toMillis(10)

    private fun resolveUnitId(): String {
        if (isDebugBuild) return "ca-app-pub-3940256099942544/9257395921"
        return getBuildConfigString("ADMOB_APP_OPEN_ID", "ca-app-pub-6717632447198427/4453915473")
            .ifEmpty { "ca-app-pub-6717632447198427/4453915473" }
    }

    fun preload() {
        val unitId = resolveUnitId()
        if (unitId.isBlank() || isLoading || isAdValid()) return
        isLoading = true
        AdMobInitializer.runWhenInitialized(context) {
            AppOpenAd.load(
                AdRequest.Builder(unitId).build(),
                object : AdLoadCallback<AppOpenAd> {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        loadTimeMs = System.currentTimeMillis()
                        isLoading = false
                        Log.d(TAG, "🟢 APP OPEN AD LOADED")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        isLoading = false
                        Log.w(TAG, "⚪ APP OPEN LOAD SKIPPED/FAILED: ${adError.message}")
                    }
                },
            )
        }
    }

    fun showIfAvailable(activity: Activity, onDone: () -> Unit = {}) {
        val cooldownPassed = System.currentTimeMillis() - lastShownMs >= showCooldownMs
        val ad = appOpenAd
        if (isShowing || !cooldownPassed || ad == null || !isAdValid()) {
            if (!isAdValid()) {
                appOpenAd = null
                preload()
            }
            onDone()
            return
        }
        isShowing = true
        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                appOpenAd = null
                lastShownMs = System.currentTimeMillis()
                preload()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                isShowing = false
                appOpenAd = null
                preload()
                onDone()
            }
        }
        ad.show(activity)
    }

    private fun isAdValid(): Boolean {
        return appOpenAd != null && (System.currentTimeMillis() - loadTimeMs < expirationMs)
    }
}

package com.dirzaaulia.footballclips.data.admob

import android.content.Context
import android.util.Log
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "AdMobInitializer"

object AdMobInitializer {
    private val isInitialized = AtomicBoolean(false)
    private val isInitializing = AtomicBoolean(false)
    private val pendingCallbacks = ConcurrentLinkedQueue<() -> Unit>()

    fun isInitialized(): Boolean = isInitialized.get()

    fun initialize(context: Context, onInitialized: (() -> Unit)? = null) {
        if (isInitialized.get()) {
            onInitialized?.invoke()
            return
        }

        onInitialized?.let { pendingCallbacks.add(it) }

        if (!isInitializing.compareAndSet(false, true)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
                val requestConfig = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()

                val admobAppId = getBuildConfigString("ADMOB_APP_ID", "ca-app-pub-6717632447198427~3285794491")
                Log.d(TAG, "🚀 INITIALIZING ADMOB SDK (App ID: $admobAppId)")

                MobileAds.initialize(
                    context.applicationContext,
                    InitializationConfig.Builder(admobAppId).build()
                ) {
                    // Next-Gen SDK requires initialization before other MobileAds calls.
                    // Configure requests before releasing any queued ad loads.
                    MobileAds.setRequestConfiguration(requestConfig)
                    isInitialized.set(true)
                    isInitializing.set(false)
                    Log.d(TAG, "✅ ADMOB SDK INITIALIZED SUCCESSFULLY")

                    while (pendingCallbacks.isNotEmpty()) {
                        try {
                            pendingCallbacks.poll()?.invoke()
                        } catch (t: Throwable) {
                            Log.e(TAG, "💥 ERROR IN PENDING INITIALIZATION CALLBACK: ${t.message}", t)
                        }
                    }
                }
            } catch (t: Throwable) {
                isInitializing.set(false)
                Log.e(TAG, "💥 ADMOB INITIALIZATION EXCEPTION: ${t.message}", t)
            }
        }
    }

    fun runWhenInitialized(context: Context, action: () -> Unit) {
        if (isInitialized.get()) {
            action()
        } else {
            initialize(context, action)
        }
    }
}

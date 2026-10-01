package com.dirzaaulia.footballclips

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.dirzaaulia.footballclips.di.appModules
import com.dirzaaulia.footballclips.util.ActivityProvider
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import java.lang.ref.WeakReference
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FootballClipsApplication : Application(), SingletonImageLoader.Factory {

    companion object {
        fun getCurrentActivity(): Activity? = ActivityProvider.getCurrentActivity()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        ActivityProvider.applicationContext = this

        if (BuildConfig.REVENUECAT_API_KEY.isNotBlank()) {
            try {
                Purchases.logLevel = LogLevel.DEBUG
                Purchases.configure(
                    PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
                )
                Log.d("FootballClipsApp", "RevenueCat initialized successfully")
            } catch (t: Throwable) {
                Log.e("FootballClipsApp", "RevenueCat initialization failed: ${t.message}")
            }
        } else {
            Log.e("FootballClipsApp", "REVENUECAT_API_KEY is empty! Skipping RevenueCat initialization to prevent crash.")
        }

        try {
            val testDeviceIds = listOf("33BE2250B43518CCDA7DE426D04EE231")
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(
                this,
                InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build()
            ) {
                Log.d("FootballClipsApp", "MobileAds initialized eagerly in Application.onCreate")
            }
        } catch (t: Throwable) {
            Log.e("FootballClipsApp", "MobileAds initialization error in Application: ${t.message}")
        }

        startKoin {
            androidContext(this@FootballClipsApplication)
            modules(appModules)
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivityProvider.currentActivity = WeakReference(activity)
            }
            override fun onActivityStarted(activity: Activity) {
                ActivityProvider.currentActivity = WeakReference(activity)
            }
            override fun onActivityResumed(activity: Activity) {
                ActivityProvider.currentActivity = WeakReference(activity)
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (ActivityProvider.currentActivity?.get() == activity) {
                    ActivityProvider.currentActivity = null
                }
            }
        })
    }
}

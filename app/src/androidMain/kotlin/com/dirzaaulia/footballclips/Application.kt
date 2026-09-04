package com.dirzaaulia.footballclips

import android.app.Application
import android.app.Activity
import android.os.Bundle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.dirzaaulia.footballclips.di.appModules
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FootballClipsApplication : Application(), SingletonImageLoader.Factory {

    companion object {
        private var currentActivity: WeakReference<Activity>? = null

        fun getCurrentActivity(): Activity? {
            return currentActivity?.get()
        }
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

        // Initialize RevenueCat in background
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARN
        Purchases.configure(
            PurchasesConfiguration.Builder(this, "goog_gHDMIAwmoTGZQkfkTZDyjnfetoK").build()
        )

        // Initialize Mobile Ads SDK asynchronously in background
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(
                this@FootballClipsApplication,
                InitializationConfig.Builder("ca-app-pub-3940256099942544~3347511713")
                    .setNativeValidatorDisabled()
                    .build()
            )
        }

        // Initialize Koin DI
        startKoin {
            androidContext(this@FootballClipsApplication)
            modules(appModules)
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = WeakReference(activity)
            }
            override fun onActivityStarted(activity: Activity) {
                currentActivity = WeakReference(activity)
            }
            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity?.get() == activity) {
                    currentActivity = null
                }
            }
        })
    }
}

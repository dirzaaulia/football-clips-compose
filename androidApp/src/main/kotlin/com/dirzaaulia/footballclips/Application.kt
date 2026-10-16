package com.dirzaaulia.footballclips

import android.app.Application
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import com.dirzaaulia.footballclips.di.appModules
import com.dirzaaulia.footballclips.data.admob.AdMobInitializer
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import java.lang.ref.WeakReference
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FootballClipsApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

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

        // Safeguard check to clean up empty or corrupted WorkDatabase files
        try {
            val workDb = getDatabasePath("androidx.work.workdb")
            if ((workDb.exists()) && (workDb.length() == 0L)) {
                deleteDatabase("androidx.work.workdb")
            }
        } catch (t: Throwable) {
            Log.w("FootballClipsApp", "Safeguard check for WorkDatabase failed: ${t.message}")
        }

        // Initialize RevenueCat before Koin DI resolves BillingManager
        if (BuildConfig.REVENUECAT_API_KEY.isNotBlank()) {
            try {
                Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARN
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

        // Initialize AdMob MobileAds eagerly on Application startup
        try {
            AdMobInitializer.initialize(this)
        } catch (t: Throwable) {
            Log.e("FootballClipsApp", "MobileAds initialization error in Application: ${t.message}")
        }

        // Initialize Koin DI
        startKoin {
            androidContext(this@FootballClipsApplication)
            modules(appModules)
        }

        // Initialize Notification Channels eagerly
        try {
            com.dirzaaulia.footballclips.notification.NotificationHelper.createNotificationChannels(this)
        } catch (t: Throwable) {
            Log.e("FootballClipsApp", "Failed to create notification channels: ${t.message}")
        }


        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = WeakReference(activity)
                com.dirzaaulia.footballclips.util.ActivityHolder.currentActivity = activity
            }
            override fun onActivityStarted(activity: Activity) {
                currentActivity = WeakReference(activity)
                com.dirzaaulia.footballclips.util.ActivityHolder.currentActivity = activity
            }
            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
                com.dirzaaulia.footballclips.util.ActivityHolder.currentActivity = activity
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity?.get() == activity) {
                    currentActivity = null
                }
                if (com.dirzaaulia.footballclips.util.ActivityHolder.currentActivity == activity) {
                    com.dirzaaulia.footballclips.util.ActivityHolder.currentActivity = null
                }
            }
        })
    }
}

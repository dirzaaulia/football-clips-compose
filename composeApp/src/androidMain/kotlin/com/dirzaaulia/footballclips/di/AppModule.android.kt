package com.dirzaaulia.footballclips.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.billing.AndroidBillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.notification.AndroidNotificationTopicManager
import com.dirzaaulia.footballclips.data.notification.NotificationTopicManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { PreferenceManager(androidContext()) }
    single<BillingManager> { AndroidBillingManager(androidContext()) }
    single { AdMobManager(androidContext()) }
    single<NotificationTopicManager> { AndroidNotificationTopicManager() }
    single<HttpClient> {
        HttpClient(OkHttp) {
            engine {
                addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                })
            }
        }
    }
}

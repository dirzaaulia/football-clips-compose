package com.dirzaaulia.footballclips.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.billing.AndroidBillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { PreferenceManager(androidContext()) }
    single<BillingManager> { AndroidBillingManager(androidContext()) }
    single { AdMobManager(androidContext()) }
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

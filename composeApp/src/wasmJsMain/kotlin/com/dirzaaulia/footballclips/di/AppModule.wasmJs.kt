package com.dirzaaulia.footballclips.di

import com.dirzaaulia.footballclips.data.admob.AdMobManager
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.billing.WasmBillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { PreferenceManager() }
    single<BillingManager> { WasmBillingManager() }
    single { AdMobManager() }
    single<HttpClient> {
        HttpClient(Js) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }
}

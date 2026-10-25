package com.dirzaaulia.footballclips.di

import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import com.dirzaaulia.footballclips.data.repository.MiddlewareRepository
import com.dirzaaulia.footballclips.data.repository.MiddlewareRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.data.repository.OracleVmRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepositoryImpl
import com.dirzaaulia.footballclips.data.telemetry.DeviceTelemetrySync
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val platformModule: Module

val databaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = DatabaseConstants.DATABASE_URL,
            supabaseKey = DatabaseConstants.DATABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = DatabaseConstants.GOOGLE_WEB_CLIENT_ID)
            }
        }
    }
    single<Auth> { get<SupabaseClient>().auth }
    single<Postgrest> { get<SupabaseClient>().postgrest }
    single<ComposeAuth> { get<SupabaseClient>().composeAuth }
}

val dataModule = module {
    single { DeviceTelemetrySync(get()) }
    single<MiddlewareRepository> { MiddlewareRepositoryImpl(get()) }
    single<OracleVmRepository> { OracleVmRepositoryImpl(get()) }
    single<ProfilesRepository> { ProfilesRepositoryImpl(get(), get()) }
}

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            middlewareRepository = get(),
            oracleVmRepository = get(),
            preferenceManager = get(),
            billingManager = get(),
            adMobManager = get(),
            profilesRepository = get(),
        )
    }
    viewModel {
        ScoreViewModel(
            oracleVmRepository = get(),
            preferenceManager = get(),
            billingManager = get(),
            profilesRepository = get(),
        )
    }
}

val appModules = listOf(platformModule, databaseModule, dataModule, viewModelModule)

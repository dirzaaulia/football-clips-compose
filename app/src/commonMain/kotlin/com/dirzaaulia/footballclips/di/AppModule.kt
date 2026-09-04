package com.dirzaaulia.footballclips.di

import com.dirzaaulia.footballclips.data.repository.*
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.data.constants.SupabaseConstants
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

val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = SupabaseConstants.SUPABASE_URL,
            supabaseKey = SupabaseConstants.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = SupabaseConstants.GOOGLE_WEB_CLIENT_ID)
            }
        }
    }
    single<Auth> { get<SupabaseClient>().auth }
    single<Postgrest> { get<SupabaseClient>().postgrest }
    single<ComposeAuth> { get<SupabaseClient>().composeAuth }
}

val dataModule = module {
    single<HighlightRepository> { HighlightRepositoryImpl(get()) }
    single<ScoreRepository> { ScoreRepositoryImpl(get()) }
    single<ProfilesRepository> { ProfilesRepositoryImpl(get(), get()) }
}

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            highlightRepository = get(),
            scoreRepository = get(),
            preferenceManager = get(),
            billingManager = get(),
            adMobManager = get(),
            profilesRepository = get()
        )
    }
    viewModel {
        ScoreViewModel(
            repository = get(),
            preferenceManager = get(),
            billingManager = get(),
            profilesRepository = get()
        )
    }
}

val appModules = listOf(platformModule, supabaseModule, dataModule, viewModelModule)

package com.dirzaaulia.footballclips.di

import com.dirzaaulia.footballclips.data.repository.ClubRepository
import com.dirzaaulia.footballclips.data.repository.ClubRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.CompetitionRepository
import com.dirzaaulia.footballclips.data.repository.CompetitionRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.HighlightRepository
import com.dirzaaulia.footballclips.data.repository.HighlightRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.ProfilesRepository
import com.dirzaaulia.footballclips.data.repository.ProfilesRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.data.repository.ScoreRepositoryImpl
import com.dirzaaulia.footballclips.data.repository.StandingRepository
import com.dirzaaulia.footballclips.data.repository.StandingRepositoryImpl
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
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

import com.dirzaaulia.footballclips.data.telemetry.DeviceTelemetrySync

expect val platformModule: Module

val databaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = DatabaseConstants.DATABASE_URL,
            supabaseKey = DatabaseConstants.DATABASE_ANON_KEY
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
    single<HighlightRepository> { HighlightRepositoryImpl(get()) }
    single<ScoreRepository> { ScoreRepositoryImpl(get()) }
    single<ClubRepository> { ClubRepositoryImpl(get()) }
    single<CompetitionRepository> { CompetitionRepositoryImpl(get()) }
    single<ProfilesRepository> { ProfilesRepositoryImpl(get(), get()) }
    single<StandingRepository> { StandingRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            highlightRepository = get(),
            scoreRepository = get(),
            preferenceManager = get(),
            billingManager = get(),
            adMobManager = get(),
            profilesRepository = get(),
            competitionRepository = get()
        )
    }
    viewModel {
        ScoreViewModel(
            repository = get(),
            preferenceManager = get(),
            billingManager = get(),
            profilesRepository = get(),
            clubRepository = get(),
            standingRepository = get(),
            competitionRepository = get()
        )
    }
}

val appModules = listOf(platformModule, databaseModule, dataModule, viewModelModule)

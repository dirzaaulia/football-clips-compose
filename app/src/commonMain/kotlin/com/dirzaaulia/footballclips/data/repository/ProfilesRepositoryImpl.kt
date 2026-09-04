package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.remote.Profile
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ProfilesRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest
) : ProfilesRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val profile: Flow<Profile?> = auth.sessionStatus.flatMapLatest { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val userId = status.session.user?.id ?: return@flatMapLatest flowOf(null)
                
                flow<Profile?> {
                    try {
                        val profile = postgrest["profiles"]
                            .select(columns = Columns.ALL) {
                                filter {
                                    eq("id", userId)
                                }
                            }
                            .decodeSingle<Profile>()
                        emit(profile)
                    } catch (e: Exception) {
                        emit(null)
                    }
                }
            }
            else -> flowOf(null)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    override suspend fun signInWithSupabase() {
        auth.signInWith(
            provider = Google,
            redirectUrl = "drzfc://login-callback"
        )
    }

    override suspend fun signInWithGoogle() {
        auth.signInWith(
            provider = Google,
            redirectUrl = "drzfc://login-callback"
        )
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}

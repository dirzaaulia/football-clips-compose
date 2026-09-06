package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.util.isWasmTarget
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
    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val profile: Flow<Profile?> = combine(
        auth.sessionStatus,
        refreshTrigger
    ) { status, _ -> status }.flatMapLatest { status ->
        println("ProfilesRepositoryImpl: SessionStatus is $status")
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = status.session.user ?: run {
                    println("ProfilesRepositoryImpl: status.session.user is null!")
                    return@flatMapLatest flowOf(null)
                }
                val userId = user.id
                val userEmail = user.email
                println("ProfilesRepositoryImpl: Authenticated as $userId ($userEmail). Querying Supabase profile...")

                flow<Profile?> {
                    try {
                        val dbProfile = postgrest["profiles"]
                            .select(columns = Columns.ALL) {
                                filter {
                                    eq("id", userId)
                                }
                            }
                            .decodeSingleOrNull<Profile>()

                        if (dbProfile != null) {
                            println("ProfilesRepositoryImpl: Found dbProfile in Supabase: $dbProfile")
                            emit(dbProfile)
                        } else {
                            println("ProfilesRepositoryImpl: No profile row in Supabase for $userId, creating fallback...")
                            val fallback = Profile(
                                id = userId,
                                isPremium = false,
                                email = userEmail
                            )
                            emit(fallback)
                            try {
                                postgrest["profiles"].upsert(fallback)
                                println("ProfilesRepositoryImpl: Initial fallback profile upserted successfully.")
                            } catch (e: Exception) {
                                println("Failed to upsert fallback profile: $e")
                            }
                        }
                    } catch (e: Exception) {
                        println("ProfilesRepositoryImpl query error: $e")
                        emit(
                            Profile(
                                id = userId,
                                isPremium = false,
                                email = userEmail
                            )
                        )
                    }
                }
            }
            else -> {
                println("ProfilesRepositoryImpl: SessionStatus is not authenticated ($status), emitting null profile.")
                flowOf(null)
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun updatePremiumStatus(isPremium: Boolean) {
        val user = auth.currentUserOrNull() ?: run {
            println("ProfilesRepositoryImpl: Cannot update is_premium, user is not logged in to Supabase!")
            return
        }
        try {
            val updated = Profile(
                id = user.id,
                isPremium = isPremium,
                email = user.email
            )
            postgrest["profiles"].upsert(updated)
            refreshTrigger.value++
            println("Successfully updated is_premium to $isPremium in Supabase for user ${user.id} (${user.email})")
        } catch (e: Exception) {
            println("Failed to upsert is_premium in Supabase: $e, attempting fallback update...")
            try {
                postgrest["profiles"].update(
                    {
                        set("is_premium", isPremium)
                    }
                ) {
                    filter {
                        eq("id", user.id)
                    }
                }
                refreshTrigger.value++
                println("Fallback update succeeded for is_premium to $isPremium in Supabase for user ${user.id}")
            } catch (e2: Exception) {
                println("Fallback update also failed in Supabase: $e2")
            }
        }
    }

    override suspend fun signInWithSupabase() {
        auth.signInWith(
            provider = Google,
            redirectUrl = if (isWasmTarget) null else "drzfc://login-callback"
        )
    }

    override suspend fun signInWithGoogle() {
        auth.signInWith(
            provider = Google,
            redirectUrl = if (isWasmTarget) null else "drzfc://login-callback"
        )
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}

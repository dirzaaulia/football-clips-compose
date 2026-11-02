package com.dirzaaulia.footballclips.data.analytics

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class AnalyticsEventDto(
    @SerialName("event_name")
    val eventName: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("payload")
    val payload: JsonObject = JsonObject(emptyMap()),
)

interface AppAnalytics {
    fun trackEvent(
        name: String,
        properties: Map<String, String> = emptyMap(),
        userId: String? = null,
    )

    fun trackSignIn(userId: String?, email: String?)
    fun trackSignOut(userId: String?)
    fun trackPaywallView(source: String)
    fun trackPurchaseAttempt(productId: String)
    fun trackPurchaseSuccess(productId: String)
    fun trackClubFollow(clubName: String, followed: Boolean)
    fun trackMatchView(matchId: String, home: String, away: String)
}

class AppAnalyticsImpl(
    private val postgrest: Postgrest,
) : AppAnalytics {

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun trackEvent(
        name: String,
        properties: Map<String, String>,
        userId: String?,
    ) {
        scope.launch {
            try {
                val jsonProps = properties.mapValues { (_, v) ->
                    JsonPrimitive(v) as JsonElement
                }
                val dto = AnalyticsEventDto(
                    eventName = name,
                    userId = userId,
                    platform = "android",
                    payload = JsonObject(jsonProps),
                )
                postgrest["user_analytics_events"].insert(dto)
            } catch (_: Exception) {
                // Defensive: analytics errors must never crash the app
            }
        }
    }

    override fun trackSignIn(userId: String?, email: String?) {
        val props = mutableMapOf<String, String>()
        email?.let { props["email"] = it }
        trackEvent(name = "sign_in_success", properties = props, userId = userId)
    }

    override fun trackSignOut(userId: String?) {
        trackEvent(name = "sign_out", userId = userId)
    }

    override fun trackPaywallView(source: String) {
        trackEvent(name = "paywall_view", properties = mapOf("source" to source))
    }

    override fun trackPurchaseAttempt(productId: String) {
        trackEvent(name = "purchase_attempted", properties = mapOf("product_id" to productId))
    }

    override fun trackPurchaseSuccess(productId: String) {
        trackEvent(name = "purchase_completed", properties = mapOf("product_id" to productId))
    }

    override fun trackClubFollow(clubName: String, followed: Boolean) {
        trackEvent(
            name = "favorite_club_toggle",
            properties = mapOf("club" to clubName, "action" to if (followed) "follow" else "unfollow"),
        )
    }

    override fun trackMatchView(matchId: String, home: String, away: String) {
        trackEvent(
            name = "match_view",
            properties = mapOf("match_id" to matchId, "home" to home, "away" to away),
        )
    }
}

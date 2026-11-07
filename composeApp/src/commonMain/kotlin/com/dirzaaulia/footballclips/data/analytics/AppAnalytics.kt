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
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("payload")
    val payload: JsonObject = JsonObject(emptyMap()),
)

interface AppAnalytics {
    fun setDeviceId(id: String)

    fun trackEvent(
        name: String,
        properties: Map<String, String> = emptyMap(),
        userId: String? = null,
    )

    fun trackScreenView(screenName: String, tab: String? = null)
    fun trackMatchClick(matchId: String, matchTitle: String, competitionName: String? = null)
    fun trackVideoPlay(videoId: String, matchTitle: String, duration: Long? = null)
    fun trackWatchOnYouTube(videoId: String, matchTitle: String? = null)
    fun trackCompetitionFilter(competitionId: String, competitionName: String)
    fun trackClubFavorite(clubId: String, clubName: String, action: String)
    fun trackTabSwitch(tabName: String)
    fun trackSessionStart(appVersion: String? = "3.0.1")
    fun trackDateFilter(date: String)

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
    private val sessionId: String = "and_" + (1..10).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.joinToString("")
    private var currentDeviceId: String = "android_device"

    override fun setDeviceId(id: String) {
        if (id.isNotBlank()) {
            currentDeviceId = id
        }
    }

    override fun trackEvent(
        name: String,
        properties: Map<String, String>,
        userId: String?,
    ) {
        scope.launch {
            try {
                val fullProps = properties.toMutableMap()
                if (!fullProps.containsKey("session_id")) {
                    fullProps["session_id"] = sessionId
                }
                val jsonProps = fullProps.mapValues { (_, v) ->
                    JsonPrimitive(v) as JsonElement
                }
                val dto = AnalyticsEventDto(
                    eventName = name,
                    userId = userId,
                    deviceId = currentDeviceId,
                    platform = "android",
                    payload = JsonObject(jsonProps),
                )
                postgrest["user_analytics_events"].insert(dto)
            } catch (_: Exception) {
                // Defensive: analytics errors must never crash the app
            }
        }
    }

    override fun trackScreenView(screenName: String, tab: String?) {
        val props = mutableMapOf("screen_name" to screenName)
        tab?.let { props["tab"] = it }
        trackEvent(name = "screen_view", properties = props)
    }

    override fun trackMatchClick(matchId: String, matchTitle: String, competitionName: String?) {
        val props = mutableMapOf("match_id" to matchId, "match_title" to matchTitle)
        competitionName?.let { props["competition_name"] = it }
        trackEvent(name = "match_click", properties = props)
    }

    override fun trackVideoPlay(videoId: String, matchTitle: String, duration: Long?) {
        val props = mutableMapOf("video_id" to videoId, "match_title" to matchTitle)
        duration?.let { props["duration"] = it.toString() }
        trackEvent(name = "video_play", properties = props)
    }

    override fun trackWatchOnYouTube(videoId: String, matchTitle: String?) {
        val props = mutableMapOf("video_id" to videoId)
        matchTitle?.let { props["match_title"] = it }
        trackEvent(name = "watch_on_youtube", properties = props)
    }

    override fun trackCompetitionFilter(competitionId: String, competitionName: String) {
        trackEvent(
            name = "competition_filter",
            properties = mapOf("competition_id" to competitionId, "competition_name" to competitionName),
        )
    }

    override fun trackClubFavorite(clubId: String, clubName: String, action: String) {
        trackEvent(
            name = "club_favorite",
            properties = mapOf("club_id" to clubId, "club_name" to clubName, "action" to action),
        )
    }

    override fun trackTabSwitch(tabName: String) {
        trackEvent(name = "tab_switch", properties = mapOf("tab_name" to tabName))
    }

    override fun trackSessionStart(appVersion: String?) {
        val props = mutableMapOf<String, String>()
        appVersion?.let { props["app_version"] = it }
        trackEvent(name = "session_start", properties = props)
    }

    override fun trackDateFilter(date: String) {
        trackEvent(name = "date_filter", properties = mapOf("date" to date))
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

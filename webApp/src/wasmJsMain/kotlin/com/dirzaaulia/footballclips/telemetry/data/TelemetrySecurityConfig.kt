package com.dirzaaulia.footballclips.telemetry.data

import kotlinx.browser.window

/**
 * Enterprise Telemetry Security Configuration.
 * Routes telemetry and API requests through unified custom domain gateways
 * with dynamic runtime configuration support (window.__ENV__ or query params).
 */
object TelemetrySecurityConfig {
    const val DEFAULT_EDGE_URL = "https://edge.fc.dirzaaulia.com"

    // Safe public PostgREST anonymous JWT (protected by Postgres RLS, matches DatabaseConstants)
    const val DEFAULT_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"

    // Default worker and Oracle VM PostgREST endpoints
    private const val DEFAULT_WORKER_ORIGIN = "https://fc-worker-519310318914.us-central1.run.app"
    private const val DEFAULT_POSTGREST_BASE = "$DEFAULT_EDGE_URL/rest/v1"

    fun getDatabaseUrl(): String = getRuntimeConfig("FC_DATABASE_URL", DEFAULT_EDGE_URL)

    fun getDatabaseAnonKey(): String = getRuntimeConfig("FC_ANON_KEY", DEFAULT_ANON_KEY)

    fun getCloudRunUrl(): String = getRuntimeConfig("FC_WORKER_URL", DEFAULT_WORKER_ORIGIN)

    fun getEdgeProxyUrl(): String = getRuntimeConfig("FC_EDGE_URL", DEFAULT_EDGE_URL)

    fun getTelemetryStreamUrl(): String =
        getRuntimeConfig(
            "FC_TELEMETRY_URL",
            "$DEFAULT_POSTGREST_BASE/telemetry_state?key=eq.telemetry&select=payload",
        )

    fun getNotificationsStreamUrl(): String =
        getRuntimeConfig(
            "FC_NOTIFICATIONS_URL",
            "$DEFAULT_POSTGREST_BASE/telemetry_state?key=eq.notifications&select=payload",
        )

    fun getAudienceStreamUrl(): String =
        getRuntimeConfig(
            "FC_AUDIENCE_URL",
            "$DEFAULT_POSTGREST_BASE/telemetry_state?key=eq.audience&select=payload",
        )

    private fun getRuntimeConfig(key: String, fallback: String): String {
        return try {
            val search = window.location.search
            if (search.contains("$key=")) {
                val regex = Regex("$key=([^&]+)")
                regex.find(search)?.groupValues?.get(1) ?: fallback
            } else {
                fallback
            }
        } catch (_: Throwable) {
            fallback
        }
    }
}

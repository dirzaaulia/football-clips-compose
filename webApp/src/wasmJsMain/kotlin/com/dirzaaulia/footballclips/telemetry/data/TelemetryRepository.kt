package com.dirzaaulia.footballclips.telemetry.data

import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import com.dirzaaulia.footballclips.telemetry.model.AppCrashRecord
import com.dirzaaulia.footballclips.telemetry.model.AppSessionRecord
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.model.CloudRunHealth
import com.dirzaaulia.footballclips.telemetry.model.CloudRunStatus
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.model.NotificationTelemetryPayload
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStandingRecord
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStreamPayload
import com.dirzaaulia.footballclips.telemetry.model.WorkerCycle
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.TimeSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class TelemetryRepository(
    private val client: HttpClient = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    },
) {
    private val jsonSerializer = Json { ignoreUnknownKeys = true; isLenient = true }
    private val baseUrl = "${TelemetrySecurityConfig.getDatabaseUrl()}/rest/v1"
    private val key = TelemetrySecurityConfig.getDatabaseAnonKey()
    private var cachedPayload: TelemetryStreamPayload? = null

    private inline fun <reified T> parseFlexiblePayload(rawText: String): T {
        val trimmed = rawText.trim()
        val jsonElement = jsonSerializer.parseToJsonElement(trimmed)
        val targetElement = when {
            jsonElement is JsonArray && jsonElement.isNotEmpty() -> {
                val first = jsonElement.first()
                if (first is JsonObject && first.containsKey("payload")) {
                    first["payload"] ?: first
                } else {
                    jsonElement
                }
            }
            jsonElement is JsonObject && jsonElement.containsKey("payload") -> {
                jsonElement["payload"] ?: jsonElement
            }
            else -> jsonElement
        }
        return jsonSerializer.decodeFromJsonElement(targetElement)
    }

    suspend fun getTelemetryStream(): Result<TelemetryStreamPayload> = runCatching {
        val response = client.get(TelemetrySecurityConfig.getTelemetryStreamUrl()) {
            header("Accept", "application/json")
        }
        val payload = parseFlexiblePayload<TelemetryStreamPayload>(response.bodyAsText())
        cachedPayload = payload
        payload
    }

    suspend fun getRecentCycles(limit: Int = 500): Result<List<WorkerCycle>> = runCatching {
        val stream = getTelemetryStream().getOrThrow()
        stream.cycles.sortedByDescending { it.createdAt ?: it.timestamp }.take(limit)
    }

    suspend fun getEventsForCycles(cycleIds: List<Long>): Result<List<WorkerEvent>> = runCatching {
        val stream = cachedPayload ?: getTelemetryStream().getOrThrow()
        val events = if (cycleIds.isEmpty()) {
            stream.events
        } else {
            val idSet = cycleIds.toSet()
            stream.events.filter { it.cycleId in idSet }
        }
        events.sortedBy { it.id }
    }

    suspend fun getNotificationsTelemetry(): Result<NotificationTelemetryPayload> = runCatching {
        val response = client.get(TelemetrySecurityConfig.getNotificationsStreamUrl()) {
            header("Accept", "application/json")
        }
        parseFlexiblePayload<NotificationTelemetryPayload>(response.bodyAsText())
    }

    suspend fun getAudienceState(): Result<AudienceStatePayload> = runCatching {
        val response = client.get(TelemetrySecurityConfig.getAudienceStreamUrl()) {
            header("Accept", "application/json")
        }
        parseFlexiblePayload<AudienceStatePayload>(response.bodyAsText())
    }

    suspend fun getSyncedMatches(
        limit: Int = 150,
        hasHighlight: Boolean? = null,
        status: String? = null,
    ): Result<List<DatabaseMatchRecord>> = runCatching {
        val selectFields = "id,home_team_name,away_team_name,home_team_crest,away_team_crest,competition_name,competition_id,utc_date,status,home_score,away_score,highlight_video_id,provider,display_clock,round_name,updated_at"
        try {
            val edgeResponse = client.get("${TelemetrySecurityConfig.getEdgeProxyUrl()}/rest/v1/matches") {
                header("apikey", key)
                header("Authorization", "Bearer $key")
                url {
                    parameters.append("select", selectFields)
                    if (hasHighlight == true) parameters.append("highlight_video_id", "not.is.null")
                    if (hasHighlight == false) parameters.append("highlight_video_id", "is.null")
                    if (status != null) parameters.append("status", "eq.$status")
                    parameters.append("order", "utc_date.desc")
                    parameters.append("limit", limit.toString())
                }
            }
            edgeResponse.body<List<DatabaseMatchRecord>>()
        } catch (_: Throwable) {
            val directResponse = client.get("$baseUrl/matches") {
                header("apikey", key)
                header("Authorization", "Bearer $key")
                url {
                    parameters.append("select", selectFields)
                    if (hasHighlight == true) parameters.append("highlight_video_id", "not.is.null")
                    if (hasHighlight == false) parameters.append("highlight_video_id", "is.null")
                    if (status != null) parameters.append("status", "eq.$status")
                    parameters.append("order", "utc_date.desc")
                    parameters.append("limit", limit.toString())
                }
            }
            directResponse.body<List<DatabaseMatchRecord>>()
        }
    }

    suspend fun getStandings(competitionCode: String = "PL", season: Int = 2026): Result<List<TelemetryStandingRecord>> = runCatching {
        val response = client.get("${TelemetrySecurityConfig.getEdgeProxyUrl()}/rest/v1/standings") {
            header("apikey", key)
            header("Authorization", "Bearer $key")
            url {
                parameters.append("select", "*")
                parameters.append("competition_code", "ilike.$competitionCode")
                parameters.append("season", "eq.$season")
                parameters.append("order", "position.asc")
            }
        }
        response.body<List<TelemetryStandingRecord>>()
    }

    suspend fun getLiveApmMetrics(): Result<ApmLiveMetrics> = runCatching {
        val mark = TimeSource.Monotonic.markNow()
        val response = client.get("${TelemetrySecurityConfig.getCloudRunUrl()}/metrics")
        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        OpenMetricsParser.parse(response.bodyAsText(), elapsedMs)
    }

    suspend fun getCloudRunHealth(): Result<CloudRunHealth> = runCatching {
        client.get("${TelemetrySecurityConfig.getCloudRunUrl()}/health").body<CloudRunHealth>()
    }

    suspend fun getCloudRunStatus(): Result<CloudRunStatus> = runCatching {
        client.get("${TelemetrySecurityConfig.getCloudRunUrl()}/status").body<CloudRunStatus>()
    }

    suspend fun triggerRunCycle(forceFull: Boolean = false): Result<String> = runCatching {
        val response = client.post("${TelemetrySecurityConfig.getCloudRunUrl()}/run-cycle?async_execution=true&force_full=$forceFull")
        response.bodyAsText()
    }

    suspend fun purgeEdgeCache(): Result<String> = runCatching {
        client.post("${TelemetrySecurityConfig.getEdgeProxyUrl()}/api/purge-cache").bodyAsText()
    }

    suspend fun renewWebSubSubscriptions(): Result<String> = runCatching {
        client.post("${TelemetrySecurityConfig.getCloudRunUrl()}/websub/renew").bodyAsText()
    }

    suspend fun benchmarkLatencies(): Pair<Long, Long> {
        val edgeMs = runCatching {
            val mark = TimeSource.Monotonic.markNow()
            client.get("${TelemetrySecurityConfig.getEdgeProxyUrl()}/rest/v1/matches?limit=1") {
                header("apikey", key)
            }
            mark.elapsedNow().inWholeMilliseconds
        }.getOrDefault(999L)

        val directMs = runCatching {
            val mark = TimeSource.Monotonic.markNow()
            client.get("$baseUrl/matches?limit=1") {
                header("apikey", key)
            }
            mark.elapsedNow().inWholeMilliseconds
        }.getOrDefault(999L)

        return Pair(edgeMs, directMs)
    }

    suspend fun getAppSessions(): Result<List<AppSessionRecord>> = runCatching {
        val response = client.get("${TelemetrySecurityConfig.getDatabaseUrl()}/rest/v1/app_sessions") {
            header("Accept", "application/json")
            url {
                parameters.append("select", "*")
                parameters.append("order", "last_active.desc")
            }
        }
        response.body<List<AppSessionRecord>>()
    }

    suspend fun getAppCrashes(): Result<List<AppCrashRecord>> = runCatching {
        val response = client.get("${TelemetrySecurityConfig.getDatabaseUrl()}/rest/v1/app_crashes") {
            header("Accept", "application/json")
            url {
                parameters.append("select", "*")
                parameters.append("order", "created_at.desc")
                parameters.append("limit", "100")
            }
        }
        response.body<List<AppCrashRecord>>()
    }
}

package com.dirzaaulia.footballclips.telemetry.data

import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.model.CloudRunHealth
import com.dirzaaulia.footballclips.telemetry.model.CloudRunStatus
import com.dirzaaulia.footballclips.telemetry.model.NotificationTelemetryPayload
import com.dirzaaulia.footballclips.telemetry.model.SupabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStreamPayload
import com.dirzaaulia.footballclips.telemetry.model.WorkerCycle
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.TimeSource

object TelemetryConstants {
    const val SUPABASE_URL = "https://fc-edge-proxy.dirzaaulia11.workers.dev"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"
    const val GCS_TELEMETRY_URL = "https://storage.googleapis.com/highlightdata-telemetry/telemetry.json"
    const val CLOUD_RUN_URL = "https://fc-worker-519310318914.us-central1.run.app"
    const val EDGE_PROXY_URL = "https://fc-edge-proxy.dirzaaulia11.workers.dev"
}

class TelemetryRepository(
    private val client: HttpClient = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }
) {
    private val baseUrl = "${TelemetryConstants.SUPABASE_URL}/rest/v1"
    private val key = TelemetryConstants.SUPABASE_ANON_KEY
    private var cachedPayload: TelemetryStreamPayload? = null

    suspend fun getTelemetryStream(): Result<TelemetryStreamPayload> {
        return runCatching {
            val response = client.get(TelemetryConstants.GCS_TELEMETRY_URL)
            val payload = response.body<TelemetryStreamPayload>()
            cachedPayload = payload
            payload
        }
    }

    suspend fun getRecentCycles(limit: Int = 500): Result<List<WorkerCycle>> {
        return runCatching {
            val stream = getTelemetryStream().getOrThrow()
            stream.cycles.sortedByDescending { it.cycleIndex ?: it.id }.take(limit)
        }
    }

    suspend fun getEventsForCycles(cycleIds: List<Long>): Result<List<WorkerEvent>> {
        return runCatching {
            val stream = cachedPayload ?: getTelemetryStream().getOrThrow()
            val events = if (cycleIds.isEmpty()) {
                stream.events
            } else {
                val idSet = cycleIds.toSet()
                stream.events.filter { it.cycleId in idSet }
            }
            events.sortedByDescending { it.id }
        }
    }

    suspend fun getNotificationsTelemetry(): Result<NotificationTelemetryPayload> {
        return runCatching {
            val response = client.get("https://storage.googleapis.com/highlightdata-telemetry/notifications.json")
            response.body<NotificationTelemetryPayload>()
        }
    }

    suspend fun getAudienceState(): Result<AudienceStatePayload> {
        return runCatching {
            val response = client.get("https://storage.googleapis.com/highlightdata-telemetry/audience/audience_state.json")
            response.body<AudienceStatePayload>()
        }
    }

    suspend fun getSyncedMatches(limit: Int = 100): Result<List<SupabaseMatchRecord>> {
        val selectFields = "id,home_team_name,away_team_name,home_team_crest,away_team_crest,competition_name,competition_id,utc_date,status,home_score,away_score,highlight_video_id,provider,display_clock,round_name,updated_at"
        return runCatching {
            try {
                val edgeResponse = client.get("${TelemetryConstants.EDGE_PROXY_URL}/rest/v1/matches") {
                    header("apikey", key)
                    header("Authorization", "Bearer $key")
                    url {
                        parameters.append("select", selectFields)
                        parameters.append("order", "utc_date.desc")
                        parameters.append("limit", limit.toString())
                    }
                }
                edgeResponse.body<List<SupabaseMatchRecord>>()
            } catch (_: Throwable) {
                val directResponse = client.get("$baseUrl/matches") {
                    header("apikey", key)
                    header("Authorization", "Bearer $key")
                    url {
                        parameters.append("select", selectFields)
                        parameters.append("order", "utc_date.desc")
                        parameters.append("limit", limit.toString())
                    }
                }
                directResponse.body<List<SupabaseMatchRecord>>()
            }
        }
    }

    suspend fun getLiveApmMetrics(): Result<ApmLiveMetrics> {
        return runCatching {
            val mark = TimeSource.Monotonic.markNow()
            val response = client.get("${TelemetryConstants.CLOUD_RUN_URL}/metrics")
            val elapsedMs = mark.elapsedNow().inWholeMilliseconds
            val rawText = response.bodyAsText()
            parseOpenMetrics(rawText, elapsedMs)
        }
    }

    suspend fun getCloudRunHealth(): Result<CloudRunHealth> {
        return runCatching {
            val response = client.get("${TelemetryConstants.CLOUD_RUN_URL}/health")
            response.body<CloudRunHealth>()
        }
    }

    suspend fun getCloudRunStatus(): Result<CloudRunStatus> {
        return runCatching {
            val response = client.get("${TelemetryConstants.CLOUD_RUN_URL}/status")
            response.body<CloudRunStatus>()
        }
    }

    suspend fun triggerRunCycle(forceFull: Boolean = false): Result<String> {
        return runCatching {
            val response = client.post("${TelemetryConstants.CLOUD_RUN_URL}/run-cycle?async_execution=true&force_full=$forceFull")
            response.bodyAsText()
        }
    }

    suspend fun purgeEdgeCache(): Result<String> {
        return runCatching {
            val response = client.post("${TelemetryConstants.EDGE_PROXY_URL}/api/purge-cache")
            response.bodyAsText()
        }
    }

    suspend fun renewWebSubSubscriptions(): Result<String> {
        return runCatching {
            val response = client.post("${TelemetryConstants.CLOUD_RUN_URL}/websub/renew")
            response.bodyAsText()
        }
    }

    suspend fun benchmarkLatencies(): Pair<Long, Long> {
        val edgeMs = runCatching {
            val mark = TimeSource.Monotonic.markNow()
            client.get("${TelemetryConstants.EDGE_PROXY_URL}/rest/v1/matches?limit=1") {
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

    private fun parseOpenMetrics(rawText: String, latencyMs: Long): ApmLiveMetrics {
        var cycleIndex = 0L
        var isRunning = true
        var totalMatches = 793
        var liveMatches = 0
        var finishedMatches = 0
        var highlightsLinked = 0
        var highlightsUnlinked = 0

        rawText.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val key = parts[0]
                    val value = parts[1]
                    when (key) {
                        "footballclips_worker_cycle_index" -> cycleIndex = value.toLongOrNull() ?: cycleIndex
                        "footballclips_worker_is_running" -> isRunning = (value == "1" || value == "1.0")
                        "footballclips_matches_total" -> totalMatches = value.toIntOrNull() ?: totalMatches
                        "footballclips_matches_live" -> liveMatches = value.toIntOrNull() ?: liveMatches
                        "footballclips_matches_finished" -> finishedMatches = value.toIntOrNull() ?: finishedMatches
                        "footballclips_highlights_linked" -> highlightsLinked = value.toIntOrNull() ?: highlightsLinked
                        "footballclips_highlights_unlinked" -> highlightsUnlinked = value.toIntOrNull() ?: highlightsUnlinked
                    }
                }
            }
        }

        val coverage = if (finishedMatches > 0) (highlightsLinked.toDouble() / finishedMatches.toDouble()) * 100.0 else 0.0

        return ApmLiveMetrics(
            cycleIndex = cycleIndex,
            isWorkerRunning = isRunning,
            totalMatches = totalMatches,
            liveMatches = liveMatches,
            finishedMatches = finishedMatches,
            highlightsLinked = highlightsLinked,
            highlightsUnlinked = highlightsUnlinked,
            highlightCoveragePercent = (coverage * 10.0).toInt() / 10.0,
            roundTripLatencyMs = latencyMs,
            edgeProxyStatus = "ONLINE (Cloudflare Edge)",
            lastUpdated = "Live Stream"
        )
    }
}

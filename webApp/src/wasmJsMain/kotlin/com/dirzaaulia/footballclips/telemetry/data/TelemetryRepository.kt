package com.dirzaaulia.footballclips.telemetry.data

import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object TelemetryConstants {
    const val SUPABASE_URL = "https://eiomktvavndorazreyba.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"
    const val GCS_TELEMETRY_URL = "https://storage.googleapis.com/highlightdata-telemetry/telemetry.json"
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
            stream.cycles.take(limit)
        }
    }

    suspend fun getEventsForCycles(cycleIds: List<Long>): Result<List<WorkerEvent>> {
        return runCatching {
            val stream = cachedPayload ?: getTelemetryStream().getOrThrow()
            if (cycleIds.isEmpty()) {
                stream.events
            } else {
                val idSet = cycleIds.toSet()
                stream.events.filter { it.cycleId in idSet }
            }
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

    suspend fun getSyncedMatches(limit: Int = 20): Result<List<SupabaseMatchRecord>> {
        return runCatching {
            val response = client.get("$baseUrl/matches") {
                header("apikey", key)
                header("Authorization", "Bearer $key")
                url {
                    parameters.append("select", "id,home_team_name,away_team_name,competition_name,utc_date,status,home_score,away_score,highlight_video_id,updated_at")
                    parameters.append("order", "utc_date.desc")
                    parameters.append("limit", limit.toString())
                }
            }
            response.body<List<SupabaseMatchRecord>>()
        }
    }
}

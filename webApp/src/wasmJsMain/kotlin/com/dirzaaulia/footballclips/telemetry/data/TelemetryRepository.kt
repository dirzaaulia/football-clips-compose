package com.dirzaaulia.footballclips.telemetry.data

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

    suspend fun getRecentCycles(limit: Int = 50): Result<List<WorkerCycle>> {
        return runCatching {
            val response = client.get("$baseUrl/worker_cycles") {
                header("apikey", key)
                header("Authorization", "Bearer $key")
                url {
                    parameters.append("select", "*")
                    parameters.append("order", "id.desc")
                    parameters.append("limit", limit.toString())
                }
            }
            response.body<List<WorkerCycle>>()
        }
    }

    suspend fun getEventsForCycles(cycleIds: List<Long>): Result<List<WorkerEvent>> {
        if (cycleIds.isEmpty()) return Result.success(emptyList())
        return runCatching {
            val allEvents = mutableListOf<WorkerEvent>()
            for (chunk in cycleIds.chunked(30)) {
                val inList = chunk.joinToString(prefix = "(", postfix = ")", separator = ",")
                val response = client.get("$baseUrl/worker_events") {
                    header("apikey", key)
                    header("Authorization", "Bearer $key")
                    url {
                        parameters.append("select", "*")
                        parameters.append("cycle_id", "in.$inList")
                        parameters.append("order", "event_order.asc")
                        parameters.append("limit", "1000")
                    }
                }
                val chunkEvents = response.body<List<WorkerEvent>>()
                allEvents.addAll(chunkEvents)
            }
            allEvents.sortedBy { it.eventOrder }
        }
    }
}

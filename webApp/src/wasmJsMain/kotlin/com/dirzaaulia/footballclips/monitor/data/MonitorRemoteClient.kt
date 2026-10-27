package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.presentation.ownerToken
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

class AuthRequiredException(message: String) : Exception(message)

class MonitorRemoteClient {
    private val client = HttpClient(Js) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
        }
    }
    private val origin = "https://api.fc.dirzaaulia.com/api/ops/v1"

    suspend fun read(path: String): JsonObject {
        val target = if (path.startsWith("http://") || path.startsWith("https://")) path else "$origin/$path"
        val response = client.get(target) {
            val token = ownerToken()
            if (token.isNotBlank()) header("Authorization", "Bearer $token")
        }
        val status = response.status.value
        if (status == 401 || status == 403) throw AuthRequiredException("Auth required: HTTP $status on $path")
        if (status != 200) {
            println("MonitorRemoteClient.read: unexpected status $status for $path, returning empty")
            return JsonObject(emptyMap())
        }
        val element = Json.parseToJsonElement(response.bodyAsText())
        return element as? JsonObject ?: JsonObject(emptyMap())
    }

    suspend fun readArray(path: String): List<JsonObject> {
        val target = if (path.startsWith("http://") || path.startsWith("https://")) path else "$origin/$path"
        val response = client.get(target) {
            val token = ownerToken()
            if (token.isNotBlank()) header("Authorization", "Bearer $token")
        }
        val status = response.status.value
        if (status == 401 || status == 403) throw AuthRequiredException("Auth required: HTTP $status on $path")
        if (status != 200) {
            println("MonitorRemoteClient.readArray: unexpected status $status for $path, returning empty")
            return emptyList()
        }
        val element = Json.parseToJsonElement(response.bodyAsText())
        return (element as? JsonArray)?.mapNotNull { it as? JsonObject }
            ?: (element as? JsonObject)?.records("items")
            ?: emptyList()
    }

    suspend fun count(path: String): Long {
        val target = if (path.startsWith("http://") || path.startsWith("https://")) path else "$origin/$path"
        val response = client.get(target) {
            val token = ownerToken()
            if (token.isNotBlank()) header("Authorization", "Bearer $token")
            header("Prefer", "count=exact")
        }
        val status = response.status.value
        if (status == 401 || status == 403) throw AuthRequiredException("Auth required: HTTP $status on $path")
        if (status != 200 && status != 206) {
            println("MonitorRemoteClient.count: unexpected status $status for $path, returning 0")
            return 0L
        }
        val range = response.headers["Content-Range"] ?: return 0L
        val totalStr = range.substringAfterLast('/', "")
        return totalStr.toLongOrNull() ?: 0L
    }

    suspend fun mutate(path: String, payload: JsonObject): JsonObject {
        val target = if (path.startsWith("http://") || path.startsWith("https://")) path else "$origin/$path"
        val response = client.request(target) {
            method = if (path == "markets") HttpMethod.Put else HttpMethod.Post
            val token = ownerToken()
            if (token.isNotBlank()) header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }
        val element = Json.parseToJsonElement(response.bodyAsText())
        return element as? JsonObject ?: JsonObject(emptyMap())
    }

    fun close() = client.close()
}

fun JsonObject.text(key: String, fallback: String = "Unknown"): String =
    (get(key) as? JsonPrimitive)?.contentOrNull ?: fallback

fun JsonObject.obj(key: String): JsonObject = get(key) as? JsonObject ?: JsonObject(emptyMap())

fun JsonObject.records(key: String): List<JsonObject> =
    (get(key) as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()

fun JsonObject.number(key: String): Double? = text(key).toDoubleOrNull()

fun jsonFields(vararg values: Pair<String, JsonElement>): JsonObject = JsonObject(mapOf(*values))

package com.dirzaaulia.footballclips.data.telemetry

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceSyncPayload(
    @SerialName("device_id") val deviceId: String,
    @SerialName("user_id") val userId: String,
    val platform: String = "android",
    @SerialName("device_name") val deviceName: String = "Android Device",
    @SerialName("favorite_clubs") val favoriteClubs: List<Int> = emptyList(),
    @SerialName("app_version") val appVersion: String = "2.14.8"
)

class DeviceTelemetrySync(
    private val client: HttpClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val endpoint = "https://fc-device-sync-519310318914.us-central1.run.app/api/device-sync"

    fun syncDevice(
        deviceId: String,
        userId: String,
        clubs: List<Int>,
        deviceName: String = "Android Device"
    ) {
        scope.launch {
            try {
                client.post(endpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DeviceSyncPayload(
                            deviceId = deviceId,
                            userId = userId,
                            platform = "android",
                            deviceName = deviceName,
                            favoriteClubs = clubs
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}

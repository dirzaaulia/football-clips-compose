package com.dirzaaulia.footballclips.data.telemetry

import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import io.ktor.client.HttpClient
import io.ktor.client.request.header
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
    @SerialName("app_version") val appVersion: String = "3.0.1"
)

@Serializable
data class AppSessionPayload(
    @SerialName("device_id") val deviceId: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("version_code") val versionCode: Long,
    val platform: String = "android"
)

class DeviceTelemetrySync(
    private val client: HttpClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val deviceSyncEndpoint = "https://fc-device-sync-519310318914.us-central1.run.app/api/device-sync"
    private val sessionEndpoint = "${DatabaseConstants.DATABASE_URL}/rest/v1/app_sessions"

    fun syncDevice(
        deviceId: String,
        userId: String,
        clubs: List<Int>,
        deviceName: String = "Android Device",
        appVersion: String = "3.0.1",
        versionCode: Long = 0L
    ) {
        scope.launch {
            // 1. Sync device & club favorites to Push Notification Worker
            try {
                client.post(deviceSyncEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DeviceSyncPayload(
                            deviceId = deviceId,
                            userId = userId,
                            platform = "android",
                            deviceName = deviceName,
                            favoriteClubs = clubs,
                            appVersion = appVersion
                        )
                    )
                }
            } catch (_: Exception) {
            }

            // 2. Record active app version session directly to self-hosted PostgreSQL
            try {
                client.post(sessionEndpoint) {
                    contentType(ContentType.Application.Json)
                    header("Prefer", "resolution=merge-duplicates")
                    setBody(
                        AppSessionPayload(
                            deviceId = deviceId,
                            appVersion = appVersion,
                            versionCode = versionCode,
                            platform = "android"
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}

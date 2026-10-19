package com.dirzaaulia.footballclips.telemetry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TriggerCycleResponse(
    val status: String = "",
    val message: String = "",
    val timestamp: String = "",
)

@Serializable
data class EdgePurgeResponse(
    val success: Boolean = false,
    val message: String = "",
    val timestamp: String = "",
)

@Serializable
data class AppSessionRecord(
    @SerialName("device_id") val deviceId: String,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("version_code") val versionCode: Long? = null,
    val platform: String? = "android",
    @SerialName("last_active") val lastActive: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AppCrashRecord(
    val id: Long? = null,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("version_code") val versionCode: Long? = null,
    val platform: String? = "android",
    @SerialName("error_type") val errorType: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("stack_trace") val stackTrace: String? = null,
    @SerialName("device_model") val deviceModel: String? = null,
    @SerialName("os_version") val osVersion: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

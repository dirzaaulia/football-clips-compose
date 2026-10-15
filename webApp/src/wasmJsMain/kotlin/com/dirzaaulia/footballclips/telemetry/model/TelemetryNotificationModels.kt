package com.dirzaaulia.footballclips.telemetry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationTelemetryPayload(
    @SerialName("updated_at") val updatedAt: String = "",
    val count: Int = 0,
    val notifications: List<NotificationLogItem> = emptyList()
)

@Serializable
data class NotificationLogItem(
    val id: String,
    val timestamp: String,
    @SerialName("event_type") val eventType: String,
    val title: String,
    val body: String,
    @SerialName("match_id") val matchId: String = "",
    @SerialName("home_team") val homeTeam: String = "",
    @SerialName("away_team") val awayTeam: String = "",
    @SerialName("club_ids") val clubIds: List<Int> = emptyList(),
    @SerialName("channel_id") val channelId: String = "channel_live_scores",
    val audience: NotificationAudienceMetrics = NotificationAudienceMetrics()
)

@Serializable
data class NotificationAudienceMetrics(
    @SerialName("distinct_users") val distinctUsers: Int = 0,
    @SerialName("total_devices") val totalDevices: Int = 0,
    @SerialName("device_to_user_ratio") val deviceToUserRatio: Double = 1.0,
    @SerialName("platform_breakdown") val platformBreakdown: NotificationPlatformBreakdown = NotificationPlatformBreakdown(),
    val distribution: NotificationDistribution = NotificationDistribution()
)

@Serializable
data class NotificationPlatformBreakdown(
    val android: Int = 0,
    val web: Int = 0
)

@Serializable
data class NotificationDistribution(
    @SerialName("single_device_users") val singleDeviceUsers: Int = 0,
    @SerialName("multi_device_users") val multiDeviceUsers: Int = 0,
    @SerialName("max_devices_for_single_user") val maxDevicesForSingleUser: Int = 1
)

@Serializable
data class AudienceStatePayload(
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("global_metrics") val globalMetrics: GlobalAudienceMetrics = GlobalAudienceMetrics(),
    val clubs: Map<String, ClubAudienceInfo> = emptyMap()
)

@Serializable
data class GlobalAudienceMetrics(
    @SerialName("total_devices") val totalDevices: Int = 0,
    @SerialName("distinct_users") val distinctUsers: Int = 0,
    @SerialName("android_devices") val androidDevices: Int = 0,
    @SerialName("web_devices") val webDevices: Int = 0
)

@Serializable
data class ClubAudienceInfo(
    @SerialName("club_id") val clubId: Int = 0,
    @SerialName("total_devices") val totalDevices: Int = 0,
    @SerialName("distinct_users") val distinctUsers: Int = 0,
    @SerialName("device_to_user_ratio") val deviceToUserRatio: Double = 1.0,
    @SerialName("platform_breakdown") val platformBreakdown: NotificationPlatformBreakdown = NotificationPlatformBreakdown(),
    val distribution: NotificationDistribution = NotificationDistribution()
)

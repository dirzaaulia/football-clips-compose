package com.dirzaaulia.footballclips.data.model.remote

import com.dirzaaulia.footballclips.domain.model.FootballShort
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FootballShortDto(
    @SerialName("id") val id: String,
    @SerialName("youtube_video_id") val youtubeVideoId: String,
    @SerialName("title") val title: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("channel_name") val channelName: String,
    @SerialName("club_id") val clubId: Int? = null,
    @SerialName("category") val category: String = "GOALS_SKILLS",
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("duration_seconds") val durationSeconds: Int = 45,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("published_at") val publishedAt: String,
)

fun FootballShortDto.toDomain(): FootballShort {
    return FootballShort(
        id = id,
        youtubeVideoId = youtubeVideoId,
        title = title,
        channelId = channelId,
        channelName = channelName,
        clubId = clubId,
        category = category,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        likeCount = likeCount,
        isFeatured = isFeatured,
        publishedAt = publishedAt,
    )
}

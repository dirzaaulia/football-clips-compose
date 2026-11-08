package com.dirzaaulia.footballclips.domain.model

data class FootballShort(
    val id: String,
    val youtubeVideoId: String,
    val title: String,
    val channelId: String,
    val channelName: String,
    val clubId: Int? = null,
    val category: String = "GOALS_SKILLS",
    val thumbnailUrl: String,
    val durationSeconds: Int = 45,
    val likeCount: Int = 0,
    val isFeatured: Boolean = false,
    val publishedAt: String,
)

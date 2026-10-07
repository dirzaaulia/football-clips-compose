package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("id")
    val id: String,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
    @SerialName("email")
    val email: String? = null
)

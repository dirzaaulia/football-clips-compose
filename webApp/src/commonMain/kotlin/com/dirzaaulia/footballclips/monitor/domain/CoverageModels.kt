package com.dirzaaulia.footballclips.monitor.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CountryStatus(
    val code: String,
    val name: String,
    val state: String,
    val required: Boolean,
    val platformLimited: Boolean,
)
data class CoverageSummary(
    val priority: String,
    val priorityTotal: String,
    val worldwide: String,
    val total: String,
    val unrestricted: Boolean,
    val countries: ImmutableList<CountryStatus> = persistentListOf(),
)
data class SourceChannel(
    val id: String,
    val name: String,
    val kind: String,
    val handle: String,
    val channelId: String,
    val identity: String,
    val approved: Boolean,
    val rights: String,
    val territories: String,
    val evidence: String,
    val verified: String,
    val mapping: String,
)

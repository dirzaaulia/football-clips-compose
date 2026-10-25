package com.dirzaaulia.footballclips.monitor.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MonitorFilter(
    val teamId: String = "",
    val teamName: String = "",
    val opponentId: String = "",
    val opponentName: String = "",
    val query: String = "",
    val competition: String = "",
    val season: String = "",
    val country: String = "ID",
    val minutes: Int = 60,
    val since: String = "",
    val until: String = "",
    val pipeline: String = "youtube",
)

data class TeamOption(val id: String, val name: String, val context: String)
data class Metric(val key: String, val value: String, val target: String)
data class Cycle(
    val id: String,
    val sequence: String,
    val pipeline: String,
    val time: String,
    val mode: String,
    val pending: String,
    val error: String,
    val indicators: ImmutableList<SignalCount> = persistentListOf(),
)
data class MatchRun(
    val id: String,
    val matchId: String,
    val title: String,
    val competition: String,
    val date: String,
    val outcome: String,
    val reason: String,
    val next: String,
    val indicators: ImmutableList<SignalCount> = persistentListOf(),
)
data class Check(
    val title: String,
    val state: String,
    val reason: String,
    val provider: String = "",
    val model: String = "",
)
data class Candidate(
    val id: String,
    val title: String,
    val channel: String,
    val published: String,
    val duration: String,
    val checks: ImmutableList<Check>,
)
data class LogLine(
    val id: String,
    val time: String,
    val level: String,
    val code: String,
    val message: String,
    val match: String = "",
    val cycle: String = "",
    val cycleId: String = "",
    val indicator: String = "",
)
data class Evidence(
    val candidates: ImmutableList<Candidate> = persistentListOf(),
    val events: ImmutableList<LogLine> = persistentListOf(),
    val next: String = "",
    val matchId: String = "",
)
data class QuotaBucket(
    val name: String,
    val used: Long,
    val allowance: Long,
    val remaining: Long,
    val reserved: Long,
    val reconciled: String,
)
data class Dashboard(
    val metrics: ImmutableList<Metric> = persistentListOf(),
    val quota: ImmutableList<QuotaBucket> = persistentListOf(),
    val scope: String = "",
    val generated: String = "",
    val reset: String = "",
    val rollout: String = "",
    val fraction: String = "",
    val series: ImmutableList<ActivitySample> = persistentListOf(),
    val countries: ImmutableList<CountryCoverage> = persistentListOf(),
    val origins: ImmutableList<OriginUsage> = persistentListOf(),
    val jobs: ImmutableList<Backlog> = persistentListOf(),
    val delivery: ImmutableList<Backlog> = persistentListOf(),
    val activationAt: String = "",
)
data class Competition(
    val code: String,
    val name: String,
    val provider: String,
    val approved: Boolean,
    val sources: String,
    val finished: String,
    val lastSync: String,
    val emblem: String,
    val format: String = "unknown",
    val formatDescription: String = "",
    val formatSeason: String = "",
    val formatEvidence: String = "",
)
data class Page<T>(val items: ImmutableList<T> = persistentListOf(), val cursor: String = "")

data class ActivitySample(
    val time: String,
    val events: Long,
    val errors: Long,
    val selections: Long,
    val visibility: Double?,
)
data class CountryCoverage(val code: String, val matches: String, val blocked: String, val unknown: String)
data class OriginUsage(val origin: String, val purpose: String, val bucket: String, val used: String)
data class Backlog(val state: String, val count: String, val oldest: String)

data class SignalCount(val kind: String, val count: String)

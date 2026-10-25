package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.Competition
import com.dirzaaulia.footballclips.monitor.domain.CoverageSummary
import com.dirzaaulia.footballclips.monitor.domain.Cycle
import com.dirzaaulia.footballclips.monitor.domain.Dashboard
import com.dirzaaulia.footballclips.monitor.domain.Evidence
import com.dirzaaulia.footballclips.monitor.domain.LogLine
import com.dirzaaulia.footballclips.monitor.domain.MatchRun
import com.dirzaaulia.footballclips.monitor.domain.MonitorFilter
import com.dirzaaulia.footballclips.monitor.domain.MonitorRepository
import com.dirzaaulia.footballclips.monitor.domain.Page
import com.dirzaaulia.footballclips.monitor.domain.SourceChannel
import com.dirzaaulia.footballclips.monitor.domain.TeamOption
import io.ktor.http.encodeURLParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class MonitorRemoteRepository(private val remote: MonitorRemoteClient) : MonitorRepository {

    private suspend fun read(path: String): JsonObject {
        val result = remote.read(path)
        cachePage(path, result.toString())
        return result
    }

    override suspend fun cachedCycles(filter: MonitorFilter): Page<Cycle> = restoredCycles(
        cachedPages("monitor/cycles?${filter.query()}&kind=${filter.pipeline}&limit="),
    )

    override suspend fun cachedMatches(cycleId: String, filter: MonitorFilter): Page<MatchRun> = restoredMatches(
        cachedPages("monitor/cycles/$cycleId/matches?${filter.query()}"),
    )

    override suspend fun coverage(matchId: String): CoverageSummary {
        val items = remote.readArray(
            "https://edge.fc.dirzaaulia.com/rest/v1/matches?id=eq.$matchId&select=id,status,highlight_video_id,youtube_region_info",
        )
        if (items.isNotEmpty()) {
            return items.first().toMatchCoverage()
        }
        return read("monitor/matches/$matchId/coverage").toCoverage()
    }

    override suspend fun sources(code: String, season: String): ImmutableList<SourceChannel> =
        read("monitor/competitions/$code/sources?season=$season").records("items").map {
            it.toSource()
        }.toImmutableList()

    private val dashboardProvider = DashboardProvider(remote)

    override suspend fun dashboard(filter: MonitorFilter): Dashboard = try {
        dashboardProvider.loadDashboard(filter)
    } catch (_: Exception) {
        read("monitor/dashboard?${filter.query()}").toDashboard()
    }

    override suspend fun teams(query: String): ImmutableList<TeamOption> {
        val items = remote.readArray("https://edge.fc.dirzaaulia.com/rest/v1/supported_clubs?select=*")
        if (items.isNotEmpty()) {
            return items.filter {
                query.isBlank() || it.text("name").contains(query, true) || it.text("short_name").contains(query, true)
            }.map {
                TeamOption(it.text("id"), it.text("name"), it.text("competition_code", ""))
            }.toImmutableList()
        }
        return read("monitor/teams?q=${query.encodeURLParameter()}").records("items").map {
            TeamOption(it.text("id"), it.text("name"), it.text("context", ""))
        }.toImmutableList()
    }

    override suspend fun cycles(filter: MonitorFilter, cursor: String, limit: Int): Page<Cycle> {
        val data =
            read("monitor/cycles?${filter.query(cursor.isNotBlank())}&kind=${filter.pipeline}&limit=$limit&$cursor")

        val next = data.obj("next_cursor")

        return Page(
            data.records("items").map {
                it.toCycle()
            }.toImmutableList(),
            if (next.isEmpty()) {
                ""
            } else {
                "before=${next.text(
                    "before",
                ).encodeURLParameter()}&before_id=${next.text("before_id")}"
            },
        )
    }

    override suspend fun matches(cycleId: String, filter: MonitorFilter, cursor: String): Page<MatchRun> {
        val data = read(
            "monitor/cycles/$cycleId/matches?${filter.query()}&after=$cursor".removeSuffix("&after="),
        )
        val filtered = data.records("items").map { it.toMatch() }
            .filter {
                if (filter.pipeline ==
                    "youtube"
                ) {
                    it.outcome != "TIMED" && it.outcome != "SCHEDULED" && it.outcome != "IN_PLAY"
                } else {
                    true
                }
            }
            .toImmutableList()

        return Page(filtered, data.text("next_cursor", ""))
    }

    override suspend fun evidence(evaluationId: String, cursor: String): Evidence = read(
        (
            if (evaluationId.startsWith(
                    "match:",
                )
            ) {
                "monitor/matches/${evaluationId.removePrefix(
                    "match:",
                )}/evidence"
            } else {
                "monitor/evaluations/$evaluationId"
            }
            ) +
            if (cursor.isBlank()) "" else "?before=$cursor",
    ).toEvidence()

    override suspend fun catalog(filter: MonitorFilter, mode: String, cursor: String): Page<MatchRun> {
        val selected = mode.removePrefix("video_")
        val postgrestUrl = buildCatalogPostgrestUrl(filter, selected)
        val items = remote.readArray(postgrestUrl)
        if (items.isNotEmpty()) {
            val runs = items.map { it.toMatch() }
                .filter {
                    if (filter.pipeline ==
                        "youtube"
                    ) {
                        it.outcome != "TIMED" && it.outcome != "SCHEDULED" && it.outcome != "IN_PLAY"
                    } else {
                        true
                    }
                }
                .toImmutableList()
            return Page(runs, "")
        }

        val route = if (mode.startsWith("video_")) "videos" else "catalog"
        val data = read(
            "monitor/$route?${filter.query()}&mode=$selected" + if (cursor.isBlank()) "" else "&after=$cursor",
        )
        val filtered = data.records("items").map { it.toMatch() }
            .filter {
                if (filter.pipeline ==
                    "youtube"
                ) {
                    it.outcome != "TIMED" && it.outcome != "SCHEDULED" && it.outcome != "IN_PLAY"
                } else {
                    true
                }
            }
            .toImmutableList()

        return Page(filtered, data.text("next_cursor", ""))
    }

    override suspend fun competitions(): ImmutableList<Competition> {
        val items = remote.readArray("https://edge.fc.dirzaaulia.com/rest/v1/competitions?select=*&is_active=eq.true")
        if (items.isNotEmpty()) {
            return items.map {
                val code = it.text("id")
                val type = it.text("type", "league")
                Competition(
                    code = code,
                    name = it.text("name"),
                    provider = it.text("short_name", code),
                    approved = true,
                    sources = "Official Channels",
                    finished = "Active",
                    lastSync = it.text("updated_at", ""),
                    emblem = it.text("emblem_url", ""),
                    format = type,
                    formatDescription = if (type ==
                        "hybrid"
                    ) {
                        "Group Stage + Knockout"
                    } else if (type == "cup") {
                        "Knockout Cup"
                    } else {
                        "League"
                    },
                    formatSeason = "2026/27",
                    formatEvidence = "PostgreSQL 16 Schema",
                )
            }.toImmutableList()
        }
        return read("monitor/competitions").records("items").map {
            Competition(
                it.text("code"),
                it.text("name"),
                it.text("provider_slug"),
                it.text("approved") == "true",
                it.text("approved_sources"),
                it.text("finished"),
                it.text("last_sync"),
                it.text("emblem", ""),
                it.text("format", it.text("type", "league")),
                it.text("format_description", ""),
                it.text("format_season", ""),
                it.text("format_evidence", ""),
            )
        }.toImmutableList()
    }

    override suspend fun logs(filter: MonitorFilter, before: String, after: String, cycle: String): Page<LogLine> {
        val paging = if (before.isNotBlank()) {
            "&before=$before"
        } else if (after.isNotBlank()) {
            "&after=$after"
        } else {
            ""
        }

        val data = read(
            "monitor/events?${filter.query()}&cycle_kind=${filter.pipeline}&limit=100&cycle_id=$cycle$paging".replace(
                "&cycle_id=&",
                "&",
            ).removeSuffix("&cycle_id="),
        )

        return Page(
            data.records("items").map {
                LogLine(
                    it.text("id"),
                    it.obj("attributes").text("occurred_at", it.text("created_at")),
                    it.text("level"),
                    it.text("code"),
                    it.text("message"),

                    listOf(it.text("home_team_name", ""), it.text("away_team_name", "")).filter { name ->
                        name.isNotBlank()
                    }.joinToString(" — "),
                    it.text("cycle_sequence", ""),
                    it.text("cycle_id", ""),
                    it.text("indicator", ""),
                )
            }.toImmutableList(),
            data.text("next_cursor", ""),
        )
    }

    override suspend fun correct(matchId: String, videoId: String, undo: Boolean) {
        val action = if (undo) "undo-rejection" else "wrong-highlight"

        remote.mutate("monitor/matches/$matchId/$action", JsonObject(mapOf("video_id" to JsonPrimitive(videoId))))
    }
}

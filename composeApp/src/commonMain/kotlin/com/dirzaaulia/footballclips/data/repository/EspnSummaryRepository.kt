package com.dirzaaulia.footballclips.data.repository

import com.dirzaaulia.footballclips.data.cache.InMemorySwrCache
import com.dirzaaulia.footballclips.data.model.remote.FormationsDto
import com.dirzaaulia.footballclips.data.model.remote.StarterPlayerDto
import com.dirzaaulia.footballclips.data.model.remote.TeamFormationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface EspnSummaryRepository {
    suspend fun getTacticalSummary(matchId: Long, competitionCode: String): Pair<FormationsDto?, Map<String, Map<String, String>>?>
}

class EspnSummaryRepositoryImpl(
    private val client: HttpClient,
) : EspnSummaryRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val summaryCache = InMemorySwrCache<Long, Pair<FormationsDto?, Map<String, Map<String, String>>?>>(ttlMillis = 300_000L)

    private val competitionSlugs = mapOf(
        "CL" to "uefa.champions",
        "EL" to "uefa.europa",
        "ECL" to "uefa.ecl",
        "PL" to "eng.1",
        "PD" to "esp.1",
        "SA" to "ita.1",
        "BL1" to "ger.1",
        "FL1" to "fra.1",
        "SPL" to "ksa.1",
        "MLS" to "usa.1",
        "DED" to "ned.1",
        "PPL" to "por.1",
    )

    override suspend fun getTacticalSummary(
        matchId: Long,
        competitionCode: String,
    ): Pair<FormationsDto?, Map<String, Map<String, String>>?> {
        val cached = summaryCache.get(matchId)
        if (cached != null) return cached

        val slug = competitionSlugs[competitionCode.uppercase()] ?: "eng.1"
        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/$slug/summary?event=$matchId"

        return try {
            val responseText: String = client.get(url) {
                header("User-Agent", "Mozilla/5.0")
            }.body()

            val root = json.parseToJsonElement(responseText).jsonObject
            val formations = parseFormations(root)
            val statistics = parseStatistics(root)

            val result = Pair(formations, statistics)
            summaryCache.put(matchId, result)
            result
        } catch (_: Exception) {
            summaryCache.getStale(matchId) ?: Pair(null, null)
        }
    }

    private fun parseFormations(root: JsonObject): FormationsDto? {
        val rosters = root["rosters"] as? JsonArray ?: return null
        var homeFormation: TeamFormationDto? = null
        var awayFormation: TeamFormationDto? = null

        for (item in rosters) {
            val rosterObj = item.jsonObject
            val side = rosterObj["homeAway"]?.jsonPrimitive?.content ?: continue
            val formationStr = rosterObj["formation"]?.jsonPrimitive?.content
            val rosterArray = rosterObj["roster"] as? JsonArray ?: continue

            val starters = mutableListOf<StarterPlayerDto>()
            for (p in rosterArray) {
                val pObj = p.jsonObject
                val isStarter = pObj["starter"]?.jsonPrimitive?.content?.toBoolean() ?: false
                if (isStarter) {
                    val athlete = pObj["athlete"]?.jsonObject
                    val name = athlete?.get("displayName")?.jsonPrimitive?.content ?: "Player"
                    val jersey = pObj["jersey"]?.jsonPrimitive?.content ?: ""
                    val position = pObj["position"]?.jsonObject?.get("abbreviation")?.jsonPrimitive?.content ?: ""
                    starters.add(StarterPlayerDto(name = name, jersey = jersey, position = position))
                }
            }

            val teamFormation = TeamFormationDto(formation = formationStr, starters = starters)
            if (side == "home") homeFormation = teamFormation else awayFormation = teamFormation
        }

        return if (homeFormation != null || awayFormation != null) {
            FormationsDto(home = homeFormation, away = awayFormation)
        } else {
            null
        }
    }

    private fun parseStatistics(root: JsonObject): Map<String, Map<String, String>>? {
        val boxscore = root["boxscore"]?.jsonObject ?: return null
        val teams = boxscore["teams"] as? JsonArray ?: return null
        val statsMap = mutableMapOf<String, MutableMap<String, String>>()

        for (t in teams) {
            val tObj = t.jsonObject
            val side = tObj["homeAway"]?.jsonPrimitive?.content ?: continue
            val sideStats = mutableMapOf<String, String>()
            val statsList = tObj["statistics"] as? JsonArray ?: continue

            for (s in statsList) {
                val sObj = s.jsonObject
                val name = sObj["name"]?.jsonPrimitive?.content ?: continue
                val displayValue = sObj["displayValue"]?.jsonPrimitive?.content ?: continue
                sideStats[name] = displayValue
            }
            statsMap[side] = sideStats
        }

        return if (statsMap.isNotEmpty()) statsMap else null
    }
}

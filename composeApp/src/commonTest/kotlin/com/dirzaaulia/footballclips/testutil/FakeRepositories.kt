package com.dirzaaulia.footballclips.testutil

import com.dirzaaulia.footballclips.data.cache.InMemorySwrCache
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.remote.CatalogPageDto
import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import com.dirzaaulia.footballclips.data.model.remote.RegionRestrictionDto
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.data.repository.ShortsRepository
import com.dirzaaulia.footballclips.domain.model.FootballShort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeOracleVmRepository : OracleVmRepository {

    var shouldFailNetwork: Boolean = false
    private val swrCache = InMemorySwrCache<String, List<MatchDto>>(ttlMillis = 60_000L)

    val mockSeason2026Matches = listOf(
        MatchDto(
            id = 401915001L,
            competitionId = "PL",
            competitionName = "Premier League",
            utcDate = "2026-11-08T16:30:00Z",
            status = "FINISHED",
            matchday = 11,
            homeTeamId = 359L,
            homeTeamName = "Arsenal",
            awayTeamId = 363L,
            awayTeamName = "Chelsea",
            homeScore = 3,
            awayScore = 2,
            highlightVideoId = "vid_ars_che_1",
            backupHighlightVideoId = "vid_ars_che_backup",
            season = 2026,
            youtubeRegionInfo = RegionRestrictionDto(blocked = listOf("GB")),
            backupYoutubeRegionInfo = RegionRestrictionDto(allowed = listOf("GB", "US", "ID")),
        ),
        MatchDto(
            id = 401915002L,
            competitionId = "CL",
            competitionName = "UEFA Champions League",
            utcDate = "2026-11-08T20:00:00Z",
            status = "IN_PLAY",
            matchday = 4,
            homeTeamId = 86L,
            homeTeamName = "Real Madrid",
            awayTeamId = 132L,
            awayTeamName = "Bayern Munich",
            homeScore = 2,
            awayScore = 2,
            displayClock = "78'",
            highlightVideoId = null,
            season = 2026,
        ),
    )

    val mockArchive2025Matches = listOf(
        MatchDto(
            id = 301800101L,
            competitionId = "PL",
            competitionName = "Premier League",
            utcDate = "2025-05-18T15:00:00Z",
            status = "FINISHED",
            matchday = 38,
            homeTeamName = "Arsenal",
            awayTeamName = "Everton",
            homeScore = 2,
            awayScore = 1,
            highlightVideoId = "vid_archive_2025",
            season = 2025,
        ),
    )

    private val _competitions = MutableStateFlow<List<BigLeague>>(emptyList())
    override val competitions: StateFlow<List<BigLeague>> = _competitions.asStateFlow()

    private val _supportedClubs = MutableStateFlow<List<SupportedClub>>(emptyList())
    override val supportedClubs: StateFlow<List<SupportedClub>> = _supportedClubs.asStateFlow()

    override suspend fun getMatches(
        competitionId: String?,
        status: String?,
        excludeStatus: String?,
        startDate: String?,
        endDate: String?,
        hasHighlight: Boolean,
        ascending: Boolean,
        limit: Int,
        offset: Int,
        season: Int?,
        query: String?,
    ): NetworkResult<List<MatchDto>> {
        val targetSeason = season ?: 2026
        val cacheKey = "$competitionId-$status-$targetSeason-$hasHighlight-$query"

        if (shouldFailNetwork) {
            val stale = swrCache.getStale(cacheKey)
            return if (stale != null) {
                NetworkResult.Success(stale)
            } else {
                NetworkResult.Error(503, "Simulated offline network error")
            }
        }

        val source = if (targetSeason == 2025) mockArchive2025Matches else mockSeason2026Matches
        val filtered = source.filter { m ->
            (competitionId.isNullOrBlank() || m.competitionId.equals(competitionId, ignoreCase = true)) &&
                (status.isNullOrBlank() || m.status.equals(status, ignoreCase = true)) &&
                (!hasHighlight || m.highlightVideoId != null) &&
                (query.isNullOrBlank() || m.homeTeamName.contains(query, ignoreCase = true) || m.awayTeamName.contains(query, ignoreCase = true))
        }

        swrCache.put(cacheKey, filtered)
        return NetworkResult.Success(filtered)
    }

    override suspend fun getMatch(id: Long): NetworkResult<MatchDto> {
        if (shouldFailNetwork) return NetworkResult.Error(503, "Offline")
        val all = mockSeason2026Matches + mockArchive2025Matches
        val found = all.firstOrNull { it.id == id }
        return if (found != null) NetworkResult.Success(found) else NetworkResult.Error(404, "Not found")
    }

    override suspend fun getLiveMatches(): NetworkResult<List<MatchDto>> {
        if (shouldFailNetwork) return NetworkResult.Error(503, "Offline")
        return NetworkResult.Success(mockSeason2026Matches.filter { it.status == "IN_PLAY" })
    }

    override suspend fun getHighlightPage(
        competition: String?,
        season: Int,
        cursor: String?,
        query: String,
    ): NetworkResult<CatalogPageDto> {
        return NetworkResult.Error(501, "Use getMatches in tests")
    }

    override suspend fun getStandings(
        competitionId: String,
        season: Int?,
    ): NetworkResult<List<StandingDto>> {
        if (shouldFailNetwork) return NetworkResult.Error(503, "Offline")
        return NetworkResult.Success(
            listOf(
                StandingDto(
                    position = 1,
                    teamId = 359,
                    teamName = "Arsenal",
                    playedGames = 11,
                    won = 9,
                    draw = 2,
                    lost = 0,
                    points = 29,
                    goalsFor = 26,
                    goalsAgainst = 8,
                    goalDifference = 18,
                    competitionId = competitionId,
                ),
            ),
        )
    }

    override suspend fun refreshCompetitions() {}
    override suspend fun refreshClubs() {}
    override fun findClubById(id: String): SupportedClub? = null
}

class FakeShortsRepository : ShortsRepository {
    val mockShorts = (1..12).map { idx ->
        FootballShort(
            id = "short_$idx",
            youtubeVideoId = "yt_short_${idx.toString().padStart(2, '0')}",
            title = "Incredible Skill & Goal Clip #$idx 🔥 #shorts",
            channelId = "UC_club_$idx",
            channelName = if (idx % 2 == 0) "Arsenal Official" else "Real Madrid",
            clubId = if (idx % 2 == 0) 359 else 86,
            category = if (idx % 3 == 0) "MOMENTS" else "GOALS_SKILLS",
            thumbnailUrl = "https://i.ytimg.com/vi/yt_short_$idx/hqdefault.jpg",
            durationSeconds = 38,
            likeCount = idx * 1250,
            isFeatured = idx == 1,
            publishedAt = "2026-11-08T12:00:00Z",
        )
    }

    override suspend fun getShorts(
        category: String?,
        limit: Int,
        offset: Int,
    ): NetworkResult<List<FootballShort>> {
        val filtered = if (category == null || category == "ALL") {
            mockShorts
        } else {
            mockShorts.filter { it.category == category }
        }
        return NetworkResult.Success(filtered.drop(offset).take(limit))
    }
}

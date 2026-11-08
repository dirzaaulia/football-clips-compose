package com.dirzaaulia.footballclips.journeys

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.testutil.FakeOracleVmRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixturesSwrJourneyTest {

    @Test
    fun testDualSeasonActive2026AndArchive2025Isolation() = runBlocking {
        val repo = FakeOracleVmRepository()

        // Active Season 2026/27
        val activeRes = repo.getMatches(season = 2026)
        assertTrue(activeRes is NetworkResult.Success)
        assertEquals(2, activeRes.data.size)
        assertTrue(activeRes.data.all { it.season == 2026 })

        // Historical Archive Season 2025/26
        val archiveRes = repo.getMatches(season = 2025)
        assertTrue(archiveRes is NetworkResult.Success)
        assertEquals(1, archiveRes.data.size)
        assertEquals(2025, archiveRes.data.first().season)
        assertEquals("Everton", archiveRes.data.first().awayTeamName)
    }

    @Test
    fun testSwrOfflineFallbackWhenNetworkFails() = runBlocking {
        val repo = FakeOracleVmRepository()

        // 1. Cold network request when offline with empty cache -> returns Error
        repo.shouldFailNetwork = true
        val coldOfflineRes = repo.getMatches(competitionId = "PL", season = 2026)
        assertTrue(coldOfflineRes is NetworkResult.Error)

        // 2. Online request populates the SWR cache
        repo.shouldFailNetwork = false
        val onlineRes = repo.getMatches(competitionId = "PL", season = 2026)
        assertTrue(onlineRes is NetworkResult.Success)
        assertEquals(1, onlineRes.data.size)
        assertEquals("Arsenal", onlineRes.data.first().homeTeamName)

        // 3. Subsequent request when network drops -> seamlessly serves cached SWR data
        repo.shouldFailNetwork = true
        val swrFallbackRes = repo.getMatches(competitionId = "PL", season = 2026)
        assertTrue(swrFallbackRes is NetworkResult.Success)
        assertEquals(1, swrFallbackRes.data.size)
        assertEquals(401915001L, swrFallbackRes.data.first().id)
    }

    @Test
    fun testStandingsRetrieval() = runBlocking {
        val repo = FakeOracleVmRepository()
        val standingsRes = repo.getStandings(competitionId = "PL", season = 2026)
        assertTrue(standingsRes is NetworkResult.Success)
        assertEquals(1, standingsRes.data.size)
        assertEquals("Arsenal", standingsRes.data.first().teamName)
        assertEquals(29, standingsRes.data.first().points)
    }
}

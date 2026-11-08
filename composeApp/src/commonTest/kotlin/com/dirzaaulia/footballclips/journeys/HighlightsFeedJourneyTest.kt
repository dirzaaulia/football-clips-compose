package com.dirzaaulia.footballclips.journeys

import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.testutil.FakeOracleVmRepository
import com.dirzaaulia.footballclips.ui.components.card.arrangeExcitementCarouselPages
import com.dirzaaulia.footballclips.ui.components.card.calculateExcitementRank
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HighlightsFeedJourneyTest {

    @Test
    fun testExcitementScoreAndSpoilerFreeTagMasking() {
        val repo = FakeOracleVmRepository()
        val arsenalVsChelsea = repo.mockSeason2026Matches.first().toMatch()
        val uiItem = HighlightUiItem.OracleMatch(arsenalVsChelsea)

        // Normal mode (isSpoilerFree = false): reveals exact goal count
        val rankedNormal = calculateExcitementRank(uiItem, isSpoilerFree = false)
        assertEquals(130, rankedNormal.score) // 65 (Clash of Titans) + 50 (5 goals) + 15 (1-goal margin)
        assertEquals("🔥 Goal Fest • 5 Goals", rankedNormal.tag)

        // Spoiler-Free mode (isSpoilerFree = true): masks goal count so result isn't spoiled
        val rankedSpoilerFree = calculateExcitementRank(uiItem, isSpoilerFree = true)
        assertEquals(130, rankedSpoilerFree.score)
        assertEquals("🔥 Goal Fest", rankedSpoilerFree.tag)
        assertFalse(rankedSpoilerFree.tag.contains("5 Goals"))
    }

    @Test
    fun testCarouselAdInjectionVsPremiumSuppression() {
        val repo = FakeOracleVmRepository()
        val match = repo.mockSeason2026Matches.first().toMatch()
        val items = (1..4).map { idx ->
            HighlightUiItem.OracleMatch(match.copy(id = idx.toLong()))
        }

        // Non-premium user (isAdsRemoved = false): BannerAd injected after every 2 items
        val freePages = arrangeExcitementCarouselPages(
            items = items,
            isAdsRemoved = false,
            isWasmWeb = false,
            isSpoilerFree = false,
        )
        assertTrue(freePages.any { it.first is HighlightUiItem.BannerAd })

        // Premium user (isAdsRemoved = true): 0 ads injected
        val premiumPages = arrangeExcitementCarouselPages(
            items = items,
            isAdsRemoved = true,
            isWasmWeb = false,
            isSpoilerFree = false,
        )
        assertEquals(4, premiumPages.size)
        assertTrue(premiumPages.none { it.first is HighlightUiItem.BannerAd })
    }

    @Test
    fun testHighlightsFilteringAndSearch() = runBlocking {
        val repo = FakeOracleVmRepository()

        // Filter by competition "PL" and hasHighlight = true
        val resPl = repo.getMatches(competitionId = "PL", hasHighlight = true)
        assertTrue(resPl is NetworkResult.Success)
        assertEquals(1, resPl.data.size)
        assertEquals("Arsenal", resPl.data.first().homeTeamName)

        // Search query "Chelsea"
        val resSearch = repo.getMatches(query = "Chelsea")
        assertTrue(resSearch is NetworkResult.Success)
        assertEquals(1, resSearch.data.size)
        assertEquals(401915001L, resSearch.data.first().id)
    }
}

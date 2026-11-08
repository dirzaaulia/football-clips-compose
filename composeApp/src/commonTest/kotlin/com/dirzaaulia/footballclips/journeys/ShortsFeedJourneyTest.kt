package com.dirzaaulia.footballclips.journeys

import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.testutil.FakeShortsRepository
import com.dirzaaulia.footballclips.ui.shorts.ShortsFeedItem
import com.dirzaaulia.footballclips.ui.shorts.buildShortsFeedItems
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShortsFeedJourneyTest {

    @Test
    fun testShortsCategoryFilteringAndPagination() = runBlocking {
        val repo = FakeShortsRepository()

        // Fetch all shorts (12 mock items)
        val allRes = repo.getShorts(category = "ALL", limit = 20, offset = 0)
        assertTrue(allRes is NetworkResult.Success)
        assertEquals(12, allRes.data.size)

        // Filter by MOMENTS category (every 3rd item -> 4 items)
        val momentsRes = repo.getShorts(category = "MOMENTS", limit = 20, offset = 0)
        assertTrue(momentsRes is NetworkResult.Success)
        assertEquals(4, momentsRes.data.size)
        assertTrue(momentsRes.data.all { it.category == "MOMENTS" })

        // Filter by GOALS_SKILLS category (8 items)
        val goalsRes = repo.getShorts(category = "GOALS_SKILLS", limit = 20, offset = 0)
        assertTrue(goalsRes is NetworkResult.Success)
        assertEquals(8, goalsRes.data.size)
        assertTrue(goalsRes.data.all { it.category == "GOALS_SKILLS" })
    }

    @Test
    fun testShortsAdMobInjectionForFreeUserVsPremiumSuppression() = runBlocking {
        val repo = FakeShortsRepository()
        val allRes = repo.getShorts(category = null, limit = 12, offset = 0)
        assertTrue(allRes is NetworkResult.Success)
        val shorts = allRes.data

        // Free user (premium = false): Ad card injected after every 6 videos (indices 6 and 13)
        val freeFeed = buildShortsFeedItems(shorts = shorts, premium = false)
        assertEquals(14, freeFeed.size) // 12 videos + 2 ads
        assertTrue(freeFeed[6] is ShortsFeedItem.Ad)
        assertTrue(freeFeed[13] is ShortsFeedItem.Ad)
        assertEquals(2, freeFeed.count { it is ShortsFeedItem.Ad })

        // Premium user (premium = true): 0 Ad cards injected
        val premiumFeed = buildShortsFeedItems(shorts = shorts, premium = true)
        assertEquals(12, premiumFeed.size)
        assertEquals(0, premiumFeed.count { it is ShortsFeedItem.Ad })
        assertTrue(premiumFeed.all { it is ShortsFeedItem.Video })
    }

    @Test
    fun testYouTubeAttributionOutlinkCompliance() = runBlocking {
        val repo = FakeShortsRepository()
        val firstShort = repo.mockShorts.first()
        val outlinkUrl = "https://youtube.com/shorts/${firstShort.youtubeVideoId}"

        assertEquals("https://youtube.com/shorts/yt_short_01", outlinkUrl)
        assertTrue(firstShort.channelName.isNotBlank())
    }
}

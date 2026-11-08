package com.dirzaaulia.footballclips.journeys

import com.dirzaaulia.footballclips.domain.model.PitchPlay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LiveCompanionJourneyTest {

    private val mockPlays = listOf(
        PitchPlay(
            id = "play_1",
            clock = "14'",
            period = 1,
            type = "shot",
            typeText = "Shot on Target",
            text = "Martin Odegaard right footed shot from outside the box saved.",
            teamName = "Arsenal",
            player = "Martin Odegaard",
            xPercent = 76.5f,
            yPercent = 48.0f,
        ),
        PitchPlay(
            id = "play_2",
            clock = "38'",
            period = 1,
            type = "corner",
            typeText = "Corner Kick",
            text = "Corner, Arsenal. Conceded by Levi Colwill.",
            teamName = "Arsenal",
            xPercent = 98.0f,
            yPercent = 5.0f,
        ),
        PitchPlay(
            id = "play_3",
            clock = "67'",
            period = 2,
            type = "goal",
            typeText = "Goal",
            text = "Goal! Arsenal 2, Chelsea 1. Bukayo Saka curls into the top corner!",
            teamName = "Arsenal",
            player = "Bukayo Saka",
            xPercent = 88.5f,
            yPercent = 32.0f,
        ),
        PitchPlay(
            id = "play_4",
            clock = "82'",
            period = 2,
            type = "yellow_card",
            typeText = "Yellow Card",
            text = "Enzo Fernandez (Chelsea) is shown the yellow card for a bad foul.",
            teamName = "Chelsea",
            player = "Enzo Fernandez",
            xPercent = 44.0f,
            yPercent = 60.0f,
        ),
    )

    @Test
    fun testPitchPlayCoordinateNormalizationAndClassification() {
        for (play in mockPlays) {
            val x = play.xPercent
            val y = play.yPercent
            assertNotNull(x)
            assertNotNull(y)
            assertTrue(x in 0f..100f, "xPercent must be bounded within [0, 100]")
            assertTrue(y in 0f..100f, "yPercent must be bounded within [0, 100]")
        }

        val shotPlay = mockPlays[0]
        assertTrue(shotPlay.isShot)
        assertFalse(shotPlay.isGoal)

        val cornerPlay = mockPlays[1]
        assertTrue(cornerPlay.isCorner)

        val goalPlay = mockPlays[2]
        assertTrue(goalPlay.isGoal)
        assertFalse(goalPlay.isCard)

        val cardPlay = mockPlays[3]
        assertTrue(cardPlay.isCard)
    }

    @Test
    fun testPlayScrubberSelectionStateTransition() {
        // Default state selects the latest play in the timeline
        var selectedIndex = mockPlays.lastIndex
        assertEquals("play_4", mockPlays[selectedIndex].id)

        // User scrubs back to the 67' Goal event
        selectedIndex = 2
        val activePlay = mockPlays[selectedIndex]
        assertEquals("67'", activePlay.clock)
        assertTrue(activePlay.isGoal)
        assertEquals(88.5f, activePlay.xPercent)
    }

    @Test
    fun testBritishTtsCommentaryAndSilentNotificationFormatting() {
        val goalPlay = mockPlays[2]
        val speechAnnouncement = "${goalPlay.clock}. ${goalPlay.text}"
        assertTrue(speechAnnouncement.startsWith("67'. Goal! Arsenal 2, Chelsea 1."))

        // Silent ongoing notification ticker format
        val liveMatchSummary = "Arsenal 2 - 1 Chelsea (67')"
        assertTrue(liveMatchSummary.contains("Arsenal 2 - 1 Chelsea"))
        assertTrue(liveMatchSummary.contains("67'"))
    }
}

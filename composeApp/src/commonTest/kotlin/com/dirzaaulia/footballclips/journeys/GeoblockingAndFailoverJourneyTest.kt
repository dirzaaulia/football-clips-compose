package com.dirzaaulia.footballclips.journeys

import com.dirzaaulia.footballclips.domain.model.RegionRestriction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

enum class SimulatedPlaybackDecision {
    PLAY_PRIMARY,
    SWITCH_TO_BACKUP,
    REGION_BLOCKED,
}

fun resolvePlaybackDecision(
    primaryVideoId: String,
    backupVideoId: String?,
    primaryRegion: RegionRestriction?,
    backupRegion: RegionRestriction?,
    userCountry: String?,
): Pair<SimulatedPlaybackDecision, String?> {
    val isPrimaryAllowed = primaryRegion?.isSupportedIn(userCountry) ?: true
    val isBackupAllowed = backupRegion?.isSupportedIn(userCountry) ?: true

    if (isPrimaryAllowed) {
        return SimulatedPlaybackDecision.PLAY_PRIMARY to primaryVideoId
    }
    if (isBackupAllowed && !backupVideoId.isNullOrBlank()) {
        return SimulatedPlaybackDecision.SWITCH_TO_BACKUP to backupVideoId
    }
    return SimulatedPlaybackDecision.REGION_BLOCKED to null
}

class GeoblockingAndFailoverJourneyTest {

    @Test
    fun testPrimaryAllowedPlaysPrimaryVideo() {
        val primaryRegion = RegionRestriction(blocked = listOf("GB"))
        val backupRegion = RegionRestriction(allowed = listOf("GB", "US", "ID"))

        // User in Indonesia ("ID") -> not in primary's blocked list
        val (decision, videoId) = resolvePlaybackDecision(
            primaryVideoId = "vid_primary_101",
            backupVideoId = "vid_backup_202",
            primaryRegion = primaryRegion,
            backupRegion = backupRegion,
            userCountry = "ID",
        )

        assertEquals(SimulatedPlaybackDecision.PLAY_PRIMARY, decision)
        assertEquals("vid_primary_101", videoId)
    }

    @Test
    fun testPrimaryBlockedAutomaticallyFailsOverToBackup() {
        val primaryRegion = RegionRestriction(blocked = listOf("GB", "FR"))
        val backupRegion = RegionRestriction(allowed = listOf("GB", "US", "ID"))

        // User in United Kingdom ("GB") -> blocked on primary, allowed on backup
        val (decision, videoId) = resolvePlaybackDecision(
            primaryVideoId = "vid_primary_101",
            backupVideoId = "vid_backup_202",
            primaryRegion = primaryRegion,
            backupRegion = backupRegion,
            userCountry = "gb", // Case-insensitive check
        )

        assertEquals(SimulatedPlaybackDecision.SWITCH_TO_BACKUP, decision)
        assertEquals("vid_backup_202", videoId)
    }

    @Test
    fun testBothBlockedTriggersRegionBlockedState() {
        val primaryRegion = RegionRestriction(allowed = listOf("US", "CA"))
        val backupRegion = RegionRestriction(blocked = listOf("DE", "IT"))

        // User in Germany ("DE") -> not in primary's allowed list, and in backup's blocked list
        val (decision, videoId) = resolvePlaybackDecision(
            primaryVideoId = "vid_primary_101",
            backupVideoId = "vid_backup_202",
            primaryRegion = primaryRegion,
            backupRegion = backupRegion,
            userCountry = "DE",
        )

        assertEquals(SimulatedPlaybackDecision.REGION_BLOCKED, decision)
        assertEquals(null, videoId)
    }

    @Test
    fun testNullOrEmptyRegionRestrictionDefaultsToGlobalPlayable() {
        val unrestricted = RegionRestriction(allowed = null, blocked = null)
        assertTrue(unrestricted.isSupportedIn("ID"))
        assertTrue(unrestricted.isSupportedIn("GB"))
        assertTrue(unrestricted.isSupportedIn(null))

        val (decision, videoId) = resolvePlaybackDecision(
            primaryVideoId = "vid_global_999",
            backupVideoId = null,
            primaryRegion = null,
            backupRegion = null,
            userCountry = "JP",
        )

        assertEquals(SimulatedPlaybackDecision.PLAY_PRIMARY, decision)
        assertEquals("vid_global_999", videoId)
    }
}

package com.dirzaaulia.footballclips.matrix

import com.dirzaaulia.footballclips.testutil.DeviceMatrixProfiles
import com.dirzaaulia.footballclips.testutil.SimulatedNavigationType
import com.dirzaaulia.footballclips.testutil.SimulatedSheetType
import com.dirzaaulia.footballclips.testutil.SimulatedWindowWidthClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceMatrixLayoutTest {

    @Test
    fun testCompactPhoneResponsiveLayout() {
        val profile = DeviceMatrixProfiles.COMPACT_PHONE
        assertEquals(SimulatedWindowWidthClass.COMPACT, profile.widthClass)
        assertEquals(1, profile.highlightsGridColumns)
        assertEquals(1, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.BOTTOM_FLOATING_PILL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_BOTTOM_SHEET, profile.sheetType)
        assertFalse(profile.isLandscapeFlex)
    }

    @Test
    fun testWidePortraitPhoneResponsiveLayout() {
        val profile = DeviceMatrixProfiles.WIDE_PORTRAIT_PHONE
        assertEquals(SimulatedWindowWidthClass.MEDIUM, profile.widthClass)
        assertEquals(2, profile.highlightsGridColumns)
        assertEquals(2, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.BOTTOM_FLOATING_PILL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_SIDE_SHEET, profile.sheetType)
    }

    @Test
    fun testZFoldCoverScreenLayout() {
        val profile = DeviceMatrixProfiles.FOLDABLE_COVER
        assertEquals(SimulatedWindowWidthClass.COMPACT, profile.widthClass)
        assertEquals(1, profile.highlightsGridColumns)
        assertEquals(1, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.BOTTOM_FLOATING_PILL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_BOTTOM_SHEET, profile.sheetType)
    }

    @Test
    fun testZFoldUnfoldedMainCanvasLayout() {
        val profile = DeviceMatrixProfiles.FOLDABLE_UNFOLDED
        assertEquals(SimulatedWindowWidthClass.MEDIUM, profile.widthClass)
        assertEquals(2, profile.highlightsGridColumns)
        assertEquals(2, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.BOTTOM_FLOATING_PILL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_SIDE_SHEET, profile.sheetType)
    }

    @Test
    fun testLandscapeFlexModeDualPaneLayout() {
        val profile = DeviceMatrixProfiles.LANDSCAPE_FLEX
        assertTrue(profile.isLandscapeFlex)
        assertEquals(2, profile.highlightsGridColumns)
        assertEquals(2, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.LEFT_NAVIGATION_RAIL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_SIDE_SHEET, profile.sheetType)
    }

    @Test
    fun testTabletDesktopExpandedLayout() {
        val profile = DeviceMatrixProfiles.TABLET_DESKTOP
        assertEquals(SimulatedWindowWidthClass.EXPANDED, profile.widthClass)
        assertEquals(3, profile.highlightsGridColumns)
        assertEquals(2, profile.fixturesGridColumns)
        assertEquals(SimulatedNavigationType.LEFT_NAVIGATION_RAIL, profile.navigationType)
        assertEquals(SimulatedSheetType.MODAL_SIDE_SHEET, profile.sheetType)
    }
}

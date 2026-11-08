package com.dirzaaulia.footballclips.testutil

enum class SimulatedWindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

enum class SimulatedNavigationType {
    BOTTOM_FLOATING_PILL,
    LEFT_NAVIGATION_RAIL,
}

enum class SimulatedSheetType {
    MODAL_BOTTOM_SHEET,
    MODAL_SIDE_SHEET,
}

data class DeviceFormFactorProfile(
    val id: String,
    val name: String,
    val widthDp: Int,
    val heightDp: Int,
) {
    val aspectRatio: Float
        get() = widthDp.toFloat() / heightDp.toFloat()

    val isLandscapeFlex: Boolean
        get() = heightDp < 500

    val widthClass: SimulatedWindowWidthClass
        get() = when {
            widthDp < 600 -> SimulatedWindowWidthClass.COMPACT
            widthDp < 840 -> SimulatedWindowWidthClass.MEDIUM
            else -> SimulatedWindowWidthClass.EXPANDED
        }

    val highlightsGridColumns: Int
        get() = when {
            isLandscapeFlex -> 2
            widthDp < 600 -> 1
            widthDp < 840 -> 2
            else -> 3
        }

    val fixturesGridColumns: Int
        get() = when {
            isLandscapeFlex -> 2
            widthDp < 600 -> 1
            else -> 2
        }

    val navigationType: SimulatedNavigationType
        get() = when {
            isLandscapeFlex || widthDp >= 840 -> SimulatedNavigationType.LEFT_NAVIGATION_RAIL
            else -> SimulatedNavigationType.BOTTOM_FLOATING_PILL
        }

    val sheetType: SimulatedSheetType
        get() = when {
            isLandscapeFlex || widthDp >= 600 -> SimulatedSheetType.MODAL_SIDE_SHEET
            else -> SimulatedSheetType.MODAL_BOTTOM_SHEET
        }
}

object DeviceMatrixProfiles {
    val COMPACT_PHONE = DeviceFormFactorProfile(
        id = "phone",
        name = "Compact Phone (390x844)",
        widthDp = 390,
        heightDp = 844,
    )

    val WIDE_PORTRAIT_PHONE = DeviceFormFactorProfile(
        id = "wide_phone",
        name = "Wide Portrait Phone (640x960)",
        widthDp = 640,
        heightDp = 960,
    )

    val FOLDABLE_COVER = DeviceFormFactorProfile(
        id = "foldable_cover",
        name = "Z Fold 8 Cover Screen (380x900)",
        widthDp = 380,
        heightDp = 900,
    )

    val FOLDABLE_UNFOLDED = DeviceFormFactorProfile(
        id = "foldable",
        name = "Z Fold 8 Unfolded Main Canvas (720x820)",
        widthDp = 720,
        heightDp = 820,
    )

    val LANDSCAPE_FLEX = DeviceFormFactorProfile(
        id = "landscape",
        name = "Landscape / Flex Mode (840x390)",
        widthDp = 840,
        heightDp = 390,
    )

    val TABLET_DESKTOP = DeviceFormFactorProfile(
        id = "tablet",
        name = "Tablet / Desktop Display (1024x768)",
        widthDp = 1024,
        heightDp = 768,
    )

    val ALL_PROFILES = listOf(
        COMPACT_PHONE,
        WIDE_PORTRAIT_PHONE,
        FOLDABLE_COVER,
        FOLDABLE_UNFOLDED,
        LANDSCAPE_FLEX,
        TABLET_DESKTOP,
    )
}

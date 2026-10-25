package com.dirzaaulia.footballclips.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// =============================================================================
// TAB 1: HIGHLIGHTS TAB MATRIX PREVIEWS
// =============================================================================

@Preview(name = "1. Highlights - Compact Phone", widthDp = 380, heightDp = 800, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsCompactPhone() {
    HighlightsTabPreviewContent(deviceLabel = "Compact Phone (380x800)")
}

@Preview(name = "2. Highlights - Wide Portrait Phone", widthDp = 680, heightDp = 920, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsWidePortrait() {
    HighlightsTabPreviewContent(deviceLabel = "Wide Portrait Phone (680x920)")
}

@Preview(name = "3. Highlights - Z Fold 8 Cover Screen", widthDp = 400, heightDp = 940, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsZFoldCover() {
    HighlightsTabPreviewContent(deviceLabel = "Z Fold 8 Cover Screen (400x940)")
}

@Preview(name = "4. Highlights - Z Fold 8 Unfolded Canvas", widthDp = 720, heightDp = 820, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsZFoldUnfolded() {
    HighlightsTabPreviewContent(deviceLabel = "Z Fold 8 Unfolded Canvas (720x820)")
}

@Preview(name = "5. Highlights - PitchScope Landscape Phone", widthDp = 840, heightDp = 390, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsPitchScopeLandscape() {
    HighlightsTabPreviewContent(deviceLabel = "PitchScope Landscape (840x390)")
}

@Preview(name = "6. Highlights - Tablet / Desktop", widthDp = 1024, heightDp = 768, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewHighlightsTabletExpanded() {
    HighlightsTabPreviewContent(deviceLabel = "Tablet / Desktop (1024x768)")
}

// =============================================================================
// TAB 2: FIXTURES TAB MATRIX PREVIEWS
// =============================================================================

@Preview(name = "1. Fixtures - Compact Phone", widthDp = 380, heightDp = 800, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewFixturesCompactPhone() {
    FixturesTabPreviewContent(deviceLabel = "Compact Phone (380x800)")
}

@Preview(name = "2. Fixtures - Z Fold 8 Unfolded Canvas", widthDp = 720, heightDp = 820, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewFixturesZFoldUnfolded() {
    FixturesTabPreviewContent(deviceLabel = "Z Fold 8 Unfolded Canvas (720x820)")
}

@Preview(name = "3. Fixtures - PitchScope Landscape Phone", widthDp = 840, heightDp = 390, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewFixturesPitchScopeLandscape() {
    FixturesTabPreviewContent(deviceLabel = "PitchScope Landscape (840x390)")
}

// =============================================================================
// TAB 3: STANDINGS TAB MATRIX PREVIEWS
// =============================================================================

@Preview(name = "1. Standings - Compact Phone", widthDp = 380, heightDp = 800, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewStandingsCompactPhone() {
    StandingsTabPreviewContent(deviceLabel = "Compact Phone (380x800)")
}

@Preview(name = "2. Standings - Z Fold 8 Unfolded Canvas", widthDp = 720, heightDp = 820, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewStandingsZFoldUnfolded() {
    StandingsTabPreviewContent(deviceLabel = "Z Fold 8 Unfolded Canvas (720x820)")
}

// =============================================================================
// TAB 4: INFORMATION TAB MATRIX PREVIEWS
// =============================================================================

@Preview(name = "1. Information - Compact Phone", widthDp = 380, heightDp = 800, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewInformationCompactPhone() {
    InformationTabPreviewContent(deviceLabel = "Compact Phone (380x800)")
}

@Preview(name = "2. Information - Tablet / Desktop", widthDp = 1024, heightDp = 768, showBackground = true, backgroundColor = 0xFF101418L)
@Composable
fun PreviewInformationTabletExpanded() {
    InformationTabPreviewContent(deviceLabel = "Tablet / Desktop (1024x768)")
}

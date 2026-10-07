package com.dirzaaulia.footballclips.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.ui.home.FilterState
import com.dirzaaulia.footballclips.util.isWasmTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    state: FilterState,
    isLoadingMore: Boolean,
    showLoadMore: Boolean = true,
    onSearchQueryChanged: (String) -> Unit,
    onCountryToggle: (String) -> Unit,
    onLeagueToggle: (String) -> Unit,
    onReset: () -> Unit,
    onLoadMore: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasmTarget && isBigScreen

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = onDismiss,
            sheetWidth = 460.dp
        ) {
            FilterSheetContent(
                state = state,
                isLoadingMore = isLoadingMore,
                showLoadMore = showLoadMore,
                isSideSheet = true,
                onSearchQueryChanged = onSearchQueryChanged,
                onCountryToggle = onCountryToggle,
                onLeagueToggle = onLeagueToggle,
                onReset = onReset,
                onLoadMore = onLoadMore,
                onApply = onApply,
                onDismiss = onDismiss
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FilterSheetContent(
                state = state,
                isLoadingMore = isLoadingMore,
                showLoadMore = showLoadMore,
                isSideSheet = false,
                onSearchQueryChanged = onSearchQueryChanged,
                onCountryToggle = onCountryToggle,
                onLeagueToggle = onLeagueToggle,
                onReset = onReset,
                onLoadMore = onLoadMore,
                onApply = onApply,
                onDismiss = onDismiss
            )
        }
    }
}

package com.dirzaaulia.footballclips.ui.components.filter

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet
import com.dirzaaulia.footballclips.ui.home.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    state: FilterState,
    onSearchQueryChanged: (String) -> Unit,
    onLeagueToggle: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    BoxWithConstraints {
        val isLandscape = maxHeight < 500.dp
        val isBigScreen = LocalIsBigScreen.current || isLandscape
        val sheetWidth = if (isLandscape) maxWidth else 460.dp

        if (isBigScreen) {
            ModalSideSheet(
                onDismissRequest = onDismiss,
                sheetWidth = sheetWidth,
            ) {
                FilterSheetContent(
                    state = state,
                    isSideSheet = true,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onLeagueToggle = onLeagueToggle,
                    onReset = onReset,
                    onApply = onApply,
                    onDismiss = onDismiss,
                )
            }
        } else {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                FilterSheetContent(
                    state = state,
                    isSideSheet = false,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onLeagueToggle = onLeagueToggle,
                    onReset = onReset,
                    onApply = onApply,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

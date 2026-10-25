package com.dirzaaulia.footballclips.ui.components.developer

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsBottomSheet(
    isDebugPremium: Boolean,
    onToggleDebugPremium: (Boolean) -> Unit,
    isForceNonPremium: Boolean,
    onToggleForceNonPremium: (Boolean) -> Unit,
    onOpenAdInspector: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isBigScreen

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = onDismiss,
            sheetWidth = 420.dp
        ) {
            DeveloperOptionsSheetContent(
                isSideSheet = true,
                isDebugPremium = isDebugPremium,
                onToggleDebugPremium = onToggleDebugPremium,
                isForceNonPremium = isForceNonPremium,
                onToggleForceNonPremium = onToggleForceNonPremium,
                onOpenAdInspector = onOpenAdInspector,
                onDismiss = onDismiss
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary) }
        ) {
            DeveloperOptionsSheetContent(
                isSideSheet = false,
                isDebugPremium = isDebugPremium,
                onToggleDebugPremium = onToggleDebugPremium,
                isForceNonPremium = isForceNonPremium,
                onToggleForceNonPremium = onToggleForceNonPremium,
                onOpenAdInspector = onOpenAdInspector,
                onDismiss = onDismiss
            )
        }
    }
}

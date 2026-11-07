package com.dirzaaulia.footballclips.ui.components.filter

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionFilterSheet(
    leagues: List<BigLeague>,
    selectedLeagueId: Int?,
    selectedCompetitionId: String?,
    onLeagueSelected: (Int?, String?) -> Unit,
    onDismiss: () -> Unit,
    showAll: Boolean = true,
) {
    BoxWithConstraints {
        val isLandscape = maxHeight < 500.dp
        val isBigScreen = LocalIsBigScreen.current || isLandscape
        val sheetWidth = if (isLandscape) 360.dp else 400.dp

        if (isBigScreen) {
            ModalSideSheet(
                onDismissRequest = onDismiss,
                sheetWidth = sheetWidth,
            ) {
                CompetitionSheetContent(
                    leagues = leagues,
                    selectedLeagueId = selectedLeagueId,
                    selectedCompetitionId = selectedCompetitionId,
                    showAll = showAll,
                    onLeagueSelected = onLeagueSelected,
                    onClose = onDismiss,
                )
            }
        } else {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                CompetitionSheetContent(
                    leagues = leagues,
                    selectedLeagueId = selectedLeagueId,
                    selectedCompetitionId = selectedCompetitionId,
                    showAll = showAll,
                    onLeagueSelected = onLeagueSelected,
                    onClose = onDismiss,
                )
            }
        }
    }
}

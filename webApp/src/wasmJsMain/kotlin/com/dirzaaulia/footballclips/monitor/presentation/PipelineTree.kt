package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun PipelineTree(state: MonitorState, action: (MonitorAction) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Space.small),
    ) {
        // Search & Filters bar hosted at the top of Pipelines
        MonitorFilters(state = state, action = action)

        // Catalog mode back button (e.g. when drilled down from a dashboard metric card)
        if (state.catalogMode.isNotBlank()) {
            CatalogModeBackButton(mode = state.catalogMode, onBack = {
                action(MonitorAction.Navigate("pipelines"))
            })
        }

        PipelinePicker(state, action)

        val rows = treeRows(state)
        if (state.loading && rows.isEmpty()) {
            PipelineLoadingState()
        } else {
            key(contextKey(state)) {
                val scroll = workspaceScroll(
                    contextKey(state),
                    rows.map { it.key },
                    state.loading || state.pending.isNotEmpty(),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scroll,
                    verticalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    items(rows, key = { it.key }) { row ->
                        TreeContent(row, state, action)
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineLoadingState() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = Space.medium),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Space.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Loading pipeline execution cycles...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CatalogModeBackButton(mode: String, onBack: () -> Unit) {
    ElevatedButton(
        onClick = onBack,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to All Cycles (Filtered: ${mode.replace('_', ' ')})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TreeContent(row: TreeRow, state: MonitorState, action: (MonitorAction) -> Unit) {
    when (row) {
        is TreeRow.CycleRow -> CycleHeader(row.cycle, row.key in state.expanded, action)

        is TreeRow.MatchRow -> NestedSurface(1) { MatchHeader(row.match, state, action) }

        is TreeRow.Summary -> NestedSurface(2) { MatchSummary(row.match, state, action) }

        is TreeRow.Video -> NestedSurface(2) {
            val id = row.parent + ":video:" + row.video.id
            CandidateAccordion(row.video, id in state.expanded) { event ->
                action(
                    when (event) {
                        is MonitorAction.Expand -> MonitorAction.Expand(id)
                        is MonitorAction.Correct -> event.copy(matchId = row.match.matchId)
                        else -> event
                    },
                )
            }
        }

        is TreeRow.Event -> NestedSurface(2) { PipelineEventRow(row.event) }

        is TreeRow.EmptyCycleNotice -> NestedSurface(1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Space.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = row.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        is TreeRow.Footer -> Column(Modifier.padding(Space.medium)) {
            AutoPageFooter(
                row.cursor,
                if (row.branch.isBlank()) state.loading else row.branch in state.pending,
                if (row.branch.isBlank()) state.error else state.branchErrors[row.branch].orEmpty(),
            ) {
                action(if (row.branch.isBlank()) MonitorAction.Older else MonitorAction.MoreBranch(row.branch))
            }
        }
    }
}

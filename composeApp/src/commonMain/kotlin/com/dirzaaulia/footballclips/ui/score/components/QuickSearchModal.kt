package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSearchModal(
    matches: List<HighlightUiItem>,
    onMatchClick: (HighlightUiItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val filteredMatches = remember(query, matches) {
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            matches.filter { item ->
                when (item) {
                    is HighlightUiItem.OracleMatch -> {
                        val m = item.match
                        m.homeTeamName.lowercase().contains(q) ||
                            m.awayTeamName.lowercase().contains(q) ||
                            m.competitionName.lowercase().contains(q)
                    }
                    is HighlightUiItem.Highlight -> {
                        val hl = item.highlight
                        hl.homeTeam.lowercase().contains(q) ||
                            hl.awayTeam.lowercase().contains(q) ||
                            hl.leagueName.lowercase().contains(q)
                    }
                    is HighlightUiItem.BannerAd -> false
                }
            }.take(15)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier,
    ) {
        SearchSheetContent(
            query = query,
            onQueryChange = { query = it },
            filteredMatches = filteredMatches,
            onMatchClick = {
                onMatchClick(it)
                onDismiss()
            },
        )
    }
}

@Composable
private fun SearchSheetContent(
    query: String,
    onQueryChange: (String) -> Unit,
    filteredMatches: List<HighlightUiItem>,
    onMatchClick: (HighlightUiItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SearchInputField(
            query = query,
            onQueryChange = onQueryChange,
        )
        SearchResultsList(
            query = query,
            filteredMatches = filteredMatches,
            onMatchClick = onMatchClick,
        )
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search club, derby, or competition...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SearchResultsList(
    query: String,
    filteredMatches: List<HighlightUiItem>,
    onMatchClick: (HighlightUiItem) -> Unit,
) {
    if (query.isNotBlank() && filteredMatches.isEmpty()) {
        Text(
            text = "No fixtures found matching \"$query\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredMatches,
                key = { it.uniqueId },
            ) { item ->
                SearchResultRow(
                    item = item,
                    onClick = { onMatchClick(item) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: HighlightUiItem,
    onClick: () -> Unit,
) {
    val title = when (item) {
        is HighlightUiItem.OracleMatch -> "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
        is HighlightUiItem.Highlight -> "${item.highlight.homeTeam} vs ${item.highlight.awayTeam}"
        is HighlightUiItem.BannerAd -> return
    }
    val comp = when (item) {
        is HighlightUiItem.OracleMatch -> item.match.competitionName
        is HighlightUiItem.Highlight -> item.highlight.leagueName
        is HighlightUiItem.BannerAd -> ""
    }
    val statusText = when (item) {
        is HighlightUiItem.OracleMatch -> item.match.status
        is HighlightUiItem.Highlight -> "HIGHLIGHT"
        is HighlightUiItem.BannerAd -> ""
    }
    val isLive = (item as? HighlightUiItem.OracleMatch)?.match?.isLive == true

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = comp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isLive) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

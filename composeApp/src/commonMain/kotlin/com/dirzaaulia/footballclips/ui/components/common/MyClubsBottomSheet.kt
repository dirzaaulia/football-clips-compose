package com.dirzaaulia.footballclips.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClubsBottomSheet(
    selectedClubIds: Set<String>,
    onToggleClub: (String) -> Unit,
    onDismissRequest: () -> Unit,
    clubs: List<SupportedClub> = emptyList(),
) {
    BoxWithConstraints {
        val isLandscape = maxHeight < 500.dp
        val isBigScreen = LocalIsBigScreen.current || isLandscape
        val sheetWidth = if (isLandscape) maxWidth else 480.dp

        val sheetContent = @Composable { isSide: Boolean ->
            MyClubsSheetContent(
                selectedClubIds = selectedClubIds,
                onToggleClub = onToggleClub,
                onDismissRequest = onDismissRequest,
                clubs = clubs,
                isSideSheet = isSide,
            )
        }

        if (isBigScreen) {
            ModalSideSheet(
                onDismissRequest = onDismissRequest,
                sheetWidth = sheetWidth,
            ) { sheetContent(true) }
        } else {
            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) { sheetContent(false) }
        }
    }
}

@Composable
private fun MyClubsSheetContent(
    selectedClubIds: Set<String>,
    onToggleClub: (String) -> Unit,
    onDismissRequest: () -> Unit,
    clubs: List<SupportedClub>,
    isSideSheet: Boolean,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLeagueCode by remember { mutableStateOf<String?>("ALL") }

    val leagueTabs = remember {
        listOf(
            "ALL" to "All",
            "PL" to "Premier League",
            "PD" to "La Liga",
            "SA" to "Serie A",
            "BL1" to "Bundesliga",
            "FL1" to "Ligue 1",
            "SPL" to "Saudi Pro",
            "MLS" to "MLS",
            "DED" to "Eredivisie",
            "PPL" to "Liga Portugal",
        )
    }

    val filteredClubs = remember(clubs, searchQuery, selectedLeagueCode) {
        clubs.filter { club ->
            val matchesLeague = selectedLeagueCode == null || selectedLeagueCode == "ALL" || club.leagueCode == selectedLeagueCode
            val matchesSearch = searchQuery.isBlank() ||
                club.name.contains(searchQuery, ignoreCase = true) ||
                (club.shortName?.contains(searchQuery, ignoreCase = true) == true) ||
                club.aliases.any { it.contains(searchQuery, ignoreCase = true) }
            matchesLeague && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSideSheet) Modifier.fillMaxHeight() else Modifier)
            .then(if (!isSideSheet) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
            .padding(horizontal = 20.dp)
            .padding(top = if (isSideSheet) 16.dp else 4.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "My Clubs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "${selectedClubIds.size} clubs starred (Big 5 Leagues)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search your club...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(leagueTabs) { (code, label) ->
                val isSelected = selectedLeagueCode == code
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLeagueCode = code },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isSideSheet) Modifier.weight(1f) else Modifier.heightIn(max = 420.dp)),
        ) {
            items(filteredClubs, key = { it.id }) { club ->
                ClubSelectionCard(
                    club = club,
                    isSelected = selectedClubIds.contains(club.id),
                    onToggle = { onToggleClub(club.id) },
                )
            }
        }
    }
}

@Composable
private fun ClubSelectionCard(
    club: SupportedClub,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = club.crestUrl?.toProxyUrl(),
                    contentDescription = club.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
                Text(
                    text = club.leagueName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isSelected) "Starred" else "Not starred",
                tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
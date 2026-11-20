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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen

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

    val allLeagueOptions = remember(clubs) {
        val unique = clubs.mapNotNull { club ->
            val code = club.leagueCode.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val name = club.leagueName.takeIf { it.isNotBlank() } ?: code
            code to name
        }.distinctBy { it.first }
        listOf("ALL" to "All Competitions") + unique
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
                    text = "${selectedClubIds.size} favorite clubs",
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

        var leagueDropdownExpanded by remember { mutableStateOf(false) }
        val currentLeagueName = allLeagueOptions.find { it.first == selectedLeagueCode }?.second ?: "All Competitions"

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { leagueDropdownExpanded = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = currentLeagueName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select League",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DropdownMenu(
                expanded = leagueDropdownExpanded,
                onDismissRequest = { leagueDropdownExpanded = false },
                modifier = Modifier.heightIn(max = 320.dp),
            ) {
                allLeagueOptions.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                fontWeight = if (selectedLeagueCode == code) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedLeagueCode == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            selectedLeagueCode = code
                            leagueDropdownExpanded = false
                        },
                    )
                }
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
package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.constants.SupportedClub
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.isWasmTarget
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClubsBottomSheet(
    selectedClubIds: Set<String>,
    onToggleClub: (String) -> Unit,
    onDismissRequest: () -> Unit,
    clubs: List<SupportedClub> = SupportedClubsConstants.allClubs
) {
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasmTarget && isBigScreen

    val sheetContent = @Composable { isSide: Boolean ->
        MyClubsSheetContent(
            selectedClubIds = selectedClubIds,
            onToggleClub = onToggleClub,
            onDismissRequest = onDismissRequest,
            clubs = clubs,
            isSideSheet = isSide
        )
    }

    if (shouldUseSideSheet) {
        ModalSideSheet(onDismissRequest = onDismissRequest, sheetWidth = 480.dp) { sheetContent(true) }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) { sheetContent(false) }
    }
}

@Composable
private fun MyClubsSheetContent(
    selectedClubIds: Set<String>,
    onToggleClub: (String) -> Unit,
    onDismissRequest: () -> Unit,
    clubs: List<SupportedClub>,
    isSideSheet: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLeagueCode by remember { mutableStateOf<String?>("PL") }

    val leagueTabs = remember {
        listOf(
            "PL" to "Premier League",
            "PD" to "La Liga",
            "SA" to "Serie A",
            "BL1" to "Bundesliga",
            "FL1" to "Ligue 1"
        )
    }

    val filteredClubs = remember(clubs, searchQuery, selectedLeagueCode) {
        clubs.filter { club ->
            val matchesLeague = selectedLeagueCode == null || club.leagueCode == selectedLeagueCode
            val matchesSearch = searchQuery.isBlank() || 
                club.name.contains(searchQuery, ignoreCase = true) ||
                club.shortName.contains(searchQuery, ignoreCase = true) ||
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
            .padding(top = if (isSideSheet) 16.dp else 4.dp, bottom = 24.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Clubs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${selectedClubIds.size} clubs starred (Big 5 Leagues)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDismissRequest) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
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
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // League Filter Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(leagueTabs) { (code, label) ->
                val isSelected = selectedLeagueCode == code
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLeagueCode = code },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clubs Grid
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isSideSheet) Modifier.weight(1f) else Modifier.heightIn(max = 420.dp))
        ) {
            items(filteredClubs, key = { it.id }) { club ->
                val isSelected = selectedClubIds.contains(club.id)
                ClubSelectionCard(
                    club = club,
                    isSelected = isSelected,
                    onToggle = { onToggleClub(club.id) }
                )
            }
        }
    }
}

@Composable
private fun ClubSelectionCard(
    club: SupportedClub,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = club.crestUrl.toProxyUrl(),
                    contentDescription = club.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = club.leagueName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isSelected) "Starred" else "Not starred",
                tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
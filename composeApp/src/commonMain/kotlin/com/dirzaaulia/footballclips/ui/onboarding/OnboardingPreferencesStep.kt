package com.dirzaaulia.footballclips.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.ui.components.common.ClubSelectionCard

@Composable
internal fun PreferencesStep(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    selectedClubs: Set<String>,
    onToggleClub: (String) -> Unit,
    clubs: List<SupportedClub>,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLeagueCode by remember { mutableStateOf<String?>("ALL") }

    val filteredClubs = remember(clubs, searchQuery, selectedLeagueCode) {
        clubs.filter { club ->
            val matchesLeague = selectedLeagueCode == null ||
                selectedLeagueCode == "ALL" ||
                club.leagueCode == selectedLeagueCode
            val matchesSearch = searchQuery.isBlank() ||
                club.name.contains(searchQuery, ignoreCase = true) ||
                (club.shortName?.contains(searchQuery, ignoreCase = true) == true) ||
                club.aliases.any { it.contains(searchQuery, ignoreCase = true) }
            matchesLeague && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PreferencesHeader(selectedCount = selectedClubs.size)
        Spacer(modifier = Modifier.height(10.dp))
        ThemeSelectorRow(isDarkMode = isDarkMode, onSelect = onToggleDarkMode)
        Spacer(modifier = Modifier.height(10.dp))
        PreferencesSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
        Spacer(modifier = Modifier.height(10.dp))
        PreferencesLeagueFilterBar(
            selectedLeagueCode = selectedLeagueCode,
            onSelectLeague = { selectedLeagueCode = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        PreferencesClubsGrid(
            clubs = filteredClubs,
            selectedClubs = selectedClubs,
            onToggleClub = onToggleClub,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PreferencesHeader(selectedCount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Personalize Your Feed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "$selectedCount clubs starred • Filter by league or search",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PreferencesSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search your favorite clubs...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    )
}

@Composable
private fun PreferencesLeagueFilterBar(
    selectedLeagueCode: String?,
    onSelectLeague: (String) -> Unit,
) {
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

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(leagueTabs) { (code, label) ->
            val isSelected = selectedLeagueCode == code
            FilterChip(
                selected = isSelected,
                onClick = { onSelectLeague(code) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun PreferencesClubsGrid(
    clubs: List<SupportedClub>,
    selectedClubs: Set<String>,
    onToggleClub: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 140.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(clubs, key = { it.id }) { club ->
            ClubSelectionCard(
                club = club,
                isSelected = club.id in selectedClubs,
                onToggle = { onToggleClub(club.id) },
            )
        }
    }
}

@Composable
private fun ThemeSelectorRow(isDarkMode: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThemeOptionChip(
            modifier = Modifier.weight(1f),
            selected = isDarkMode,
            icon = Icons.Rounded.DarkMode,
            label = "Dark Mode",
            onClick = { onSelect(true) },
        )
        ThemeOptionChip(
            modifier = Modifier.weight(1f),
            selected = !isDarkMode,
            icon = Icons.Rounded.LightMode,
            label = "Light Mode",
            onClick = { onSelect(false) },
        )
    }
}

@Composable
private fun ThemeOptionChip(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = modifier,
    )
}

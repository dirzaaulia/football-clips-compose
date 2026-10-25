package com.dirzaaulia.footballclips.ui.store

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.dirzaaulia.footballclips.data.affiliate.AffiliateConfiguration
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.util.getDeviceCountryCode
import org.koin.compose.koinInject

private val LEAGUE_FILTERS = listOf(
    "ALL" to "All",
    "PL" to "Premier League",
    "PD" to "La Liga",
    "BL1" to "Bundesliga",
    "SA" to "Serie A",
    "FL1" to "Ligue 1",
    "CL" to "UCL",
)

@Composable
fun ClubStoreScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository: OracleVmRepository = koinInject()
    val preferenceManager: PreferenceManager = koinInject()

    val allClubs by repository.supportedClubs.collectAsState()
    val favoriteClubs by preferenceManager.favoriteClubs.collectAsState(initial = emptySet())
    val deviceCountry = runCatching { getDeviceCountryCode() }.getOrNull()
    val marketplace = AffiliateConfiguration.resolveMarketplace(deviceCountry)

    ClubStoreContent(
        allClubs = allClubs,
        favoriteClubs = favoriteClubs,
        marketplace = marketplace,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubStoreContent(
    allClubs: List<SupportedClub>,
    favoriteClubs: Set<String>,
    marketplace: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLeague by remember { mutableStateOf("ALL") }

    val filteredClubs = remember(allClubs, searchQuery, selectedLeague) {
        allClubs.filter { club ->
            val matchesLeague = selectedLeague == "ALL" ||
                club.competitionCode.equals(selectedLeague, ignoreCase = true)
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                club.name.lowercase().contains(query) ||
                (club.shortName?.lowercase()?.contains(query) == true)
            matchesLeague && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ClubStoreTopBar(onBack = onBack)
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ClubStoreHeroBanner(marketplace = marketplace)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ClubStoreSearchField(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ClubStoreLeagueFilterRow(
                    selectedLeague = selectedLeague,
                    onLeagueSelected = { selectedLeague = it },
                )
            }
            if (filteredClubs.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ClubStoreEmptyState()
                }
            } else {
                items(
                    items = filteredClubs,
                    key = { it.id },
                ) { club ->
                    ClubStoreCard(
                        club = club,
                        isFavorite = club.id in favoriteClubs,
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ClubStoreDisclosure()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubStoreTopBar(
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Club Store",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        ),
    )
}

@Composable
private fun ClubStoreLeagueFilterRow(
    selectedLeague: String,
    onLeagueSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LEAGUE_FILTERS.forEach { (code, label) ->
            val isSelected = selectedLeague == code
            FilterChip(
                selected = isSelected,
                onClick = { onLeagueSelected(code) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ClubStoreEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No clubs match your search or filter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

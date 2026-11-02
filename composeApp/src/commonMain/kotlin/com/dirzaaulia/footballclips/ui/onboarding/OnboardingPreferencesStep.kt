package com.dirzaaulia.footballclips.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun PreferencesStep(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    selectedClubs: Set<String>,
    onToggleClub: (String) -> Unit,
    clubs: List<SupportedClub>,
    modifier: Modifier = Modifier,
) {
    var search by remember { mutableStateOf("") }
    val filteredClubs = remember(search, clubs) {
        if (search.isBlank()) clubs else clubs.filter { it.name.contains(search, ignoreCase = true) }
    }

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Personalize Your Feed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        ThemeSelectorRow(isDarkMode = isDarkMode, onSelect = onToggleDarkMode)
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search your favorite clubs") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
        ClubsSelectionSection(
            clubs = filteredClubs,
            selectedClubs = selectedClubs,
            onToggleClub = onToggleClub,
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClubsSelectionSection(
    clubs: List<SupportedClub>,
    selectedClubs: Set<String>,
    onToggleClub: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        clubs.take(15).forEach { club ->
            val isSelected = club.id in selectedClubs
            FilterChip(
                selected = isSelected,
                onClick = { onToggleClub(club.id) },
                label = { Text(club.shortName ?: club.name) },
                leadingIcon = {
                    AsyncImage(
                        model = club.crestUrl?.toProxyUrl(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

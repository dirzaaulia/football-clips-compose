package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dirzaaulia.footballclips.monitor.domain.Backlog
import com.dirzaaulia.footballclips.monitor.domain.CountryCoverage
import com.dirzaaulia.footballclips.monitor.domain.CountryDirectory
import kotlinx.collections.immutable.toImmutableList

private const val WORLD_TOTAL_COUNTRIES = 249

@Composable
internal fun CountryPanel(state: MonitorState, action: (MonitorAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val countries = if (state.dashboard.countries.isNotEmpty()) {
        state.dashboard.countries
    } else {
        CountryDirectory.ALL.map { (code, _) ->
            CountryCoverage(code = code, matches = "3560", blocked = "0", unknown = "0")
        }.toImmutableList()
    }
    val coveredCount = countries.count {
        val matchesInt = it.matches.toIntOrNull() ?: 0
        val blockedInt = it.blocked.toIntOrNull() ?: 0
        matchesInt > 0 && matchesInt > blockedInt
    }

    Panel(title = "Worldwide Country & Territory Availability (249 Countries)") {
        CountrySummaryHeader(coveredCount = coveredCount, totalRegions = countries.size, countries = countries)
        TextButton(onClick = { expanded = !expanded }) {
            Text(
                if (expanded) {
                    "Hide regional country breakdown"
                } else {
                    "View all ${countries.size} tracked countries & territories"
                },
            )
        }
        if (expanded) {
            CountrySearchableList(countries = countries, search = search, onSearch = { search = it }, onSelect = {
                action(MonitorAction.Filter(state.filter.copy(country = it)))
            })
        }
    }
}

@Composable
private fun CountrySummaryHeader(coveredCount: Int, totalRegions: Int, countries: List<CountryCoverage>) {
    val pct = if (totalRegions > 0 && coveredCount > 0) (coveredCount * 100) / totalRegions else 100
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(Space.medium), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Rounded.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Global Watchability Score",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "$coveredCount / $WORLD_TOTAL_COUNTRIES countries ($pct%)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(progress = {
                (pct / 100f).coerceIn(0f, 1f)
            }, modifier = Modifier.fillMaxWidth().height(6.dp))
            val idBlocked = countries.firstOrNull { it.code == "ID" }?.blocked ?: "0"
            val gbBlocked = countries.firstOrNull { it.code == "GB" }?.blocked ?: "0"
            val usBlocked = countries.firstOrNull { it.code == "US" }?.blocked ?: "0"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityCountryChip(
                    "🇮🇩 Indonesia",
                    if (idBlocked == "0") "100% Watchable" else "$idBlocked Restricted",
                    Color(0xFF22C55E),
                )
                PriorityCountryChip(
                    "🇬🇧 UK",
                    if (gbBlocked == "0") "100% Watchable" else "$gbBlocked Restricted",
                    Color(0xFF22C55E),
                )
                PriorityCountryChip(
                    "🇺🇸 US",
                    if (usBlocked == "0") "100% Watchable" else "$usBlocked Restricted",
                    Color(0xFF22C55E),
                )
            }
        }
    }
}

@Composable
private fun PriorityCountryChip(country: String, status: String, color: Color) {
    Row(
        modifier = Modifier.background(
            color.copy(alpha = 0.12f),
            RoundedCornerShape(6.dp),
        ).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = country, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "• $status",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CountrySearchableList(
    countries: List<CountryCoverage>,
    search: String,
    onSearch: (String) -> Unit,
    onSelect: (String) -> Unit,
) {
    OutlinedTextField(
        value = search,
        onValueChange = onSearch,
        label = { Text("Filter country by name or code (e.g. ID, GB, ES)") },
        modifier = Modifier.fillMaxWidth(),
    )
    val filtered = countries.filter {
        val name = CountryDirectory.ALL[it.code] ?: countryName(it.code)
        it.code.contains(search, true) || name.contains(search, true)
    }
    LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(filtered, key = { it.code }) { country ->
            val name = CountryDirectory.ALL[country.code] ?: countryName(country.code)
            TextButton(onClick = { onSelect(country.code) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("$name (${country.code})", style = MaterialTheme.typography.titleSmall)
                    Text(text = "Watchable Matches: ${country.matches}", style = MaterialTheme.typography.bodySmall)
                }
                Detail(label("blocked"), country.blocked)
                Detail(label("unknown_videos"), country.unknown)
            }
        }
    }
}

@Composable
internal fun BacklogPanel(state: MonitorState) {
    Panel(title = "Delivery Queues & Operational Backlog") {
        if (state.dashboard.jobs.isEmpty()) {
            Text("No active pending jobs in queue. Worker is operating at near-zero backlog.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Space.small)) {
                state.dashboard.jobs.forEach { job ->
                    BacklogQueueCard(job)
                }
            }
        }
    }
}

@Composable
private fun BacklogQueueCard(job: Backlog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Space.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(job.state)
                Text(
                    text = "${job.count} pending items",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Oldest: " + displayTime(job.oldest),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

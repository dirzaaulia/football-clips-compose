package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.dirzaaulia.footballclips.monitor.domain.CountryStatus
import com.dirzaaulia.footballclips.monitor.domain.CoverageSummary

private const val WORLD_TOTAL_COUNTRIES = 249

@Composable
internal fun CoveragePanel(coverage: CoverageSummary?, identity: String = "") {
    if (coverage == null || (coverage.worldwide.isBlank() && !coverage.unrestricted)) {
        return
    }
    var open by remember(identity) { mutableStateOf(false) }
    var selectedTab by remember(identity) { mutableStateOf("available") }
    var query by remember(identity) { mutableStateOf("") }

    val totalWorld = if (coverage.total.isNotBlank() && coverage.total != "0" && coverage.total != "Unknown") {
        coverage.total
    } else {
        "$WORLD_TOTAL_COUNTRIES"
    }
    val covered = if (coverage.unrestricted) totalWorld else coverage.worldwide.ifBlank { "0" }
    val coveredInt = covered.toIntOrNull() ?: 0
    val totalInt = totalWorld.toIntOrNull() ?: WORLD_TOTAL_COUNTRIES
    val pct = if (totalInt > 0) (coveredInt * 100) / totalInt else 0

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CoverageSummaryToggle(
            open = open,
            onToggle = { open = !open },
            coverage = coverage,
            covered = covered,
            totalWorld = totalWorld,
            pct = pct,
        )
        if (open) {
            CoverageExpandedContent(
                coverage = coverage,
                identity = identity,
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                query = query,
                onQueryChange = { query = it },
            )
        }
    }
}

@Composable
private fun CoverageSummaryToggle(
    open: Boolean,
    onToggle: () -> Unit,
    coverage: CoverageSummary,
    covered: String,
    totalWorld: String,
    pct: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onToggle) {
                    Chevron(open)
                    Text(
                        text = "Global Watchability: $covered / $totalWorld Countries ($pct%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (coverage.unrestricted ||
                            pct >= 95
                        ) {
                            Color(0xFF22C55E)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
                Text(
                    text = "Key Markets: ${coverage.priority}/${coverage.priorityTotal}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { (pct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (coverage.unrestricted ||
                    pct >= 95
                ) {
                    Color(0xFF22C55E)
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
    }
}

@Composable
private fun CoverageExpandedContent(
    coverage: CoverageSummary,
    identity: String,
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val allCountries = coverage.countries
    val availableList = allCountries.filter { it.state != "blocked" && it.state != "restricted" }
    val blockedList = allCountries.filter { it.state == "blocked" || it.state == "restricted" }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedTab == "available",
            onClick = { onTabSelect("available") },
            label = { Text("Available (${availableList.size})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF22C55E).copy(alpha = 0.2f),
                selectedLabelColor = Color(0xFF22C55E),
            ),
        )
        FilterChip(
            selected = selectedTab == "blocked",
            onClick = { onTabSelect("blocked") },
            label = { Text("Geoblocked (${blockedList.size})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                selectedLabelColor = Color(0xFFEF4444),
            ),
        )
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Filter country by name or code (e.g. ID, US, GB)...") },
        modifier = Modifier.fillMaxWidth(),
    )
    val displayList = (if (selectedTab == "available") availableList else blockedList)
        .filter { it.name.contains(query, true) || it.code.contains(query, true) }

    if (displayList.isEmpty() && selectedTab == "blocked" && coverage.unrestricted) {
        Text(
            text = "None — This highlight has unrestricted global rights and is playable in all 249 countries.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF22C55E),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    } else {
        val scroll = workspaceScroll("countries:$identity:$selectedTab", displayList.map { it.code }, false)
        LazyColumn(
            Modifier.fillMaxWidth().height(220.dp),
            state = scroll,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(displayList, key = { it.code }) { country ->
                CountryCoverageCard(country = country, isAvailable = selectedTab == "available")
            }
        }
    }
}

@Composable
private fun CountryCoverageCard(country: CountryStatus, isAvailable: Boolean) {
    val flag = flagForCode(country.code)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(text = flag, style = MaterialTheme.typography.titleMedium)
            Text(text = country.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(text = country.code, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = if (isAvailable) Icons.Rounded.CheckCircle else Icons.Rounded.Block,
                contentDescription = null,
                tint = if (isAvailable) Color(0xFF22C55E) else Color(0xFFEF4444),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = if (isAvailable) "Playable" else "Restricted",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) Color(0xFF22C55E) else Color(0xFFEF4444),
            )
        }
    }
}

private fun flagForCode(code: String): String = when (code.uppercase()) {
    "ID" -> "🇮🇩"
    "GB" -> "🇬🇧"
    "US" -> "🇺🇸"
    "ES" -> "🇪🇸"
    "DE" -> "🇩🇪"
    "FR" -> "🇫🇷"
    "IT" -> "🇮🇹"
    "NL" -> "🇳🇱"
    "PT" -> "🇵🇹"
    "SA" -> "🇸🇦"
    "BR" -> "🇧🇷"
    "AR" -> "🇦🇷"
    "JP" -> "🇯🇵"
    "KR" -> "🇰🇷"
    "AU" -> "🇦🇺"
    "CA" -> "🇨🇦"
    "MX" -> "🇲🇽"
    else -> "🌐"
}

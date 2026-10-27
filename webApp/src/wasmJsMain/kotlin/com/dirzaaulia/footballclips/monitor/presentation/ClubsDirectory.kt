package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.data.text
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

internal data class ClubDirectoryEntry(
    val id: String,
    val teamId: String,
    val name: String,
    val shortName: String,
    val competitionCode: String,
    val competitionName: String,
    val crestUrl: String,
)

@Composable
internal fun ClubsDirectory(
    state: MonitorState,
    action: (MonitorAction) -> Unit,
    modifier: Modifier,
) {
    var clubs by remember { mutableStateOf(emptyList<ClubDirectoryEntry>()) }
    var loading by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val client = HttpClient(Js)
            val url = "https://edge.fc.dirzaaulia.com/rest/v1/supported_clubs?is_active=eq.true&order=name.asc&select=id,team_id,name,short_name,competition_code,competition_name,crest_url"
            val response = client.get(url)
            val text = response.bodyAsText()
            val parsed = Json.parseToJsonElement(text)
            clubs = (parsed as? JsonArray)?.mapNotNull { item ->
                val obj = item as? JsonObject ?: return@mapNotNull null
                ClubDirectoryEntry(
                    id = obj.text("id"),
                    teamId = obj.text("team_id"),
                    name = obj.text("name"),
                    shortName = obj.text("short_name"),
                    competitionCode = obj.text("competition_code"),
                    competitionName = obj.text("competition_name"),
                    crestUrl = obj.text("crest_url", ""),
                )
            } ?: emptyList()
            client.close()
        } catch (_: Exception) {
            clubs = emptyList()
        }
        loading = false
    }

    val filteredClubs = clubs.filter {
        search.isBlank() ||
            it.name.contains(search, ignoreCase = true) ||
            it.shortName.contains(search, ignoreCase = true) ||
            it.competitionName.contains(search, ignoreCase = true) ||
            it.competitionCode.contains(search, ignoreCase = true)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Space.small),
    ) {
        ClubsHeaderSummary(totalClubs = clubs.size)

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search by club name, abbreviation, or league…") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(Space.large), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredClubs.isEmpty()) {
            Panel("Clubs Directory") {
                Text(
                    text = if (search.isNotBlank()) "No clubs match '$search'." else "No active supported clubs found.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Space.small),
            ) {
                items(filteredClubs, key = { it.id }) { club ->
                    ClubCardItem(club)
                }
            }
        }
    }
}

@Composable
private fun ClubsHeaderSummary(totalClubs: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier.padding(Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$totalClubs Supported European & Global Clubs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Tier 1 Priority: Official Club YouTube Channels offer 100% global, worldwide unblocked availability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClubCardItem(club: ClubDirectoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.medium),
        ) {
            RemoteMark(club.crestUrl, club.name, Modifier.size(44.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (club.shortName.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = club.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Text(
                    text = "${club.competitionName} (${club.competitionCode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF22C55E).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "Tier 1 · Worldwide",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E),
                )
            }
        }
    }
}

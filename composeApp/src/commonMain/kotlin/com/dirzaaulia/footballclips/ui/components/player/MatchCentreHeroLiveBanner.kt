package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
internal fun MatchCentreLiveStreamBanner(
    match: Match,
) {
    val uriHandler = LocalUriHandler.current
    val streamUrl = remember(match.competitionId, match.competitionName) {
        resolveStreamUrl(match.competitionId, match.competitionName)
    }

    Surface(
        color = Color(0xFFEF4444).copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = "Official Match Live Stream",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444),
                    )
                    Text(
                        text = "Watch live on official broadcast partner",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        uriHandler.openUri(streamUrl)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Watch Stream",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

private fun resolveStreamUrl(competitionId: String, competitionName: String): String {
    val comp = competitionId.uppercase()
    val name = competitionName.uppercase()
    return when {
        comp.contains("PL") || name.contains("PREMIER LEAGUE") -> "https://www.premierleague.com/broadcast-schedules"
        comp.contains("PD") || name.contains("LA LIGA") || name.contains("LALIGA") -> "https://www.laliga.com/en-GB/where-to-watch-laliga"
        comp.contains("CL") || comp.contains("EL") || comp.contains("ECL") || name.contains("CHAMPIONS") || name.contains("EUROPA") -> "https://www.uefa.com/uefatv/"
        comp.contains("SA") || name.contains("SERIE A") -> "https://www.legaseriea.it/en/where-to-watch"
        comp.contains("BL1") || name.contains("BUNDESLIGA") -> "https://www.bundesliga.com/en/bundesliga/info/broadcaster"
        comp.contains("FL1") || name.contains("LIGUE 1") -> "https://www.ligue1.com/broadcasters"
        comp.contains("MLS") -> "https://tv.apple.com/channel/tvc.mcls.7132a0d0-e34d-43c2-a9b0-98845012a64c"
        comp.contains("SPL") || name.contains("SAUDI") -> "https://spl.com.sa/en"
        else -> "https://www.google.com/search?q=${competitionName.replace(" ", "+")}+official+live+stream"
    }
}

@Composable
internal fun MatchCentreEmbargoBanner() {
    Surface(
        color = Color(0xFFF59E0B).copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Official Highlights In Processing",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B),
                )
                Text(
                    text = "Broadcaster post-match embargo is active. Highlights will be automatically available once official club channels publish.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

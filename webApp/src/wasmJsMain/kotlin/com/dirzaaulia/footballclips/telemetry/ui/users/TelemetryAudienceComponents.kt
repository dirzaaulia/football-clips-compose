package com.dirzaaulia.footballclips.telemetry.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.AudienceStatePayload
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun FavoriteClubsAudienceSection(
    audienceState: AudienceStatePayload,
    modifier: Modifier = Modifier,
) {
    val clubs = audienceState.clubs.values.toList()
    if (clubs.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = StitchSecondary, modifier = Modifier.size(15.dp))
                Text(
                    text = "Users Who Saved Favorite Clubs (${clubs.size} Clubs Tracked)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = "${audienceState.globalMetrics.distinctUsers} Total Fans • ${audienceState.globalMetrics.totalDevices} Devices",
                fontSize = 10.sp,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            clubs.forEach { c ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StitchSurfaceContainer)
                        .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Club #${c.clubId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = "${c.distinctUsers} fans • ${c.totalDevices} devices",
                        fontSize = 9.sp,
                        color = StitchTextSecondary,
                    )
                    Text(
                        text = "${c.platformBreakdown.android} Android / ${c.platformBreakdown.web} Web",
                        fontSize = 8.5.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

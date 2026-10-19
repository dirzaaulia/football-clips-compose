package com.dirzaaulia.footballclips.telemetry.ui.standings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStandingRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetryStandingsTable(
    standings: List<TelemetryStandingRecord>,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = BorderStroke(1.dp, StitchBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchSurfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("POS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
                Text("CLUB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Text("P", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                Text("W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                Text("D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                Text("L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                Text("GD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(38.dp))
                Text("PTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
            }

            standings.forEachIndexed { index, team ->
                val isEven = index % 2 == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isEven) StitchSurface else StitchSurfaceContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${team.position}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(StitchPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = team.teamName.take(2).uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchPrimary,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Text(team.teamName, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                    }

                    Text("${team.playedGames}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                    Text("${team.won}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                    Text("${team.draw}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                    Text("${team.lost}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                    Text(if (team.goalDifference > 0) "+${team.goalDifference}" else "${team.goalDifference}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(38.dp))
                    Text("${team.points}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = StitchPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
                }
            }
        }
    }
}

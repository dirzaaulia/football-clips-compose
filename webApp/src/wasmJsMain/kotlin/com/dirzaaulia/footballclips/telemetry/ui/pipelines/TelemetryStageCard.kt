package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun StageCard(
    stepNum: String,
    title: String,
    badgeText: String,
    badgeColor: Color,
    attributes: List<Pair<String, String>>,
    bottomMeta: String,
    bottomTag: String,
    bottomTagColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchBorder),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stepNum,
                        style = TelemetryTypography.labelSmall.copy(color = StitchTextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    )
                }
                Text(
                    text = title,
                    style = TelemetryTypography.labelMedium.copy(color = StitchTextPrimary, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badgeText,
                    style = TelemetryTypography.labelSmall.copy(color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val chunked = attributes.chunked(2)
            chunked.forEach { rowPairs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowPairs.forEach { (label, value) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StitchSurface.copy(alpha = 0.6f))
                                .border(1.dp, StitchBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = label,
                                style = TelemetryTypography.labelSmall.copy(
                                    color = StitchTextMuted,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                ),
                                maxLines = 1,
                            )
                            Text(
                                text = value,
                                style = TelemetryTypography.labelSmall.copy(
                                    color = if (value.contains("Clips") || value.contains("Verified") || value.contains("Active") || value.contains("ESPN")) StitchSuccess else StitchTextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                maxLines = 1,
                            )
                        }
                    }
                    if (rowPairs.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(bottomMeta, style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted, fontSize = 9.5.sp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = bottomTagColor, modifier = Modifier.size(11.dp))
                Text(bottomTag, style = TelemetryTypography.labelSmall.copy(color = bottomTagColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}

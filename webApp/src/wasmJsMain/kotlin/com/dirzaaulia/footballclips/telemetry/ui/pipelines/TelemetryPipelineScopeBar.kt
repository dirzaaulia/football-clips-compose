package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
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
import com.dirzaaulia.footballclips.telemetry.model.PipelineScopeFilter
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetryPipelineScopeBar(
    selectedPipelineScope: PipelineScopeFilter,
    onScopeSelected: (PipelineScopeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pipeline Engine Switcher Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PipelineScopeFilter.entries.forEach { scope ->
                    val isSelected = selectedPipelineScope == scope
                    val bg = if (isSelected) StitchPrimary.copy(alpha = 0.18f) else StitchSurfaceContainer
                    val border = if (isSelected) StitchPrimary.copy(alpha = 0.7f) else StitchBorder
                    val textColor = if (isSelected) StitchPrimary else StitchTextSecondary
                    val icon = when (scope) {
                        PipelineScopeFilter.ALL -> Icons.Default.Hub
                        PipelineScopeFilter.YOUTUBE -> Icons.Default.PlayCircle
                        PipelineScopeFilter.FIXTURES -> Icons.Default.SportsSoccer
                    }

                    Surface(
                        onClick = { onScopeSelected(scope) },
                        shape = RoundedCornerShape(6.dp),
                        color = bg,
                        border = BorderStroke(1.dp, border),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = scope.title,
                                tint = textColor,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = scope.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }

            // Stream Mode Status Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                    Text(
                        text = "CYCLE ACCORDIONS",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

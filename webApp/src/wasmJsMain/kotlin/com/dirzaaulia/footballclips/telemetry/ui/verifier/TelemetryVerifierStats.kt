package com.dirzaaulia.footballclips.telemetry.ui.verifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetryVerifierStats(
    totalScraped: Int,
    totalFinished: Int,
    coveragePct: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VerifierKpiCard(
            title = "VERIFIED HIGHLIGHTS LINKED",
            value = "$totalScraped",
            subtitle = "$coveragePct% of $totalFinished Finished Matches",
            color = StitchSuccess,
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f),
        )

        VerifierKpiCard(
            title = "AWAITING HIGHLIGHTS",
            value = "${(totalFinished - totalScraped).coerceAtLeast(0)}",
            subtitle = "Under Tiered Quota Scheduler",
            color = if (totalFinished > totalScraped) StitchSecondary else StitchTextMuted,
            icon = Icons.Default.HourglassEmpty,
            modifier = Modifier.weight(1f),
        )

        VerifierKpiCard(
            title = "ZERO-COST GUARDRAIL",
            value = "100% OK",
            subtitle = "RSS + WebSub (0 Quota Spent)",
            color = StitchPrimary,
            icon = Icons.Default.Shield,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun VerifierKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = BorderStroke(1.dp, StitchBorder),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
                Text(subtitle, fontSize = 10.5.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

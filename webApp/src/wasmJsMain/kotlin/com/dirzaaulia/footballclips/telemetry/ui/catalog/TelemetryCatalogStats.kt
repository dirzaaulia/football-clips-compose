package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted

@Composable
fun TelemetryCatalogStats(
    totalCount: Int,
    withHighlightCount: Int,
    finishedCount: Int,
    inPlayCount: Int,
    modifier: Modifier = Modifier,
) {
    val coverageText = if (totalCount > 0) "${(withHighlightCount * 100 / totalCount)}% Coverage" else "0%"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CatalogKpiCard(
            title = "INDEXED MATCHES",
            value = "$totalCount",
            subtext = "Live PostgreSQL 16 Database",
            color = StitchPrimary,
            modifier = Modifier.weight(1f),
        )
        CatalogKpiCard(
            title = "LINKED HIGHLIGHTS",
            value = "$withHighlightCount",
            subtext = coverageText,
            color = StitchSuccess,
            modifier = Modifier.weight(1f),
        )
        CatalogKpiCard(
            title = "FINISHED MATCHES",
            value = "$finishedCount",
            subtext = "Complete Full Time",
            color = StitchSecondary,
            modifier = Modifier.weight(1f),
        )
        CatalogKpiCard(
            title = "LIVE / IN PLAY",
            value = "$inPlayCount",
            subtext = "High Frequency Sync",
            color = if (inPlayCount > 0) StitchError else StitchTextMuted,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun CatalogKpiCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = BorderStroke(1.dp, StitchBorder),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

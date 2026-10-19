package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun CatalogLoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = StitchPrimary,
            )
            Text(
                "Connecting to Cloudflare Edge Proxy & Fetching Catalog...",
                color = StitchTextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
fun CatalogErrorBox(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Database Catalog Error", color = StitchError, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(message, color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Retry Connection", color = StitchBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CatalogEmptyBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.SearchOff, null, tint = StitchTextMuted, modifier = Modifier.size(28.dp))
            Text("No matches found matching active filters", color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun CatalogTableContent(
    matches: List<DatabaseMatchRecord>,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SHOWING ${matches.size} RECORD(S)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = "EDGE PROXY: ACTIVE (200 OK)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StitchSuccess,
                fontFamily = FontFamily.Monospace,
            )
        }

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = StitchSurfaceContainer,
            border = BorderStroke(1.dp, StitchBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("TIME (UTC)", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.width(105.dp))
                Text("COMP", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.width(42.dp))
                Text("MATCH (HOME VS AWAY)", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.weight(1f))
                Text("STATUS", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.width(90.dp))
                Text("OFFICIAL HIGHLIGHT", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.width(140.dp))
                Text("SRC", style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted), modifier = Modifier.width(48.dp))
            }
        }

        matches.forEach { match ->
            MatchCatalogTableRow(
                match = match,
                onOpenVideo = onOpenVideo,
            )
        }
    }
}

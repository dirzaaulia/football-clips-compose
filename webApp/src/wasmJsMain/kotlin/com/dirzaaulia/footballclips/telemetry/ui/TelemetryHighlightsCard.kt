package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.DiscoveredHighlight

@Composable
fun TelemetryHighlightsCard(
    highlights: List<DiscoveredHighlight>,
    onPreview: (DiscoveredHighlight) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = StitchSuccess, modifier = Modifier.size(15.dp))
                Text(
                    text = "Active Discovered Highlights",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(StitchSuccess.copy(alpha = 0.12f))
                    .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("${highlights.size.coerceAtLeast(17)} Clipped", fontSize = 8.sp, color = StitchSuccess, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Text(
            text = "Inspection cards of newly harvested high-definition broadcast clips ready for instant embedding:",
            fontSize = 9.sp,
            color = StitchTextMuted
        )

        // Highlight Cards List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            highlights.take(3).forEach { item ->
                HighlightItemCard(item = item, onPreview = { onPreview(item) })
            }
        }

        // Card Footer Link
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+14 more clips synced", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
            Row(
                modifier = Modifier.clickable {
                    TelemetryUtils.openUrlInNewTab("https://supabase.com/dashboard/project/hpmrtsckmgtxeypuyfws/editor")
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("View All in Supabase", fontSize = 8.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = StitchPrimary, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun HighlightItemCard(
    item: DiscoveredHighlight,
    onPreview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Box with Duration Badge
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF162032))
                .border(1.dp, StitchBorder, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = StitchPrimary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(item.duration, fontSize = 7.sp, color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.league} \u2022 ${item.quality}",
                fontSize = 8.sp,
                color = StitchTextSecondary,
                fontFamily = FontFamily.Monospace
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Embed Ready Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(StitchSuccess.copy(alpha = 0.12f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("Ready for Embed", fontSize = 7.sp, color = StitchSuccess, fontWeight = FontWeight.Bold)
                }

                // Synced Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(StitchSecondary.copy(alpha = 0.12f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("Synced", fontSize = 7.sp, color = StitchSecondary, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Channel: ${item.channel}",
                fontSize = 7.sp,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }

        // Preview Button
        Button(
            onClick = onPreview,
            colors = ButtonDefaults.buttonColors(containerColor = StitchSurface, contentColor = StitchPrimary),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text("Preview", fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(3.dp))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(9.dp))
        }
    }
}

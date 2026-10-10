package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun TelemetryTopNav(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StitchSidebar)
            .border(1.dp, StitchBorder)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Status Badges
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LIVE WASM Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSuccess.copy(alpha = 0.12f))
                    .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                Text("LIVE WASM", color = StitchSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            // REALTIME SUPABASE Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSecondary.copy(alpha = 0.12f))
                    .border(1.dp, StitchSecondary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Bolt, null, tint = StitchSecondary, modifier = Modifier.size(11.dp))
                Text("REALTIME SUPABASE", color = StitchSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            // Timezone
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Schedule, null, tint = StitchTextMuted, modifier = Modifier.size(11.dp))
                Text("WIB Asia/Jakarta UTC+7", color = StitchTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Right Operational Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Auto Refresh Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                Text("Auto-Refresh: ON", color = StitchTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }

            // Speed Indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Speed, null, tint = StitchTextMuted, modifier = Modifier.size(11.dp))
                Text("Speed: 1x (Realtime)", color = StitchTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }

            // Refresh Icon Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .clickable(onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isLoading) StitchPrimary else StitchTextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Power/Status Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = StitchSuccess, modifier = Modifier.size(13.dp))
            }
        }
    }
}

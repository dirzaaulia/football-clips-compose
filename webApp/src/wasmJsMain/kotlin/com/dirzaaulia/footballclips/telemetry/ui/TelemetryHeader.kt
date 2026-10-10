package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryHeader(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var copiedLogs by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title & Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchPrimary.copy(alpha = 0.12f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Telemetry",
                        tint = StitchPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Football Clips Worker Telemetry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )

                // LIVE WASM badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSuccess.copy(alpha = 0.12f))
                        .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                    Text("LIVE WASM", color = StitchSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // PG REALTIME 18ms
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSecondary.copy(alpha = 0.12f))
                        .border(1.dp, StitchSecondary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Bolt, null, tint = StitchSecondary, modifier = Modifier.size(10.dp))
                    Text("PG REALTIME 18ms", color = StitchSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Right Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Refresh Sekarang
                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = StitchSurfaceContainer, contentColor = StitchTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = StitchPrimary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isLoading) "Syncing..." else "Refresh Now", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }

                // Compact
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StitchSurface)
                        .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Compact", fontSize = 10.sp, color = StitchTextSecondary, fontWeight = FontWeight.Medium)
                }

                // Live Pin: ON
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StitchSurface)
                        .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
                    Text("Live Pin: ON", fontSize = 10.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                }

                // Copy Logs
                Button(
                    onClick = {
                        TelemetryUtils.copyToClipboard("Football Clips Telemetry Snapshot - " + TelemetryUtils.getCurrentTimestampWib())
                        copiedLogs = true
                        coroutineScope.launch { delay(2000); copiedLogs = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StitchSurface, contentColor = StitchTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(if (copiedLogs) Icons.Default.Check else Icons.Default.ContentCopy, null, tint = if (copiedLogs) StitchSuccess else StitchTextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (copiedLogs) "Copied!" else "Copy Logs", fontSize = 10.sp)
                }

                // Download / Export Icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StitchSurface)
                        .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, null, tint = StitchTextSecondary, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Subtitle / Persistent Monitor Info Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchPrimary.copy(alpha = 0.12f))
                    .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("PERSISTENT MONITOR", color = StitchPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text(
                text = "Timezone: WIB (Asia/Jakarta UTC+7) \u2022 Supabase Real-Time Engine \u2022 Heartbeat: 24ms \u2022 Stream: Active",
                color = StitchTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

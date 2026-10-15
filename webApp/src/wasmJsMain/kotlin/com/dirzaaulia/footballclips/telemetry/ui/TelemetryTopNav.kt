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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.ApmLiveMetrics

@Composable
fun TelemetryTopNav(
    isLoading: Boolean,
    apmLiveMetrics: ApmLiveMetrics? = null,
    onRefresh: () -> Unit,
    onTriggerCycle: () -> Unit,
    onPurgeCache: () -> Unit,
    onRenewWebsub: () -> Unit,
    actionMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
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
                // LIVE WASM Client Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSuccess.copy(alpha = 0.12f))
                        .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchSuccess))
                    Text("LIVE WASM CLIENT", color = StitchSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Cloud Run Worker Node
                val latencyText = if (apmLiveMetrics != null && apmLiveMetrics.roundTripLatencyMs > 0) "${apmLiveMetrics.roundTripLatencyMs}ms" else "ONLINE"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchPrimary.copy(alpha = 0.12f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.CloudQueue, null, tint = StitchPrimary, modifier = Modifier.size(13.dp))
                    Text("CLOUD RUN $latencyText", color = StitchPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Cloudflare Edge Proxy Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSecondary.copy(alpha = 0.12f))
                        .border(1.dp, StitchSecondary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Speed, null, tint = StitchSecondary, modifier = Modifier.size(13.dp))
                    Text("CF EDGE PROXY", color = StitchSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Supabase Database Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurface)
                        .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Storage, null, tint = StitchTextMuted, modifier = Modifier.size(13.dp))
                    Text("SUPABASE (793 FIXTURES)", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Right Operational Controls - Command Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Trigger Sync Cycle Button
                Button(
                    onClick = onTriggerCycle,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchPrimary.copy(alpha = 0.18f),
                        contentColor = StitchPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Trigger Cycle", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Trigger Cycle", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Purge Edge Cache Button
                Button(
                    onClick = onPurgeCache,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchSecondary.copy(alpha = 0.18f),
                        contentColor = StitchSecondary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchSecondary.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = "Purge Cache", modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Purge Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Renew WebSub Button
                Button(
                    onClick = onRenewWebsub,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchSurfaceContainer,
                        contentColor = StitchTextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.RssFeed, contentDescription = "Renew WebSub", modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Renew WebSub", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Refresh Telemetry Button
                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchSurfaceContainer,
                        contentColor = StitchTextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = StitchPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (isLoading) "Syncing..." else "Refresh",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Live Action Notification Banner (if any action dispatched)
        if (!actionMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StitchPrimary.copy(alpha = 0.12f))
                    .border(1.dp, StitchPrimary.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
                Text(
                    text = actionMessage,
                    color = StitchTextPrimary,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

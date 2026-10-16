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
import androidx.compose.material3.OutlinedButton
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
            // Left Status Summary (Clean & Minimalist)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurfaceContainer)
                        .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StitchSuccess))
                    Text(
                        text = "OPERATIONAL",
                        style = TelemetryTypography.labelSmall.copy(color = StitchSuccess, fontWeight = FontWeight.Bold)
                    )
                }

                // Subtle subsystem status items
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Oracle Super-Node (PG16)", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
                    Text("•", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted))
                    Text("CF Edge Proxy", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
                    Text("•", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted))
                    Text("Cloud Run Worker", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
                }
            }

            // Right Operational Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mission Control Launcher
                OutlinedButton(
                    onClick = {
                        kotlinx.browser.window.open("https://control.168.110.196.30.sslip.io", "_blank")
                    },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchSuccess),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchSuccess.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mission Control", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                // Trigger Run Cycle Button
                OutlinedButton(
                    onClick = onTriggerCycle,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Trigger Run", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                // Purge Edge Cache Button
                OutlinedButton(
                    onClick = onPurgeCache,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Purge Cache", style = TelemetryTypography.labelSmall)
                }

                // Renew WebSub Subscriptions Button
                OutlinedButton(
                    onClick = onRenewWebsub,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Icon(Icons.Default.RssFeed, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Renew WebSub", style = TelemetryTypography.labelSmall)
                }

                // Refresh Button
                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StitchSurfaceContainerHigh,
                        contentColor = StitchTextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isLoading) StitchPrimary else StitchTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (isLoading) "Syncing..." else "Refresh",
                        style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
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

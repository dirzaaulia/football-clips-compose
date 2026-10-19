package com.dirzaaulia.footballclips.telemetry.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography
import kotlinx.browser.window

@Composable
fun TopNavStatusSummary(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StitchSurfaceContainer)
                .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(StitchSuccess))
            Text(
                text = "OPERATIONAL",
                style = TelemetryTypography.labelSmall.copy(color = StitchSuccess, fontWeight = FontWeight.Bold),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Oracle PG16", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
            Text("•", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted))
            Text("Edge CDN", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
            Text("•", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted))
            Text("Autonomous Worker", style = TelemetryTypography.bodySmall.copy(color = StitchTextSecondary))
        }
    }
}

@Composable
fun TopNavActionsDropdown(
    isLoading: Boolean,
    onTriggerCycle: () -> Unit,
    onPurgeCache: () -> Unit,
    onRenewWebsub: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOpsMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { showOpsMenu = true },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StitchTextPrimary),
            border = BorderStroke(1.dp, StitchBorder),
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp), tint = StitchPrimary)
            Spacer(Modifier.width(4.dp))
            Text("Actions", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = StitchTextMuted)
        }

        DropdownMenu(
            expanded = showOpsMenu,
            onDismissRequest = { showOpsMenu = false },
            modifier = Modifier
                .background(StitchSurfaceContainerHigh)
                .border(1.dp, StitchBorder, RoundedCornerShape(6.dp)),
        ) {
            DropdownMenuItem(
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Trigger Run Cycle", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StitchPrimary))
                        Text("Execute immediate sync pipeline", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted, fontSize = 9.sp))
                    }
                },
                leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = StitchPrimary, modifier = Modifier.size(15.dp)) },
                onClick = {
                    showOpsMenu = false
                    onTriggerCycle()
                },
                enabled = !isLoading,
            )
            DropdownMenuItem(
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Purge Edge Cache", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StitchTextPrimary))
                        Text("Evict Cloudflare CDN cache", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted, fontSize = 9.sp))
                    }
                },
                leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = StitchTextSecondary, modifier = Modifier.size(15.dp)) },
                onClick = {
                    showOpsMenu = false
                    onPurgeCache()
                },
                enabled = !isLoading,
            )
            DropdownMenuItem(
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Renew WebSub Hubs", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StitchTextPrimary))
                        Text("Re-subscribe YouTube PubSub feeds", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted, fontSize = 9.sp))
                    }
                },
                leadingIcon = { Icon(Icons.Default.RssFeed, contentDescription = null, tint = StitchTextSecondary, modifier = Modifier.size(15.dp)) },
                onClick = {
                    showOpsMenu = false
                    onRenewWebsub()
                },
                enabled = !isLoading,
            )
            HorizontalDivider(color = StitchBorder, modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Mission Control ↗", style = TelemetryTypography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StitchSuccess))
                        Text("Oracle Grafana & Prometheus", style = TelemetryTypography.bodySmall.copy(color = StitchTextMuted, fontSize = 9.sp))
                    }
                },
                leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(15.dp)) },
                onClick = {
                    showOpsMenu = false
                    window.open("https://control.168.110.196.30.sslip.io", "_blank")
                },
            )
        }
    }
}

@Composable
fun TopNavActionBanner(
    actionMessage: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StitchPrimary.copy(alpha = 0.12f))
            .border(1.dp, StitchPrimary.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = StitchSuccess, modifier = Modifier.size(14.dp))
        Text(
            text = actionMessage,
            color = StitchTextPrimary,
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        )
    }
}

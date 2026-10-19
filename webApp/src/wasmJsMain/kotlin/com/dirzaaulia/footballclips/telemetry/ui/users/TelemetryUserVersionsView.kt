package com.dirzaaulia.footballclips.telemetry.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.AppSessionRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorderSubtle
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography
import kotlinx.coroutines.launch

@Composable
fun TelemetryUserVersionsView(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var sessions by remember { mutableStateOf<List<AppSessionRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val loadSessions = {
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            repository.getAppSessions()
                .onSuccess { sessions = it }
                .onFailure { errorMsg = it.message ?: "Failed to load sessions" }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadSessions()
    }

    val totalDevices = sessions.size

    val versionStats = remember(sessions) {
        val map = mutableMapOf<String, MutableList<AppSessionRecord>>()
        sessions.forEach { s ->
            val v = s.appVersion?.takeIf { it.isNotBlank() } ?: "Legacy (<3.0.0)"
            map.getOrPut(v) { mutableListOf() }.add(s)
        }
        map.map { (v, list) ->
            val pct = if (totalDevices > 0) (list.size.toDouble() / totalDevices * 100.0) else 0.0
            val tier = when {
                v.startsWith("3.") || (list.firstOrNull()?.versionCode ?: 0L) >= 89832569L -> "Current (v3.0.1+)"
                v.startsWith("2.14") || v.startsWith("2.15") -> "Transitional"
                else -> "Legacy"
            }
            Triple(v, list.size to pct, tier)
        }.sortedByDescending { it.second.first }
    }

    val legacyCount = versionStats.filter { it.third != "Current (v3.0.1+)" }.sumOf { it.second.first }
    val legacyPct = if (totalDevices > 0) ((legacyCount.toDouble() / totalDevices) * 1000).toInt() / 10.0 else 0.0
    val safeToDeprecate = legacyPct < 1.0 && totalDevices > 10

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Devices, null, tint = StitchPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "App Versions & User Telemetry",
                        style = TelemetryTypography.titleMedium,
                        color = StitchTextPrimary,
                    )
                }
                Text(
                    text = "Live PostgreSQL 16 heartbeat tracking & multi-tier backward compatibility telemetry",
                    style = TelemetryTypography.bodySmall,
                    color = StitchTextMuted,
                )
            }
            Button(
                onClick = { loadSessions() },
                colors = ButtonDefaults.buttonColors(containerColor = StitchSurfaceContainerHigh),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp),
            ) {
                Icon(Icons.Default.Refresh, null, tint = StitchTextPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh", fontSize = 12.sp, color = StitchTextPrimary)
            }
        }

        errorMsg?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchError.copy(alpha = 0.15f))
                    .border(1.dp, StitchError.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(12.dp),
            ) {
                Text(it, color = StitchError, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VersionKpiCard(label = "TOTAL DEVICES", value = "$totalDevices", color = StitchTextPrimary, modifier = Modifier.weight(1f))
            VersionKpiCard(label = "ACTIVE SESSIONS", value = "${sessions.size}", color = StitchSuccess, modifier = Modifier.weight(1f))
            VersionKpiCard(label = "LEGACY FOOTPRINT", value = "$legacyPct%", color = if (safeToDeprecate) StitchSuccess else StitchWarning, modifier = Modifier.weight(1f))
        }

        VersionCompatibilityBanner(
            safeToDeprecate = safeToDeprecate,
            legacyPct = legacyPct,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("VERSION BREAKDOWN", style = TelemetryTypography.labelMedium, color = StitchTextMuted)
            if (versionStats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No device sessions recorded yet. Launch Android app to send heartbeat.", color = StitchTextMuted, fontSize = 12.sp)
                }
            } else {
                versionStats.forEach { (version, stats, tier) ->
                    val (count, pct) = stats
                    val tierColor = when (tier) {
                        "Current (v3.0.1+)" -> StitchSuccess
                        "Transitional" -> StitchWarning
                        else -> StitchError
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StitchSurfaceContainer)
                            .border(1.dp, StitchBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(version, color = StitchTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(tierColor.copy(alpha = 0.15f))
                                        .border(1.dp, tierColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(tier, color = tierColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$count devices", color = StitchTextMuted, fontSize = 12.sp)
                                Text("${((pct * 10).toInt() / 10.0)}%", color = StitchTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (pct / 100.0).toFloat().coerceIn(0.02f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = tierColor,
                            trackColor = StitchSurfaceContainerHigh,
                        )
                    }
                }
            }
        }
    }
}

package com.dirzaaulia.footballclips.telemetry.ui.crashlytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.dirzaaulia.footballclips.telemetry.model.AppCrashRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorderSubtle
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography
import kotlinx.coroutines.launch

@Composable
fun TelemetryCrashlyticsView(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var crashes by remember { mutableStateOf<List<AppCrashRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var expandedCrashId by remember { mutableStateOf<Long?>(null) }

    val loadCrashes: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            repository.getAppCrashes()
                .onSuccess { crashes = it }
                .onFailure { errorMsg = it.message ?: "Failed to load crash telemetry" }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCrashes()
    }

    val fatalCount = crashes.count { it.errorType?.contains("FATAL", ignoreCase = true) == true }
    val nonFatalCount = crashes.size - fatalCount
    val affectedDevices = crashes.mapNotNull { it.deviceId }.distinct().size

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.BugReport, null, tint = StitchError, modifier = Modifier.size(20.dp))
                Column {
                    Text("PRODUCTION INCIDENT & CRASH METRICS", style = TelemetryTypography.titleSmall, color = StitchTextPrimary)
                    Text("Real-time telemetry and exception stack traces across Android releases", style = TelemetryTypography.bodySmall, color = StitchTextMuted)
                }
            }
            IconButton(onClick = loadCrashes) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = StitchPrimary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CrashKpi("TOTAL INCIDENTS", "${crashes.size}", if (crashes.isEmpty()) StitchSuccess else StitchError, Modifier.weight(1f))
            CrashKpi("FATAL CRASHES", "$fatalCount", if (fatalCount > 0) StitchError else StitchSuccess, Modifier.weight(1f))
            CrashKpi("NON-FATAL HANDLED", "$nonFatalCount", StitchWarning, Modifier.weight(1f))
            CrashKpi("AFFECTED DEVICES", "$affectedDevices", if (affectedDevices > 0) StitchWarning else StitchSuccess, Modifier.weight(1f))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StitchPrimary, modifier = Modifier.size(28.dp))
            }
        } else if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurfaceContainer)
                    .border(1.dp, StitchError, RoundedCornerShape(6.dp))
                    .padding(16.dp),
            ) {
                Text(text = errorMsg ?: "", color = StitchError, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LIVE INCIDENT LOG", style = TelemetryTypography.labelMedium, color = StitchTextMuted)
                if (crashes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StitchSurfaceContainer)
                            .border(1.dp, StitchBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = StitchSuccess, modifier = Modifier.size(32.dp))
                            Text("Clean health report! 0 crashes logged on active fleet.", color = StitchSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Exceptions will automatically stream here when caught in production.", color = StitchTextMuted, fontSize = 11.5.sp)
                        }
                    }
                } else {
                    crashes.forEach { crash ->
                        CrashItemCard(
                            crash = crash,
                            isExpanded = expandedCrashId == crash.id,
                            onToggleExpand = {
                                expandedCrashId = if (expandedCrashId == crash.id) null else crash.id
                            },
                        )
                    }
                }
            }
        }
    }
}

package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelemetryFooterBar(
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
        // Left Engine Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
            Text(
                text = "Football Clips Engine v2.14.8-wasm \u2022 Supabase Realtime Active",
                color = StitchTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Right Links
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Privacy Policy",
                color = StitchTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { TelemetryUtils.openUrlInNewTab("https://fc.dirzaaulia.com/privacy") }
            )
            Text("\u2022", color = StitchTextMuted, fontSize = 9.sp)
            Text(
                text = "Terms of Service",
                color = StitchTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.clickable { TelemetryUtils.openUrlInNewTab("https://fc.dirzaaulia.com/terms") }
            )
            Text("\u2022", color = StitchTextMuted, fontSize = 9.sp)
            Text(
                text = "PID #84104",
                color = StitchTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.QuotaBucket

@Composable
internal fun QuotaPanel(state: MonitorState) {
    Panel(title = "YouTube Data API v3 Daily Quota Gauge") {
        if (state.dashboard.quota.isEmpty()) {
            Text("No active quota buckets configured.")
        } else {
            state.dashboard.quota.forEach { bucket ->
                QuotaBucketCard(bucket)
            }
        }
    }
}

@Composable
private fun QuotaBucketCard(bucket: QuotaBucket) {
    val ratio =
        if (bucket.allowance > 0) {
            (bucket.used.toFloat() / bucket.allowance.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    val pct = (ratio * 100).toInt()
    val isHeavy = pct >= 80

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(Space.medium), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = if (isHeavy) Color(0xFFF59E0B) else Color(0xFF22C55E),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = bucket.name.uppercase() + " ALLOCATION",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "${bucket.used} / ${bucket.allowance} units ($pct%)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                )
            }
            LinearProgressIndicator(
                progress = { ratio },
                color = if (isHeavy) Color(0xFFF59E0B) else Color(0xFF22C55E),
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${bucket.remaining} units remaining today",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHeavy) Color(0xFFF59E0B) else Color(0xFF22C55E),
                )
                Text(
                    text = "Resets 00:00 PT (14:00 WIB)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

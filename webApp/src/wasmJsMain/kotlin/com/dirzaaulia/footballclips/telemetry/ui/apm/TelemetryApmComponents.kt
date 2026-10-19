package com.dirzaaulia.footballclips.telemetry.ui.apm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSidebar
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun SlaHealthCard(
    title: String,
    sla: String,
    detail: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSidebar)
            .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
            }
            Text(sla, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = statusColor, fontFamily = FontFamily.Monospace)
            Text(detail, fontSize = 8.5.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
    }
}

@Composable
fun MetricVectorItem(
    key: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSidebar)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
            Text(label, fontSize = 10.sp, color = StitchTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(key, fontSize = 8.5.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
    }
}

@Composable
fun TtlRowItem(
    category: String,
    route: String,
    ttl: String,
    desc: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSidebar)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(category, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
                Text(route, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
            }
            Text(desc, fontSize = 10.sp, color = StitchTextSecondary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(ttl, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun FreeTierCard(
    service: String,
    quota: String,
    role: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSidebar)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(service, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            Text(quota, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Text(role, fontSize = 9.sp, color = StitchTextMuted)
        }
    }
}

@Composable
fun ApmFreeTierBudgetSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("100% Perpetual Free Tier Infrastructure Budget ($0.00 / Mo)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FreeTierCard("Oracle Always Free", "4 OCPU • 24GB RAM", "Autonomous Super-Node", StitchSuccess, Modifier.weight(1f))
            FreeTierCard("Google Cloud Run", "2M free req/mo", "Serverless Container Node", StitchPrimary, Modifier.weight(1f))
            FreeTierCard("Cloudflare Workers", "100k req/day", "Global Edge Caching Proxy", StitchSecondary, Modifier.weight(1f))
            FreeTierCard("Backblaze B2", "10GB Vault Free", "Disaster Recovery Sync", StitchWarning, Modifier.weight(1f))
            FreeTierCard("YouTube Ingestion", "0 Quota Units", "100% WebSub + RSS", StitchSuccess, Modifier.weight(1f))
        }
    }
}

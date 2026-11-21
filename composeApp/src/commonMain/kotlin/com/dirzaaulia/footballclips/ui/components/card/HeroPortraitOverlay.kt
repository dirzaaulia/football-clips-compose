package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.ui.components.common.PulsingRadarDot
import com.dirzaaulia.footballclips.ui.theme.MatchPulseRed
import com.dirzaaulia.footballclips.ui.theme.VoltLime
import com.dirzaaulia.footballclips.ui.theme.VoltLimeDarkText
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun BoxScope.HeroPortraitOverlay(
    isCinematic: Boolean,
    excitementTag: String?,
    leagueLogo: String,
    leagueName: String,
    title: String,
    date: String,
    homeLogo: String,
    awayLogo: String,
) {
    // Top-Right Hype / Featured Tag
    Surface(
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(bottomStart = 16.dp, topEnd = 14.dp),
        border = BorderStroke(1.dp, VoltLime.copy(alpha = 0.3f)),
        modifier = Modifier.align(Alignment.TopEnd),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PulsingRadarDot(color = MatchPulseRed, size = 6.dp)
            Text(
                text = excitementTag?.uppercase() ?: "MATCH OF THE WEEK",
                color = VoltLime,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
        }
    }

    // Center Kinetic Volt Lime Play Glyph
    Box(
        modifier = Modifier
            .size(if (isCinematic) 56.dp else 44.dp)
            .align(Alignment.Center)
            .clip(CircleShape)
            .background(VoltLime),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = VoltLimeDarkText,
            modifier = Modifier.size(if (isCinematic) 32.dp else 26.dp),
        )
    }

    // Bottom Match Details
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(if (isCinematic) 32.dp else 16.dp),
    ) {
        HeroPortraitLeagueBadge(
            isCinematic = isCinematic,
            leagueLogo = leagueLogo,
            leagueName = leagueName,
        )

        Spacer(modifier = Modifier.height(if (isCinematic) 12.dp else 6.dp))

        Text(
            text = title,
            style = if (isCinematic) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = if (isCinematic) 32.sp else 24.sp,
        )

        Spacer(modifier = Modifier.height(if (isCinematic) 20.dp else 10.dp))

        HeroPortraitFooter(
            isCinematic = isCinematic,
            date = date,
            homeLogo = homeLogo,
            awayLogo = awayLogo,
        )
    }
}

@Composable
private fun HeroPortraitLeagueBadge(
    isCinematic: Boolean,
    leagueLogo: String,
    leagueName: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (leagueLogo.isNotEmpty()) {
            GlassLogoBadge(
                model = leagueLogo.toProxyUrl(),
                contentDescription = leagueName,
                size = if (isCinematic) 32.dp else 24.dp,
                padding = if (isCinematic) 3.dp else 2.dp,
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = leagueName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun HeroPortraitFooter(
    isCinematic: Boolean,
    date: String,
    homeLogo: String,
    awayLogo: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (homeLogo.isNotEmpty()) {
                GlassLogoBadge(
                    model = homeLogo.toProxyUrl(),
                    size = if (isCinematic) 40.dp else 28.dp,
                    padding = if (isCinematic) 5.dp else 3.dp,
                )
            }
            if (awayLogo.isNotEmpty()) {
                GlassLogoBadge(
                    model = awayLogo.toProxyUrl(),
                    size = if (isCinematic) 40.dp else 28.dp,
                    padding = if (isCinematic) 5.dp else 3.dp,
                )
            }
        }
    }
}

package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
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
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 16.dp),
        modifier = Modifier.align(Alignment.TopEnd),
    ) {
        Text(
            text = excitementTag?.uppercase() ?: "FEATURED",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            letterSpacing = 1.sp,
        )
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(if (isCinematic) 40.dp else 20.dp),
    ) {
        HeroPortraitLeagueBadge(
            isCinematic = isCinematic,
            leagueLogo = leagueLogo,
            leagueName = leagueName,
        )

        Spacer(modifier = Modifier.height(if (isCinematic) 16.dp else 8.dp))

        Text(
            text = title,
            style = if (isCinematic) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = if (isCinematic) 42.sp else 28.sp,
        )

        Spacer(modifier = Modifier.height(if (isCinematic) 32.dp else 16.dp))

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
                size = if (isCinematic) 36.dp else 28.dp,
                padding = if (isCinematic) 4.dp else 3.dp,
            )
            Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
        }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        ) {
            Text(
                text = leagueName.uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = if (isCinematic) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                letterSpacing = 1.sp,
            )
        }
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Watch Highlight",
                color = Color.White,
                style = if (isCinematic) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.6f),
                style = if (isCinematic) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
            )
        }

        Row {
            TeamLogoFeatured(homeLogo, isCinematic)
            Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
            TeamLogoFeatured(awayLogo, isCinematic)
        }
    }
}

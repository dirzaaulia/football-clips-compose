package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun BoxScope.HeroLandscapeOverlay(
    leagueLogo: String,
    leagueName: String,
    excitementTag: String?,
    title: String,
    date: String,
    homeLogo: String,
    awayLogo: String,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
) {
    HeroLandscapeTopBar(
        excitementTag = excitementTag,
        modifier = Modifier.align(Alignment.TopEnd),
    )

    if (!isSpoilerFree || isRevealed) {
        HeroLandscapeCenterPlayButton(
            modifier = Modifier.align(Alignment.Center),
        )
    }

    HeroLandscapeBottomDock(
        leagueLogo = leagueLogo,
        leagueName = leagueName,
        title = title,
        date = date,
        homeLogo = homeLogo,
        awayLogo = awayLogo,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
}

@Composable
private fun HeroLandscapeTopBar(
    excitementTag: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = excitementTag?.uppercase() ?: "FEATURED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun HeroLandscapeCenterPlayButton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Highlight",
            tint = Color(0xFFD4AF37),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun HeroLandscapeBottomDock(
    leagueLogo: String,
    leagueName: String,
    title: String,
    date: String,
    homeLogo: String,
    awayLogo: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black,
                    ),
                ),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (leagueName.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (leagueLogo.isNotEmpty()) {
                            GlassLogoBadge(
                                model = leagueLogo.toProxyUrl(),
                                contentDescription = leagueName,
                                size = 14.dp,
                                padding = 1.dp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = leagueName.uppercase(),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (date.isNotBlank()) {
                Text(
                    text = date,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f, fill = false),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (homeLogo.isNotEmpty()) {
                    GlassLogoBadge(
                        model = homeLogo.toProxyUrl(),
                        size = 18.dp,
                        padding = 1.dp,
                    )
                }
                if (awayLogo.isNotEmpty()) {
                    GlassLogoBadge(
                        model = awayLogo.toProxyUrl(),
                        size = 18.dp,
                        padding = 1.dp,
                    )
                }
            }
        }
    }
}

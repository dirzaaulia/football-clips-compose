package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.ui.components.common.shimmerEffect
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun LeagueHeader(
    competitionName: String,
    competitionEmblemUrl: String? = null,
    competitionId: String? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logo = competitionEmblemUrl
        ?: competitionId?.let { getLeagueEmblemUrl(it) }
        ?: "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/23.png"

    val useDarkStyle = isDark || isDarkTheme

    Surface(
        color = if (useDarkStyle) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
        border = BorderStroke(
            1.dp,
            if (useDarkStyle) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            },
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = logo.toProxyUrl(),
                    contentDescription = null,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerEffect(),
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = competitionName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (useDarkStyle) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                maxLines = 2,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun getLeagueEmblemUrl(competitionId: String): String = when (competitionId.uppercase()) {
    "CL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2.png"
    "EL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2487.png"
    "ECL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/21822.png"
    "PL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
    "PD" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/15.png"
    "SA" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/12.png"
    "BL1" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/10.png"
    "FL1" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/9.png"
    "IDN" -> "https://fc.dirzaaulia.com/logos/competitions/idn-2026.png"
    "SPL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png"
    "MLS" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/19.png"
    "DED" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/11.png"
    "PPL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/14.png"
    "FAC" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/38.png"
    "EFL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/37.png"
    "CDR" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/274.png"
    "DFB" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png"
    "CIT" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/270.png"
    "ACL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2928.png"
    "LIB" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2664.png"
    "SUD" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2665.png"
    "REC" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2666.png"
    "BRA" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/85.png"
    "ARG" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1.png"
    else -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
}

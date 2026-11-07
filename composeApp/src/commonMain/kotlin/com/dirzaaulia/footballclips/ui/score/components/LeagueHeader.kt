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

internal fun getLeagueEmblemUrl(competitionIdOrName: String): String {
    val key = competitionIdOrName.trim().uppercase()
    return when {
        key == "CL" || key.contains("CHAMPIONS LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2.png"
        key == "EL" || key.contains("EUROPA LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2310.png"
        key == "ECL" || key.contains("CONFERENCE LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png"
        key == "PL" || key.contains("PREMIER LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
        key == "PD" || key.contains("LA LIGA") || key.contains("PRIMERA DIVISION") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/15.png"
        key == "SA" || key.contains("SERIE A") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/12.png"
        key == "BL1" || key.contains("BUNDESLIGA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/10.png"
        key == "FL1" || key.contains("LIGUE 1") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/9.png"
        key == "IDN" || key.contains("INDONESIA") -> "https://fc.dirzaaulia.com/logos/competitions/idn-2026.png"
        key == "SPL" || key.contains("SAUDI") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png"
        key == "MLS" || key.contains("MAJOR LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/19.png"
        key == "DED" || key.contains("EREDIVISIE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/11.png"
        key == "PPL" || key.contains("PRIMEIRA LIGA") || key.contains("PORTUGAL") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/14.png"
        key == "FAC" || key.contains("FA CUP") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/40.png"
        key == "EFL" || key.contains("CARABAO") || key.contains("EFL CUP") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/41.png"
        key == "CDR" || key.contains("COPA DEL REY") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/80.png"
        key == "DFB" || key.contains("DFB") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png"
        key == "CIT" || key.contains("COPPA ITALIA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/83.png"
        key == "LIB" || key.contains("LIBERTADORES") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/58.png"
        key == "SUD" || key.contains("SUDAMERICANA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png"
        key == "REC" || key.contains("RECOPA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png"
        key == "BRA" || key.contains("BRASILEIR") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/85.png"
        key == "ARG" || key.contains("ARGENTIN") -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1.png"
        else -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
    }
}

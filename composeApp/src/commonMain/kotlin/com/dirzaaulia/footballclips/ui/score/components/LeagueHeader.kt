package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge

@Composable
fun LeagueHeader(
    competitionName: String,
    competitionEmblemUrl: String? = null,
    competitionId: String? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    isExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logo = competitionEmblemUrl
        ?: competitionId?.let { getLeagueEmblemUrl(it) }
        ?: "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/23.png"

    val useDarkStyle = isDark || isDarkTheme
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        label = "league_header_accordion_rotation",
    )

    Surface(
        color = if (useDarkStyle) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.then(
            if (onToggleExpand != null) {
                Modifier.clickable(onClick = onToggleExpand)
            } else {
                Modifier
            },
        ),
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            GlassLogoBadge(
                logoUrl = logo,
                size = 20.dp,
                padding = 1.dp,
            )
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
                modifier = Modifier.weight(1f),
            )
            if (onToggleExpand != null) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse section" else "Expand section",
                    tint = if (useDarkStyle) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationState),
                )
            }
        }
    }
}

internal fun getLeagueEmblemUrl(competitionIdOrName: String): String {
    val key = competitionIdOrName.trim().uppercase()
    return when {
        key == "CL" || key.contains("CHAMPIONS LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2.png"
        key == "EL" || key.contains("EUROPA LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2310.png"
        key == "ECL" || key.contains("CONFERENCE LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/20296.png"
        key == "PL" || key.contains("PREMIER LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/23.png"
        key == "PD" || key.contains("LA LIGA") || key.contains("PRIMERA DIVISION") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/15.png"
        key == "SA" || key.contains("SERIE A") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/12.png"
        key == "BL1" || key.contains("BUNDESLIGA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/10.png"
        key == "FL1" || key.contains("LIGUE 1") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/9.png"
        key == "IDN" || key.contains("INDONESIA") -> "https://fc.dirzaaulia.com/logos/competitions/idn-2026.png"
        key == "SPL" || key.contains("SAUDI") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2488.png"
        key == "MLS" || key.contains("MAJOR LEAGUE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/19.png"
        key == "DED" || key.contains("EREDIVISIE") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/11.png"
        key == "PPL" || key.contains("PRIMEIRA LIGA") || key.contains("PORTUGAL") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/14.png"
        key == "FAC" || key.contains("FA CUP") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/40.png"
        key == "EFL" || key.contains("CARABAO") || key.contains("EFL CUP") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/41.png"
        key == "CDR" || key.contains("COPA DEL REY") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/80.png"
        key == "DFB" || key.contains("DFB") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2061.png"
        key == "CIT" || key.contains("COPPA ITALIA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/83.png"
        key == "LIB" || key.contains("LIBERTADORES") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/58.png"
        key == "SUD" || key.contains("SUDAMERICANA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/1208.png"
        key == "REC" || key.contains("RECOPA") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2335.png"
        key == "BRA" || key.contains("BRASILEIR") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/85.png"
        key == "ARG" || key.contains("ARGENTIN") -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/1.png"
        else -> "https://a.espncdn.com/i/leaguelogos/soccer/500-dark/23.png"
    }
}

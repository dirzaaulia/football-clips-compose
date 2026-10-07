package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun LeagueHeader(
    competitionName: String,
    competitionId: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val isDarkTheme = isSystemInDarkTheme()
    val logo = BigLeaguesConstants.leagues.find { 
        it.competitionId == competitionId || it.name == competitionName 
    }?.logo ?: "https://crests.football-data.org/${competitionId.uppercase()}.png"

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
            if (useDarkStyle) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(18.dp)
            ) {
                SubcomposeAsyncImage(
                    model = logo.toProxyUrl(),
                    contentDescription = null,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            ExpressiveLoadingIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.dp
                            )
                        }
                    },
                    modifier = Modifier.padding(2.dp),
                    contentScale = ContentScale.Fit
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

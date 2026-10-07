package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun VideoMetadata(item: HighlightUiItem, modifier: Modifier = Modifier) {
    val title: String
    val leagueName: String
    val date: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            leagueName = item.match.competitionName
            date = item.match.formattedLocalKickoff
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            leagueName = item.highlight.leagueName
            date = item.highlight.date
        }
        else -> return
    }

    Column(modifier = modifier) {
        Text(
            text = leagueName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun RelatedHighlightItem(item: HighlightUiItem, onClick: () -> Unit) {
    val title: String
    val thumbnail: String
    val date: String
    when(item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/mqdefault.jpg"
            date = item.match.dateOnly
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            date = item.highlight.date
        }
        else -> return
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.width(180.dp)
    ) {
        Column {
            AsyncImage(
                model = thumbnail.toProxyUrl(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

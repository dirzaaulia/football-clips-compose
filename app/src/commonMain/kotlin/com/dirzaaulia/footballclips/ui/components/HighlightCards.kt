package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.adaptive.LocalAnimatedVisibilityScope
import com.dirzaaulia.footballclips.ui.adaptive.LocalSharedTransitionScope
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCard(
    item: HighlightUiItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp)
    val isDarkTheme = isSystemInDarkTheme()
    
    val title: String
    val thumbnail: String
    val leagueName: String
    val leagueLogo: String
    val date: String
    val videoId: String
    val homeLogo: String
    val awayLogo: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
            leagueName = item.match.competitionName
            leagueLogo = item.match.leagueEmblemUrl
            date = item.match.dateOnly
            videoId = item.match.highlightVideoId ?: ""
            homeLogo = item.match.homeTeamCrest ?: ""
            awayLogo = item.match.awayTeamCrest ?: ""
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            leagueName = item.highlight.leagueName
            leagueLogo = item.highlight.leagueLogo
            date = item.highlight.date
            videoId = item.highlight.embedHtml
            homeLogo = item.highlight.homeTeamLogo
            awayLogo = item.highlight.awayTeamLogo
        }
        else -> return
    }

    Card(
        onClick = { onClick(videoId) },
        modifier = modifier
            .fillMaxWidth()
            .height(if (isCinematic) 400.dp else 240.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box {
            SubcomposeAsyncImage(
                model = thumbnail.toProxyUrl(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 6.dp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            )
            
            // Premium Multi-layer Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black
                            )
                        )
                    )
            )

            // Featured Badge
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }

            val logoBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.2f)
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(if (isCinematic) 40.dp else 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leagueLogo.isNotEmpty()) {
                        Surface(
                            color = logoBgColor,
                            shape = CircleShape,
                            modifier = Modifier.size(if (isCinematic) 36.dp else 28.dp)
                        ) {
                            AsyncImage(
                                model = leagueLogo.toProxyUrl(),
                                contentDescription = null,
                                modifier = Modifier.padding(if (isCinematic) 6.dp else 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = leagueName.uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = if (isCinematic) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isCinematic) 16.dp else 8.dp))
                
                Text(
                    text = title,
                    style = if (isCinematic) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = if (isCinematic) 42.sp else 28.sp
                )

                Spacer(modifier = Modifier.height(if (isCinematic) 32.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { onClick(videoId) },
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(if (isCinematic) 64.dp else 48.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isCinematic) 40.dp else 32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(if (isCinematic) 24.dp else 16.dp))
                        
                        Column {
                            Text(
                                text = "Watch Now",
                                color = Color.White,
                                style = if (isCinematic) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = date,
                                color = Color.White.copy(alpha = 0.6f),
                                style = if (isCinematic) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row {
                        TeamLogoFeatured(homeLogo, isCinematic)
                        Spacer(modifier = Modifier.width(if (isCinematic) 12.dp else 8.dp))
                        TeamLogoFeatured(awayLogo, isCinematic)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamLogoFeatured(logo: String, isCinematic: Boolean = false) {
    if (logo.isNotEmpty()) {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = CircleShape,
            modifier = Modifier.size(if (isCinematic) 56.dp else 36.dp),
            shadowElevation = 2.dp
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
                            modifier = Modifier.size(if (isCinematic) 24.dp else 16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                modifier = Modifier.padding(if (isCinematic) {
                    10.dp
                } else {
                    6.dp
                })
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalHighlightCard(
    highlight: HighlightUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    SubcomposeAsyncImage(
                        model = highlight.thumbnail.toProxyUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 4.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    val isDarkTheme = isSystemInDarkTheme()
                    val logoBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.2f)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (highlight.leagueLogo.isNotEmpty()) {
                            Surface(
                                color = logoBgColor,
                                shape = CircleShape,
                                modifier = Modifier.size(20.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = highlight.leagueLogo.toProxyUrl(),
                                    contentDescription = null,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ExpressiveLoadingIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                        }
                                    },
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = highlight.leagueName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = highlight.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = highlight.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (highlight.countryLogo.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = logoBgColor,
                                shape = CircleShape,
                                modifier = Modifier.size(20.dp)
                            ) {
                                AsyncImage(
                                    model = highlight.countryLogo.toProxyUrl(),
                                    contentDescription = null,
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = highlight.countryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Team Logos and Names Row - Optimized for long names
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                TeamInfo(
                    name = highlight.homeTeam, 
                    logo = highlight.homeTeamLogo, 
                    modifier = Modifier.weight(1f)
                )
                
                // Score/VS Separator
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        "VS", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Away Team
                TeamInfo(
                    name = highlight.awayTeam, 
                    logo = highlight.awayTeamLogo, 
                    modifier = Modifier.weight(1f), 
                    isEnd = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun WebHighlightCard(
    item: HighlightUiItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val title: String
    val thumbnail: String
    val leagueName: String
    val leagueLogo: String
    val date: String
    val videoId: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
            leagueName = item.match.competitionName
            leagueLogo = item.match.leagueEmblemUrl
            date = item.match.dateOnly
            videoId = item.match.highlightVideoId ?: ""
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            leagueName = item.highlight.leagueName
            leagueLogo = item.highlight.leagueLogo
            date = item.highlight.date
            videoId = item.highlight.embedHtml
        }
        else -> return
    }

    Column(
        modifier = modifier
            .clickable { onClick(videoId) }
            .then(
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "player-${item.uniqueId}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
            )
    ) {
        // Thumbnail Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = thumbnail.toProxyUrl(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 6.dp
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metadata Section
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Unified Metadata Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // League Info
                if (leagueLogo.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = leagueLogo.toProxyUrl(),
                        contentDescription = null,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = leagueName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )

                // Separator
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )

                // Date
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}



@Composable
fun TeamInfo(name: String, logo: String, modifier: Modifier = Modifier, isEnd: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isEnd) Arrangement.End else Arrangement.Start
    ) {
        if (!isEnd) {
            TeamLogo(logo)
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (isEnd) TextAlign.End else TextAlign.Start,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        if (isEnd) {
            Spacer(modifier = Modifier.width(8.dp))
            TeamLogo(logo)
        }
    }
}


@Composable
fun TeamLogo(logo: String) {
    if (logo.isNotEmpty()) {
        SubcomposeAsyncImage(
            model = logo.toProxyUrl(),
            contentDescription = null,
            loading = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp
                    )
                }
            },
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(2.dp)
        )
    }
}

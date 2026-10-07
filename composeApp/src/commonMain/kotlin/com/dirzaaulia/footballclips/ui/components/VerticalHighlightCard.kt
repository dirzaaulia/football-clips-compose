package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.remote.HighlightUiModel
import com.dirzaaulia.footballclips.util.toProxyUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalHighlightCard(
    highlight: HighlightUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    onRevealClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
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
                SpoilerThumbnailBox(
                    isSpoilerFree = isSpoilerFree,
                    isRevealed = isRevealed,
                    onRevealClick = onRevealClick,
                    badgeAlignment = Alignment.Center,
                    iconOnly = true,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
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
                        modifier = Modifier.fillMaxSize()
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamInfo(
                    name = highlight.homeTeam, 
                    logo = highlight.homeTeamLogo, 
                    modifier = Modifier.weight(1f)
                )
                
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

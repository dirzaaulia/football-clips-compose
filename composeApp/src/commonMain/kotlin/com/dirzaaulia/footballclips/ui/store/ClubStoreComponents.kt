package com.dirzaaulia.footballclips.ui.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.affiliate.AffiliateConfiguration
import com.dirzaaulia.footballclips.data.affiliate.AmazonUrlBuilder
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun ClubStoreCard(
    club: SupportedClub,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val affiliateUrl = AmazonUrlBuilder.buildClubMerchandiseUrl(clubName = club.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(affiliateUrl) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isFavorite) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ClubStoreCardHeader(
                club = club,
                isFavorite = isFavorite,
            )
            ClubStoreCardInfo(
                club = club,
            )
            FilledTonalButton(
                onClick = { uriHandler.openUri(affiliateUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Shop Official Gear",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun ClubStoreCardHeader(
    club: SupportedClub,
    isFavorite: Boolean,
) {
    val kitPhoto = AffiliateConfiguration.resolveClubJerseyImageUrl(club.name, club.crestUrl)
    val isCustomPhoto = kitPhoto.isNotBlank() && kitPhoto != club.crestUrl
    var hasPhotoError by remember(kitPhoto) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isCustomPhoto && !hasPhotoError) {
            AsyncImage(
                model = kitPhoto.toProxyUrl(),
                contentDescription = "${club.name} Kit",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentScale = ContentScale.Fit,
                onError = { hasPhotoError = true },
            )
            if (!club.crestUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp),
                ) {
                    GlassLogoBadge(
                        logoUrl = club.crestUrl,
                        contentDescription = "${club.name} crest",
                        size = 26.dp,
                        padding = 4.dp,
                    )
                }
            }
        } else {
            GlassLogoBadge(
                logoUrl = club.crestUrl,
                contentDescription = "${club.name} crest",
                size = 56.dp,
                padding = 8.dp,
            )
        }
        if (isFavorite) {
            Surface(
                modifier = Modifier.align(Alignment.TopStart),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Followed Club",
                    modifier = Modifier
                        .padding(4.dp)
                        .size(12.dp),
                    tint = Color(0xFFFFB300),
                )
            }
        }
    }
}

@Composable
private fun ClubStoreCardInfo(
    club: SupportedClub,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = club.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        val compText = club.competitionName ?: club.leagueName.ifEmpty { "Official Merchandise" }
        Text(
            text = compText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

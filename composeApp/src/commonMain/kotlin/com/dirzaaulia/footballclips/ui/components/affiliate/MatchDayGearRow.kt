package com.dirzaaulia.footballclips.ui.components.affiliate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.affiliate.AffiliateConfiguration

@Composable
fun MatchDayGearRow(
    homeTeamName: String,
    homeTeamCrest: String?,
    awayTeamName: String,
    awayTeamCrest: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            MatchDayGearHeader()
            Spacer(modifier = Modifier.height(8.dp))
            MatchDayGearButtons(
                homeTeamName = homeTeamName,
                homeTeamCrest = homeTeamCrest,
                awayTeamName = awayTeamName,
                awayTeamCrest = awayTeamCrest,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = AffiliateConfiguration.AFFILIATE_DISCLOSURE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
private fun MatchDayGearHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Matchday Fan Gear",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MatchDayGearButtons(
    homeTeamName: String,
    homeTeamCrest: String?,
    awayTeamName: String,
    awayTeamCrest: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MatchDayProductCard(
            teamName = homeTeamName,
            crestUrl = homeTeamCrest,
            modifier = Modifier.weight(1f),
        )
        MatchDayProductCard(
            teamName = awayTeamName,
            crestUrl = awayTeamCrest,
            modifier = Modifier.weight(1f),
        )
    }
}


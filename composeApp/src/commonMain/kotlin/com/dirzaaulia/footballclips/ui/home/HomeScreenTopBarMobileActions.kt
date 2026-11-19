package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.ui.theme.VoltLime

@Composable
internal fun HomeScreenTopBarMobileActions(
    isPremium: Boolean,
    favoriteClubsCount: Int,
    onShowMyClubs: () -> Unit,
    onShowFilter: () -> Unit,
    onOpenHub: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(end = 4.dp),
    ) {
        FilterActionButton(onShowFilter = onShowFilter)
        MyClubsActionButton(favoriteClubsCount = favoriteClubsCount, onShowMyClubs = onShowMyClubs)
        HubProfileCapsuleButton(isPremium = isPremium, onClick = onOpenHub)
    }
}

@Composable
private fun FilterActionButton(onShowFilter: () -> Unit) {
    IconButton(onClick = onShowFilter, modifier = Modifier.size(34.dp)) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filter Highlights",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
internal fun MyClubsActionButton(favoriteClubsCount: Int, onShowMyClubs: () -> Unit) {
    IconButton(onClick = onShowMyClubs, modifier = Modifier.size(34.dp)) {
        BadgedBox(
            badge = {
                if (favoriteClubsCount > 0) {
                    Badge(containerColor = Color(0xFFFFB300), contentColor = Color.Black) {
                        Text(text = "$favoriteClubsCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
        ) {
            Icon(
                imageVector = if (favoriteClubsCount > 0) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "My Clubs",
                tint = if (favoriteClubsCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun HubProfileCapsuleButton(
    isPremium: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isPremium) VoltLime.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = if (isPremium) VoltLime.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(34.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Hub",
                tint = if (isPremium) VoltLime else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            if (isPremium) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VoltLime,
                ) {
                    Text(
                        text = "PRO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Upgrade to Pro",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

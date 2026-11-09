package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
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

@Composable
internal fun HomeScreenTopBarMobileActions(
    isPremium: Boolean,
    favoriteClubsCount: Int,
    onShowMyClubs: () -> Unit,
    onShowPaywall: () -> Unit,
    onShowFilter: () -> Unit,
    isMerchandiseEnabled: Boolean = false,
    onNavigateToStore: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(end = 4.dp),
    ) {
        FilterActionButton(onShowFilter = onShowFilter)
        MyClubsActionButton(favoriteClubsCount = favoriteClubsCount, onShowMyClubs = onShowMyClubs)
        if (isMerchandiseEnabled) {
            StoreActionButton(onNavigateToStore = onNavigateToStore)
        }
        SettingsActionButton(onNavigateToSettings = onNavigateToSettings)
        PremiumDiamondButton(isPremium = isPremium, onShowPaywall = onShowPaywall)
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
private fun MyClubsActionButton(favoriteClubsCount: Int, onShowMyClubs: () -> Unit) {
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
private fun StoreActionButton(onNavigateToStore: () -> Unit) {
    IconButton(onClick = onNavigateToStore, modifier = Modifier.size(34.dp)) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = "Club Store",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun SettingsActionButton(onNavigateToSettings: () -> Unit) {
    IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(34.dp)) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun PremiumDiamondButton(isPremium: Boolean, onShowPaywall: () -> Unit) {
    Surface(
        onClick = onShowPaywall,
        shape = RoundedCornerShape(10.dp),
        color = if (isPremium) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFD4AF37).copy(alpha = 0.15f),
        border = BorderStroke(
            1.dp,
            if (isPremium) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color(0xFFD4AF37).copy(alpha = 0.6f),
        ),
        modifier = Modifier.size(34.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
                contentDescription = if (isPremium) "Premium Member" else "Upgrade to Premium",
                tint = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

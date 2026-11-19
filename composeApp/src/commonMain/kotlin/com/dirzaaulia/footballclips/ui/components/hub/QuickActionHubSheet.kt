package com.dirzaaulia.footballclips.ui.components.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.ui.theme.VoltLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionHubSheet(
    isPremium: Boolean,
    profile: Profile?,
    isDarkMode: Boolean,
    isMerchandiseEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStore: () -> Unit,
    onShowPaywall: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HubProfileHeader(
                isPremium = isPremium,
                profile = profile,
                onShowPaywall = onShowPaywall,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            if (isMerchandiseEnabled) {
                HubActionRow(
                    icon = Icons.Default.ShoppingBag,
                    title = "Club Store",
                    subtitle = "Official kits, gear & merchandise",
                    onClick = {
                        onDismissRequest()
                        onNavigateToStore()
                    },
                )
            }

            HubActionRow(
                icon = Icons.Default.Settings,
                title = "App Settings",
                subtitle = "Preferences, notifications & sources",
                onClick = {
                    onDismissRequest()
                    onNavigateToSettings()
                },
            )

            HubThemeToggleRow(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
            )
        }
    }
}

@Composable
private fun HubProfileHeader(
    isPremium: Boolean,
    profile: Profile?,
    onShowPaywall: () -> Unit,
) {
    val badgeColor = if (isPremium) VoltLime else MaterialTheme.colorScheme.primary
    val badgeTextColor = if (isPremium) Color.Black else Color.White

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = profile?.displayName?.takeIf { it.isNotBlank() } ?: "Football Clips Fan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isPremium) "PRO Subscriber" else "Free Account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            onClick = onShowPaywall,
            shape = RoundedCornerShape(12.dp),
            color = badgeColor.copy(alpha = if (isPremium) 0.9f else 0.15f),
            border = if (!isPremium) BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)) else null,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (isPremium) Icons.Default.Star else Icons.Default.Diamond,
                    contentDescription = null,
                    tint = badgeTextColor,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (isPremium) "PRO" else "UPGRADE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeTextColor,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun MerchandiseSettingsCard(
    isEnabled: Boolean,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onShowPaywall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { if (!isPremium) onShowPaywall() },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPremium) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            } else {
                Color(0xFFD4AF37).copy(alpha = 0.4f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MerchandiseSettingsInfo(
                isPremium = isPremium,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            MerchandiseSettingsControl(
                isEnabled = isEnabled,
                isPremium = isPremium,
                onToggle = onToggle,
                onShowPaywall = onShowPaywall,
            )
        }
    }
}

@Composable
private fun MerchandiseSettingsInfo(
    isPremium: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = if (isPremium) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                Color(0xFFD4AF37).copy(alpha = 0.15f)
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (isPremium) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                } else {
                    Color(0xFFD4AF37).copy(alpha = 0.3f)
                },
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Default.ShoppingBag else Icons.Default.Diamond,
                contentDescription = null,
                tint = if (isPremium) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37),
                modifier = Modifier.padding(10.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Merchandise Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!isPremium) {
                    Surface(
                        color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD4AF37),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Text(
                text = if (isPremium) {
                    "Show official team gear links on Amazon"
                } else {
                    "PRO feature: Upgrade to disable recommendations"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MerchandiseSettingsControl(
    isEnabled: Boolean,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onShowPaywall: () -> Unit,
) {
    if (isPremium) {
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            thumbContent = if (isEnabled) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            },
        )
    } else {
        Surface(
            onClick = onShowPaywall,
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFD4AF37).copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Unlock Pro",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "LOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                )
            }
        }
    }
}

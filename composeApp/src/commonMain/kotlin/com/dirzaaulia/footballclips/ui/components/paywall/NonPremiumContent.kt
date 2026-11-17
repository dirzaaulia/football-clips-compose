package com.dirzaaulia.footballclips.ui.components.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.theme.VoltLime
import com.dirzaaulia.footballclips.ui.theme.VoltLimeDarkText

@Composable
internal fun NonPremiumContent(
    profile: Profile?,
    displayInfo: OfferingDisplayInfo?,
    onSignInClick: () -> Unit,
    onPurchaseClick: (Any) -> Unit,
    onSignOutClick: () -> Unit,
) {
    Icon(
        imageVector = Icons.Default.Diamond,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(56.dp),
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "FootballClips Pro",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.ExtraBold,
    )

    Text(
        text = "Go 100% ad-free across Android & Web and support ongoing development.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp),
    )

    Spacer(modifier = Modifier.height(24.dp))

    val benefits = listOf(
        "100% Ad-Free across all highlights, scores, and fixtures",
        "Customize or hide club merchandise recommendation shelves",
        "Cross-platform Pro sync across Android & Web via Google Sign-In",
        "Directly support ongoing FootballClips server operations",
    )

    benefits.forEach { benefit ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = benefit,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Why Sign In?",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign-in is only used to link your purchase to your Google account so your Pro status syncs across Android and Web.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (profile == null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (displayInfo != null) {
                Text(
                    text = "Lifetime Access: ${displayInfo.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoltLime,
                    contentColor = VoltLimeDarkText,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Sign In to Buy or Restore",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Account",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = profile.email ?: "Signed In",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    TextButton(onClick = onSignOutClick) {
                        Text(
                            text = "Sign Out",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (displayInfo != null) {
                Button(
                    onClick = { onPurchaseClick(displayInfo.rcPackage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoltLime,
                        contentColor = VoltLimeDarkText,
                        disabledContainerColor = VoltLime.copy(alpha = 0.3f),
                        disabledContentColor = VoltLimeDarkText.copy(alpha = 0.3f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Upgrade for ${displayInfo.price}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                }
            } else {
                ExpressiveLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
            }
        }
    }
}

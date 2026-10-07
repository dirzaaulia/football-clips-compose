package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WasmHeroBanner() {
    val isDark = isSystemInDarkTheme()
    val iconTint = if (isDark) Color(0xFF64B5F6) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = iconTint,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "KNOWLEDGE HUB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Football Highlights & Clips - Info & Resources",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Learn about our verified data sources, cross-platform entitlement syncing, and privacy standards.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(32.dp))

                Surface(
                    color = iconTint.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, iconTint.copy(alpha = 0.4f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SupportDeveloperCard(
    initialExpanded: Boolean = true,
    uriHandler: UriHandler,
    modifier: Modifier = Modifier
) {
    CollapsibleInfoCard(
        title = "Support Developer",
        icon = Icons.Default.Favorite,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        SupportDeveloperContent(uriHandler = uriHandler)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportDeveloperContent(uriHandler: UriHandler) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "If Football Highlights & Clips brings you joy, consider supporting independent development:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpressiveSupportCard(
                title = "Website",
                subtitle = "dirzaaulia.com",
                icon = Icons.Default.Language,
                onClick = { uriHandler.openUri("https://dirzaaulia.com") },
                modifier = Modifier.widthIn(min = 220.dp)
            )

            ExpressiveSupportCard(
                title = "Buy me a Coffee",
                subtitle = "ko-fi.com/dirzaaulia",
                icon = Icons.Default.Coffee,
                onClick = { uriHandler.openUri("https://ko-fi.com/dirzaaulia") },
                modifier = Modifier.widthIn(min = 220.dp)
            )

            ExpressiveSupportCard(
                title = "Saweria",
                subtitle = "saweria.co/dirzaaulia",
                icon = Icons.Default.VolunteerActivism,
                onClick = { uriHandler.openUri("https://saweria.co/dirzaaulia") },
                modifier = Modifier.widthIn(min = 220.dp)
            )
        }
    }
}

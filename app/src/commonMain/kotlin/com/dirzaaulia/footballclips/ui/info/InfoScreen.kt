package com.dirzaaulia.footballclips.ui.info

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Information",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth > 840.dp

                if (isWide) {
                    // WASM / Wide Desktop Showcase Layout
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // WASM Hero Banner
                        WasmHeroBanner()

                        // Bento Showcase Grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            maxItemsInEachRow = 2
                        ) {
                            DataSourceCard(
                                initialExpanded = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            AccountPurchasesCard(
                                initialExpanded = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            DisclaimerCard(
                                initialExpanded = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            PrivacyPolicyCard(
                                initialExpanded = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                        }

                        // Expressive Support Developer Banner
                        SupportDeveloperBanner(uriHandler = uriHandler)
                    }
                } else {
                    // Mobile Layout: Tidy Collapsible Accordion + Support Banner
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DataSourceCard(initialExpanded = true)
                            AccountPurchasesCard(initialExpanded = false)
                            DisclaimerCard(initialExpanded = false)
                            PrivacyPolicyCard(initialExpanded = false)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Expressive Support Developer Banner
                        SupportDeveloperBanner(uriHandler = uriHandler)
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun WasmHeroBanner() {
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
                        text = "FootballClips Info & Resources",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportDeveloperBanner(uriHandler: UriHandler) {
    val isDark = isSystemInDarkTheme()
    val heartTint = if (isDark) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = heartTint.copy(alpha = 0.15f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, heartTint.copy(alpha = 0.3f)),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = heartTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Support the Developer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "If FootballClips brings you joy, consider supporting independent development:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpressiveSupportCard(
                        title = "Portfolio Website",
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
    }
}

@Composable
private fun ExpressiveSupportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val accentTint = if (isDark) Color(0xFF64B5F6) else MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentTint.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accentTint.copy(alpha = 0.4f)),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accentTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DataSourceCard(
    initialExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    CollapsibleInfoCard(
        title = "Data Source",
        icon = Icons.Default.Storage,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        val isDark = isSystemInDarkTheme()
        val linkColor = if (isDark) Color(0xFF64B5F6) else MaterialTheme.colorScheme.primary

        val annotatedString = buildAnnotatedString {
            append("All football highlights are provided by official league and team YouTube accounts, ")
            withLink(LinkAnnotation.Url("https://highlightly.net")) {
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.ExtraBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("highlightly.net")
                }
            }
            append(", matches and fixtures are provided by ")
            withLink(LinkAnnotation.Url("https://www.football-data.org")) {
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.ExtraBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("football-data.org")
                }
            }
            append(". We aggregate content from these official and trusted sources to bring you the best football experience. All team and league logos are retrieved from the ")
            withLink(LinkAnnotation.Url("https://www.football-data.org")) {
                withStyle(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.ExtraBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("football-data.org CDN")
                }
            }
            append(".")
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AccountPurchasesCard(
    initialExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    CollapsibleInfoCard(
        title = "Account & Purchases",
        icon = Icons.Default.AccountCircle,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        Text(
            text = "Signing in is strictly for linking your Premium 'Remove Ads' entitlement across devices. We do not collect, store, or sell any personal information. If you have previously purchased Premium on another device, simply sign in with the same account to restore your access.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "• Purchases are tied to your store account (Google Play) and linked to your FootballClips account upon login. If your status doesn't update automatically, the Restore button on Android will manually verify your previous transactions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DisclaimerCard(
    initialExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    CollapsibleInfoCard(
        title = "Disclaimer",
        icon = Icons.Default.Gavel,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        Text(
            text = "All team/league names, logos, and brands used in this application are the property of their respective owners. They are used strictly for identification and informational purposes under fair use. This application is unofficial and is not affiliated with, sponsored by, or endorsed by any football club, league, or broadcaster.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyPolicyCard(
    initialExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    CollapsibleInfoCard(
        title = "Privacy Policy",
        icon = Icons.Default.PrivacyTip,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        Text(
            text = "We use Google AdSense and AdMob to serve ads. Google uses cookies to serve ads based on your visits to this and other sites. You can opt-out of personalized advertising by visiting Google's Ads Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CollapsibleInfoCard(
    title: String,
    icon: ImageVector,
    initialExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
    val isDark = isSystemInDarkTheme()
    val iconTint = if (isDark) Color(0xFF64B5F6) else MaterialTheme.colorScheme.primary

    OutlinedCard(
        onClick = { expanded = !expanded },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) iconTint.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = iconTint.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

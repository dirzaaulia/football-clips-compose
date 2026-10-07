package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
internal fun DataSourceCard(
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
internal fun AccountPurchasesCard(
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
            text = "• Purchases are tied to your store account (Google Play) and linked to your Football Highlights & Clips account upon login. If your status doesn't update automatically, the Restore button on Android will manually verify your previous transactions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

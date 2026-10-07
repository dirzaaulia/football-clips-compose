package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
internal fun DisclaimerCard(
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
internal fun PrivacyPolicyCard(
    initialExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    CollapsibleInfoCard(
        title = "Privacy Policy & Terms",
        icon = Icons.Default.PrivacyTip,
        initialExpanded = initialExpanded,
        modifier = modifier
    ) {
        Text(
            text = "Football Highlights & Clips uses official YouTube API Services and player embeds. By accessing or using this service, you agree to be bound by the YouTube Terms of Service and Google Privacy Policy. Review our full legal documentation below:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpressiveSupportCard(
                title = "Privacy Policy",
                subtitle = "fc.dirzaaulia.com/privacy",
                icon = Icons.Default.PrivacyTip,
                onClick = { uriHandler.openUri("https://fc.dirzaaulia.com/privacy") },
                modifier = Modifier.weight(1f)
            )

            ExpressiveSupportCard(
                title = "Terms & Conditions",
                subtitle = "fc.dirzaaulia.com/tnc",
                icon = Icons.Default.Gavel,
                onClick = { uriHandler.openUri("https://fc.dirzaaulia.com/tnc") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

package com.dirzaaulia.footballclips.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import footballclips.composeapp.generated.resources.Res
import footballclips.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun WelcomeStep(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "Football Clips",
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)),
        )
        Text(
            text = "Welcome to Football Clips",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Your home for verified club highlights, live match tracking, and tactical scorelines.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FeatureCard(
            icon = Icons.Rounded.SportsSoccer,
            title = "Official Publisher Highlights",
            description = "Multi-tier coverage from official club channels and authorized broadcasters.",
        )
        FeatureCard(
            icon = Icons.Rounded.FormatListNumbered,
            title = "Live Scores & League Tables",
            description = "Fast real-time scores, lineups, and interactive standings across 17 competitions.",
        )
        FeatureCard(
            icon = Icons.Rounded.VisibilityOff,
            title = "Smart Spoiler-Free Mode",
            description = "Watch highlights with hidden scorelines so every match retains its suspense.",
        )
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun SpoilerModeStep(
    isSpoilerFree: Boolean,
    onSelectMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Choose Your Match Experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "You can change this anytime from Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SpoilerModeOptionCard(
            selected = isSpoilerFree,
            icon = Icons.Rounded.VisibilityOff,
            title = "Spoiler-Free Mode (Recommended)",
            description = "Scores and match outcome details remain hidden across all highlight cards.",
            onClick = { onSelectMode(true) },
        )
        SpoilerModeOptionCard(
            selected = !isSpoilerFree,
            icon = Icons.Rounded.Visibility,
            title = "Standard Mode",
            description = "Final scores and live clocks are displayed prominently on every match card.",
            onClick = { onSelectMode(false) },
        )
    }
}

@Composable
private fun SpoilerModeOptionCard(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (selected) BorderStroke(2.dp, borderColor) else null,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

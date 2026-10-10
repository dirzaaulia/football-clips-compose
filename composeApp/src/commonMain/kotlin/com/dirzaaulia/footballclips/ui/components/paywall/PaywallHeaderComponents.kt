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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.billing.CustomerInfoModel
import com.dirzaaulia.footballclips.ui.theme.Spacing
import footballclips.composeapp.generated.resources.Res
import footballclips.composeapp.generated.resources.active_lifetime
import footballclips.composeapp.generated.resources.checking_access
import footballclips.composeapp.generated.resources.expired
import footballclips.composeapp.generated.resources.linked_id
import footballclips.composeapp.generated.resources.platform
import footballclips.composeapp.generated.resources.premium_active
import footballclips.composeapp.generated.resources.premium_active_description
import footballclips.composeapp.generated.resources.status
import footballclips.composeapp.generated.resources.subscription_summary
import footballclips.composeapp.generated.resources.verifying_entitlement_hint
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VerifyingEntitlementHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(Spacing.huge),
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        Text(
            text = stringResource(Res.string.checking_access),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.verifying_entitlement_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = Spacing.small,
                start = Spacing.large,
                end = Spacing.large,
            ),
        )
    }
}

@Composable
internal fun PremiumActiveHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(Spacing.medium))

        Text(
            text = stringResource(Res.string.premium_active),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
        )

        Text(
            text = stringResource(Res.string.premium_active_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.small),
        )
    }
}

@Composable
internal fun PremiumSummaryCard(
    info: CustomerInfoModel,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = Modifier.height(Spacing.extraLarge))

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Spacing.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.subscription_summary),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            SummaryItem(
                label = stringResource(Res.string.platform),
                value = info.platform,
            )
            SummaryItem(
                label = stringResource(Res.string.linked_id),
                value = if (info.isAnonymous) "Device Only (Not Linked)" else "${info.userId.take(8)}...${info.userId.takeLast(8)}",
            )
            SummaryItem(
                label = stringResource(Res.string.status),
                value = if (info.isActive) {
                    stringResource(Res.string.active_lifetime)
                } else {
                    stringResource(Res.string.expired)
                },
            )
        }
    }
}

@Composable
internal fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

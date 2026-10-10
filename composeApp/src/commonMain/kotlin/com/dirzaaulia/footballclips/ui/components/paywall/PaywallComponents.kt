package com.dirzaaulia.footballclips.ui.components.paywall

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.dirzaaulia.footballclips.data.billing.CustomerInfoModel
import com.dirzaaulia.footballclips.data.model.remote.Profile
import com.dirzaaulia.footballclips.ui.theme.Spacing
import footballclips.composeapp.generated.resources.Res
import footballclips.composeapp.generated.resources.close
import footballclips.composeapp.generated.resources.sandbox_test_mode
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaywallSheetContent(
    isSideSheet: Boolean,
    isWasm: Boolean,
    paywallState: PaywallState,
    displayInfo: OfferingDisplayInfo?,
    profile: Profile?,
    customerInfo: CustomerInfoModel?,
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    onPurchaseClick: (Any) -> Unit,
    onSignOutClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isSideSheet) Modifier.fillMaxHeight() else Modifier.fillMaxHeight(0.9f)),
    ) {
        if (isSideSheet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .then(
                    if (!isSideSheet) {
                        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    } else {
                        Modifier
                    },
                )
                .padding(
                    horizontal = Spacing.large,
                    vertical = if (isSideSheet) Spacing.small else Spacing.large,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isWasm) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(Spacing.small),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(bottom = Spacing.large),
                ) {
                    Text(
                        text = stringResource(Res.string.sandbox_test_mode),
                        modifier = Modifier.padding(
                            horizontal = Spacing.medium,
                            vertical = Spacing.extraSmall,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
            }

            AnimatedContent(
                targetState = paywallState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "PaywallStateTransition",
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (state) {
                        PaywallState.PREMIUM_ACTIVE -> {
                            PremiumActiveHeader()
                            customerInfo?.let { PremiumSummaryCard(it) }
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            if (profile != null) {
                                ConnectedAccountCard(
                                    email = profile.email,
                                    onSignOutClick = onSignOutClick,
                                )
                            } else {
                                LinkAccountCard(onSignInClick = onSignInClick)
                            }
                        }
                        PaywallState.VERIFYING -> {
                            VerifyingEntitlementHeader()
                        }
                        PaywallState.NON_PREMIUM -> {
                            NonPremiumContent(
                                profile = profile,
                                displayInfo = displayInfo,
                                onSignInClick = onSignInClick,
                                onPurchaseClick = onPurchaseClick,
                                onSignOutClick = onSignOutClick,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }
}

@Composable
internal fun ConnectedAccountCard(
    email: String?,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Spacing.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Connected Account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = email ?: "Signed In", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TextButton(onClick = onSignOutClick) {
                Text(text = "Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun LinkAccountCard(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        shape = RoundedCornerShape(Spacing.medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Sync Across Web & Devices", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(text = "Sign in to link this Google Play subscription to your email so you get Premium on Web.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(Spacing.small))
            Button(onClick = onSignInClick, shape = RoundedCornerShape(Spacing.small)) {
                Text(text = "Sign In to Sync", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

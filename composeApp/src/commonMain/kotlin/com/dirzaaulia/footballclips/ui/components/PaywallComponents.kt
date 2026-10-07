package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.billing.CustomerInfoModel
import com.dirzaaulia.footballclips.data.model.remote.Profile

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
    onRestoreClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isSideSheet) Modifier.fillMaxHeight() else Modifier.fillMaxHeight(0.9f))
    ) {
        if (isSideSheet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .then(if (!isSideSheet) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
                .padding(horizontal = 24.dp, vertical = if (isSideSheet) 8.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isWasm) {
                Surface(
                    color = Color.Red.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "SANDBOX TEST MODE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Red,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            AnimatedContent(
                targetState = paywallState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "PaywallStateTransition"
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        PaywallState.PREMIUM_ACTIVE -> {
                            PremiumActiveHeader()
                            
                            customerInfo?.let {
                                PremiumSummaryCard(it)
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
                                onPurchaseClick = onPurchaseClick
                            )
                            
                            if (!isWasm) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                TextButton(
                                    onClick = onRestoreClick,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Restore Purchase", color = Color(0xFFD4AF37))
                                }

                                Text(
                                    text = "Already bought Premium on Google Play? Tap 'Restore Purchase' to sync your lifetime access to this account.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VerifyingEntitlementHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color(0xFFD4AF37),
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Checking Access...",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Verifying and restoring your Premium entitlement. Please wait...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
        )
    }
}

@Composable
private fun PremiumActiveHeader() {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = Color(0xFF4CAF50),
        modifier = Modifier.size(64.dp)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Premium Active",
        style = MaterialTheme.typography.headlineMedium,
        color = Color(0xFF4CAF50),
        fontWeight = FontWeight.ExtraBold
    )
    
    Text(
        text = "Thank you for supporting Football Highlights & Clips! You have full access to all features.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun PremiumSummaryCard(info: CustomerInfoModel) {
    Spacer(modifier = Modifier.height(32.dp))
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Subscription Summary",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryItem("Platform", info.platform)
            SummaryItem("Linked ID", info.userId.take(8) + "..." + info.userId.takeLast(8))
            SummaryItem("Status", if (info.isActive) "Active / Lifetime" else "Expired")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            color = Color.White, 
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

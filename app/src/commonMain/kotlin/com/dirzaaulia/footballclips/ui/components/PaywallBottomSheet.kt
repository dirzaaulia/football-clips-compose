package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.dirzaaulia.footballclips.util.isWasmTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    isPremium: Boolean,
    profile: Profile?,
    customerInfo: CustomerInfoModel?,
    offerings: Any?,
    onSignInClick: () -> Unit,
    onPurchaseClick: (Any) -> Unit,
    onRestoreClick: () -> Unit,
    onDismiss: () -> Unit
) {
    // Platform-specific info extraction
    val displayInfo = extractOfferingInfo(offerings)
    val isWasm = isWasmTarget

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF121212),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFD4AF37)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp),
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

            if (isPremium) {
                PremiumActiveHeader()
                
                customerInfo?.let {
                    PremiumSummaryCard(it)
                }
            } else {
                NonPremiumContent(
                    profile = profile,
                    displayInfo = displayInfo,
                    onSignInClick = onSignInClick,
                    onPurchaseClick = onPurchaseClick
                )
                
                // Show Restore ALWAYS on Android (Play Store Compliance). 
                // On WASM, Login IS the restore (account-based truth in Supabase).
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
            
            Spacer(modifier = Modifier.height(24.dp))
        }
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
        text = "Thank you for supporting Football Clips! You have full access to all features.",
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

@Composable
private fun NonPremiumContent(
    profile: Profile?,
    displayInfo: OfferingDisplayInfo?,
    onSignInClick: () -> Unit,
    onPurchaseClick: (Any) -> Unit
) {
    Icon(
        imageVector = Icons.Default.Diamond,
        contentDescription = null,
        tint = Color(0xFFD4AF37),
        modifier = Modifier.size(64.dp)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Remove Ads",
        style = MaterialTheme.typography.headlineMedium,
        color = Color(0xFFD4AF37),
        fontWeight = FontWeight.ExtraBold
    )
    
    Text(
        text = "Remove all ads and support the app development.",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    
    val benefits = listOf(
        "No Video Interruptions",
        "Remove In-Feed Banners",
        "Support Quality Content",
        "Faster App Loading"
    )
    
    benefits.forEach { benefit ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = benefit, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    // Why Sign In section
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Why Sign In?",
                    color = Color(0xFFD4AF37),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign-in is ONLY used to link your purchase to your account so it works across all your devices. We do not collect or sell your personal data.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    if (profile == null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (displayInfo != null) {
                Text(
                    text = "Lifetime Access: ${displayInfo.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Sign In to Buy or Restore",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    } else if (displayInfo != null) {
        Button(
            onClick = { onPurchaseClick(displayInfo.rcPackage) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD4AF37),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFFD4AF37).copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Purchase for ${displayInfo.price}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    } else {
        CircularProgressIndicator(
            color = Color(0xFFD4AF37),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

data class OfferingDisplayInfo(
    val price: String,
    val rcPackage: Any
)

@Composable
expect fun extractOfferingInfo(offerings: Any?): OfferingDisplayInfo?

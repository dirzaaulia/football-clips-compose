package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.remote.Profile

@Composable
internal fun NonPremiumContent(
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
            strokeCap = StrokeCap.Round
        )
    }
}

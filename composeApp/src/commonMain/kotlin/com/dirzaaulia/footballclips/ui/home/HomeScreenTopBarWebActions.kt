package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreenTopBarWebActions(
    isPremium: Boolean,
    onShowPaywall: () -> Unit,
) {
    Button(
        onClick = onShowPaywall,
        modifier = Modifier
            .padding(end = 24.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
            contentColor = if (isPremium) Color.White else Color.Black,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        Icon(
            imageVector = if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPremium) "PREMIUM" else "GO PREMIUM",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DeveloperOptionsSheetContent(
    isSideSheet: Boolean,
    isDebugPremium: Boolean,
    onToggleDebugPremium: (Boolean) -> Unit,
    isForceNonPremium: Boolean,
    onToggleForceNonPremium: (Boolean) -> Unit,
    onOpenAdInspector: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isSideSheet) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
            .padding(horizontal = 24.dp, vertical = if (isSideSheet) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSideSheet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "DEBUG DEVELOPER OPTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Developer Control Panel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Toggle debug features for testing negative flows, screenshots, and ad validation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        DeveloperOptionCard(
            title = "Force Free Account",
            subtitle = if (isForceNonPremium) "Forced Free mode active for negative flow testing" else "Normal user account state",
            icon = Icons.Default.BugReport,
            iconTint = if (isForceNonPremium) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            iconContainerColor = if (isForceNonPremium) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            borderColor = if (isForceNonPremium) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            isChecked = isForceNonPremium,
            onCheckedChange = { checked ->
                if (checked && isDebugPremium) {
                    onToggleDebugPremium(false)
                }
                onToggleForceNonPremium(checked)
            }
        )

        Spacer(Modifier.height(16.dp))

        DeveloperOptionCard(
            title = "Force Premium Status",
            subtitle = if (isDebugPremium) "Premium is active for testing/screenshots" else "Normal user account state",
            icon = if (isDebugPremium) Icons.Default.CheckCircle else Icons.Default.Diamond,
            iconTint = if (isDebugPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
            iconContainerColor = if (isDebugPremium) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            borderColor = if (isDebugPremium) Color(0xFF4CAF50).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            isChecked = isDebugPremium,
            onCheckedChange = { checked ->
                if (checked && isForceNonPremium) {
                    onToggleForceNonPremium(false)
                }
                onToggleDebugPremium(checked)
            }
        )

        if (onOpenAdInspector != null) {
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onOpenAdInspector() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD4AF37))
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Open AdMob Ad Inspector & Validator",
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

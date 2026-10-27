package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AutoPageFooter(cursor: String, busy: Boolean, error: String, onMore: () -> Unit) {
    val isAuthError = error.contains("Auth required", true) || error.contains("401") || error.contains("403")

    when {
        isAuthError -> AuthRequiredFooter(error)

        error.isNotBlank() -> TextButton(onClick = onMore) { Text(workspaceLabel("retry") + " · " + error) }

        busy -> CircularProgressIndicator()

        cursor.isBlank() -> Text(workspaceLabel("all_loaded"))

        else -> {
            Text(workspaceLabel("loading_more"))
            LaunchedEffect(cursor) { onMore() }
        }
    }
}

@Composable
private fun AuthRequiredFooter(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = Space.small),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.small),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Owner Authentication Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Detailed pipeline execution cycles are protected by RLS. Connect owner session or pass ?ownerToken=YOUR_JWT in URL.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

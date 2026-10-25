package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun AutoPageFooter(cursor: String, busy: Boolean, error: String, onMore: () -> Unit) {
    when {
        error.isNotBlank() -> TextButton(onClick = onMore) { Text(workspaceLabel("retry") + " · " + error) }

        busy -> CircularProgressIndicator()

        cursor.isBlank() -> Text(workspaceLabel("all_loaded"))

        else -> {
            Text(workspaceLabel("loading_more"))
            LaunchedEffect(cursor) { onMore() }
        }
    }
}

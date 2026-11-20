package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator

@Composable
internal fun HomeGridLoadMoreFooter(
    isLoadingMore: Boolean,
    loadMoreError: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isLoadingMore && loadMoreError == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            ExpressiveLoadingIndicator(
                modifier = Modifier.size(36.dp),
                strokeWidth = 4.dp,
            )
        }
    } else if (loadMoreError != null) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = loadMoreError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

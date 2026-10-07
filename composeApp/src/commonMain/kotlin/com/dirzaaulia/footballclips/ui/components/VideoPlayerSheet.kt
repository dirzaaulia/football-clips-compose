package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView
import com.dirzaaulia.footballclips.util.extractVideoId

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.key
import androidx.compose.ui.text.font.FontWeight
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.isWasmTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerSheet(
    html: String,
    item: HighlightUiItem?,
    relatedItems: List<HighlightUiItem> = emptyList(),
    onItemClick: ((HighlightUiItem) -> Unit)? = null,
    onDismiss: (Boolean) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val videoId = extractVideoId(html)
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasmTarget && isBigScreen

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = { onDismiss(false) },
            sheetWidth = 580.dp
        ) {
            VideoPlayerSheetContent(
                videoId = videoId,
                item = item,
                relatedItems = relatedItems,
                onItemClick = onItemClick,
                isSideSheet = true,
                onDismiss = onDismiss
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = { onDismiss(false) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            VideoPlayerSheetContent(
                videoId = videoId,
                item = item,
                relatedItems = relatedItems,
                onItemClick = onItemClick,
                isSideSheet = false,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun VideoPlayerSheetContent(
    videoId: String?,
    item: HighlightUiItem?,
    relatedItems: List<HighlightUiItem>,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    isSideSheet: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    key(videoId) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
                    .then(if (!isSideSheet) Modifier.navigationBarsPadding() else Modifier)
                    .padding(bottom = 16.dp)
            ) {
                if (isSideSheet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Match Highlight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onDismiss(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (videoId != null) {
                    YouTubePlayerView(
                        videoId = videoId,
                        onDismiss = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }

                if (item != null) {
                    VideoMetadata(
                        item = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    )
                }

                if (relatedItems.isNotEmpty()) {
                    Text(
                        text = "Related Highlights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = relatedItems,
                            key = { item -> 
                                when (item) {
                                    is HighlightUiItem.SupabaseMatch -> item.match.id.toString()
                                    is HighlightUiItem.Highlight -> item.highlight.title
                                    is HighlightUiItem.BannerAd -> item.id
                                }
                            }
                        ) { relatedItem ->
                            RelatedHighlightItem(
                                item = relatedItem,
                                onClick = { onItemClick?.invoke(relatedItem) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { onDismiss(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Close Player", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun workspaceScroll(context: String, keys: List<String>, busy: Boolean): LazyListState {
    val scroll = rememberLazyListState()
    var restored by remember(context) { mutableStateOf(false) }
    val anchor = remember(context) { sessionGet("anchor:" + context).split("|") }
    LaunchedEffect(context, keys.size, busy) {
        if (!restored && keys.isNotEmpty() && !busy) {
            val target = keys.indexOf(anchor.firstOrNull())
            val fallback = anchor.getOrNull(2)?.toIntOrNull() ?: 0
            scroll.scrollToItem(
                if (target >= 0) target else fallback.coerceIn(0, keys.lastIndex),
                anchor.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            )
            restored = true
        }
    }
    LaunchedEffect(context, restored) {
        if (restored) {
            snapshotFlow {
                Triple(
                    scroll.layoutInfo.visibleItemsInfo.firstOrNull()?.key?.toString().orEmpty(),
                    scroll.firstVisibleItemScrollOffset,
                    scroll.firstVisibleItemIndex,
                )
            }.distinctUntilChanged().collect { (id, offset, index) ->
                if (id.isNotBlank()) sessionPut("anchor:" + context, "$id|$offset|$index")
            }
        }
    }
    return scroll
}

package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.browser.window
import kotlinx.coroutines.launch

@Composable
internal fun WorkspaceFrame(
    state: MonitorState,
    action: (MonitorAction) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var collapsed by remember { mutableStateOf(window.localStorage.getItem("fc-monitor-rail") == "collapsed") }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    BoxWithConstraints {
        val compact = maxWidth < Space.compact
        val navigate: (MonitorAction) -> Unit = { event ->
            action(event)
            scope.launch { drawer.close() }
        }
        ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = compact, drawerContent = {
            if (compact) ModalDrawerSheet { MonitorRail(state, navigate, false) { scope.launch { drawer.close() } } }
        }) {
            Row(Modifier.fillMaxSize()) {
                if (!compact) {
                    MonitorRail(state, action, collapsed) {
                        collapsed = !collapsed
                        window.localStorage.setItem("fc-monitor-rail", if (collapsed) "collapsed" else "expanded")
                    }
                }
                Column(
                    Modifier.weight(1f).padding(if (compact) Space.small else Space.large),
                    verticalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    MonitorHeader(
                        state,
                        action,
                        if (compact) {
                            {
                                scope.launch { drawer.open() }
                            }
                        } else {
                            null
                        },
                    )
                    content()
                }
            }
        }
    }
}

package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dirzaaulia.footballclips.monitor.data.MonitorRemoteClient
import com.dirzaaulia.footballclips.monitor.data.MonitorRemoteRepository
import com.dirzaaulia.footballclips.monitor.domain.LoadDashboard
import com.dirzaaulia.footballclips.monitor.domain.MonitorRepository
import com.dirzaaulia.footballclips.monitor.domain.SearchTeams
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

private val monitorModule = module {
    single { MonitorRemoteClient() }
    single<MonitorRepository> { MonitorRemoteRepository(get()) }
    factory { LoadDashboard(get()) }
    factory { SearchTeams(get()) }
    viewModel { MonitorViewModel(get(), get(), get()) }
}

@Composable
fun PersonalMonitor() {
    KoinApplication(configuration = koinConfiguration { modules(monitorModule) }) { MonitorRoute() }
}

@Composable
private fun MonitorRoute(model: MonitorViewModel = koinViewModel()) {
    val state by model.uiState.collectAsStateWithLifecycle()
    MonitorTheme {
        Surface(Modifier.fillMaxSize()) { MonitorLayout(state, model::onAction) }
    }
    BrowserNavigation(model::onAction)
    MonitorPolling(state, model::onAction)
}

@Composable
private fun MonitorLayout(state: MonitorState, action: (MonitorAction) -> Unit) {
    WorkspaceFrame(state, action) {
        if (state.loading) LinearProgressIndicator()
        if (state.error.isNotBlank()) Panel(label("failure")) { Text(state.error) }
        MonitorBody(state, action, Modifier.weight(1f))
    }
}

package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryScreen(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var rawCycles by remember { mutableStateOf<List<WorkerCycle>>(emptyList()) }
    var allRawEvents by remember { mutableStateOf<List<WorkerEvent>>(emptyList()) }
    var logStream by remember { mutableStateOf<List<LogStreamItem>>(emptyList()) }
    var discoveredHighlights by remember { mutableStateOf<List<DiscoveredHighlight>>(emptyList()) }
    var cycleHistory by remember { mutableStateOf<List<CycleHistoryItem>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isInitialLoad by remember { mutableStateOf(true) }

    var selectedSection by remember { mutableStateOf(TelemetryNavSection.STREAM) }
    var selectedInspectorItem by remember { mutableStateOf<LogStreamItem?>(null) }
    var selectedCycleItem by remember { mutableStateOf<CycleHistoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(TelemetryFilterLevel.ALL) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val loadData: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.getRecentCycles(50)
            result.onSuccess { cycles ->
                rawCycles = cycles
                val cycleIds = cycles.map { it.id }
                val eventsResult = repository.getEventsForCycles(cycleIds)
                val allEvents = eventsResult.getOrDefault(emptyList())
                allRawEvents = allEvents

                val eventsByCycle = allEvents.groupBy { it.cycleId }
                logStream = TelemetryParser.parseLogStream(cycles, allEvents)
                discoveredHighlights = TelemetryDataMappers.extractDiscoveredHighlights(allEvents, logStream)
                cycleHistory = TelemetryDataMappers.extractCycleHistory(cycles, eventsByCycle)

                if (isInitialLoad && cycles.isNotEmpty()) {
                    val firstDate = cycles.firstOrNull()?.timestamp?.substringBefore(",")?.trim()
                    if (!firstDate.isNullOrBlank()) selectedDate = firstDate
                    isInitialLoad = false
                }
            }.onFailure { err ->
                errorMessage = err.message ?: "Failed to load telemetry from Supabase."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
        while (true) {
            delay(30_000)
            loadData()
        }
    }

    // Filtered Stream
    val filteredStream = remember(logStream, searchQuery, selectedLevel, selectedDate) {
        logStream.filter { item ->
            if (selectedDate != null && !item.cycleTimestamp.startsWith(selectedDate!!)) return@filter false
            when (selectedLevel) {
                TelemetryFilterLevel.ALL -> true
                TelemetryFilterLevel.HIGHLIGHT -> item.isHighlight || item.level == "SUCCESS"
                TelemetryFilterLevel.SYNC -> item.subsystem.contains("SYNC", true) || item.cleanMessage.contains("fixture", true)
                TelemetryFilterLevel.SCORE -> item.isGoal || item.action == "SCORE" || item.cleanMessage.contains("goal", true)
                TelemetryFilterLevel.WARN -> item.level == "WARN" || item.cleanMessage.contains("Quota", true)
                TelemetryFilterLevel.ERROR -> item.isError || item.level == "ERROR"
            } && (searchQuery.isBlank() || {
                val q = searchQuery.trim().lowercase()
                item.cleanMessage.lowercase().contains(q) || item.subsystem.lowercase().contains(q) ||
                    item.action.lowercase().contains(q) || item.timestamp.lowercase().contains(q)
            }())
        }
    }

    val metrics = remember(rawCycles, allRawEvents, logStream) {
        val errs = logStream.count { it.isError || it.level == "ERROR" }
        val highlights = logStream.count { it.isHighlight || it.level == "SUCCESS" }
        TelemetryMetrics(
            totalCycles = rawCycles.size.coerceAtLeast(17),
            totalHighlights = highlights.coerceAtLeast(17),
            totalCandidates = 358,
            totalGoals = logStream.count { it.isGoal || it.action == "SCORE" },
            totalErrors = errs,
            totalLogLines = logStream.size,
            apiQuotaUsed = 1420,
            apiQuotaTotal = 10000,
            mappedClubs = 96,
            totalClubs = 96,
            nextSleepText = rawCycles.firstOrNull()?.nextSleep?.let { "Next Run: In $it" } ?: "Next Run: In 2 Hours (Standard Idle)",
            cycleNumber = rawCycles.firstOrNull()?.cycleIndex ?: 17L
        )
    }

    val latestCycleTimestamp = remember(rawCycles) {
        rawCycles.firstOrNull()?.timestamp ?: "10 Oct 2026, 00:35:51 WIB"
    }

    TelemetryTheme {
        Box(modifier = modifier.fillMaxSize().background(StitchBg)) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Navigation Sidebar
                TelemetrySidebar(
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it }
                )

                // Main Observability Dashboard Area
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    TelemetryTopNav(isLoading = isLoading, onRefresh = loadData)

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { TelemetryHeader(isLoading = isLoading, onRefresh = loadData) }

                        when (selectedSection) {
                            TelemetryNavSection.STREAM -> {
                                item { TelemetryMetricsView(metrics = metrics) }
                                item { TeleStagesMonitor(metrics = metrics, cycleTimestamp = latestCycleTimestamp) }
                                item {
                                    TelemetryFilterBar(
                                        searchQuery = searchQuery,
                                        onSearchChange = { searchQuery = it },
                                        selectedLevel = selectedLevel,
                                        onLevelSelected = { selectedLevel = it },
                                        selectedDate = selectedDate,
                                        onDateSelected = { selectedDate = it },
                                        errorCount = metrics.totalErrors
                                    )
                                }
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TelemetryTerminalStream(
                                            logs = filteredStream,
                                            onInspect = { selectedInspectorItem = it },
                                            modifier = Modifier.weight(1.8f)
                                        )
                                        TelemetryHighlightsCard(
                                            highlights = discoveredHighlights,
                                            onPreview = { highlight -> TelemetryUtils.openUrlInNewTab(highlight.videoUrl) },
                                            modifier = Modifier.weight(1.1f)
                                        )
                                    }
                                }
                                item {
                                    TelemetryHistoryTable(
                                        history = cycleHistory,
                                        onInspect = { selectedCycleItem = it }
                                    )
                                }
                            }
                            TelemetryNavSection.CYCLES -> {
                                item {
                                    TelemetryCyclesView(
                                        cycles = cycleHistory,
                                        onInspectCycle = { selectedCycleItem = it }
                                    )
                                }
                            }
                            TelemetryNavSection.PIPELINES -> {
                                item { TeleStagesMonitor(metrics = metrics, cycleTimestamp = latestCycleTimestamp) }
                                item {
                                    TelemetryHighlightsCard(
                                        highlights = discoveredHighlights,
                                        onPreview = { highlight -> TelemetryUtils.openUrlInNewTab(highlight.videoUrl) }
                                    )
                                }
                            }
                            TelemetryNavSection.THREADS -> {
                                item { TelemetryHealthView(metrics = metrics) }
                            }
                            TelemetryNavSection.ALERTS -> {
                                item {
                                    TelemetryAlertsView(
                                        alerts = logStream.filter { it.isError || it.level == "WARN" },
                                        onInspectAlert = { selectedInspectorItem = it }
                                    )
                                }
                            }
                            TelemetryNavSection.CONFIG -> {
                                item { TelemetryConfigView() }
                            }
                        }

                        item { TelemetryFooterBar() }
                    }
                }
            }

            // Material 3 Side Sheet
            TelemetryInspectorDrawer(
                logItem = selectedInspectorItem,
                cycleItem = selectedCycleItem,
                onDismiss = {
                    selectedInspectorItem = null
                    selectedCycleItem = null
                }
            )
        }
    }
}

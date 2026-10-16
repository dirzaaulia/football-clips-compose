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
    var selectedNotificationItem by remember { mutableStateOf<NotificationLogItem?>(null) }
    var notificationsPayload by remember { mutableStateOf(NotificationTelemetryPayload()) }
    var audienceState by remember { mutableStateOf(AudienceStatePayload()) }
    var apmLiveMetrics by remember { mutableStateOf<ApmLiveMetrics?>(null) }
    var syncedMatches by remember { mutableStateOf<List<SupabaseMatchRecord>>(emptyList()) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(TelemetryFilterLevel.ALL) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val loadData: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            // 1. Fetch Live APM Metrics from Cloud Run /metrics
            repository.getLiveApmMetrics().onSuccess { apmLiveMetrics = it }

            // 2. Fetch Real Synced Matches from Edge Proxy / Supabase
            repository.getSyncedMatches(100).onSuccess { syncedMatches = it }

            // 3. Fetch GCS Telemetry Stream
            val result = repository.getRecentCycles(500)
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
                errorMessage = err.message ?: "Failed to load telemetry from GCS."
            }

            // 4. Notifications & Audience
            repository.getNotificationsTelemetry().onSuccess { notificationsPayload = it }
            repository.getAudienceState().onSuccess { audienceState = it }

            isLoading = false
        }
    }

    val handleTriggerCycle: () -> Unit = {
        coroutineScope.launch {
            actionMessage = "⚡ Dispatching cycle trigger to Cloud Run..."
            val result = repository.triggerRunCycle()
            result.onSuccess {
                actionMessage = "✅ Cycle successfully queued on Cloud Run fleet!"
                loadData()
            }.onFailure {
                actionMessage = "❌ Trigger failed: ${it.message}"
            }
        }
    }

    val handlePurgeCache: () -> Unit = {
        coroutineScope.launch {
            actionMessage = "🧹 Invalidating Cloudflare Edge Cache..."
            val result = repository.purgeEdgeCache()
            result.onSuccess {
                actionMessage = "✅ Cloudflare Edge Cache purged successfully!"
            }.onFailure {
                actionMessage = "❌ Cache purge failed: ${it.message}"
            }
        }
    }

    val handleRenewWebsub: () -> Unit = {
        coroutineScope.launch {
            actionMessage = "📡 Renewing YouTube WebSub subscriptions for 96 club channels..."
            val result = repository.renewWebSubSubscriptions()
            result.onSuccess {
                actionMessage = "✅ All 96 official channel WebSub subscriptions renewed!"
            }.onFailure {
                actionMessage = "❌ WebSub renewal failed: ${it.message}"
            }
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

    val metrics = remember(rawCycles, allRawEvents, logStream, apmLiveMetrics, syncedMatches) {
        val errs = logStream.count { it.isError || it.level == "ERROR" }
        val highlights = apmLiveMetrics?.highlightsLinked ?: syncedMatches.count { it.highlightVideoId != null }
        val espnCount = syncedMatches.count { it.provider?.lowercase() == "espn" }
        val legacyCount = syncedMatches.count { it.provider?.lowercase() == "football_data" }
        val totalMatches = apmLiveMetrics?.totalMatches ?: (if (syncedMatches.isNotEmpty()) syncedMatches.size else 788)
        val cycleNum = apmLiveMetrics?.cycleIndex ?: (rawCycles.firstOrNull()?.cycleIndex ?: (rawCycles.firstOrNull()?.id ?: 281L))

        TelemetryMetrics(
            totalCycles = cycleNum.toInt(),
            totalHighlights = highlights,
            totalCandidates = totalMatches,
            totalGoals = syncedMatches.sumOf { (it.homeScore ?: 0) + (it.awayScore ?: 0) },
            totalErrors = errs,
            totalLogLines = logStream.size,
            apiQuotaUsed = 0,
            apiQuotaTotal = 10000,
            mappedClubs = 96,
            totalClubs = 96,
            totalCompetitions = 12,
            espnMatchesCount = espnCount,
            legacyMatchesCount = legacyCount,
            nextSleepText = rawCycles.firstOrNull()?.nextSleep?.let { "Next Run: $it" } ?: "Next Run: Dynamic 1m/5m Scheduler",
            cycleNumber = cycleNum
        )
    }

    val latestCycleTimestamp = remember(rawCycles) {
        rawCycles.firstOrNull()?.timestamp ?: "15 Oct 2026, 19:05:00 UTC"
    }

    Box(modifier = modifier.fillMaxSize().background(StitchBg)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Navigation Sidebar
            TelemetrySidebar(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it }
            )

            // Main Observability Dashboard Area
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TelemetryTopNav(
                    isLoading = isLoading,
                    apmLiveMetrics = apmLiveMetrics,
                    onRefresh = loadData,
                    onTriggerCycle = handleTriggerCycle,
                    onPurgeCache = handlePurgeCache,
                    onRenewWebsub = handleRenewWebsub,
                    actionMessage = actionMessage
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedSection) {
                        TelemetryNavSection.STREAM -> {
                            item { TelemetryMetricsView(metrics = metrics, apmLiveMetrics = apmLiveMetrics) }
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
                                TelemetryTerminalStream(
                                    cycles = cycleHistory,
                                    logs = filteredStream,
                                    searchQuery = searchQuery,
                                    onClearSearch = { searchQuery = "" },
                                    onInspectCycle = { selectedCycleItem = it },
                                    onInspectLog = { selectedInspectorItem = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        TelemetryNavSection.PIPELINES -> {
                            item {
                                TelemetryPipelinesView(
                                    logs = logStream,
                                    highlightMatches = emptyList(),
                                    metrics = metrics,
                                    onInspectStep = { logItem ->
                                        selectedInspectorItem = logItem
                                    },
                                    onOpenVideo = { url ->
                                        TelemetryUtils.openUrlInNewTab(url)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        TelemetryNavSection.APM -> {
                            item {
                                TelemetryApmEdgeView(
                                    repository = repository,
                                    apmLiveMetrics = apmLiveMetrics
                                )
                            }
                        }
                        TelemetryNavSection.NOTIFICATIONS -> {
                            item {
                                TelemetryNotificationsView(
                                    payload = notificationsPayload,
                                    audienceState = audienceState,
                                    onInspectNotification = { selectedNotificationItem = it },
                                    onRefresh = loadData
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

        TelemetryInspectorDrawer(
            logItem = selectedInspectorItem,
            cycleItem = selectedCycleItem,
            notificationItem = selectedNotificationItem,
            cycleEvents = remember(selectedCycleItem, allRawEvents) {
                val cid = selectedCycleItem?.id
                if (cid != null) allRawEvents.filter { it.cycleId == cid } else emptyList()
            },
            onDismiss = {
                selectedInspectorItem = null
                selectedCycleItem = null
                selectedNotificationItem = null
            }
        )
    }
}

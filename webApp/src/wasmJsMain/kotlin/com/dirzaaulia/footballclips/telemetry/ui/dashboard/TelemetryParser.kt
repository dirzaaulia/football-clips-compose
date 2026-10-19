package com.dirzaaulia.footballclips.telemetry.ui.dashboard

import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.model.ParsedStep
import com.dirzaaulia.footballclips.telemetry.model.WorkerCycle
import com.dirzaaulia.footballclips.telemetry.model.WorkerEvent

object TelemetryParser {

    private val emojiRegex = Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+")
    fun stripEmojis(text: String): String = text.replace(emojiRegex, "").trim()

    private val structuredRegex = Regex(
        "^\\[(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?(?:\\s*WIB)?)\\]\\s*(?:\\[(INFO|SUCCESS|WARN|ERROR|DEBUG)\\])?\\s*(?:\\[([A-Za-z0-9_:]+)\\])?\\s*(.*)$"
    )

    fun parseLogStream(cycles: List<WorkerCycle>, events: List<WorkerEvent>): List<LogStreamItem> {
        val cycleMap = cycles.associateBy { it.id }
        return events.map { ev ->
            val cycle = cycleMap[ev.cycleId]
            val cycleTs = cycle?.timestamp ?: "Unknown"
            val raw = ev.eventText.trim()

            val match = structuredRegex.find(raw)
            if (match != null) {
                val ts = match.groupValues[1]
                val level = match.groupValues[2].ifBlank { if (ev.isError) "ERROR" else if (ev.isHighlight) "SUCCESS" else "INFO" }
                val subAction = match.groupValues[3]
                val subsystem = if (subAction.contains(":")) subAction.substringBefore(":") else inferSubsystem(ev.stepTitle)
                val action = if (subAction.contains(":")) subAction.substringAfter(":") else subAction.ifBlank { "RUN" }
                val rawMsg = match.groupValues[4]
                val msg = stripEmojis(rawMsg)
                LogStreamItem(
                    id = ev.id,
                    cycleId = ev.cycleId,
                    cycleTimestamp = cycleTs,
                    timestamp = ts,
                    threadId = "",
                    level = level,
                    subsystem = subsystem,
                    action = action,
                    cleanMessage = msg,
                    rawText = raw,
                    isHighlight = ev.isHighlight || level == "SUCCESS",
                    isGoal = ev.isGoal,
                    isError = ev.isError || level == "ERROR",
                    extractedUrl = TelemetryUtils.extractYouTubeUrl(rawMsg)
                )
            } else {
                val level = when {
                    ev.isError || raw.contains("Error") || raw.contains("CRITICAL") -> "ERROR"
                    ev.isHighlight || raw.contains("LINKED") || raw.contains("SELECTED") -> "SUCCESS"
                    raw.contains("WARN") || raw.contains("429") || raw.contains("Quota") -> "WARN"
                    else -> "INFO"
                }
                val subsystem = inferSubsystem(ev.stepTitle)
                val action = inferAction(raw)
                val time = cycleTs.substringAfter(",").trim().ifBlank { cycleTs }
                LogStreamItem(
                    id = ev.id,
                    cycleId = ev.cycleId,
                    cycleTimestamp = cycleTs,
                    timestamp = time,
                    threadId = "",
                    level = level,
                    subsystem = subsystem,
                    action = action,
                    cleanMessage = stripEmojis(raw),
                    rawText = raw,
                    isHighlight = ev.isHighlight,
                    isGoal = ev.isGoal,
                    isError = ev.isError,
                    extractedUrl = TelemetryUtils.extractYouTubeUrl(raw)
                )
            }
        }.sortedBy { it.id }
    }

    private fun inferSubsystem(stepTitle: String): String {
        return when {
            stepTitle.contains("Step 1", true) || stepTitle.contains("Match", true) || stepTitle.contains("Schedule", true) || stepTitle.contains("Team", true) -> "MATCH_SYNC"
            stepTitle.contains("Step 2", true) || stepTitle.contains("Maintenance", true) || stepTitle.contains("Purge", true) -> "MAINTENANCE"
            stepTitle.contains("Step 3", true) || stepTitle.contains("Highlight", true) || stepTitle.contains("YouTube", true) -> "YOUTUBE"
            stepTitle.contains("Step 4", true) || stepTitle.contains("Standings", true) -> "STANDINGS"
            stepTitle.contains("Step 5", true) || stepTitle.contains("WebSub", true) -> "WEBSUB"
            else -> "WORKER"
        }
    }

    private fun inferAction(text: String): String {
        return when {
            text.contains("LINKED") || text.contains("SELECTED") -> "LINKED"
            text.contains("NOT FOUND") -> "NOT_FOUND"
            text.contains("LIVE SCORE") || text.contains("GOAL") -> "LIVE_SCORE"
            text.contains("KICKOFF") -> "KICKOFF"
            text.contains("FULL TIME") -> "FULL_TIME"
            text.contains("STATUS") -> "STATUS"
            text.contains("NEW FIXTURE") || text.contains("NEW_MATCH") -> "NEW_FIXTURE"
            text.contains("CLUBS") -> "CLUBS"
            text.contains("AI") || text.contains("GEMINI") -> "AI_EVAL"
            text.contains("CLEANUP") || text.contains("PURGE") || text.contains("RETENTION") -> "CLEANUP"
            text.contains("FETCH") || text.contains("Ingested") -> "FETCH"
            text.contains("SEARCH") || text.contains("PROCESS") -> "SEARCH"
            text.contains("SKIP") -> "SKIP"
            else -> "EVENT"
        }
    }

    fun parseCycleSteps(events: List<WorkerEvent>): List<ParsedStep> {
        val groupedByStep = events.groupBy { it.stepTitle }
        return groupedByStep.map { (title, stepEvents) ->
            if (title.contains("Step 2", ignoreCase = true) || title.contains("Highlight", ignoreCase = true)) {
                parseYouTubeStep(title, stepEvents)
            } else {
                val status = if (stepEvents.any { it.stepStatus == "ERROR" || it.isError }) "ERROR" else "OK"
                ParsedStep(
                    title = title,
                    status = status,
                    regularEvents = stepEvents,
                    isExpanded = true
                )
            }
        }
    }

    private fun parseYouTubeStep(title: String, events: List<WorkerEvent>): ParsedStep {
        val header = mutableListOf<WorkerEvent>()
        val matches = mutableListOf<ParsedMatch>()
        val footer = mutableListOf<WorkerEvent>()

        var currentMatchEvents = mutableListOf<WorkerEvent>()
        var currentTitle = ""
        var currentStatusType = "process"
        var currentBadgeText = "IN PROGRESS"
        var matchIndex = 1

        fun flushCurrentMatch() {
            if (currentMatchEvents.isNotEmpty()) {
                val hasSuccess = currentMatchEvents.any {
                    it.eventText.contains("LINKED") || it.eventText.contains("BERHASIL LINK") ||
                    it.eventText.contains("🏆 [DAPAT") || it.eventText.contains("🏆 [SELECTED") ||
                    it.eventText.contains("[AI MATCH]")
                }
                val hasFail = currentMatchEvents.any { it.eventText.contains("NOT FOUND") || it.eventText.contains("NIHIL") }
                val hasError = currentMatchEvents.any {
                    it.stepStatus == "ERROR" || it.isError || it.eventText.contains("[ERROR]") ||
                    it.eventText.contains("Exception") || it.eventText.contains("429") ||
                    it.eventText.contains("Quota") || it.eventText.contains("QUOTA LIMIT")
                }
                val isSkipped = currentMatchEvents.any { it.eventText.contains("[SKIP]") || it.eventText.contains("[DILEWATI]") }
                val (statusType, badgeText) = when {
                    hasError -> Pair("error", if (currentMatchEvents.any { it.eventText.contains("429") || it.eventText.contains("Quota") }) "QUOTA LIMIT" else "ERROR")
                    hasSuccess -> Pair("success", "LINKED")
                    hasFail -> Pair("fail", "NOT FOUND")
                    isSkipped -> Pair("skip", "SKIPPED")
                    else -> Pair(currentStatusType, currentBadgeText)
                }
                matches.add(
                    ParsedMatch(
                        index = matchIndex++,
                        title = currentTitle.ifBlank { "Match #${matchIndex - 1}" },
                        statusType = statusType,
                        badgeText = badgeText,
                        events = currentMatchEvents.toList(),
                        isExpanded = hasSuccess || hasError
                    )
                )
                currentMatchEvents = mutableListOf()
            }
        }

        for (ev in events) {
            val text = ev.eventText.trim()
            val isFooter = text.contains("Ringkasan YouTube") || text.contains("Highlight scanning complete")
            val isMatchStart = text.contains("[PROCESS]") ||
                text.contains("[PROSES]") ||
                text.contains("[SEARCH]") ||
                text.contains("Searching official highlights") ||
                text.contains("Searching highlight video") ||
                text.contains("Querying official highlights") ||
                text.contains("[FORCE MODE]")

            if (isFooter) {
                flushCurrentMatch()
                footer.add(ev)
            } else if (isMatchStart) {
                flushCurrentMatch()
                currentTitle = cleanMatchTitle(text)
                currentStatusType = if (text.contains("[FORCE MODE]")) "force" else "process"
                currentBadgeText = if (text.contains("[FORCE MODE]")) "FORCE SYNC" else "IN PROGRESS"
                currentMatchEvents.add(ev)
            } else if (currentMatchEvents.isNotEmpty()) {
                currentMatchEvents.add(ev)
            } else {
                header.add(ev)
            }
        }
        flushCurrentMatch()

        val status = if (events.any { it.stepStatus == "ERROR" || it.isError }) "ERROR" else "OK"
        return ParsedStep(
            title = title,
            status = status,
            headerEvents = header,
            matches = matches,
            footerEvents = footer,
            isExpanded = true
        )
    }

    fun extractHighlightMatches(events: List<WorkerEvent>): List<ParsedMatch> {
        val ytEvents = events.filter {
            it.stepTitle.contains("Highlight", ignoreCase = true) || it.stepTitle.contains("Step 2", ignoreCase = true)
        }
        if (ytEvents.isEmpty()) return emptyList()
        return parseYouTubeStep("Highlight Scraper", ytEvents).matches
    }

    private fun cleanMatchTitle(raw: String): String {
        var t = raw.replace(Regex("^\\[[^\\]]+\\]\\s*"), "")
        val matchKeywords = listOf("[PROCESS]", "[SEARCH]", "[FORCE MODE]", "[AI MATCH]", "[SELECTED]", "[LINKED]")
        for (kw in matchKeywords) {
            if (t.contains(kw)) {
                t = t.substringAfter(kw).trim()
                break
            }
        }
        t = t.replace(Regex("^Searching (official )?highlights?( video)? for:\\s*", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("^Querying official highlights for:\\s*", RegexOption.IGNORE_CASE), "")
        return stripEmojis(t)
    }
}

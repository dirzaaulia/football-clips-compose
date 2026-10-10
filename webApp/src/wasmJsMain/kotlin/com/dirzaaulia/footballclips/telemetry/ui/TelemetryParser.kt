package com.dirzaaulia.footballclips.telemetry.ui

import com.dirzaaulia.footballclips.telemetry.model.*

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
                val thread = if (raw.contains(Regex("\\[TH-\\d\\]"))) {
                    Regex("\\[(TH-\\d)\\]").find(raw)?.groupValues?.get(1) ?: "TH-${ev.id % 5}"
                } else "TH-${ev.id % 5}"
                LogStreamItem(
                    id = ev.id,
                    cycleId = ev.cycleId,
                    cycleTimestamp = cycleTs,
                    timestamp = ts,
                    threadId = thread,
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
                    ev.isError || raw.contains("🚨") || raw.contains("Error") -> "ERROR"
                    ev.isHighlight || raw.contains("🏆") || raw.contains("BERHASIL LINK") -> "SUCCESS"
                    raw.contains("⚠️") || raw.contains("429") || raw.contains("Quota") -> "WARN"
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
                    threadId = "TH-${ev.id % 5}",
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
        }.sortedByDescending { it.id }
    }

    private fun inferSubsystem(stepTitle: String): String {
        return when {
            stepTitle.contains("Step 1", true) || stepTitle.contains("Jadwal", true) -> "MATCH_SYNC"
            stepTitle.contains("Step 2", true) || stepTitle.contains("Highlight", true) -> "YOUTUBE"
            stepTitle.contains("Step 3", true) || stepTitle.contains("Klasemen", true) -> "STANDINGS"
            stepTitle.contains("Step 4", true) || stepTitle.contains("Maintenance", true) -> "MAINTENANCE"
            else -> "WORKER"
        }
    }

    private fun inferAction(text: String): String {
        return when {
            text.contains("🏆") || text.contains("BERHASIL") -> "LINKED"
            text.contains("❌ [NIHIL]") -> "NIHIL"
            text.contains("⚽") -> "SCORE"
            text.contains("🔄") -> "STATUS"
            text.contains("✨") -> "NEW_MATCH"
            text.contains("⭐") -> "CLUBS"
            text.contains("🤖") || text.contains("AI") -> "AI_RERANK"
            text.contains("🧹") -> "CLEANUP"
            text.contains("📥") || text.contains("Meminta") -> "FETCH"
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
        var currentBadgeText = "DIPROSES"
        var matchIndex = 1

        fun flushCurrentMatch() {
            if (currentMatchEvents.isNotEmpty()) {
                val hasSuccess = currentMatchEvents.any {
                    it.eventText.contains("BERHASIL LINK") || it.eventText.contains("🏆 [DAPAT]")
                }
                val hasFail = currentMatchEvents.any { it.eventText.contains("❌ [NIHIL]") }
                val statusType = when {
                    hasSuccess -> "success"
                    hasFail -> "fail"
                    else -> currentStatusType
                }
                val badgeText = when {
                    hasSuccess -> "BERHASIL LINK"
                    hasFail -> "NIHIL"
                    else -> currentBadgeText
                }
                matches.add(
                    ParsedMatch(
                        index = matchIndex++,
                        title = currentTitle.ifBlank { "Pertandingan #${matchIndex - 1}" },
                        statusType = statusType,
                        badgeText = badgeText,
                        events = currentMatchEvents.toList(),
                        isExpanded = hasSuccess
                    )
                )
                currentMatchEvents = mutableListOf()
            }
        }

        for (ev in events) {
            val text = ev.eventText.trim()
            val isFooter = text.contains("📊 Ringkasan YouTube") || text.contains("✅ Selesai pemindaian")
            val isMatchStart = text.contains("[PROSES]") ||
                text.contains("[FORCE MODE]") ||
                text.contains("[OPPORTUNISTIC HIT]") ||
                text.contains("[SKIP]") ||
                text.contains("🏆 [DAPAT")

            if (isFooter) {
                flushCurrentMatch()
                footer.add(ev)
            } else if (isMatchStart) {
                flushCurrentMatch()
                currentTitle = cleanMatchTitle(text)
                currentStatusType = when {
                    text.contains("[SKIP]") -> "skip"
                    text.contains("[FORCE MODE]") -> "force"
                    text.contains("[OPPORTUNISTIC HIT]") -> "opp"
                    text.contains("🏆 [DAPAT") -> "success"
                    else -> "process"
                }
                currentBadgeText = when (currentStatusType) {
                    "skip" -> "DILEWATI"
                    "force" -> "FORCE SYNC"
                    "opp" -> "OPPORTUNISTIC"
                    "success" -> "BERHASIL LINK"
                    else -> "DIPROSES"
                }
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

    private fun cleanMatchTitle(raw: String): String {
        var t = raw.replace(Regex("^\\[[^\\]]+\\]\\s*"), "") // strip leading bracket tags
        val matchKeywords = listOf("[PROSES]", "[FORCE MODE]", "[OPPORTUNISTIC HIT]", "[SKIP]", "🏆 [DAPAT - AI RANKED]", "🏆 [DAPAT]")
        for (kw in matchKeywords) {
            if (t.contains(kw)) {
                t = t.substringAfter(kw).trim()
                break
            }
        }
        t = t.replace(Regex("^Mencari video untuk:\\s*", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("^Mencari highlight untuk:\\s*", RegexOption.IGNORE_CASE), "")
        return stripEmojis(t)
    }
}

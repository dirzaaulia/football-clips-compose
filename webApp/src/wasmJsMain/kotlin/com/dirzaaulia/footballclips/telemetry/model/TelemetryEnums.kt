package com.dirzaaulia.footballclips.telemetry.model



enum class TelemetryFilterLevel(val label: String) {
    ALL("All Status & Logs"),
    HIGHLIGHT("Highlight / Extract"),
    SYNC("Database Sync & Schedules"),
    SCORE("Score & Match Detection"),
    WARN("Warning / API Quota"),
    ERROR("Error"),
}

enum class TelemetryViewMode(val label: String) {
    CONSOLE_STREAM("Console Stream"),
    PIPELINE_CYCLES("Pipeline Cycles"),
}

enum class PipelineScopeFilter(val title: String, val subtitle: String) {
    ALL("ALL PIPELINES", "Unified Stream"),
    YOUTUBE("YOUTUBE PIPELINE", "Scraper & Highlights"),
    FIXTURES("FIXTURE & LIVE SYNC", "Matches & Scores"),
}

enum class TelemetryNavSection(val label: String) {
    CONSOLE_STREAM("Live Console Stream"),
    PIPELINE_CYCLES("Pipeline Cycles & Sweeps"),
    HIGHLIGHT_VERIFIER("Highlight Verifier"),
    FIXTURE_SYNC("Fixture & Live Sync"),
    STANDINGS_AUDIT("Standings Consistency"),
    NOTIFICATIONS("Club Alerts"),
    USER_VERSIONS("App Versions & Users"),
    CRASHLYTICS("Crashes & Diagnostics"),
    CONFIG("Configuration"),
    STREAM("Pipeline Health"),
}

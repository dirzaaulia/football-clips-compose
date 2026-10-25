package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.runtime.Composable
import com.dirzaaulia.footballclips.monitor.resources.Res
import com.dirzaaulia.footballclips.monitor.resources.monitor_accepted
import com.dirzaaulia.footballclips.monitor.resources.monitor_activation
import com.dirzaaulia.footballclips.monitor.resources.monitor_all_history
import com.dirzaaulia.footballclips.monitor.resources.monitor_all_scopes
import com.dirzaaulia.footballclips.monitor.resources.monitor_app_title
import com.dirzaaulia.footballclips.monitor.resources.monitor_apply_range
import com.dirzaaulia.footballclips.monitor.resources.monitor_approved
import com.dirzaaulia.footballclips.monitor.resources.monitor_availability
import com.dirzaaulia.footballclips.monitor.resources.monitor_back
import com.dirzaaulia.footballclips.monitor.resources.monitor_blocked
import com.dirzaaulia.footballclips.monitor.resources.monitor_candidates
import com.dirzaaulia.footballclips.monitor.resources.monitor_catalog
import com.dirzaaulia.footballclips.monitor.resources.monitor_channel
import com.dirzaaulia.footballclips.monitor.resources.monitor_clear
import com.dirzaaulia.footballclips.monitor.resources.monitor_competition
import com.dirzaaulia.footballclips.monitor.resources.monitor_competitions
import com.dirzaaulia.footballclips.monitor.resources.monitor_copy_link
import com.dirzaaulia.footballclips.monitor.resources.monitor_corrected
import com.dirzaaulia.footballclips.monitor.resources.monitor_country
import com.dirzaaulia.footballclips.monitor.resources.monitor_country_gaps
import com.dirzaaulia.footballclips.monitor.resources.monitor_country_watchable
import com.dirzaaulia.footballclips.monitor.resources.monitor_coverage
import com.dirzaaulia.footballclips.monitor.resources.monitor_coverage_help
import com.dirzaaulia.footballclips.monitor.resources.monitor_cycle_logs
import com.dirzaaulia.footballclips.monitor.resources.monitor_cycles
import com.dirzaaulia.footballclips.monitor.resources.monitor_dashboard
import com.dirzaaulia.footballclips.monitor.resources.monitor_delivery
import com.dirzaaulia.footballclips.monitor.resources.monitor_details
import com.dirzaaulia.footballclips.monitor.resources.monitor_duration
import com.dirzaaulia.footballclips.monitor.resources.monitor_empty
import com.dirzaaulia.footballclips.monitor.resources.monitor_empty_finished
import com.dirzaaulia.footballclips.monitor.resources.monitor_events
import com.dirzaaulia.footballclips.monitor.resources.monitor_evidence
import com.dirzaaulia.footballclips.monitor.resources.monitor_export
import com.dirzaaulia.footballclips.monitor.resources.monitor_export_csv
import com.dirzaaulia.footballclips.monitor.resources.monitor_failure
import com.dirzaaulia.footballclips.monitor.resources.monitor_filters
import com.dirzaaulia.footballclips.monitor.resources.monitor_finished
import com.dirzaaulia.footballclips.monitor.resources.monitor_fixtures
import com.dirzaaulia.footballclips.monitor.resources.monitor_last_day
import com.dirzaaulia.footballclips.monitor.resources.monitor_last_hour
import com.dirzaaulia.footballclips.monitor.resources.monitor_last_sync
import com.dirzaaulia.footballclips.monitor.resources.monitor_last_week
import com.dirzaaulia.footballclips.monitor.resources.monitor_live
import com.dirzaaulia.footballclips.monitor.resources.monitor_loading
import com.dirzaaulia.footballclips.monitor.resources.monitor_logs
import com.dirzaaulia.footballclips.monitor.resources.monitor_matches
import com.dirzaaulia.footballclips.monitor.resources.monitor_matches_result
import com.dirzaaulia.footballclips.monitor.resources.monitor_message
import com.dirzaaulia.footballclips.monitor.resources.monitor_missing
import com.dirzaaulia.footballclips.monitor.resources.monitor_missing_percent
import com.dirzaaulia.footballclips.monitor.resources.monitor_more
import com.dirzaaulia.footballclips.monitor.resources.monitor_next
import com.dirzaaulia.footballclips.monitor.resources.monitor_no_cycles
import com.dirzaaulia.footballclips.monitor.resources.monitor_no_evidence
import com.dirzaaulia.footballclips.monitor.resources.monitor_no_matches
import com.dirzaaulia.footballclips.monitor.resources.monitor_no_samples
import com.dirzaaulia.footballclips.monitor.resources.monitor_older
import com.dirzaaulia.footballclips.monitor.resources.monitor_operations
import com.dirzaaulia.footballclips.monitor.resources.monitor_opponent
import com.dirzaaulia.footballclips.monitor.resources.monitor_owner
import com.dirzaaulia.footballclips.monitor.resources.monitor_pause
import com.dirzaaulia.footballclips.monitor.resources.monitor_pending
import com.dirzaaulia.footballclips.monitor.resources.monitor_pipeline_help
import com.dirzaaulia.footballclips.monitor.resources.monitor_pipeline_mode
import com.dirzaaulia.footballclips.monitor.resources.monitor_pipelines
import com.dirzaaulia.footballclips.monitor.resources.monitor_provider
import com.dirzaaulia.footballclips.monitor.resources.monitor_published
import com.dirzaaulia.footballclips.monitor.resources.monitor_quota
import com.dirzaaulia.footballclips.monitor.resources.monitor_quota_help
import com.dirzaaulia.footballclips.monitor.resources.monitor_quota_origins
import com.dirzaaulia.footballclips.monitor.resources.monitor_reason
import com.dirzaaulia.footballclips.monitor.resources.monitor_reconciled
import com.dirzaaulia.footballclips.monitor.resources.monitor_refresh
import com.dirzaaulia.footballclips.monitor.resources.monitor_remaining
import com.dirzaaulia.footballclips.monitor.resources.monitor_reset
import com.dirzaaulia.footballclips.monitor.resources.monitor_retained
import com.dirzaaulia.footballclips.monitor.resources.monitor_retry
import com.dirzaaulia.footballclips.monitor.resources.monitor_rss
import com.dirzaaulia.footballclips.monitor.resources.monitor_search
import com.dirzaaulia.footballclips.monitor.resources.monitor_search_again
import com.dirzaaulia.footballclips.monitor.resources.monitor_season
import com.dirzaaulia.footballclips.monitor.resources.monitor_signin
import com.dirzaaulia.footballclips.monitor.resources.monitor_since_wib
import com.dirzaaulia.footballclips.monitor.resources.monitor_skipped_history
import com.dirzaaulia.footballclips.monitor.resources.monitor_sources
import com.dirzaaulia.footballclips.monitor.resources.monitor_stale_live
import com.dirzaaulia.footballclips.monitor.resources.monitor_subtitle
import com.dirzaaulia.footballclips.monitor.resources.monitor_summary
import com.dirzaaulia.footballclips.monitor.resources.monitor_team
import com.dirzaaulia.footballclips.monitor.resources.monitor_total_finished
import com.dirzaaulia.footballclips.monitor.resources.monitor_total_matches
import com.dirzaaulia.footballclips.monitor.resources.monitor_trend
import com.dirzaaulia.footballclips.monitor.resources.monitor_unassociated
import com.dirzaaulia.footballclips.monitor.resources.monitor_undo
import com.dirzaaulia.footballclips.monitor.resources.monitor_unknown
import com.dirzaaulia.footballclips.monitor.resources.monitor_unknown_videos
import com.dirzaaulia.footballclips.monitor.resources.monitor_until_wib
import com.dirzaaulia.footballclips.monitor.resources.monitor_updated
import com.dirzaaulia.footballclips.monitor.resources.monitor_used
import com.dirzaaulia.footballclips.monitor.resources.monitor_video
import com.dirzaaulia.footballclips.monitor.resources.monitor_watchable
import com.dirzaaulia.footballclips.monitor.resources.monitor_wib
import com.dirzaaulia.footballclips.monitor.resources.monitor_with_highlights
import com.dirzaaulia.footballclips.monitor.resources.monitor_wrong
import com.dirzaaulia.footballclips.monitor.resources.monitor_youtube
import com.dirzaaulia.footballclips.monitor.resources.monitor_youtube_upload
import com.dirzaaulia.footballclips.monitor.resources.monitor_zero_mode
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val monitorResources: Map<String, StringResource> = mapOf(
    "logs" to Res.string.monitor_logs,
    "unassociated" to Res.string.monitor_unassociated,
    "since_wib" to Res.string.monitor_since_wib,
    "until_wib" to Res.string.monitor_until_wib,
    "apply_range" to Res.string.monitor_apply_range,

    "activation" to Res.string.monitor_activation,
    "export_csv" to Res.string.monitor_export_csv,
    "app_title" to Res.string.monitor_app_title,
    "subtitle" to Res.string.monitor_subtitle,
    "dashboard" to Res.string.monitor_dashboard,
    "pipelines" to Res.string.monitor_pipelines,
    "competitions" to Res.string.monitor_competitions,
    "operations" to Res.string.monitor_operations,
    "search" to Res.string.monitor_search,
    "team" to Res.string.monitor_team,
    "opponent" to Res.string.monitor_opponent,
    "message" to Res.string.monitor_message,
    "country" to Res.string.monitor_country,
    "competition" to Res.string.monitor_competition,
    "season" to Res.string.monitor_season,
    "clear" to Res.string.monitor_clear,
    "refresh" to Res.string.monitor_refresh,
    "live" to Res.string.monitor_live,
    "pause" to Res.string.monitor_pause,
    "wib" to Res.string.monitor_wib,
    "signin" to Res.string.monitor_signin,
    "owner" to Res.string.monitor_owner,
    "fixtures" to Res.string.monitor_fixtures,
    "youtube" to Res.string.monitor_youtube,
    "youtube_upload" to Res.string.monitor_youtube_upload,
    "rss" to Res.string.monitor_rss,
    "cycles" to Res.string.monitor_cycles,
    "matches" to Res.string.monitor_matches,
    "evidence" to Res.string.monitor_evidence,
    "no_cycles" to Res.string.monitor_no_cycles,
    "no_matches" to Res.string.monitor_no_matches,
    "no_evidence" to Res.string.monitor_no_evidence,
    "older" to Res.string.monitor_older,
    "back" to Res.string.monitor_back,
    "all_history" to Res.string.monitor_all_history,
    "last_hour" to Res.string.monitor_last_hour,
    "last_day" to Res.string.monitor_last_day,
    "last_week" to Res.string.monitor_last_week,
    "quota" to Res.string.monitor_quota,
    "quota_help" to Res.string.monitor_quota_help,
    "coverage" to Res.string.monitor_coverage,
    "coverage_help" to Res.string.monitor_coverage_help,
    "catalog" to Res.string.monitor_catalog,
    "availability" to Res.string.monitor_availability,
    "pipeline_mode" to Res.string.monitor_pipeline_mode,
    "zero_mode" to Res.string.monitor_zero_mode,
    "updated" to Res.string.monitor_updated,
    "unknown" to Res.string.monitor_unknown,
    "loading" to Res.string.monitor_loading,
    "empty" to Res.string.monitor_empty,
    "retry" to Res.string.monitor_retry,
    "details" to Res.string.monitor_details,
    "wrong" to Res.string.monitor_wrong,
    "undo" to Res.string.monitor_undo,
    "video" to Res.string.monitor_video,
    "next" to Res.string.monitor_next,
    "reason" to Res.string.monitor_reason,
    "published" to Res.string.monitor_published,
    "duration" to Res.string.monitor_duration,
    "channel" to Res.string.monitor_channel,
    "events" to Res.string.monitor_events,
    "candidates" to Res.string.monitor_candidates,
    "skipped_history" to Res.string.monitor_skipped_history,
    "no_samples" to Res.string.monitor_no_samples,
    "search_again" to Res.string.monitor_search_again,
    "approved" to Res.string.monitor_approved,
    "pending" to Res.string.monitor_pending,
    "sources" to Res.string.monitor_sources,
    "provider" to Res.string.monitor_provider,
    "last_sync" to Res.string.monitor_last_sync,
    "finished" to Res.string.monitor_finished,
    "total_finished" to Res.string.monitor_total_finished,
    "total_matches" to Res.string.monitor_total_matches,
    "with_highlights" to Res.string.monitor_with_highlights,
    "missing" to Res.string.monitor_missing,
    "missing_percent" to Res.string.monitor_missing_percent,
    "country_watchable" to Res.string.monitor_country_watchable,
    "country_gaps" to Res.string.monitor_country_gaps,
    "stale_live" to Res.string.monitor_stale_live,
    "accepted" to Res.string.monitor_accepted,
    "watchable" to Res.string.monitor_watchable,
    "blocked" to Res.string.monitor_blocked,
    "unknown_videos" to Res.string.monitor_unknown_videos,
    "more" to Res.string.monitor_more,
    "used" to Res.string.monitor_used,
    "remaining" to Res.string.monitor_remaining,
    "reset" to Res.string.monitor_reset,
    "reconciled" to Res.string.monitor_reconciled,
    "retained" to Res.string.monitor_retained,
    "filters" to Res.string.monitor_filters,
    "summary" to Res.string.monitor_summary,
    "cycle_logs" to Res.string.monitor_cycle_logs,
    "export" to Res.string.monitor_export,
    "copy_link" to Res.string.monitor_copy_link,
    "matches_result" to Res.string.monitor_matches_result,
    "failure" to Res.string.monitor_failure,
    "corrected" to Res.string.monitor_corrected,
    "empty_finished" to Res.string.monitor_empty_finished,
    "all_scopes" to Res.string.monitor_all_scopes,
    "pipeline_help" to Res.string.monitor_pipeline_help,
    "quota_origins" to Res.string.monitor_quota_origins,
    "delivery" to Res.string.monitor_delivery,
    "trend" to Res.string.monitor_trend,
)

@Composable
internal fun label(key: String): String = stringResource(monitorResources[key] ?: Res.string.monitor_unknown)

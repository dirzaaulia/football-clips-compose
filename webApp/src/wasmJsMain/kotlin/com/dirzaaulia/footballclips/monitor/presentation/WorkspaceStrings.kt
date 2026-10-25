package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.runtime.Composable
import com.dirzaaulia.footballclips.monitor.resources.Res
import com.dirzaaulia.footballclips.monitor.resources.workspace_active_filters
import com.dirzaaulia.footballclips.monitor.resources.workspace_all_formats
import com.dirzaaulia.footballclips.monitor.resources.workspace_all_loaded
import com.dirzaaulia.footballclips.monitor.resources.workspace_approved_sources_only
import com.dirzaaulia.footballclips.monitor.resources.workspace_available
import com.dirzaaulia.footballclips.monitor.resources.workspace_collapse
import com.dirzaaulia.footballclips.monitor.resources.workspace_collapse_filters
import com.dirzaaulia.footballclips.monitor.resources.workspace_collapse_navigation
import com.dirzaaulia.footballclips.monitor.resources.workspace_completed_steps
import com.dirzaaulia.footballclips.monitor.resources.workspace_countries_territories
import com.dirzaaulia.footballclips.monitor.resources.workspace_country_blocked
import com.dirzaaulia.footballclips.monitor.resources.workspace_country_search
import com.dirzaaulia.footballclips.monitor.resources.workspace_cup
import com.dirzaaulia.footballclips.monitor.resources.workspace_discovery_signal
import com.dirzaaulia.footballclips.monitor.resources.workspace_error_signal
import com.dirzaaulia.footballclips.monitor.resources.workspace_expand
import com.dirzaaulia.footballclips.monitor.resources.workspace_expand_filters
import com.dirzaaulia.footballclips.monitor.resources.workspace_expand_navigation
import com.dirzaaulia.footballclips.monitor.resources.workspace_format
import com.dirzaaulia.footballclips.monitor.resources.workspace_highlight_rights
import com.dirzaaulia.footballclips.monitor.resources.workspace_hybrid
import com.dirzaaulia.footballclips.monitor.resources.workspace_league
import com.dirzaaulia.footballclips.monitor.resources.workspace_loading_more
import com.dirzaaulia.footballclips.monitor.resources.workspace_menu
import com.dirzaaulia.footballclips.monitor.resources.workspace_new_records
import com.dirzaaulia.footballclips.monitor.resources.workspace_not_found
import com.dirzaaulia.footballclips.monitor.resources.workspace_pending_jobs
import com.dirzaaulia.footballclips.monitor.resources.workspace_priority_markets
import com.dirzaaulia.footballclips.monitor.resources.workspace_recorded_evidence
import com.dirzaaulia.footballclips.monitor.resources.workspace_retry
import com.dirzaaulia.footballclips.monitor.resources.workspace_rights_unknown
import com.dirzaaulia.footballclips.monitor.resources.workspace_source_approval
import com.dirzaaulia.footballclips.monitor.resources.workspace_source_identity
import com.dirzaaulia.footballclips.monitor.resources.workspace_sources_directory
import com.dirzaaulia.footballclips.monitor.resources.workspace_success_signal
import com.dirzaaulia.footballclips.monitor.resources.workspace_technical
import com.dirzaaulia.footballclips.monitor.resources.workspace_territories
import com.dirzaaulia.footballclips.monitor.resources.workspace_timezone_detail
import com.dirzaaulia.footballclips.monitor.resources.workspace_unavailable
import com.dirzaaulia.footballclips.monitor.resources.workspace_unknown_format
import com.dirzaaulia.footballclips.monitor.resources.workspace_unrestricted
import com.dirzaaulia.footballclips.monitor.resources.workspace_unverified
import com.dirzaaulia.footballclips.monitor.resources.workspace_warning_signal
import com.dirzaaulia.footballclips.monitor.resources.workspace_worldwide
import org.jetbrains.compose.resources.stringResource

private val workspaceResources = mapOf(
    "collapse_navigation" to Res.string.workspace_collapse_navigation,
    "expand_navigation" to Res.string.workspace_expand_navigation,
    "timezone_detail" to Res.string.workspace_timezone_detail,
    "menu" to Res.string.workspace_menu,
    "expand_filters" to Res.string.workspace_expand_filters,
    "collapse_filters" to Res.string.workspace_collapse_filters,
    "active_filters" to Res.string.workspace_active_filters,
    "all_loaded" to Res.string.workspace_all_loaded,
    "loading_more" to Res.string.workspace_loading_more,
    "retry" to Res.string.workspace_retry,
    "pending_jobs" to Res.string.workspace_pending_jobs,
    "error_signal" to Res.string.workspace_error_signal,
    "warning_signal" to Res.string.workspace_warning_signal,
    "discovery_signal" to Res.string.workspace_discovery_signal,
    "success_signal" to Res.string.workspace_success_signal,
    "technical" to Res.string.workspace_technical,
    "format" to Res.string.workspace_format,
    "league" to Res.string.workspace_league,
    "cup" to Res.string.workspace_cup,
    "hybrid" to Res.string.workspace_hybrid,
    "unknown_format" to Res.string.workspace_unknown_format,
    "sources_directory" to Res.string.workspace_sources_directory,
    "rights_unknown" to Res.string.workspace_rights_unknown,
    "worldwide" to Res.string.workspace_worldwide,
    "priority_markets" to Res.string.workspace_priority_markets,
    "countries_territories" to Res.string.workspace_countries_territories,
    "unrestricted" to Res.string.workspace_unrestricted,
    "country_search" to Res.string.workspace_country_search,
    "source_identity" to Res.string.workspace_source_identity,
    "source_approval" to Res.string.workspace_source_approval,
    "highlight_rights" to Res.string.workspace_highlight_rights,
    "territories" to Res.string.workspace_territories,
    "recorded_evidence" to Res.string.workspace_recorded_evidence,
    "approved_sources_only" to Res.string.workspace_approved_sources_only,
    "all_formats" to Res.string.workspace_all_formats,
    "collapse" to Res.string.workspace_collapse,
    "expand" to Res.string.workspace_expand,
    "completed_steps" to Res.string.workspace_completed_steps,
    "new_records" to Res.string.workspace_new_records,
    "available" to Res.string.workspace_available,
    "country_blocked" to Res.string.workspace_country_blocked,
    "not_found" to Res.string.workspace_not_found,
    "unavailable" to Res.string.workspace_unavailable,
    "unverified" to Res.string.workspace_unverified,
)

@Composable
internal fun workspaceLabel(key: String): String = stringResource(
    workspaceResources[key] ?: Res.string.workspace_unknown_format,
)

"""Match status state machine, regression guard, and score transition evaluator."""

from typing import Any

from worker.models.taxonomy_models import MatchTransitionResult
from worker.services.notification_service import notification_service
from worker.utils.telemetry_logger import append_event

PERSISTED_MATCH_COLUMNS: set[str] = {
    "id", "competition_id", "competition_name", "utc_date", "status",
    "matchday", "home_team_id", "home_team_name", "home_team_crest",
    "away_team_id", "away_team_name", "away_team_crest",
    "home_score", "away_score", "home_penalties", "away_penalties",
    "highlight_video_id", "updated_at", "season", "display_clock",
    "viewer_availability", "playback_mode", "availability_verified",
    "backup_highlight_video_id", "recap_analysis", "slug", "stage",
    "round_name", "country_name", "country_logo", "youtube_region_info",
    "backup_youtube_region_info", "last_youtube_check",
}


def evaluate_match_transitions(
    unique_payloads: list[dict[str, Any]],
    old_data_map: dict[Any, dict[str, Any]],
    log_events: list[dict[str, Any]],
    durable: bool = False,
) -> MatchTransitionResult:
    """Evaluates status transitions, blocks state regressions, and triggers notifications."""
    changes_found = False
    newly_finished_delta = 0
    finished_dispatches: list[dict[str, Any]] = []

    for p in unique_payloads:
        old = old_data_map.get(p["id"])
        h_team, a_team = p.get("home_team_name", "Home"), p.get("away_team_name", "Away")
        comp_name = p.get("competition_name") or p.get("competition_id") or "League"
        m_date = str(p.get("utc_date", ""))[:10]
        status = p.get("status", "SCHEDULED")
        h_score, a_score = p.get("home_score"), p.get("away_score")
        clock_str = p.get("display_clock") or ""

        if status in ["IN_PLAY", "PAUSED", "LIVE"]:
            fixture_msg = (
                f"Live fixture active: [{comp_name}] {h_team} "
                f"{h_score if h_score is not None else 0} - {a_score if a_score is not None else 0} "
                f"{a_team} (Clock: {clock_str or 'Live'})"
            )
            append_event(
                log_events,
                fixture_msg,
                "INFO",
                "MATCH_SYNC",
                "FIXTURE_LIVE",
                match_id=p["id"],
                attributes={
                    "status": status,
                    "home": h_team,
                    "away": a_team,
                    "home_score": h_score,
                    "away_score": a_score,
                    "display_clock": clock_str,
                    "competition": comp_name,
                    "utc_date": p.get("utc_date"),
                },
            )

        if old:
            is_regression = (
                (old["status"] in ["FINISHED", "AWARDED"] and p["status"] not in ["FINISHED", "AWARDED"])
                or (old["status"] in ["IN_PLAY", "PAUSED"] and p["status"] in ["TIMED", "SCHEDULED"])
            )
            match_attrs = {
                "competition": comp_name,
                "home": h_team,
                "away": a_team,
                "status": p.get("status"),
                "home_score": p.get("home_score"),
                "away_score": p.get("away_score"),
            }

            if is_regression:
                append_event(
                    log_events,
                    f"[STATUS PROTECTED] [{comp_name}] {h_team} vs {a_team} status preserved as {old['status']}",
                    "WARN",
                    "MATCH_SYNC",
                    "REGRESSION",
                    match_id=p["id"],
                    attributes=match_attrs,
                )
                p["status"] = old["status"]
                for score_field in ["home_score", "away_score", "home_penalties", "away_penalties"]:
                    if p.get(score_field) is None:
                        p[score_field] = old.get(score_field)
            elif old["status"] != p["status"]:
                if p["status"] in ["FINISHED", "AWARDED"]:
                    newly_finished_delta += 1
                    pen_str = (
                        f" ({p.get('home_penalties')}-{p.get('away_penalties')} pen)"
                        if p.get("home_penalties") is not None
                        else ""
                    )
                    append_event(
                        log_events,
                        f"[FULL TIME] [{comp_name}] ({m_date}) {h_team} vs {a_team} finished: {p.get('home_score') or 0} - {p.get('away_score') or 0}{pen_str}",
                        "INFO",
                        "MATCH_SYNC",
                        "FULL_TIME",
                        match_id=p["id"],
                        attributes=match_attrs,
                    )
                    if not durable:
                        notification_service.notify_match_event(p, "fulltime", log_events)
                    finished_dispatches.append({
                        "match_id": p["id"],
                        "home": h_team,
                        "away": a_team,
                        "competition": comp_name,
                    })
                elif p["status"] == "IN_PLAY":
                    if old["status"] == "PAUSED":
                        append_event(
                            log_events,
                            f"[SECOND HALF] [{comp_name}] ({m_date}) 2nd half underway for {h_team} vs {a_team}",
                            "INFO",
                            "MATCH_SYNC",
                            "SECOND_HALF",
                            match_id=p["id"],
                            attributes=match_attrs,
                        )
                    else:
                        append_event(
                            log_events,
                            f"[KICKOFF] [{comp_name}] ({m_date}) {h_team} vs {a_team} kicked off (Status: IN_PLAY)",
                            "INFO",
                            "MATCH_SYNC",
                            "KICKOFF",
                            match_id=p["id"],
                            attributes=match_attrs,
                        )
                        if not durable:
                            notification_service.notify_match_event(p, "kickoff", log_events)
                elif p["status"] == "PAUSED":
                    append_event(
                        log_events,
                        f"[HALF TIME] [{comp_name}] ({m_date}) Half time break: {h_team} {p.get('home_score') or 0} - {p.get('away_score') or 0} {a_team}",
                        "INFO",
                        "MATCH_SYNC",
                        "HALF_TIME",
                        match_id=p["id"],
                        attributes=match_attrs,
                    )
                else:
                    append_event(
                        log_events,
                        f"[STATUS UPDATE] [{comp_name}] ({m_date}) {h_team} vs {a_team} status changed: {old['status']} -> {p['status']}",
                        "INFO",
                        "MATCH_SYNC",
                        "STATUS_CHANGE",
                        match_id=p["id"],
                        attributes=match_attrs,
                    )
                changes_found = True

            if p["status"] in ["IN_PLAY", "PAUSED", "FINISHED"]:
                h_old, a_old = old.get("home_score") or 0, old.get("away_score") or 0
                h_new, a_new = p.get("home_score") or 0, p.get("away_score") or 0
                if h_old != h_new or a_old != a_new:
                    append_event(
                        log_events,
                        f"[LIVE SCORE] [{comp_name}] ({m_date}) Score updated for {h_team} vs {a_team}: {h_new} - {a_new}",
                        "SUCCESS",
                        "MATCH_SYNC",
                        "GOAL_SCORE",
                        match_id=p["id"],
                        attributes=match_attrs,
                    )
                    if p["status"] in ["IN_PLAY", "PAUSED"] and not durable:
                        notification_service.notify_match_event(p, "score", log_events)
                    changes_found = True
        else:
            changes_found = True
            kickoff_time = p.get("utc_date", "")[11:16] + " UTC" if len(p.get("utc_date", "")) >= 16 else "TBD"
            if p["status"] in ["TIMED", "SCHEDULED"]:
                append_event(
                    log_events,
                    f"[NEW FIXTURE] [{comp_name}] ({m_date}) Inserted upcoming fixture: {h_team} vs {a_team} (Kickoff: {kickoff_time})",
                    "INFO",
                    "MATCH_SYNC",
                    "NEW_MATCH",
                    match_id=p["id"],
                    attributes={
                        "competition": comp_name,
                        "home": h_team,
                        "away": a_team,
                        "utc_date": p.get("utc_date"),
                        "status": p.get("status"),
                        "round": p.get("matchday") or p.get("round") or "",
                    },
                )

    return MatchTransitionResult(
        changes_found=changes_found,
        newly_finished_delta=newly_finished_delta,
        finished_dispatches=finished_dispatches,
    )

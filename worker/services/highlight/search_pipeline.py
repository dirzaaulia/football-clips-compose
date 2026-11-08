from datetime import UTC, datetime
from typing import Any

from worker.services.highlight.candidate_finder import CandidateFinder
from worker.services.highlight.candidate_ranker import CandidateRanker
from worker.utils.telemetry_logger import append_event


class SearchPipeline:
    def __init__(self, quota_checker):
        self.finder = CandidateFinder(quota_checker)
        self.ranker = CandidateRanker()

    def search_highlight_for_match(
        self,
        match: dict[str, Any],
        log_events: list[dict[str, Any]] | None = None,
        claimed_video_ids: set[str] | None = None,
        allow_search_api: bool = False,
    ) -> tuple[str | None, dict[str, Any] | None, str | None, dict[str, Any] | None, list[dict[str, Any]]]:
        if log_events is None:
            log_events = []

        home = match["home_team_name"]
        away = match["away_team_name"]
        comp_id = (match.get("competition_id") or "").upper()
        m_id = match.get("id")
        m_date = str(match.get("utc_date", ""))[:10]
        status = match.get("status", "")

        comp_name = match.get("competition_name") or match.get("competition", {}).get("name") or comp_id
        utc_date = match.get("utc_date")

        # 1. Finished Status Guard
        if status and status not in ["FINISHED", "AWARDED"]:
            append_event(
                log_events,
                f"   [SKIP UNFINISHED] {home} vs {away} status is {status}. Only FINISHED matches are eligible.",
                "INFO",
                "YOUTUBE",
                "SKIP_UNFINISHED",
                match_id=m_id,
                attributes={
                    "competition_id": comp_id,
                    "competition_name": comp_name,
                    "utc_date": utc_date,
                    "home_team": home,
                    "away_team": away,
                },
            )
            return None, None, None, None, []

        # 2. UEFA European media embargo (2.5h)
        match_start = datetime.fromisoformat(match["utc_date"].replace("Z", "+00:00"))
        now_utc = datetime.now(UTC)
        match_age_h = (now_utc - match_start).total_seconds() / 3600.0
        if comp_id in ["CL", "EL", "ECL"] and match_age_h < 2.5:
            append_event(
                log_events,
                f"   [SKIP EMBARGO] {home} vs {away} finished {match_age_h:.1f}h ago (< 2.5h UEFA media embargo).",
                "INFO",
                "YOUTUBE",
                "UEFA_EMBARGO",
                match_id=m_id,
                attributes={
                    "competition_id": comp_id,
                    "competition_name": comp_name,
                    "utc_date": utc_date,
                    "home_team": home,
                    "away_team": away,
                },
            )
            return None, None, None, None, []

        # 3. Find candidates
        candidates, target_handles, query = self.finder.find_candidates(match, log_events, allow_search_api)
        if not target_handles:
            append_event(
                log_events,
                f"[SKIP] ({m_date}) No official YouTube handles mapped for: {home} vs {away}",
                "INFO",
                "YOUTUBE",
                "NO_HANDLE",
                match_id=m_id,
                attributes={
                    "step": "RESULT",
                    "status": "NO_HANDLE",
                    "reason": f"No official YouTube handles mapped for {home} vs {away}",
                    "match_title": f"{home} vs {away}",
                    "home_team": home,
                    "away_team": away,
                    "competition_id": comp_id,
                    "competition_name": comp_name,
                    "utc_date": utc_date,
                },
            )
            return None, None, None, None, []

        if not candidates:
            rejected_reasons = []
            for ev in log_events:
                ev_code = ev.get("code") if isinstance(ev, dict) else getattr(ev, "code", None)
                ev_mid = ev.get("match_id") if isinstance(ev, dict) else getattr(ev, "match_id", None)
                ev_attrs = (ev.get("attributes") if isinstance(ev, dict) else getattr(ev, "attributes", None)) or {}
                if ev_code == "CANDIDATE_REJECTED" and str(ev_mid) == str(m_id) and ev_attrs.get("reason"):
                    rejected_reasons.append(f"{ev_attrs.get('candidate_id')}: {ev_attrs.get('reason')}")
            if rejected_reasons:
                reason_msg = (
                    f"   [RESULT] {len(rejected_reasons)} preliminary candidate(s) rejected for {home} vs {away} — "
                    f"{'; '.join(rejected_reasons[:2])}"
                )
            else:
                reason_msg = (
                    f"   [RESULT] 0 matching highlight videos found across {len(target_handles)} official channel(s) "
                    f"({', '.join(target_handles[:4])}) for {home} vs {away}."
                )
            append_event(
                log_events,
                reason_msg,
                "INFO",
                "YOUTUBE",
                "RESULT_EMPTY",
                match_id=m_id,
                attributes={
                    "step": "RESULT",
                    "status": "NOT_INSERTED",
                    "inserted_to_db": False,
                    "match_title": f"{home} vs {away}",
                    "home_team": home,
                    "away_team": away,
                    "competition_id": comp_id,
                    "competition_name": comp_name,
                    "utc_date": utc_date,
                },
            )
            return None, None, None, None, []

        if claimed_video_ids:
            current_vid = match.get("highlight_video_id")
            candidates = [
                c for c in candidates
                if c.get("id") not in claimed_video_ids or (current_vid and c.get("id") == current_vid)
            ]
            if not candidates:
                append_event(
                    log_events,
                    f"   [ALL CLAIMED] All detected candidates ({home} vs {away}) are already linked to other matches.",
                    "INFO",
                    "YOUTUBE",
                    "CLAIMED",
                    match_id=m_id,
                    attributes={
                        "step": "RESULT",
                        "status": "NOT_INSERTED",
                        "inserted_to_db": False,
                        "match_title": f"{home} vs {away}",
                        "home_team": home,
                        "away_team": away,
                        "competition_id": comp_id,
                        "competition_name": comp_name,
                        "utc_date": utc_date,
                    },
                )
                return None, None, None, None, []

        # 4. Rank and select
        return self.ranker.rank_and_select(match, candidates, target_handles, log_events)

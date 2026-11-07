from datetime import datetime
from typing import Any

from loguru import logger

from worker.clients.ai_gateway_client import ai_gateway_client
from worker.services.highlight.filters import check_forbidden_title, has_highlight_keyword, has_scoreline
from worker.services.highlight.normalizer import get_team_aliases, matches_alias, normalize_for_alias_matching
from worker.services.highlight.scorer import fallback_score_video
from worker.utils.telemetry_logger import append_event


class CandidateRanker:
    @staticmethod
    def rank_and_select(
        match: dict[str, Any],
        candidates: list[dict[str, Any]],
        target_handles: list[str],
        log_events: list[dict[str, Any]],
    ) -> tuple[str | None, dict[str, Any] | None, str | None, dict[str, Any] | None, list[dict[str, Any]]]:
        home = match["home_team_name"]
        away = match["away_team_name"]
        comp = match.get("competition_name") or match.get("competition_id") or "League"
        m_id = match.get("id")

        h_aliases = get_team_aliases(home)
        a_aliases = get_team_aliases(away)

        embeddable = [c for c in candidates if c.get("is_embeddable", True)]
        selected_pool = embeddable if embeddable else candidates

        def is_both_teams(cand: dict[str, Any] | str) -> bool:
            if isinstance(cand, dict):
                cand_title = cand.get("title", "")
                cand_desc = cand.get("description", "")
                cand_chan = cand.get("channel", "")
            else:
                cand_title = str(cand)
                cand_desc = ""
                cand_chan = ""

            norm_cand = normalize_for_alias_matching(cand_title)
            norm_desc = normalize_for_alias_matching(cand_desc) if cand_desc else ""

            has_h = any(matches_alias(a, norm_cand) for a in h_aliases)
            has_a = any(matches_alias(a, norm_cand) for a in a_aliases)

            if has_h and has_a:
                return True

            is_club_channel = bool(cand_chan and target_handles and cand_chan in target_handles)
            if is_club_channel:
                has_desc_h = any(matches_alias(a, norm_desc) for a in h_aliases)
                has_desc_a = any(matches_alias(a, norm_desc) for a in a_aliases)
                if has_a or has_desc_a or has_h or has_desc_h:
                    return True

            return False

        def is_valid_highlight_candidate(cand: dict[str, Any] | str) -> bool:
            cand_title = cand.get("title", "") if isinstance(cand, dict) else str(cand)
            is_forbidden, _ = check_forbidden_title(cand_title)
            if is_forbidden:
                return False
            return is_both_teams(cand) and has_highlight_keyword(cand_title)

        h_score = match.get("home_score")
        a_score = match.get("away_score")
        match_utc_raw = match.get("utc_date")

        for c in selected_pool:
            c["heuristic_score"] = fallback_score_video(
                title=c["title"],
                duration_sec=c.get("duration", 0),
                home_team=home,
                away_team=away,
                h_aliases=h_aliases,
                a_aliases=a_aliases,
                geo_penalty=c.get("geo_penalty", 0),
                is_embeddable=c.get("is_embeddable", True),
                home_score=h_score,
                away_score=a_score,
                published_at=c.get("published_at"),
                match_utc=match_utc_raw,
                channel_handle=c.get("channel"),
                official_channel_targets=target_handles,
                description=c.get("description", ""),
            )

        def candidate_sort_key(c):
            embed_rank = 0 if c.get("is_embeddable", True) else 1
            watchability = c.get("watchability_score", 0.0)
            cat_rank = 0 if c.get("is_target_catered", True) else 1
            h_score = c.get("heuristic_score", 0)
            return (embed_rank, -watchability, cat_rank, -h_score)

        selected_pool.sort(key=candidate_sort_key)

        def find_backup_candidate(primary_id: str, pool: list[dict], primary_reg: dict | None = None) -> tuple[str | None, dict | None]:
            p_blocked = set((primary_reg or {}).get("blocked") or [])
            for cand in pool:
                c_id = cand.get("id")
                if c_id and c_id != primary_id and is_valid_highlight_candidate(cand):
                    c_reg = cand.get("region") or {}
                    c_allowed = set(c_reg.get("allowed") or [])
                    is_complementary = bool(p_blocked and bool(p_blocked.intersection(c_allowed)))
                    if is_complementary or cand.get("heuristic_score", 0) >= 20:
                        return c_id, cand.get("region")
            return None, None

        def build_ranked_videos(pool: list[dict]) -> list[dict]:
            videos = []
            for cand in pool:
                if is_valid_highlight_candidate(cand):
                    c_id = cand.get("id")
                    c_reg = cand.get("region") or {}
                    allowed = c_reg.get("allowed") or []
                    blocked = c_reg.get("blocked") or []
                    is_worldwide = (not allowed) and (len(blocked) <= 5)
                    videos.append({
                        "video_id": c_id,
                        "title": cand.get("title", ""),
                        "channel": cand.get("channel", ""),
                        "duration": cand.get("duration", 0),
                        "allowed_countries": allowed,
                        "blocked_countries": blocked,
                        "is_worldwide": is_worldwide,
                        "heuristic_score": cand.get("heuristic_score", 0),
                    })

            def sort_key(v):
                reach = (249 - len(v["blocked_countries"])) if not v["allowed_countries"] else len(v["allowed_countries"])
                ww_rank = 0 if v["is_worldwide"] else 1
                return (ww_rank, -reach, -v.get("heuristic_score", 0))

            videos.sort(key=sort_key)
            return videos

        # Mandatory AI Gateway Evaluation (Zero bypass: Every candidate pool goes to LLM)
        best_idx, ai_reason, evaluator_name = ai_gateway_client.rerank_highlights(home, away, comp, selected_pool)
        if best_idx is not None and 0 <= best_idx < len(selected_pool):
            best = selected_pool[best_idx]
            backup_vid, backup_reg = find_backup_candidate(best["id"], selected_pool, best.get("region"))
            for c_idx, cand in enumerate(selected_pool):
                verdict = "ACCEPTED" if c_idx == best_idx else "REJECTED"
                level = "SUCCESS" if verdict == "ACCEPTED" else "WARN"
                append_event(
                    log_events,
                    f"Candidate #{c_idx + 1} [{verdict}]: '{cand['title']}' ({cand['channel']}) -> https://youtu.be/{cand['id']} | Verdict: {ai_reason if verdict == 'ACCEPTED' else 'Not selected'}",
                    level,
                    "AI_GATEWAY",
                    f"CANDIDATE_{verdict}",
                    match_id=m_id,
                    attributes={"candidate_id": cand["id"], "verdict": verdict, "evaluator": evaluator_name, "score": cand.get("heuristic_score", 100), "url": f"https://youtu.be/{cand['id']}"},
                )
            append_event(
                log_events,
                f"   [AI ACCEPTED] AI Gateway selected candidate #{best_idx + 1}: '{best['title']}' ({best['channel']}) -> https://youtu.be/{best['id']}",
                "SUCCESS",
                "AI_GATEWAY",
                "RESULT_LINKED",
                match_id=m_id,
                attributes={"step": "RESULT", "status": "LINKED", "inserted_to_db": True, "video_id": best["id"], "confidence_score": best.get("heuristic_score", 100)},
            )
            return best["id"], best.get("region"), backup_vid, backup_reg, build_ranked_videos(selected_pool)

        if evaluator_name != "DETERMINISTIC_FALLBACK":
            # AI Gateway was online and evaluated the candidates, but no candidate was accepted
            append_event(
                log_events,
                f"AI Gateway explicitly rejected all candidates: {ai_reason or 'No candidate qualified as genuine match highlight'}",
                "WARN",
                "AI_GATEWAY",
                "RESULT_UNLINKED",
                match_id=m_id,
            )
            return None, None, None, None, []

        # 3. Emergency Circuit-Breaker (AI Gateway unreachable / network outage)
        append_event(
            log_events,
            "   [CIRCUIT BREAKER] AI Gateway offline/unavailable, evaluating candidates via hardened deterministic heuristic",
            "WARN",
            "YOUTUBE",
            "CIRCUIT_BREAKER",
            match_id=m_id,
        )
        valid_candidates = [c for c in selected_pool if c["heuristic_score"] > 0 and is_valid_highlight_candidate(c)]
        if valid_candidates:
            best = valid_candidates[0]
            backup_vid, backup_reg = find_backup_candidate(best["id"], selected_pool, best.get("region"))
            append_event(
                log_events,
                f"   [FALLBACK SELECT] Circuit-breaker heuristic selected #{best['heuristic_score']}pts from {best['channel']}: '{best['title']}' -> https://youtu.be/{best['id']}",
                "SUCCESS",
                "YOUTUBE",
                "RESULT_LINKED",
                match_id=m_id,
                attributes={"step": "RESULT", "status": "LINKED", "inserted_to_db": True, "video_id": best["id"], "confidence_score": best["heuristic_score"]},
            )
            return best["id"], best.get("region"), backup_vid, backup_reg, build_ranked_videos(selected_pool)

        append_event(log_events, f"   [VALIDATION FAILED] Candidates do not match both teams with verified highlight keywords.", "WARN", "YOUTUBE", "RESULT_UNLINKED", match_id=m_id)
        return None, None, None, None, []

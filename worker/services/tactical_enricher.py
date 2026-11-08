from loguru import logger
from worker.clients.espn_client import espn_client
from worker.utils.telemetry_logger import append_event


async def enrich_tactical_data(
    unique_payloads: list[dict],
    old_data_map: dict,
    log_events: list,
    provider: str = "espn",
    max_enrich_per_cycle: int = 5,
) -> int:
    """Detects live timeline events (goals, cards, substitutions) for telemetry logging.
    
    Tactical boxscore formations, pitch coordinates, and statistics are NOT persisted
    to the database; clients stream them on-demand directly from ESPN API.
    """
    if provider != "espn":
        return 0

    enriched_count = 0
    for p in unique_payloads:
        p.setdefault("season", 2026)
        is_live = p.get("status") in ["IN_PLAY", "PAUSED"]
        old = old_data_map.get(p["id"])
        is_newly_finished = old and old.get("status") != p.get("status") and p.get("status") in ["FINISHED", "AWARDED"]

        if is_live or is_newly_finished:
            try:
                summary = await espn_client.get_match_summary(p["id"], p.get("competition_id", "PL"))
                if summary and summary.get("events"):
                    p_comp = p.get("competition_name") or p.get("competition_id") or "League"
                    p_h_team = p.get("home_team_name") or "Home"
                    p_a_team = p.get("away_team_name") or "Away"
                    match_attrs = {
                        "competition": p_comp,
                        "home": p_h_team,
                        "away": p_a_team,
                        "status": p.get("status"),
                        "home_score": p.get("home_score"),
                        "away_score": p.get("away_score"),
                    }
                    for ev in (summary.get("events") or [])[-3:]:
                        ev_type = str(ev.get("type", "")).lower()
                        ev_text = ev.get("text") or ev.get("description") or ""
                        ev_clock = ev.get("clock", "")
                        clock_prefix = f"{ev_clock}' " if ev_clock else ""
                        ev_attrs = {**match_attrs, "display_clock": ev_clock}
                        if "goal" in ev_type:
                            append_event(
                                log_events,
                                f"[GOAL] [{p_comp}] {clock_prefix}{p_h_team} vs {p_a_team}: {ev_text}",
                                "SUCCESS",
                                "MATCH_SYNC",
                                "GOAL",
                                match_id=p["id"],
                                attributes=ev_attrs,
                            )
                        elif "red" in ev_type or "card" in ev_type:
                            append_event(
                                log_events,
                                f"[CARD] [{p_comp}] {clock_prefix}{p_h_team} vs {p_a_team}: {ev_text}",
                                "WARN",
                                "MATCH_SYNC",
                                "CARD",
                                match_id=p["id"],
                                attributes=ev_attrs,
                            )
                        elif "sub" in ev_type:
                            append_event(
                                log_events,
                                f"[SUBSTITUTION] [{p_comp}] {clock_prefix}{p_h_team} vs {p_a_team}: {ev_text}",
                                "INFO",
                                "MATCH_SYNC",
                                "SUB",
                                match_id=p["id"],
                                attributes=ev_attrs,
                            )
                    enriched_count += 1
            except Exception as ex:
                logger.warning(f"[TACTICAL DATA] Error processing live events for {p['id']}: {ex}")

    return enriched_count


async def enrich_editorial_articles(unique_payloads: list[dict], log_events: list):
    """No-op: Editorial articles are streamed on-demand by client endpoints to maintain lean database."""
    return

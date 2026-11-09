from datetime import UTC, datetime, timedelta
import json

from worker.services.highlight.filters import (
    get_worldwide_watchability_score,
    is_target_market_catered,
)

COMPETITION_PRIORITY: dict[str, int] = {
    "CL": 1, "EL": 2, "ECL": 3,
    "PL": 4, "PD": 5, "SA": 6, "BL1": 7, "FL1": 8,
    "FAC": 9, "CDR": 10, "CIT": 11, "DFB": 12, "EFL": 13,
    "DED": 14, "PPL": 15, "SPL": 16, "MLS": 17,
    "BRA": 18, "ARG": 19, "LIB": 20, "SUD": 21, "REC": 22, "IDN": 23,
}


def parse_iso_date(date_str: str | None) -> datetime:
    if not date_str:
        return datetime.now(UTC)
    try:
        cleaned = str(date_str).replace("Z", "+00:00")
        return datetime.fromisoformat(cleaned)
    except Exception:
        return datetime.now(UTC)


def should_check_youtube_for_match(match: dict, now_utc: datetime) -> tuple[bool, str]:
    """
    Tiered quota optimization guardrail:
    - Tier 1 (< 6 hours post-match): Check at most once every 30 minutes (fresh uploads).
    - Tier 2 (6 to 24 hours post-match): Check at most once every 2 hours.
    - Tier 3 (24 to 72 hours post-match): Check at most once every 6 hours.
    - Tier 4 (> 72 hours post-match): Check at most once every 24 hours.
    """
    last_check_str = match.get("last_youtube_check")
    if not last_check_str:
        return True, "Never checked"

    last_check = parse_iso_date(last_check_str)
    minutes_since_last_check = (now_utc - last_check).total_seconds() / 60.0

    match_utc_str = match.get("utc_date")
    match_time = parse_iso_date(match_utc_str) if match_utc_str else now_utc
    hours_since_match = (now_utc - match_time).total_seconds() / 3600.0

    if hours_since_match < 6:
        tier_name = "Tier 1 (<6h)"
        cooldown_min = 30
    elif hours_since_match < 24:
        tier_name = "Tier 2 (6-24h)"
        cooldown_min = 120
    elif hours_since_match < 72:
        tier_name = "Tier 3 (24-72h)"
        cooldown_min = 360
    else:
        tier_name = "Tier 4 (>72h)"
        cooldown_min = 1440

    if minutes_since_last_check >= cooldown_min:
        return True, f"{tier_name} expired ({minutes_since_last_check:.0f}m >= {cooldown_min}m)"
    remaining_min = cooldown_min - minutes_since_last_check
    next_check_dt = last_check + timedelta(minutes=cooldown_min)
    next_check_str = next_check_dt.strftime("%Y-%m-%d %H:%M UTC")
    return False, f"{tier_name} active ({remaining_min:.0f}m remaining of {cooldown_min}m; next check at {next_check_str})"


def get_next_check_eta(match: dict, now_utc: datetime) -> tuple[str, str]:
    """Returns (eta_relative_str, eta_timestamp_str). E.g. ('in 42m', '2026-11-08 23:15 UTC')"""
    last_check_str = match.get("last_youtube_check")
    if not last_check_str:
        return "immediate", "Immediate"

    last_check = parse_iso_date(last_check_str)
    minutes_since_last_check = (now_utc - last_check).total_seconds() / 60.0

    match_utc_str = match.get("utc_date")
    match_time = parse_iso_date(match_utc_str) if match_utc_str else now_utc
    hours_since_match = (now_utc - match_time).total_seconds() / 3600.0

    if hours_since_match < 6:
        cooldown_min = 30
    elif hours_since_match < 24:
        cooldown_min = 120
    elif hours_since_match < 72:
        cooldown_min = 360
    else:
        cooldown_min = 1440

    if minutes_since_last_check >= cooldown_min:
        return "due now", "Scheduled for next sweep"

    rem_min = int(cooldown_min - minutes_since_last_check)
    next_dt = last_check + timedelta(minutes=cooldown_min)
    if rem_min < 60:
        eta_rel = f"in {rem_min}m"
    else:
        eta_rel = f"in {rem_min // 60}h {rem_min % 60}m"
    eta_ts = next_dt.strftime("%Y-%m-%d %H:%M UTC")
    return eta_rel, eta_ts


def is_backlog_safe_exhausted(
    match: dict,
    now_utc: datetime,
    sweep_count: int | None = None,
) -> tuple[bool, str]:
    """
    Backlog-safe market exhaustion check:
    A match is only marked exhausted if:
    1. At least 3 complete sweeps across official channels have run (sweep_count >= 3).
    2. Kickoff date is >= 7 days ago (age_days >= 7.0).
    Protects backlog or quota-frozen matches from premature termination.
    """
    reg = match.get("youtube_region_info")
    if isinstance(reg, str):
        try:
            reg = json.loads(reg)
        except Exception:
            reg = {}
    elif not isinstance(reg, dict):
        reg = {}

    if reg.get("terminal") or reg.get("target_market_exhausted"):
        return True, "Already marked terminal"

    if sweep_count is None:
        sweep_count = reg.get("sweep_count", 0)

    match_utc_str = match.get("utc_date")
    match_time = parse_iso_date(match_utc_str) if match_utc_str else now_utc
    age_days = (now_utc - match_time).total_seconds() / 86400.0

    if sweep_count >= 3 and age_days >= 7.0:
        return True, f"Completed {sweep_count} sweeps across official channels and match finished {age_days:.1f} days ago"

    return False, f"Not exhausted (sweeps: {sweep_count}/3, age: {age_days:.1f}/7.0 days)"


def sort_unlinked_matches_by_priority(matches: list[dict], now_utc: datetime) -> list[dict]:
    """
    Sort matches with 4-tier priority:
    1. Priority Rank:
       - P1 (0): Urgent missing highlights (<24h)
       - P2 (1): High geo-deficit (highlight present, but key football traffic markets blocked)
       - P3 (2): Older missing highlights (>24h)
       - P4 (3): Low-priority upgrade (target markets covered, seeking 99% worldwide)
       - P5 (4): Satisfied / archive
    """
    def get_priority_key(m: dict):
        raw_date = m.get("utc_date")
        try:
            if raw_date:
                m_time = datetime.fromisoformat(str(raw_date).replace("Z", "+00:00"))
                age_hours = (now_utc - m_time).total_seconds() / 3600.0
                m_timestamp = m_time.timestamp()
            else:
                age_hours = 999999.0
                m_timestamp = 0.0
        except Exception:
            age_hours = 999999.0
            m_timestamp = 0.0

        has_vid = bool(m.get("highlight_video_id"))
        region = m.get("youtube_region_info")
        is_catered = is_target_market_catered(region)
        watchability = get_worldwide_watchability_score(region)

        if not has_vid:
            if age_hours < 24.0:
                tier_rank = 0  # P1: Urgent missing (<24h)
            else:
                tier_rank = 2  # P3: Older missing (>24h)
        else:
            if not is_catered:
                tier_rank = 1  # P2: High Geo Deficit (Key football markets blocked)
            elif watchability < 99.0:
                tier_rank = 3  # P4: Low-priority upgrade (target covered, seeking 99% worldwide)
            else:
                tier_rank = 4  # P5: Satisfied

        raw_last_check = m.get("last_youtube_check")
        never_checked = 0 if not raw_last_check else 1
        last_check_bucket = 0
        if raw_last_check:
            try:
                lc_dt = datetime.fromisoformat(str(raw_last_check).replace("Z", "+00:00"))
                last_check_bucket = int(lc_dt.timestamp() // 1800)
            except Exception:
                last_check_bucket = 0

        comp_id = (m.get("competition_id") or "").upper()
        comp_prio = COMPETITION_PRIORITY.get(comp_id, 99)
        return (tier_rank, never_checked, last_check_bucket, comp_prio, -m_timestamp)

    return sorted(matches, key=get_priority_key)

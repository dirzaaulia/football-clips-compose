import re
from datetime import datetime

from worker.services.highlight.filters import check_forbidden_title
from worker.services.highlight.normalizer import matches_alias, normalize_for_alias_matching


def fallback_score_video(
    title: str,
    duration_sec: int,
    home_team: str,
    away_team: str,
    h_aliases: list[str],
    a_aliases: list[str],
    geo_penalty: int = 0,
    is_embeddable: bool = True,
    home_score: int | None = None,
    away_score: int | None = None,
    published_at: str | datetime | None = None,
    match_utc: str | datetime | None = None,
    channel_handle: str | None = None,
    official_channel_targets: list[str] | None = None,
    description: str = "",
) -> int:
    """
    Hardened deterministic mathematical heuristic scorer (Tier 1 Autonomous Engine).
    Evaluates video quality, accuracy, upload proximity, channel authority, and scoreline consistency.
    Guarantees 100% autonomous operation even when Gemini AI is offline or rate-limited.
    """
    t = title.lower()
    score = 0

    # 0. Hard disqualifiers
    is_forbidden, _ = check_forbidden_title(title)
    if is_forbidden:
        return -100

    throwback_indicators = [
        "on this day", "throwback", "classic clash", "classic match", "retro",
        "memories", "history", "legend", "iconic goals", "rewind", "vintage",
        "flashback", "greatest goals", "best goals of", "all-time",
    ]
    if any(w in t for w in throwback_indicators):
        return -100

    forbidden_words = [
        "tous les buts", "all highlights", "inside", "press conference", "press conf",
        "interview", "interviews", "training session", "training", "previa", "preview",
        "reaction", "conferência", "conferencia", "imprensa", "prensa", "stampa",
        "entrevista", "declarações", "declaraciones", "rescaldo", "dopogara",
        "pressekonferenz", "youth", "u20", "u19", "u21", "u23", "u18", "u17",
        "primavera", "juvenil", "cantera", "junioren", "behind the scenes",
        "vlog", "fan cam", "watchalong", "tactical analysis",
    ]
    if any(w in t for w in forbidden_words):
        return -100

    # Past Year Filter
    match_year = None
    if match_utc:
        try:
            m_dt = match_utc if isinstance(match_utc, datetime) else datetime.fromisoformat(str(match_utc).replace("Z", "+00:00"))
            match_year = m_dt.year
        except Exception:
            pass

    if match_year:
        years_in_title = [int(y) for y in re.findall(r"\b(19\d\d|20\d\d)\b", t)]
        for y in years_in_title:
            if y < match_year - 1 or y > match_year + 1:
                return -100

    # 1. Official Target Channel Authority Boost
    if channel_handle and official_channel_targets:
        if channel_handle in official_channel_targets:
            score += 25
        elif any(target.lower() in channel_handle.lower() for target in official_channel_targets):
            score += 15

    # 2. Upload Proximity & Time Decay
    if published_at and match_utc:
        try:
            p_dt = published_at if isinstance(published_at, datetime) else datetime.fromisoformat(str(published_at).replace("Z", "+00:00"))
            m_dt = match_utc if isinstance(match_utc, datetime) else datetime.fromisoformat(str(match_utc).replace("Z", "+00:00"))
            diff_hours = (p_dt - m_dt).total_seconds() / 3600.0

            if diff_hours < 0.5:
                # Video published before or at kickoff
                score -= 50
            elif 0.5 <= diff_hours <= 36.0:
                # Golden window: uploaded within 36 hours post-match
                score += 20
            elif 36.0 < diff_hours <= 72.0:
                score += 5
            elif diff_hours > 120.0:  # > 5 days (7 days decay penalty)
                score -= 35
            elif diff_hours > 72.0:
                score -= 15
        except Exception:
            pass

    # 3. Team Alias Verification
    norm_title = normalize_for_alias_matching(title)
    norm_desc = normalize_for_alias_matching(description) if description else ""

    has_h = any(matches_alias(a, norm_title) for a in h_aliases)
    has_a = any(matches_alias(a, norm_title) for a in a_aliases)

    is_club_channel = bool(channel_handle and official_channel_targets and channel_handle in official_channel_targets)

    if is_club_channel and not (has_h and has_a):
        has_desc_h = any(matches_alias(a, norm_desc) for a in h_aliases)
        has_desc_a = any(matches_alias(a, norm_desc) for a in a_aliases)
        if has_a or has_desc_a:
            has_h = True
            has_a = True
        elif has_h or has_desc_h:
            has_h = True
            has_a = True

    if has_h and has_a:
        score += 35
        if is_club_channel:
            score += 15
    elif has_h or has_a:
        if is_club_channel:
            score += 15
        else:
            score -= 30
    else:
        # Neither team present in video title or description -> completely reject unrelated video
        return -100

    # 4. Scoreline Alignment
    score_matches = re.findall(r"\b(\d+)\s*[-–]\s*(\d+)\b", t)
    if home_score is not None and away_score is not None:
        if score_matches:
            is_exact = any(
                (int(s1) == home_score and int(s2) == away_score) or
                (int(s1) == away_score and int(s2) == home_score)
                for s1, s2 in score_matches
            )
            if is_exact:
                score += 25
            else:
                score -= 50
    elif score_matches:
        score += 5

    # 5. Keyword Quality
    if "extended" in t:
        score += 15
    elif any(w in t for w in ["highlight", "highlights", "resumen", "résumé", "sintesi", "zusammenfassung", "goles", "buts"]):
        score += 10

    # 6. Duration
    if 120 <= duration_sec <= 900:
        score += 10
    elif 90 <= duration_sec < 120:
        score += 5
    elif duration_sec < 90:
        score -= 20
    elif duration_sec > 1800:
        score -= 25

    # 7. Embeddability & Geoblocks
    if is_embeddable:
        score += 10
    else:
        score -= 10

    if geo_penalty == 0:
        score += 10
    elif geo_penalty >= 950:
        score -= 60
    elif geo_penalty >= 900:
        score -= 30
    elif geo_penalty > 0:
        score -= 10

    return score

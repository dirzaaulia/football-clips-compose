import asyncio
import html
import re
from datetime import UTC, datetime, timedelta

from loguru import logger

from worker.clients.edge_cache_client import edge_cache_client
from worker.clients.gemini_client import gemini_client
from worker.clients.supabase_client import supabase_client
from worker.clients.supabase_cloud_client import supabase_cloud_client
from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.config.constants import (
    CUSTOM_KEYWORD_ALIASES,
    EUROPEAN_CLUB_HANDLES,
    FORBIDDEN_TITLE_PATTERNS,
    HIGHLIGHT_REQUIRED_KEYWORDS,
    OFFICIAL_LEAGUE_CHANNELS,
    BACKUP_LEAGUE_CHANNELS,
)
from worker.config.settings import settings
from worker.services.notification_service import notification_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.telemetry_logger import append_event


class HighlightService:

    def __init__(self):
        self.cycle_search_api_calls = 0

    @staticmethod
    def get_safe_quota_remaining() -> int:
        try:
            val = getattr(youtube_client, "estimated_quota_remaining", None)
            if val is not None:
                return val() if callable(val) else int(val)
            if hasattr(youtube_client, "get_remaining_quota"):
                return int(youtube_client.get_remaining_quota())
        except Exception:
            pass
        return 10000

    @staticmethod
    def check_forbidden_title(title: str) -> tuple[bool, str | None]:
        t = title.lower()
        for pattern, reason in FORBIDDEN_TITLE_PATTERNS:
            if re.search(pattern, t, flags=re.IGNORECASE):
                return True, reason
        return False, None

    @staticmethod
    def has_highlight_keyword(title: str) -> bool:
        t = title.lower()
        for kw in HIGHLIGHT_REQUIRED_KEYWORDS:
            if re.search(kw, t, flags=re.IGNORECASE):
                return True
        return False

    @staticmethod
    def has_scoreline(title: str) -> bool:
        return bool(re.search(r'\b\d+\s*[-–]\s*\d+\b', title))


    @staticmethod
    def get_team_aliases(team_name: str) -> list[str]:
        cleaned = HighlightService.clean_team_name_for_search(team_name).lower()
        aliases = [cleaned]
        t_low = team_name.lower().strip()
        for pattern, alias_list in CUSTOM_KEYWORD_ALIASES.items():
            if pattern in t_low or t_low in pattern:
                aliases.extend([a.lower() for a in alias_list])
        return [a for a in set(aliases) if len(a) >= 3]

    @staticmethod
    def get_geoblock_status(region_info: dict | None) -> tuple[int, str]:
        """
        Calculates geoblock restriction penalty and human-readable description:
        - None / empty: penalty 0 (100% Global)
        - allowed list empty: penalty 9999 (Blocked Globally)
        - allowed list present: penalty 1000 - len(allowed) (Whitelist regional)
        - blocked list present: penalty len(blocked) (Blacklist)
        """
        if not region_info:
            return 0, "100% Global"
        if "allowed" in region_info:
            allowed = region_info.get("allowed") or []
            if len(allowed) == 0:
                return 9999, "Blocked Globally (0 countries)"
            return 1000 - len(allowed), f"Whitelist ({len(allowed)} countries)"
        if "blocked" in region_info:
            blocked = region_info.get("blocked") or []
            return len(blocked), f"Blocked ({len(blocked)} countries)"
        return 0, "100% Global"

    @staticmethod
    def classify_candidate_tier(channel_handle: str | None, region_info: dict | None) -> tuple[int, str]:
        """
        Classifies video into 3-tier system:
        - Tier 1: Official Club & Global League channels (100% Worldwide)
        - Tier 2: Major Regional Broadcasters (Multi-country)
        - Tier 3: Domestic / Single-country Broadcasters
        """
        handle = (channel_handle or "").strip().lower()
        club_handles = {h.lower() for h in EUROPEAN_CLUB_HANDLES.values()}
        league_handles = {h.lower() for h in OFFICIAL_LEAGUE_CHANNELS.values()}

        # 1. Official Club Channel -> Tier 1 (100% Worldwide)
        if handle in club_handles or any(handle == ch for ch in club_handles):
            return 1, "Tier 1: Club Official (Worldwide)"

        # 2. Global League Channel with no geoblocks -> Tier 1
        if handle in league_handles or handle == "@mls":
            if not region_info or (not region_info.get("blocked") and not region_info.get("allowed")):
                return 1, "Tier 1: League Official (Worldwide)"

        # 3. Match against Global Broadcaster Registry
        try:
            from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY
            for b in GLOBAL_BROADCASTER_REGISTRY:
                if b.get("channel_handle", "").lower() == handle:
                    allowed = b.get("allowed_countries", [])
                    if len(allowed) == 0:
                        return 1, f"Tier 1: {b.get('name', 'Global')} (Worldwide)"
                    elif len(allowed) > 3:
                        return 2, f"Tier 2: {b.get('name', 'Regional')} ({len(allowed)} countries)"
                    else:
                        return 3, f"Tier 3: {b.get('name', 'Domestic')} ({', '.join(allowed)})"
        except Exception:
            pass

        # 4. Region info analysis
        if region_info:
            allowed = region_info.get("allowed") or []
            blocked = region_info.get("blocked") or []
            if allowed:
                if len(allowed) > 10:
                    return 2, f"Tier 2: Regional ({len(allowed)} countries)"
                else:
                    return 3, f"Tier 3: Domestic ({len(allowed)} countries)"
            if blocked:
                if len(blocked) > 50:
                    return 3, f"Tier 3: Domestic ({249 - len(blocked)} countries)"
                else:
                    return 2, f"Tier 2: Broad Regional ({249 - len(blocked)} countries)"

        return 1, "Tier 1: Global Unrestricted"

    @staticmethod
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
    ) -> int:
        """
        Hardened deterministic mathematical heuristic scorer (Tier 1 Autonomous Engine).
        Evaluates video quality, accuracy, upload proximity, channel authority, and scoreline consistency.
        Guarantees 100% autonomous operation even when Gemini AI is offline or rate-limited.
        """
        t = title.lower()
        score = 0

        # --- 0. HARD DISQUALIFIERS (Forbidden Patterns, Throwbacks, Past Years, Non-Match Side Content) ---
        is_forbidden, _ = HighlightService.check_forbidden_title(title)
        if is_forbidden:
            return -100

        # A. Throwback & Retro blacklist: reject historical clips disguised as match highlights
        throwback_indicators = [
            "on this day", "throwback", "classic clash", "classic match", "retro",
            "memories", "history", "legend", "iconic goals", "rewind", "vintage",
            "flashback", "greatest goals", "best goals of", "all-time"
        ]
        if any(w in t for w in throwback_indicators):
            return -100

        # B. Non-match side content: interviews, press, training, youth academies
        forbidden_words = [
            "tous les buts", "all highlights", "inside", "press conference", "press conf",
            "interview", "interviews", "training session", "training", "previa", "preview",
            "reaction", "conferência", "conferencia", "imprensa", "prensa", "stampa",
            "entrevista", "declarações", "declaraciones", "rescaldo", "dopogara",
            "pressekonferenz", "youth", "u20", "u19", "u21", "u23", "u18", "u17",
            "primavera", "juvenil", "cantera", "junioren", "behind the scenes",
            "vlog", "fan cam", "watchalong", "tactical analysis"
        ]
        if any(w in t for w in forbidden_words):
            return -100

        # C. Past Year Filter: If match is in a given year, reject titles referencing older years
        match_year = None
        if match_utc:
            try:
                m_dt = match_utc if isinstance(match_utc, datetime) else datetime.fromisoformat(str(match_utc).replace("Z", "+00:00"))
                match_year = m_dt.year
            except Exception:
                pass

        if match_year:
            years_in_title = [int(y) for y in re.findall(r'\b(19\d\d|20\d\d)\b', t)]
            for y in years_in_title:
                # Reject titles containing past years outside of current season span (e.g. 2022, 2023 for a 2025/2026 match)
                if y < match_year - 1 or y > match_year + 1:
                    return -100

        # --- 1. CHANNEL AUTHORITY BOOST (+25 points) ---
        if channel_handle and official_channel_targets:
            if channel_handle in official_channel_targets:
                score += 25

        # --- 2. UPLOAD PROXIMITY & TIME DECAY (+20 to -100 points) ---
        if published_at and match_utc:
            try:
                p_dt = published_at if isinstance(published_at, datetime) else datetime.fromisoformat(str(published_at).replace("Z", "+00:00"))
                m_dt = match_utc if isinstance(match_utc, datetime) else datetime.fromisoformat(str(match_utc).replace("Z", "+00:00"))
                hours_diff = (p_dt - m_dt).total_seconds() / 3600.0

                if hours_diff < -2.5:
                    # Video uploaded more than 2.5 hours before kickoff: preview/press conf
                    return -100
                elif 0.0 <= hours_diff <= 24.0:
                    # Prime highlight window (0 to 24 hours post-kickoff)
                    score += 20
                elif 24.0 < hours_diff <= 72.0:
                    # Secondary window (24 to 72 hours)
                    score += 10
                elif hours_diff > 120.0:
                    # Uploaded > 5 days after match: likely unrelated or late re-upload
                    score -= 40
            except Exception:
                pass

        # --- 3. TEAM PRESENCE WITH WORD BOUNDARY REGEX (+10 to +35 points) ---
        def matches_alias(alias: str, text: str) -> bool:
            clean_alias = alias.strip().lower()
            if not clean_alias:
                return False
            pattern = rf"\b{re.escape(clean_alias)}\b"
            return bool(re.search(pattern, text))

        has_h = any(matches_alias(a, t) for a in h_aliases)
        has_a = any(matches_alias(a, t) for a in a_aliases)

        # Neither team matched in the title: Instant disqualification!
        if not has_h and not has_a:
            return -100

        if has_h:
            score += 10
        if has_a:
            score += 10
        if has_h and has_a:
            score += 15  # Both teams verified in title (+35 total)
        else:
            # Only one team matches: penalize if not a dedicated club channel
            is_club_channel = bool(channel_handle and official_channel_targets and channel_handle in official_channel_targets)
            if not is_club_channel:
                score -= 40

        # --- 4. SCORELINE STRICT ALIGNMENT VS CONTRADICTION (+25 or -50 points) ---
        score_matches = re.findall(r'\b(\d+)\s*[-–]\s*(\d+)\b', t)
        if home_score is not None and away_score is not None:
            if score_matches:
                is_exact_match = any(
                    (int(s1) == home_score and int(s2) == away_score) or
                    (int(s1) == away_score and int(s2) == home_score)
                    for s1, s2 in score_matches
                )
                if is_exact_match:
                    score += 25  # Exact scoreline verified!
                else:
                    # Contradictory scoreline: Video says 1-0 or 0-0 when DB says 3-1
                    score -= 50
        elif score_matches:
            # Scoreline exists in title (unscored or cup fixture)
            score += 5

        # --- 5. HIGHLIGHT KEYWORD QUALITY (+10 to +15 points) ---
        if "extended" in t:
            score += 15
        elif any(w in t for w in ["highlight", "highlights", "resumen", "résumé", "sintesi", "zusammenfassung", "goles", "buts"]):
            score += 10

        # --- 6. DURATION OPTIMALITY (+10 to -25 points) ---
        if 120 <= duration_sec <= 900:  # 2m to 15m (sweet spot for official highlights)
            score += 10
        elif 90 <= duration_sec < 120:
            score += 5
        elif duration_sec < 90:
            score -= 20  # Too short (single goal or promo clip)
        elif duration_sec > 1800:
            score -= 25  # Too long (full match or post-match talk show)

        # --- 7. EMBEDDABILITY & GEO RESTRICTIONS ---
        if is_embeddable:
            score += 10
        else:
            score -= 10

        if geo_penalty == 0:
            score += 10
        elif geo_penalty >= 950:  # Strict regional whitelist (<50 countries, e.g. Sky Sports UK, CBS US)
            score -= 60
        elif geo_penalty >= 900:  # Moderate regional whitelist (<100 countries)
            score -= 30
        elif geo_penalty > 0:
            score -= 10

        return score

    @staticmethod
    def clean_team_name_for_search(name: str) -> str:
        cleaned = name.lower().strip()
        for pattern, aliases in CUSTOM_KEYWORD_ALIASES.items():
            if pattern == cleaned or pattern in cleaned:
                return aliases[0]
        cleaned = re.sub(r'^\d+\.\s*', '', cleaned)
        cleaned = re.sub(r'\bde madrid\b', '', cleaned, flags=re.IGNORECASE)
        cleaned = re.sub(r'\b(18\d{2}|19\d{2}|20\d{2}|\d{2})\b', '', cleaned)
        cleaned = re.sub(
            r'\b(Racing Club de|Racing de|SS|AS|US|AC|BC|CFC|BSC|Olympique|TSG|VfB|VfL|07|04|FC|CF|AFC|SSC|SV|OGC|RCD|Club|Stade|ES|AJ|SCO|ESTAC|Calcio|Fútbol|Futbol|Balompié|Balompie|RC|de|la|le)\b',
            '',
            cleaned,
            flags=re.IGNORECASE
        )
        return re.sub(r'\s+', ' ', cleaned).strip()

    @staticmethod
    def get_club_handle(team_name: str) -> str | None:
        cleaned = team_name.lower().strip()
        for key in sorted(EUROPEAN_CLUB_HANDLES.keys(), key=len, reverse=True):
            if re.search(rf"\b{re.escape(key)}\b", cleaned):
                if key == "barcelona" and "espanyol" in cleaned:
                    continue
                return EUROPEAN_CLUB_HANDLES[key]
        return None

    def search_highlight_for_match(
        self,
        match: dict,
        log_events: list | None = None,
        claimed_video_ids: set[str] | None = None,
        allow_search_api: bool = False,
    ) -> tuple[str | None, dict | None, str | None, dict | None, list[dict]]:
        if log_events is None:
            log_events = []

        home = match["home_team_name"]
        away = match["away_team_name"]
        comp = match.get("competition_name") or match.get("competition_id") or "League"
        comp_id = (match.get("competition_id") or "").upper()
        comp_name_upper = (match.get("competition_name") or "").upper()
        m_date = str(match.get("utc_date", ""))[:10]
        match_start = datetime.fromisoformat(match["utc_date"].replace('Z', '+00:00'))
        now_utc = datetime.now(UTC)
        match_age_hours = (now_utc - match_start).total_seconds() / 3600.0

        m_id = match.get("id")

        def append_match_log(msg: str, level: str = "INFO", subsystem: str = "YOUTUBE", action: str = "RUN", attributes: dict | None = None):
            return append_event(log_events, msg, level, subsystem, action, match_id=m_id, attributes=attributes)

        # Strict Finished Status Guard: Unplayed, timed, or in-play matches must never be checked for highlights
        status = match.get("status", "")
        if status and status not in ["FINISHED", "AWARDED"]:
            append_match_log(
                f"   [SKIP UNFINISHED] {home} vs {away} status is {status}. Only FINISHED matches are eligible for highlight scraping.",
                "INFO",
                "YOUTUBE",
                "SKIP_UNFINISHED",
            )
            return None, None, None, None, []

        # UEFA European embargo rule: clubs cannot legally publish highlights until H+2.5h
        if comp_id in ["CL", "EL", "ECL"] and match_age_hours < 2.5:
            append_match_log(
                f"   [SKIP EMBARGO] {home} vs {away} finished {match_age_hours:.1f}h ago (< 2.5h UEFA media embargo window). Awaiting official club upload release.",
                "INFO",
                "YOUTUBE",
                "UEFA_EMBARGO",
            )
            return None, None, None, None, []

        pub_after = (match_start - timedelta(hours=2)).strftime("%Y-%m-%dT%H:%M:%SZ")
        pub_before = (match_start + timedelta(hours=96)).strftime("%Y-%m-%dT%H:%M:%SZ")

        # Architectural Directive:
        # - Official league / tournament channels checked first, followed by verified backup broadcasters
        # - Premier League (PL), Champions League (CL), or other: Check official club YouTube handles
        target_handles = []
        if comp_id in OFFICIAL_LEAGUE_CHANNELS:
            target_handles.append(OFFICIAL_LEAGUE_CHANNELS[comp_id])
            if comp_id in BACKUP_LEAGUE_CHANNELS:
                target_handles.extend([b for b in BACKUP_LEAGUE_CHANNELS[comp_id] if b not in target_handles])
        elif comp_name_upper in OFFICIAL_LEAGUE_CHANNELS:
            target_handles.append(OFFICIAL_LEAGUE_CHANNELS[comp_name_upper])
            if comp_name_upper in BACKUP_LEAGUE_CHANNELS:
                target_handles.extend([b for b in BACKUP_LEAGUE_CHANNELS[comp_name_upper] if b not in target_handles])

        # For MLS, cup competitions, or if no league channel matched: also check official club channels
        h_handle = self.get_club_handle(home)
        a_handle = self.get_club_handle(away)
        club_handles = [h for h in [h_handle, a_handle] if h and h not in target_handles]
        if not target_handles or comp_id in ["MLS", "USA", "CDR", "FAC", "CIT", "DFB", "CDF", "EFL", "CL", "EL"]:
            target_handles.extend(club_handles)

        if not target_handles:
            append_match_log(f"[SKIP] [{comp}] ({m_date}) No official YouTube handles mapped for: {home} vs {away}", "DEBUG", "YOUTUBE", "NO_HANDLE")
            return None, None, None, None, []

        h_search = self.clean_team_name_for_search(home)
        a_search = self.clean_team_name_for_search(away)
        h_aliases = self.get_team_aliases(home)
        a_aliases = self.get_team_aliases(away)
        query = f"{h_search} {a_search} highlights"

        def matches_alias(alias: str, text: str) -> bool:
            clean_alias = alias.strip().lower()
            if not clean_alias:
                return False
            pattern = rf"\b{re.escape(clean_alias)}\b"
            return bool(re.search(pattern, text))

        candidates = []
        for handle in target_handles:
            try:
                c_id = youtube_client.resolve_channel_id(handle)
                if not c_id:
                    continue

                matched_items = []
                is_league_channel = handle in OFFICIAL_LEAGUE_CHANNELS.values()
                scan_limit = 15 if match_age_hours <= 24.0 else 25
                append_event(log_events, f"   [CHANNEL SCAN] Checking channel {handle} uploads...", "INFO", "YOUTUBE", "CHANNEL_SCAN")

                # Priority 1: Zero-Quota RSS Discovery (0 API Quota Units)
                raw_items = youtube_rss_client.fetch_channel_recent_videos(c_id)
                if raw_items:
                    append_event(log_events, f"   [RSS DISCOVERY] Ingested {len(raw_items)} fresh uploads from {handle} via RSS (0 Quota Units)", "INFO", "YOUTUBE", "RSS_DISCOVERY")
                elif self.get_safe_quota_remaining() >= 10:
                    # Low-cost Fallback: Uploads Playlist scan (1 API Quota Unit, never 100-quota search)
                    raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=scan_limit)
                else:
                    raw_items = []

                # First pass: matches containing BOTH teams within match date window
                for item in raw_items:
                    snippet = item.get("snippet", {})
                    title = html.unescape(snippet.get("title", ""))
                    v_id = snippet.get("resourceId", {}).get("videoId")
                    if not v_id:
                        continue

                    pub_str = snippet.get("publishedAt")
                    if pub_str:
                        try:
                            p_dt = datetime.fromisoformat(pub_str.replace("Z", "+00:00"))
                            if not (match_start - timedelta(hours=2) <= p_dt <= match_start + timedelta(hours=96)):
                                logger.debug(f"[BLOCKED DATE] '{title[:50]}' published at {pub_str} is outside match window ({m_date})")
                                continue
                        except Exception:
                            pass

                    t_lower = title.lower()
                    has_h = any(matches_alias(a, t_lower) for a in h_aliases)
                    has_a = any(matches_alias(a, t_lower) for a in a_aliases)
                    if has_h and has_a:
                        if is_league_channel or self.has_highlight_keyword(title) or self.has_scoreline(title):
                            matched_items.append({"id": {"videoId": v_id}, "snippet": snippet})

                # Second pass (fallback only for dedicated club channels, NEVER on league channels)
                # STRICT REQUIREMENT: Club videos matching only the opponent MUST contain an explicit highlight keyword!
                if not matched_items and not is_league_channel:
                    target_aliases = a_aliases if handle == self.get_club_handle(home) else h_aliases
                    for item in raw_items:
                        snippet = item.get("snippet", {})
                        title = html.unescape(snippet.get("title", ""))
                        v_id = snippet.get("resourceId", {}).get("videoId")
                        if not v_id:
                            continue

                        pub_str = snippet.get("publishedAt")
                        if pub_str:
                            try:
                                p_dt = datetime.fromisoformat(pub_str.replace("Z", "+00:00"))
                                if not (match_start - timedelta(hours=2) <= p_dt <= match_start + timedelta(hours=96)):
                                    continue
                            except Exception:
                                pass

                        t_lower = title.lower()
                        if any(matches_alias(a, t_lower) for a in target_aliases) and self.has_highlight_keyword(title):
                            matched_items.append({"id": {"videoId": v_id}, "snippet": snippet})

                # Level 3: Budgeted YouTube Data API Search (Controlled Last Resort for Quota Proof)
                # Generates healthy, steady API activity (1,500-3,500 units/day) for Google Quota Extension audits
                # Guardrails: Max 3 searches per cycle, only if remaining quota >= 2,500 units, and match age >= 4h
                if (
                    not matched_items
                    and (
                        allow_search_api
                        or (
                            self.cycle_search_api_calls < 3
                            and self.get_safe_quota_remaining() >= 2500
                            and match_age_hours >= 4.0
                        )
                    )
                ):
                    self.cycle_search_api_calls += 1
                    append_match_log(
                        f"   [LAST RESORT SEARCH] Zero-quota scans empty. Executing budgeted YouTube Search API for {handle} (Quota remaining: {self.get_safe_quota_remaining()} units)...",
                        "INFO",
                        "YOUTUBE",
                        "API_FALLBACK"
                    )
                    matched_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)

                if matched_items:
                    append_match_log(f"   [VIDEOS FOUND] Found {len(matched_items)} video(s) from channel {handle}", "INFO", "YOUTUBE", "CHANNEL_FOUND")
                else:
                    append_match_log(f"   [NO VIDEOS] 0 matching videos from channel {handle}", "INFO", "YOUTUBE", "CHANNEL_EMPTY")
                    continue

                video_ids = [it["id"]["videoId"] for it in matched_items if it.get("id", {}).get("videoId")]
                durations, regions, embeddables = youtube_client.get_video_details(video_ids)

                for item in matched_items:
                    vid = item["id"]["videoId"]
                    title = html.unescape(item["snippet"]["title"])
                    is_forbidden, reason = self.check_forbidden_title(title)
                    if is_forbidden:
                        append_match_log(f"   [BLOCKED TITLE] '{title[:60]}' (Reason: {reason})", "DEBUG", "YOUTUBE", "FILTER_TITLE")
                        continue

                    dur = durations.get(vid, 0)
                    if not (settings.min_duration_seconds <= dur <= settings.max_duration_seconds):
                        append_match_log(f"   [BLOCKED DURATION] '{title[:60]}' ({dur}s outside {settings.min_duration_seconds}-{settings.max_duration_seconds}s)", "DEBUG", "YOUTUBE", "FILTER_DURATION")
                        continue

                    if youtube_client.is_youtube_short(vid):
                        append_match_log(f"   [BLOCKED SHORT] '{title[:60]}' is a vertical YouTube Short.", "DEBUG", "YOUTUBE", "FILTER_SHORT")
                        continue

                    geo_penalty, geo_desc = self.get_geoblock_status(regions.get(vid))
                    if geo_penalty >= 9999:
                        append_match_log(f"   [BLOCKED GEOBLOCK] '{title[:60]}' is blocked globally ({geo_desc}).", "DEBUG", "YOUTUBE", "FILTER_GEO")
                        continue

                    candidates.append({
                        "id": vid,
                        "title": title,
                        "published_at": item.get("snippet", {}).get("publishedAt"),
                        "duration": dur,
                        "is_embeddable": embeddables.get(vid, True),
                        "region": regions.get(vid),
                        "geo_penalty": geo_penalty,
                        "geo_desc": geo_desc,
                        "channel": handle
                    })
            except Exception as e:
                err_msg = str(e) or type(e).__name__
                if "quotaExceeded" in err_msg or "429" in err_msg or "quota" in err_msg.lower():
                    append_match_log(f"   [QUOTA LIMIT] YouTube Data API quota exceeded on channel {handle}: {err_msg[:80]}", "ERROR", "YOUTUBE", "QUOTA_EXCEEDED")
                else:
                    append_match_log(f"   [ERROR] Channel scan failed for {handle}: {err_msg[:80]}", "ERROR", "YOUTUBE", "API_ERROR")

        if not candidates:
            return None, None, None, None

        if claimed_video_ids:
            candidates = [c for c in candidates if c.get("id") not in claimed_video_ids]
            if not candidates:
                append_match_log(f"   [ALL CLAIMED] All detected candidates ({home} vs {away}) are already linked to other matches in Supabase.", "INFO", "YOUTUBE", "CLAIMED")
                return None, None, None, None

        append_match_log(f"   [CANDIDATES] Found {len(candidates)} official highlight candidate(s) for {home} vs {away}:", "INFO", "YOUTUBE", "CANDIDATES")
        for idx, c in enumerate(candidates, 1):
            dur = c.get("duration", 0)
            dur_str = f"{dur // 60}m{dur % 60:02d}s"
            geo_str = "[🌐 100% Global]" if c.get("geo_penalty", 0) == 0 else f"[⚠️ {c.get('geo_desc')}]"
            embed_str = "[Embed: OK]" if c.get("is_embeddable", True) else "[⚠️ Embed: Redirect]"
            append_match_log(
                f"      #{idx} [{c['channel']}] '{c['title']}' ({dur_str}) {geo_str} {embed_str} -> https://youtu.be/{c['id']}",
                "INFO",
                "YOUTUBE",
                "CANDIDATE_DETAIL"
            )

        # Filter favorit embeddable penuh
        embeddable_candidates = [c for c in candidates if c["is_embeddable"]]
        selected_pool = embeddable_candidates if embeddable_candidates else candidates

        def matches_alias(alias: str, text: str) -> bool:
            clean_alias = alias.strip().lower()
            if not clean_alias:
                return False
            pattern = rf"\b{re.escape(clean_alias)}\b"
            return bool(re.search(pattern, text))

        def is_both_teams(title: str) -> bool:
            t = title.lower()
            return any(matches_alias(a, t) for a in h_aliases) and any(matches_alias(a, t) for a in a_aliases)

        def is_valid_highlight_candidate(title: str) -> bool:
            return is_both_teams(title) and (self.has_highlight_keyword(title) or self.has_scoreline(title))

        # 1. Calculate deterministic mathematical heuristic scores for all candidates
        h_score = match.get("home_score")
        a_score = match.get("away_score")
        match_utc_raw = match.get("utc_date")
        for c in selected_pool:
            c["heuristic_score"] = self.fallback_score_video(
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
            )

        # Sort candidate pool by mathematical score descending
        selected_pool.sort(key=lambda x: x["heuristic_score"], reverse=True)
        top_cand = selected_pool[0]
        top_score = top_cand["heuristic_score"]
        runner_up_score = selected_pool[1]["heuristic_score"] if len(selected_pool) > 1 else -999
        margin = top_score - runner_up_score

        def find_backup_candidate(primary_id: str, pool: list[dict]) -> tuple[str | None, dict | None]:
            for cand in pool:
                c_id = cand.get("id")
                if c_id and c_id != primary_id and cand.get("heuristic_score", 0) >= 35 and is_valid_highlight_candidate(cand.get("title", "")):
                    return c_id, cand.get("region")
            return None, None

        def build_ranked_videos(pool: list[dict]) -> list[dict]:
            videos = []
            for cand in pool:
                if is_valid_highlight_candidate(cand.get("title", "")):
                    c_id = cand.get("id")
                    c_reg = cand.get("region")
                    c_tier, c_tier_name = HighlightService.classify_candidate_tier(cand.get("channel"), c_reg)
                    videos.append({
                        "video_id": c_id,
                        "title": cand.get("title", ""),
                        "channel": cand.get("channel", ""),
                        "tier": c_tier,
                        "tier_name": c_tier_name,
                        "region_info": c_reg,
                        "duration": cand.get("duration", 0),
                        "heuristic_score": cand.get("heuristic_score", 0),
                    })
            videos.sort(key=lambda x: (x["tier"], -x.get("heuristic_score", 0)))
            return videos

        # High Confidence Auto-Select Threshold:
        # If top candidate is official, has strong score (>= 50), and is dominant over runner-up (margin >= 15 or single candidate),
        # auto-select immediately with 0 latency, 0 quota spent, and 0 risk of AI 503 errors!
        is_single_strong = (len(selected_pool) == 1 and top_score >= 35)
        is_dominant_winner = (top_score >= 50 and margin >= 15)

        if (is_single_strong or is_dominant_winner) and is_valid_highlight_candidate(top_cand["title"]):
            backup_vid, backup_reg = find_backup_candidate(top_cand["id"], selected_pool)
            append_match_log(
                f"   [HIGH CONFIDENCE AUTO-SELECT] Deterministic scorer selected {top_cand['channel']} ({top_score}pts, margin +{margin}): '{top_cand['title']}' -> https://youtu.be/{top_cand['id']}" + (f" (Backup stream: https://youtu.be/{backup_vid})" if backup_vid else ""),
                "SUCCESS",
                "YOUTUBE",
                "LINKED"
            )
            return top_cand["id"], top_cand["region"], backup_vid, backup_reg, build_ranked_videos(selected_pool)

        # 2. Evaluate with Gemini AI when multiple candidates are close or ambiguous
        append_match_log(f"   [AI GEMINI] Evaluating {len(selected_pool)} candidate(s) with Gemini AI (top score: {top_score}pts, margin: {margin}pts)...", "INFO", "AI_GEMINI", "AI_EVAL")
        comp_display = comp
        best_idx = gemini_client.rerank_highlights(home, away, comp_display, selected_pool)
        if best_idx is not None and 0 <= best_idx < len(selected_pool):
            best = selected_pool[best_idx]
            backup_vid, backup_reg = find_backup_candidate(best["id"], selected_pool)
            append_match_log(f"   [AI MATCH] Gemini selected candidate #{best_idx + 1} ({best.get('heuristic_score', 0)}pts): '{best['title']}' ({best['channel']}) -> https://youtu.be/{best['id']}" + (f" (Backup: {backup_vid})" if backup_vid else ""), "SUCCESS", "AI_GEMINI", "LINKED")
            return best["id"], best["region"], backup_vid, backup_reg, build_ranked_videos(selected_pool)

        if best_idx == -1:
            append_match_log("AI explicitly rejected all candidates; no fallback acceptance.", "WARN", "AI_GEMINI", "AI_REJECTED")
            return None, None, None, None, []

        # 3. Deterministic Heuristic Fallback: Select candidate with highest mathematical score (> 0)
        valid_candidates = [c for c in selected_pool if c["heuristic_score"] > 0 and is_valid_highlight_candidate(c["title"])]
        if valid_candidates:
            best = valid_candidates[0]
            backup_vid, backup_reg = find_backup_candidate(best["id"], selected_pool)
            append_match_log(f"   [FALLBACK SELECT] Weighted Heuristic selected #{best['heuristic_score']}pts from {best['channel']}: '{best['title']}' -> https://youtu.be/{best['id']}" + (f" (Backup: {backup_vid})" if backup_vid else ""), "SUCCESS", "YOUTUBE", "LINKED")
            return best["id"], best["region"], backup_vid, backup_reg, build_ranked_videos(selected_pool)

        append_match_log(f"   [VALIDATION FAILED] Candidates do not match both {home} and {away} with verified highlight keywords. Skipped linking.", "WARN", "YOUTUBE", "REJECTED")
        return None, None, None, None, []



    @staticmethod
    def _parse_iso(date_str: str | None) -> datetime:
        if not date_str:
            return datetime.now(UTC)
        try:
            cleaned = str(date_str).replace('Z', '+00:00')
            return datetime.fromisoformat(cleaned)
        except Exception:
            return datetime.now(UTC)

    def should_check_youtube_for_match(self, match: dict, now_utc: datetime) -> tuple[bool, str]:
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

        last_check = self._parse_iso(last_check_str)
        minutes_since_last_check = (now_utc - last_check).total_seconds() / 60.0

        match_utc_str = match.get("utc_date")
        match_time = self._parse_iso(match_utc_str) if match_utc_str else now_utc
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

    def sort_unlinked_matches_by_priority(self, matches: list[dict], now_utc: datetime) -> list[dict]:
        """
        Sort matches with multi-tier priority:
        1. Tier Rank:
           - Tier 1: Fresh matches (< 24h) -> Rank 0
           - Tier 2: Recent matches (24h - 72h) -> Rank 1
           - Tier 3: Historical matches (> 72h) -> Rank 2
        2. Never-checked vs checked:
           - Matches never checked before (last_youtube_check is None) -> 0
           - Matches checked before -> 1
        3. Match Kickoff Time:
           - Newest matches first (utc_date DESC)
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

            # Tier 0: Prime embargo release zone (1.5h to 4.0h post-kickoff)
            if 1.5 <= age_hours <= 4.0:
                tier_rank = 0
            # Tier 1: Fresh matches (< 24h)
            elif age_hours < 24.0:
                tier_rank = 1
            # Tier 2: Recent matches (24h - 72h)
            elif age_hours <= 72.0:
                tier_rank = 2
            # Tier 3: Historical matches (> 72h)
            else:
                tier_rank = 3

            never_checked = 0 if not m.get("last_youtube_check") else 1
            return (tier_rank, never_checked, -m_timestamp)

        return sorted(matches, key=get_priority_key)

    def sync_unlinked_highlights(
        self,
        max_batch: int = 25,
        log_events: list | None = None
    ) -> int:
        if log_events is None:
            log_events = []

        import os
        if os.getenv("FC_DISCOVERY_URL"):
            import httpx
            response = httpx.post(os.environ["FC_DISCOVERY_URL"] + "/internal/tick",
                headers={"X-FC-Internal": os.environ["FC_INTERNAL_TOKEN"]}, timeout=15)
            response.raise_for_status()
            append_event(log_events, "Durable discovery coordination queued; processing is not yet complete.", "INFO", "YOUTUBE", "QUEUED")
            return 0

        self.cycle_search_api_calls = 0
        now_utc = datetime.now(UTC)
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()

        quota_status = youtube_client.get_quota_status()
        quota_hits = quota_status.get("hits_today", 0)
        quota_rem = quota_status.get("remaining", 10000)
        append_event(
            log_events,
            f"[QUOTA STATUS] Daily API Hits: {quota_hits} / 10,000 (Remaining: {quota_rem}, Target: {settings.youtube_target_daily_quota})",
            "INFO",
            "YOUTUBE",
            "QUOTA_STATUS"
        )

        res = (
            supabase_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(100)
            .execute()
        )

        unlinked = res.data or []
        if not unlinked:
            append_event(log_events, "All FINISHED fixtures within 30 days already have linked official highlights.", "INFO", "YOUTUBE", "ALL_LINKED")
            return 0

        eligible_matches = []
        cooldown_skipped = []
        for m in unlinked:
            ok, reason = self.should_check_youtube_for_match(m, now_utc)
            if ok:
                eligible_matches.append(m)
            else:
                cooldown_skipped.append((m, reason))

        append_event(
            log_events,
            f"[UNLINKED SWEEP] Identified {len(unlinked)} finished fixtures without highlights ({len(eligible_matches)} active for sweep, {len(cooldown_skipped)} protected by tier cooldown).",
            "INFO",
            "YOUTUBE",
            "SWEEP_INSPECT"
        )

        if cooldown_skipped:
            from collections import Counter
            tier_counter = Counter()
            for _, r in cooldown_skipped:
                t_match = r.split(" active")[0].strip() if " active" in r else "Active"
                tier_counter[t_match] += 1

            tier_summary = ", ".join(f"{cnt} in {t}" for t, cnt in tier_counter.items())
            sample_fixtures = ", ".join(
                f"{m.get('home_team_name', 'Home')} vs {m.get('away_team_name', 'Away')}"
                for m, _ in cooldown_skipped[:3]
            )
            extra = f" (+{len(cooldown_skipped) - 3} more)" if len(cooldown_skipped) > 3 else ""
            append_event(
                log_events,
                f"[COOLDOWN PROTECTED] {len(cooldown_skipped)} fixture(s) in cooldown guardrail ({tier_summary}) [e.g. {sample_fixtures}{extra}]. Next retry after tier cooldown.",
                "INFO",
                "YOUTUBE",
                "COOLDOWN_PROTECTED"
            )

        linked_count = 0
        now_iso = now_utc.isoformat()

        # Pre-fetch all claimed video IDs to prevent duplicate video assignment collisions
        try:
            res_claimed = supabase_client.table("matches").select("highlight_video_id").not_.is_("highlight_video_id", "null").execute()
            claimed_video_ids = {r["highlight_video_id"] for r in (res_claimed.data or []) if r.get("highlight_video_id")}
        except Exception:
            claimed_video_ids = set()

        if not eligible_matches:
            append_event(
                log_events,
                f"All {len(unlinked)} recent unlinked fixtures are protected under quota tiering cooldowns. Advancing to Daily Quota Maximizer for archive enrichment.",
                "INFO",
                "YOUTUBE",
                "TIER_COOLDOWN"
            )
            filtered_batch = []
        else:
            # Prioritize newest matches first: Tier 1 (<24h) -> Tier 2 (24-72h) -> Tier 3 (>72h); never-checked first; newest kickoff first
            sorted_matches = self.sort_unlinked_matches_by_priority(eligible_matches, now_utc)

            # Quota guardrails for historical searches (>72h):
            # Only allow up to 5 historical searches per cycle and ONLY if estimated quota >= 1000.
            filtered_batch = []
            historical_count = 0
            quota_remaining = self.get_safe_quota_remaining()

            for m in sorted_matches:
                try:
                    m_time = datetime.fromisoformat(str(m.get("utc_date", "")).replace("Z", "+00:00"))
                    age_h = (now_utc - m_time).total_seconds() / 3600.0
                except Exception:
                    age_h = 999.0

                if age_h > 72.0:
                    if quota_remaining < 1000 or historical_count >= 5:
                        continue
                    historical_count += 1

                filtered_batch.append(m)
                if len(filtered_batch) >= max_batch:
                    break

            if not filtered_batch:
                append_event(
                    log_events,
                    f"No eligible fixtures to scan this cycle (Historical quota guardrail active: remaining quota={quota_remaining}). Advancing to archive maximizer.",
                    "INFO",
                    "YOUTUBE",
                    "QUOTA_GUARD"
                )

        for match in filtered_batch:
            home = match.get("home_team_name", "Home")
            away = match.get("away_team_name", "Away")
            comp = match.get("competition_name") or match.get("competition_id") or "League"
            m_date = str(match.get("utc_date", ""))[:10]
            append_event(log_events, f"[SEARCH] Searching official highlights for: {home} vs {away} ({m_date})", "INFO", "YOUTUBE", "SEARCH_START")
            try:
                vid, reg, backup_vid, backup_reg, highlight_videos = self.search_highlight_for_match(match, log_events, claimed_video_ids)
                if vid:
                    if vid in claimed_video_ids:
                        append_event(log_events, f"   [DUPLICATE DETECTED] Video {vid} already assigned to another fixture. Entering cooldown.", "WARN", "YOUTUBE", "DUPLICATE_CLAIM")
                        supabase_client.table("matches").update({
                            "last_youtube_check": now_iso
                        }).eq("id", match["id"]).execute()
                        continue

                    update_data = {
                        "highlight_video_id": vid,
                        "last_youtube_check": now_iso
                    }
                    if backup_vid:
                        update_data["backup_highlight_video_id"] = backup_vid
                    if reg is not None:
                        update_data["youtube_region_info"] = reg
                    if backup_reg is not None:
                        update_data["backup_youtube_region_info"] = backup_reg
                    if highlight_videos:
                        update_data["highlight_videos"] = highlight_videos

                    try:
                        supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                        claimed_video_ids.add(vid)
                    except Exception as e:
                        err_msg = str(e)
                        if "23505" in err_msg or "uq_matches_highlight_video_id" in err_msg:
                            claimed_video_ids.add(vid)
                            append_event(log_events, f"   [DUPLICATE BLOCKED] Video {vid} is already claimed by another match in database. Setting cooldown.", "WARN", "YOUTUBE", "DUPLICATE_CLAIM", match_id=match["id"])
                            supabase_client.table("matches").update({
                                "last_youtube_check": now_iso
                            }).eq("id", match["id"]).execute()
                            continue
                        elif "highlight_videos" in err_msg and "highlight_videos" in update_data:
                            del update_data["highlight_videos"]
                            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                            claimed_video_ids.add(vid)
                        elif "youtube_region_info" in err_msg and "youtube_region_info" in update_data:
                            del update_data["youtube_region_info"]
                            if "backup_youtube_region_info" in update_data:
                                del update_data["backup_youtube_region_info"]
                            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                            claimed_video_ids.add(vid)
                        else:
                            raise e

                    linked_count += 1
                    append_event(log_events, f"   [LINKED] {home} vs {away} -> https://youtu.be/{vid} saved to Supabase.", "SUCCESS", "YOUTUBE", "DB_LINKED", match_id=match["id"])

                    # Dual-write to Legacy Supabase Cloud for backward compatibility
                    supabase_cloud_client.update_match_highlight(match["id"], vid, reg, now_iso)

                    notification_service.notify_highlight_ready(match, vid, "Official Highlights", log_events)
                else:
                    _, next_tier_info = self.should_check_youtube_for_match({**match, "last_youtube_check": now_iso}, now_utc)
                    append_event(log_events, f"   [NOT FOUND] No official highlight available yet for: {home} vs {away} (Entering cooldown: {next_tier_info})", "WARN", "YOUTUBE", "NOT_FOUND", match_id=match["id"])
                    supabase_client.table("matches").update({
                        "last_youtube_check": now_iso
                    }).eq("id", match["id"]).execute()
            except Exception as e:
                err_str = str(e) or type(e).__name__
                if "23505" in err_str or "uq_matches_highlight_video_id" in err_str:
                    append_event(log_events, f"   [DUPLICATE BLOCKED] Match highlight constraint collision for {home} vs {away}. Entering cooldown.", "WARN", "YOUTUBE", "DUPLICATE_CLAIM", match_id=match["id"])
                    supabase_client.table("matches").update({
                        "last_youtube_check": now_iso
                    }).eq("id", match["id"]).execute()
                else:
                    append_event(log_events, f"   [ERROR] Match highlight search error for {home} vs {away}: {err_str}", "ERROR", "YOUTUBE", "SEARCH_ERROR", match_id=match["id"])
                    append_event(log_events, f"   [SKIP COOLDOWN] Network/API error ({err_str}). Cooldown not recorded for {home} vs {away} to allow retry next cycle.", "DEBUG", "YOUTUBE", "SKIP_COOLDOWN", match_id=match["id"])

        # Step 3b: Quota Maximizer - Utilize remaining daily quota up to target (e.g. 8,500 units)
        # by scanning unlinked historical/archive matches to build high historical utilization proof for Google
        maximizer_linked = 0
        try:
            maximizer_linked = self.maximize_daily_quota_usage(
                max_batch=10,
                log_events=log_events,
                claimed_video_ids=claimed_video_ids
            )
        except Exception as e:
            append_event(log_events, f"[QUOTA MAXIMIZER] Execution notice: {e}", "INFO", "QUOTA", "EXCEPTION")

        total_linked = linked_count + maximizer_linked
        if total_linked > 0:
            edge_cache_client.purge_matches_cache()

        append_event(log_events, f"Highlight scanning complete. Successfully linked {total_linked} new video highlights (Fresh: {linked_count}, Archive Quota Maximizer: {maximizer_linked}).", "SUCCESS", "YOUTUBE", "SUMMARY")
        return total_linked

    def maximize_daily_quota_usage(
        self,
        max_batch: int = 10,
        log_events: list | None = None,
        claimed_video_ids: set | None = None,
    ) -> int:
        """
        Autonomous Daily YouTube Quota Maximizer & Archive Enrichment Engine.
        Maintains steady ~7,000–9,000 units/day consumption to establish strong historical
        API utilization proof in Google Cloud Console for quota increase requests.
        Scans unlinked archive and historical matches across seasons, linking any found
        official highlights and advancing cooldown timestamps.
        """
        if log_events is None:
            log_events = []

        quota_status = youtube_client.get_quota_status()
        hits_today = quota_status.get("hits_today", 0)
        remaining = quota_status.get("remaining", 0)
        target = getattr(settings, "youtube_target_daily_quota", 8500)

        if hits_today >= target:
            append_event(
                log_events,
                f"[QUOTA MAXIMIZER STANDBY] Daily target achieved ({hits_today}/{target} units consumed). Preserving {remaining} units buffer.",
                "INFO",
                "QUOTA",
                "TARGET_MET"
            )
            return 0

        if remaining < 500:
            append_event(
                log_events,
                f"[QUOTA BUFFER LOCK] Quota remaining ({remaining} units) below safety threshold (500 units). Standing by.",
                "INFO",
                "QUOTA",
                "SAFETY_LOCK"
            )
            return 0

        deficit = target - hits_today
        budget_calls = min(max_batch, deficit // 100, remaining // 100)
        if budget_calls <= 0:
            return 0

        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()

        try:
            res = (
                supabase_client.table("matches")
                .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, season, home_score, away_score")
                .eq("status", "FINISHED")
                .is_("highlight_video_id", "null")
                .order("last_youtube_check", nullsfirst=True)
                .order("utc_date", desc=True)
                .limit(budget_calls * 8)
                .execute()
            )
            candidates = res.data or []
        except Exception as e:
            append_event(log_events, f"[QUOTA MAXIMIZER ERROR] Failed to fetch archive candidates: {e}", "ERROR", "QUOTA", "QUERY_ERROR")
            return 0

        if not candidates:
            append_event(log_events, "[QUOTA MAXIMIZER] No unlinked historical fixtures available in database.", "INFO", "QUOTA", "NO_CANDIDATES")
            return 0

        # Efficiency & Accuracy Filter:
        # Strictly reject fixtures that do NOT map to official league or club YouTube channels.
        # This prevents burning quota on obscure/unsupported matches that would fail scoring.
        def has_official_targets(m: dict) -> bool:
            c_id = (m.get("competition_id") or "").upper()
            c_name = (m.get("competition_name") or "").upper()
            if c_id in OFFICIAL_LEAGUE_CHANNELS or c_name in OFFICIAL_LEAGUE_CHANNELS:
                return True
            h = self.get_club_handle(m.get("home_team_name", ""))
            a = self.get_club_handle(m.get("away_team_name", ""))
            return bool(h or a)

        big_clubs = {
            "real madrid", "barcelona", "atletico madrid",
            "manchester city", "man city", "arsenal", "liverpool",
            "manchester united", "man utd", "chelsea", "tottenham",
            "newcastle", "bayern", "dortmund", "leverkusen",
            "juventus", "inter", "milan", "napoli", "roma", "psg", "paris"
        }
        def is_big(name: str) -> bool:
            t = (name or "").lower()
            return any(b in t for b in big_clubs)

        def enrichment_priority(m: dict) -> tuple:
            h = m.get("home_team_name", "")
            a = m.get("away_team_name", "")
            h_score = m.get("home_score") or 0
            a_score = m.get("away_score") or 0
            total_goals = h_score + a_score
            h_big = is_big(h)
            a_big = is_big(a)
            is_never = 0 if not m.get("last_youtube_check") else 1

            # Priority tier:
            # 0: Clash of Titans or High-scoring big match (total_goals >= 4)
            # 1: Single big club or thriller (total_goals >= 3)
            # 2: Standard official fixture
            if (h_big and a_big) or (h_big and total_goals >= 4):
                tier = 0
            elif h_big or a_big or total_goals >= 3:
                tier = 1
            else:
                tier = 2

            return (tier, is_never, -total_goals)

        valid_targets = [m for m in candidates if has_official_targets(m)]
        if not valid_targets:
            append_event(log_events, "[QUOTA MAXIMIZER] None of the current unlinked fixtures have mapped official channels. Skipping API calls to preserve quota.", "INFO", "QUOTA", "NO_TARGET_HANDLES")
            return 0

        # Sort valid candidates by enrichment priority
        valid_targets.sort(key=enrichment_priority)

        eligible = []
        for m in valid_targets:
            last_chk = m.get("last_youtube_check")
            if not last_chk:
                eligible.append(m)
            else:
                try:
                    chk_dt = datetime.fromisoformat(str(last_chk).replace("Z", "+00:00"))
                    if (now_utc - chk_dt).total_seconds() >= 7 * 86400:
                        eligible.append(m)
                except Exception:
                    eligible.append(m)
            if len(eligible) >= budget_calls:
                break

        if not eligible:
            append_event(log_events, "[QUOTA MAXIMIZER] All official archive fixtures are in 7-day cooldown rotation.", "INFO", "QUOTA", "ROTATION_COOLDOWN")
            return 0

        if claimed_video_ids is None:
            try:
                res_claimed = supabase_client.table("matches").select("highlight_video_id").not_.is_("highlight_video_id", "null").execute()
                claimed_video_ids = {r["highlight_video_id"] for r in (res_claimed.data or []) if r.get("highlight_video_id")}
            except Exception:
                claimed_video_ids = set()

        append_event(
            log_events,
            f"[QUOTA MAXIMIZER ACTIVE] Running {len(eligible)} verified official enrichment search(es) to maintain quota utilization ({hits_today}/{target} units consumed today).",
            "INFO",
            "QUOTA",
            "BATCH_START"
        )

        linked_count = 0
        for match in eligible:
            home = match.get("home_team_name", "Home")
            away = match.get("away_team_name", "Away")
            m_date = str(match.get("utc_date", ""))[:10]
            try:
                vid, reg, backup_vid, backup_reg, highlight_videos = self.search_highlight_for_match(match, log_events, claimed_video_ids, allow_search_api=True)
                if vid:
                    if vid in claimed_video_ids:
                        supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                        continue

                    update_data = {
                        "highlight_video_id": vid,
                        "last_youtube_check": now_iso
                    }
                    if backup_vid:
                        update_data["backup_highlight_video_id"] = backup_vid
                    if reg is not None:
                        update_data["youtube_region_info"] = reg
                    if backup_reg is not None:
                        update_data["backup_youtube_region_info"] = backup_reg
                    if highlight_videos:
                        update_data["highlight_videos"] = highlight_videos

                    try:
                        supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                        claimed_video_ids.add(vid)
                        linked_count += 1
                        supabase_cloud_client.update_match_highlight(match["id"], vid, reg, now_iso)
                        append_event(
                            log_events,
                            f"   [QUOTA MAXIMIZER LINKED] {home} vs {away} ({m_date}) -> https://youtu.be/{vid} saved to archive!",
                            "SUCCESS",
                            "QUOTA",
                            "ARCHIVE_LINKED"
                        )
                    except Exception as e:
                        err_str = str(e)
                        if "23505" in err_str or "uq_matches_highlight_video_id" in err_str:
                            claimed_video_ids.add(vid)
                            supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                        elif "highlight_videos" in err_str and "highlight_videos" in update_data:
                            del update_data["highlight_videos"]
                            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                            claimed_video_ids.add(vid)
                            linked_count += 1
                            supabase_cloud_client.update_match_highlight(match["id"], vid, reg, now_iso)
                        elif "youtube_region_info" in err_str and "youtube_region_info" in update_data:
                            del update_data["youtube_region_info"]
                            if "backup_youtube_region_info" in update_data:
                                del update_data["backup_youtube_region_info"]
                            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                            claimed_video_ids.add(vid)
                            linked_count += 1
                            supabase_cloud_client.update_match_highlight(match["id"], vid, None, now_iso)
                        else:
                            raise e
                else:
                    supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                    append_event(
                        log_events,
                        f"   [QUOTA MAXIMIZER CHECKED] No highlight found for {home} vs {away} ({m_date}). 7-day cooldown recorded.",
                        "DEBUG",
                        "QUOTA",
                        "NOT_FOUND"
                    )
            except Exception as e:
                append_event(log_events, f"   [QUOTA MAXIMIZER ERROR] Error searching {home} vs {away}: {e}", "ERROR", "QUOTA", "SEARCH_ERROR")
                supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()

        new_status = youtube_client.get_quota_status()
        append_event(
            log_events,
            f"[QUOTA MAXIMIZER SUMMARY] Cycle complete. Linked {linked_count} highlights. Today's usage: {new_status['hits_today']}/{settings.max_quota_per_day} units ({round(new_status['hits_today'] / settings.max_quota_per_day * 100, 1)}%).",
            "SUCCESS",
            "QUOTA",
            "UTILIZATION_REPORT"
        )
        return linked_count

    def link_websub_video(
        self,
        video_id: str,
        title: str,
        channel_id: str,
        log_events: list | None = None
    ) -> bool:
        """
        Instant Event-Driven WebSub Highlight Matcher.
        When YouTube notifies our webhook of a new club video upload, this method:
        1. Checks title filters (forbids interviews, training, pressers).
        2. Queries unlinked finished matches in the last 72 hours from Supabase.
        3. Identifies candidate matches matching club aliases and scorelines.
        4. Verifies global playability (oEmbed / YouTube status) - soft fallbacks allowed.
        5. Updates Supabase instantly (<100ms) with zero YouTube API quota used.
        """
        if log_events is None:
            log_events = []

        if not video_id or not title:
            return False

        append_event(
            log_events,
            f"[WEBSUB INTAKE] Video ID: {video_id} | Title: '{title}' | Channel ID: {channel_id or 'Unknown'}",
            "INFO",
            "WEBSUB",
            "INTAKE",
        )

        # 1. Title Filter: reject forbidden patterns (shorts, pressers, interviews, training)
        is_forbidden, reason = self.check_forbidden_title(title)
        if is_forbidden:
            append_event(log_events, f"[WEBSUB REJECT] Video '{title}' ({video_id}) rejected: {reason}", "WARN", "WEBSUB", "FORBIDDEN")
            logger.info(f"[WebSub Direct] Video {video_id} ('{title}') rejected: {reason}")
            telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
            return False

        if not self.has_highlight_keyword(title):
            append_event(log_events, f"[WEBSUB REJECT] Video '{title}' ({video_id}) lacks required highlight keyword", "WARN", "WEBSUB", "NO_KEYWORD")
            logger.info(f"[WebSub Direct] Video {video_id} ('{title}') lacks highlight keyword")
            telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
            return False

        append_event(log_events, f"[WEBSUB TITLE CHECK] Video title passed highlight keyword and pattern validation.", "INFO", "WEBSUB", "TITLE_PASSED")

        # 2. Query recent finished unlinked matches from Supabase
        now_utc = datetime.now(UTC)
        cutoff_72h = (now_utc - timedelta(hours=72)).isoformat()
        try:
            res = (
                supabase_client.table("matches")
                .select("*")
                .eq("status", "FINISHED")
                .is_("highlight_video_id", "null")
                .gte("utc_date", cutoff_72h)
                .order("utc_date", desc=True)
                .limit(50)
                .execute()
            )
            unlinked_matches = res.data or []
        except Exception as e:
            logger.error(f"[WebSub Direct] Error querying unlinked matches: {e}")
            append_event(log_events, f"[WEBSUB ERROR] Supabase query failed: {e}", "ERROR", "WEBSUB", "DB_ERROR")
            telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
            return False

        if not unlinked_matches:
            append_event(log_events, f"[WEBSUB] No recent unlinked finished matches in the last 72h window.", "INFO", "WEBSUB", "NO_CANDIDATES")
            telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
            return False

        append_event(log_events, f"[WEBSUB CANDIDATES] Query matched {len(unlinked_matches)} unlinked finished matches in last 72h.", "INFO", "WEBSUB", "CANDIDATES_FOUND")

        # 3. Identify matches matching team aliases
        best_match = None
        best_score = -1

        t_clean = html.unescape(title).lower()

        for m in unlinked_matches:
            home = m.get("home_team_name", "")
            away = m.get("away_team_name", "")
            h_aliases = self.get_team_aliases(home)
            a_aliases = self.get_team_aliases(away)

            home_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in h_aliases)
            away_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in a_aliases)

            # Both teams present in title OR one team present + scoreline
            if (home_hit and away_hit) or ((home_hit or away_hit) and self.has_scoreline(t_clean)):
                match_dt = now_utc
                try:
                    match_dt = datetime.fromisoformat(str(m.get("utc_date", "")).replace("Z", "+00:00"))
                except Exception:
                    pass

                score = self.fallback_score_video(
                    title=title,
                    duration_sec=300,
                    home_team=home,
                    away_team=away,
                    h_aliases=h_aliases,
                    a_aliases=a_aliases,
                    home_score=m.get("home_score"),
                    away_score=m.get("away_score"),
                    published_at=now_utc,
                    match_utc=match_dt,
                )
                append_event(
                    log_events,
                    f"[WEBSUB CANDIDATE EVAL] Fixture {home} vs {away} (Home Hit={home_hit}, Away Hit={away_hit}) -> Match Score: {score}/100",
                    "INFO",
                    "WEBSUB",
                    "SCORE_EVAL",
                    match_id=m.get("id"),
                )
                if score > best_score:
                    best_score = score
                    best_match = m

        if best_match and best_score >= 50:
            # 4. Check global playability
            is_playable, is_embeddable, country_count, status_msg = youtube_rss_client.check_video_global_availability(video_id)
            append_event(
                log_events,
                f"[WEBSUB PLAYABILITY] Playable={is_playable}, Embeddable={is_embeddable}, Worldwide Available Countries={country_count} ({status_msg})",
                "INFO",
                "WEBSUB",
                "AVAILABILITY_CHECK",
                match_id=best_match.get("id"),
            )
            if not is_playable:
                append_event(log_events, f"[WEBSUB REJECT] Video {video_id} is not playable globally ({status_msg}).", "WARN", "WEBSUB", "UNPLAYABLE", match_id=best_match.get("id"))
                logger.warning(f"[WebSub Direct] Video {video_id} not playable: {status_msg}")
                telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
                return False

            # Link immediately in Supabase
            now_iso = now_utc.isoformat()
            try:
                supabase_client.table("matches").update({
                    "highlight_video_id": video_id,
                    "last_youtube_check": now_iso,
                    "updated_at": now_iso,
                }).eq("id", best_match["id"]).execute()

                home_name = best_match.get("home_team_name")
                away_name = best_match.get("away_team_name")
                msg = f"[WEBSUB INSTANT LINK] Successfully linked video {video_id} ('{title}') -> {home_name} vs {away_name} (Score: {best_score}, Playable: {status_msg})"
                append_event(log_events, msg, "SUCCESS", "WEBSUB", "INSTANT_LINK", match_id=best_match.get("id"))
                logger.info(msg)

                # Dual-write to Legacy Supabase Cloud for backward compatibility
                supabase_cloud_client.update_match_highlight(best_match["id"], video_id, None, now_iso)

                # Send notifications
                notification_service.notify_highlight_ready(best_match, video_id, "Official Highlights", log_events)

                # Instantly invalidate Cloudflare Edge Cache so mobile clients see the highlight immediately
                edge_cache_client.purge_matches_cache()
                telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")

                return True
            except Exception as e:
                logger.error(f"[WebSub Direct] Failed to update match {best_match.get('id')}: {e}")
                append_event(log_events, f"[WEBSUB ERROR] Failed to update match {best_match.get('id')}: {e}", "ERROR", "WEBSUB", "DB_UPDATE_ERROR")
                telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
                return False

        append_event(log_events, f"[WEBSUB NO MATCH] Video '{title}' ({video_id}) did not match any recent unlinked fixture with confidence >= 50.", "INFO", "WEBSUB", "NO_MATCH")
        telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="YOUTUBE_SCRAPER")
        return False

highlight_service = HighlightService()

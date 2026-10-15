import html
import re
from datetime import UTC, datetime, timedelta

from loguru import logger

from worker.clients.gemini_client import gemini_client
from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.config.constants import (
    CUSTOM_KEYWORD_ALIASES,
    EUROPEAN_CLUB_HANDLES,
    FORBIDDEN_TITLE_PATTERNS,
    HIGHLIGHT_REQUIRED_KEYWORDS,
    OFFICIAL_LEAGUE_CHANNELS,
)
from worker.config.settings import settings
from worker.services.notification_service import notification_service
from worker.utils.telemetry_logger import append_event


class HighlightService:

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

        # --- 0. HARD DISQUALIFIERS (Throwbacks, Past Years, Non-Match Side Content) ---
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
            score += 5
        elif geo_penalty >= 900:
            score -= 25

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
        log_events: list | None = None
    ) -> tuple[str | None, dict | None]:
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

        # UEFA Champions League & Europa League embargo rule: clubs cannot legally publish highlights until H+2.5h
        if comp_id in ["CL", "EL"] and match_age_hours < 2.5:
            append_event(
                log_events,
                f"   [SKIP EMBARGO] {home} vs {away} finished {match_age_hours:.1f}h ago (< 2.5h UEFA media embargo window). Awaiting official club upload release.",
                "INFO",
                "YOUTUBE",
                "UEFA_EMBARGO"
            )
            return None, None

        pub_after = (match_start - timedelta(hours=2)).strftime("%Y-%m-%dT%H:%M:%SZ")
        pub_before = (match_start + timedelta(hours=96)).strftime("%Y-%m-%dT%H:%M:%SZ")

        # Architectural Directive:
        # - LaLiga (PD), Bundesliga (BL1), Ligue 1 (FL1), Serie A (SA): Check official league YouTube channels
        # - Premier League (PL), Champions League (CL), or other: Check official club YouTube handles
        if comp_id in OFFICIAL_LEAGUE_CHANNELS:
            target_handles = [OFFICIAL_LEAGUE_CHANNELS[comp_id]]
        elif comp_name_upper in OFFICIAL_LEAGUE_CHANNELS:
            target_handles = [OFFICIAL_LEAGUE_CHANNELS[comp_name_upper]]
        else:
            h_handle = self.get_club_handle(home)
            a_handle = self.get_club_handle(away)
            target_handles = [h for h in [h_handle, a_handle] if h]

        if not target_handles:
            append_event(log_events, f"[SKIP] [{comp}] ({m_date}) No official YouTube handles mapped for: {home} vs {away}", "DEBUG", "YOUTUBE", "NO_HANDLE")
            return None, None

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
                is_historical = match_age_hours > 72.0

                if is_historical:
                    append_event(log_events, f"   [HISTORICAL SEARCH] Match age {match_age_hours / 24:.1f}d (>3d). Bypassing playlist scan directly to date-bounded Search API ({m_date}) for {handle}...", "INFO", "YOUTUBE", "HISTORICAL_SEARCH")
                    matched_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)
                else:
                    scan_limit = 15 if match_age_hours <= 24.0 else 25
                    append_event(log_events, f"   [CHANNEL SCAN] Checking channel {handle} uploads...", "INFO", "YOUTUBE", "CHANNEL_SCAN")
                    # Priority 1: Zero-Quota RSS Discovery (0 API Quota Units)
                    raw_items = youtube_rss_client.fetch_channel_recent_videos(c_id)
                    if raw_items:
                        append_event(log_events, f"   [RSS DISCOVERY] Ingested {len(raw_items)} fresh uploads from {handle} via RSS (0 Quota Units)", "INFO", "YOUTUBE", "RSS_DISCOVERY")
                    else:
                        # Fallback to YouTube Data API playlist scan if RSS is unavailable
                        raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=scan_limit)

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

                    # Fallback to Search API only if playlist scan returned nothing, match is at least 12h old, and quota is sufficient
                    if not matched_items and match_age_hours >= 12.0 and youtube_client.estimated_quota_remaining >= 500:
                        append_event(log_events, f"   [SEARCH FALLBACK] Playlist scan empty and match age {match_age_hours:.1f}h >= 12h. Querying Search API for {handle}...", "INFO", "YOUTUBE", "SEARCH_FALLBACK")
                        matched_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)

                if matched_items:
                    append_event(log_events, f"   [VIDEOS FOUND] Found {len(matched_items)} video(s) from channel {handle}", "INFO", "YOUTUBE", "CHANNEL_FOUND")
                else:
                    append_event(log_events, f"   [NO VIDEOS] 0 matching videos from channel {handle}", "INFO", "YOUTUBE", "CHANNEL_EMPTY")
                    continue

                video_ids = [it["id"]["videoId"] for it in matched_items if it.get("id", {}).get("videoId")]
                durations, regions, embeddables = youtube_client.get_video_details(video_ids)

                for item in matched_items:
                    vid = item["id"]["videoId"]
                    title = html.unescape(item["snippet"]["title"])
                    is_forbidden, reason = self.check_forbidden_title(title)
                    if is_forbidden:
                        append_event(log_events, f"   [BLOCKED TITLE] '{title[:60]}' (Reason: {reason})", "DEBUG", "YOUTUBE", "FILTER_TITLE")
                        continue

                    dur = durations.get(vid, 0)
                    if not (settings.min_duration_seconds <= dur <= settings.max_duration_seconds):
                        append_event(log_events, f"   [BLOCKED DURATION] '{title[:60]}' ({dur}s outside {settings.min_duration_seconds}-{settings.max_duration_seconds}s)", "DEBUG", "YOUTUBE", "FILTER_DURATION")
                        continue

                    if youtube_client.is_youtube_short(vid):
                        append_event(log_events, f"   [BLOCKED SHORT] '{title[:60]}' is a vertical YouTube Short.", "DEBUG", "YOUTUBE", "FILTER_SHORT")
                        continue

                    geo_penalty, geo_desc = self.get_geoblock_status(regions.get(vid))
                    if geo_penalty >= 9999:
                        append_event(log_events, f"   [BLOCKED GEOBLOCK] '{title[:60]}' is blocked globally ({geo_desc}).", "DEBUG", "YOUTUBE", "FILTER_GEO")
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
                    append_event(log_events, f"   [QUOTA LIMIT] YouTube Data API quota exceeded on channel {handle}: {err_msg[:80]}", "ERROR", "YOUTUBE", "QUOTA_EXCEEDED")
                else:
                    append_event(log_events, f"   [ERROR] Channel scan failed for {handle}: {err_msg[:80]}", "ERROR", "YOUTUBE", "API_ERROR")

        if not candidates:
            return None, None

        append_event(log_events, f"   [CANDIDATES] Found {len(candidates)} official highlight candidate(s) for {home} vs {away}:", "INFO", "YOUTUBE", "CANDIDATES")
        for idx, c in enumerate(candidates, 1):
            dur = c.get("duration", 0)
            dur_str = f"{dur // 60}m{dur % 60:02d}s"
            geo_str = "[🌐 100% Global]" if c.get("geo_penalty", 0) == 0 else f"[⚠️ {c.get('geo_desc')}]"
            embed_str = "[Embed: OK]" if c.get("is_embeddable", True) else "[⚠️ Embed: Redirect]"
            append_event(
                log_events,
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

        # High Confidence Auto-Select Threshold:
        # If top candidate is official, has strong score (>= 50), and is dominant over runner-up (margin >= 15 or single candidate),
        # auto-select immediately with 0 latency, 0 quota spent, and 0 risk of AI 503 errors!
        is_single_strong = (len(selected_pool) == 1 and top_score >= 35)
        is_dominant_winner = (top_score >= 50 and margin >= 15)

        if (is_single_strong or is_dominant_winner) and is_valid_highlight_candidate(top_cand["title"]):
            append_event(
                log_events,
                f"   [HIGH CONFIDENCE AUTO-SELECT] Deterministic scorer selected {top_cand['channel']} ({top_score}pts, margin +{margin}): '{top_cand['title']}' -> https://youtu.be/{top_cand['id']}",
                "SUCCESS",
                "YOUTUBE",
                "LINKED"
            )
            return top_cand["id"], top_cand["region"]

        # 2. Evaluate with Gemini AI when multiple candidates are close or ambiguous
        append_event(log_events, f"   [AI GEMINI] Evaluating {len(selected_pool)} candidate(s) with Gemini AI (top score: {top_score}pts, margin: {margin}pts)...", "INFO", "AI_GEMINI", "AI_EVAL")
        comp_display = comp
        best_idx = gemini_client.rerank_highlights(home, away, comp_display, selected_pool)
        if best_idx is not None and 0 <= best_idx < len(selected_pool):
            best = selected_pool[best_idx]
            append_event(log_events, f"   [AI MATCH] Gemini selected candidate #{best_idx + 1} ({best.get('heuristic_score', 0)}pts): '{best['title']}' ({best['channel']}) -> https://youtu.be/{best['id']}", "SUCCESS", "AI_GEMINI", "LINKED")
            return best["id"], best["region"]

        # 3. Deterministic Heuristic Fallback: Select candidate with highest mathematical score (> 0)
        valid_candidates = [c for c in selected_pool if c["heuristic_score"] > 0 and is_valid_highlight_candidate(c["title"])]
        if valid_candidates:
            best = valid_candidates[0]
            append_event(log_events, f"   [FALLBACK SELECT] Weighted Heuristic selected #{best['heuristic_score']}pts from {best['channel']}: '{best['title']}' -> https://youtu.be/{best['id']}", "SUCCESS", "YOUTUBE", "LINKED")
            return best["id"], best["region"]

        append_event(log_events, f"   [VALIDATION FAILED] Candidates do not match both {home} and {away} with verified highlight keywords. Skipped linking.", "WARN", "YOUTUBE", "REJECTED")
        return None, None



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
        return False, f"{tier_name} active ({minutes_since_last_check:.0f}m < {cooldown_min}m)"

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

            if age_hours < 24.0:
                tier_rank = 0
            elif age_hours <= 72.0:
                tier_rank = 1
            else:
                tier_rank = 2

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

        now_utc = datetime.now(UTC)
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()

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

        if cooldown_skipped:
            sample_reasons = [f"{m.get('home_team_name', 'Home')} vs {m.get('away_team_name', 'Away')} ({r})" for m, r in cooldown_skipped[:3]]
            more_str = f" (+{len(cooldown_skipped) - 3} more)" if len(cooldown_skipped) > 3 else ""
            append_event(
                log_events,
                f"[COOLDOWN PROTECTED] {len(cooldown_skipped)} fixture(s) currently in cooldown guardrail: {'; '.join(sample_reasons)}{more_str}.",
                "INFO",
                "YOUTUBE",
                "COOLDOWN_PROTECTED"
            )

        if not eligible_matches:
            append_event(
                log_events,
                f"All {len(unlinked)} unlinked fixtures are protected under quota tiering cooldowns (T1: 30m, T2: 2h, T3: 6h, T4: 24h). Skipping YouTube API calls.",
                "INFO",
                "YOUTUBE",
                "TIER_COOLDOWN"
            )
            return 0

        # Prioritize newest matches first: Tier 1 (<24h) -> Tier 2 (24-72h) -> Tier 3 (>72h); never-checked first; newest kickoff first
        sorted_matches = self.sort_unlinked_matches_by_priority(eligible_matches, now_utc)

        # Quota guardrails for historical searches (>72h):
        # Only allow up to 5 historical searches per cycle and ONLY if estimated quota >= 1000.
        filtered_batch = []
        historical_count = 0
        quota_remaining = youtube_client.estimated_quota_remaining

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
                f"No eligible fixtures to scan this cycle (Historical quota guardrail active: remaining quota={quota_remaining}).",
                "INFO",
                "YOUTUBE",
                "QUOTA_GUARD"
            )
            return 0

        linked_count = 0
        now_iso = now_utc.isoformat()

        for match in filtered_batch:
            home = match.get("home_team_name", "Home")
            away = match.get("away_team_name", "Away")
            comp = match.get("competition_name") or match.get("competition_id") or "League"
            m_date = str(match.get("utc_date", ""))[:10]
            append_event(log_events, f"[SEARCH] Searching official highlights for: {home} vs {away} ({m_date})", "INFO", "YOUTUBE", "SEARCH_START")
            try:
                vid, reg = self.search_highlight_for_match(match, log_events)
                if vid:
                    update_data = {
                        "highlight_video_id": vid,
                        "last_youtube_check": now_iso
                    }
                    if reg is not None:
                        update_data["youtube_region_info"] = reg

                    try:
                        supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                    except Exception as e:
                        if "youtube_region_info" in str(e) and "youtube_region_info" in update_data:
                            del update_data["youtube_region_info"]
                            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                        else:
                            raise e
                    linked_count += 1
                    append_event(log_events, f"   [LINKED] {home} vs {away} -> https://youtu.be/{vid} saved to Supabase.", "SUCCESS", "YOUTUBE", "DB_LINKED")
                    notification_service.notify_highlight_ready(match, vid, "Official Highlights", log_events)
                else:
                    _, next_tier_info = self.should_check_youtube_for_match({**match, "last_youtube_check": now_iso}, now_utc)
                    append_event(log_events, f"   [NOT FOUND] No official highlight available yet for: {home} vs {away} (Entering cooldown: {next_tier_info})", "WARN", "YOUTUBE", "NOT_FOUND")
                    supabase_client.table("matches").update({
                        "last_youtube_check": now_iso
                    }).eq("id", match["id"]).execute()
            except Exception as e:
                err_str = str(e) or type(e).__name__
                append_event(log_events, f"   [ERROR] Match highlight search error for {home} vs {away}: {err_str}", "ERROR", "YOUTUBE", "SEARCH_ERROR")
                append_event(log_events, f"   [SKIP COOLDOWN] Network/API error occurred. Cooldown not recorded for {home} vs {away} to allow retry next cycle.", "DEBUG", "YOUTUBE", "SKIP_COOLDOWN")

        append_event(log_events, f"Highlight scanning complete. Successfully linked {linked_count} new video highlights.", "SUCCESS", "YOUTUBE", "SUMMARY")
        return linked_count

highlight_service = HighlightService()

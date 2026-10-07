import html
import re
from datetime import UTC, datetime, timedelta

from worker.clients.gemini_client import gemini_client
from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.constants import CUSTOM_KEYWORD_ALIASES, EUROPEAN_CLUB_HANDLES, FORBIDDEN_TITLE_PATTERNS
from worker.config.settings import settings


class HighlightService:

    @staticmethod
    def check_forbidden_title(title: str) -> tuple[bool, str | None]:
        t = title.lower()
        for pattern, reason in FORBIDDEN_TITLE_PATTERNS:
            if re.search(pattern, t, flags=re.IGNORECASE):
                return True, reason
        return False, None

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
        for key, handle in EUROPEAN_CLUB_HANDLES.items():
            if key in cleaned:
                return handle
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
        match_start = datetime.fromisoformat(match["utc_date"].replace('Z', '+00:00'))

        pub_after = (match_start - timedelta(hours=2)).strftime("%Y-%m-%dT%H:%M:%SZ")
        pub_before = (match_start + timedelta(hours=72)).strftime("%Y-%m-%dT%H:%M:%SZ")

        # Prioritaskan channel resmi kedua tim
        h_handle = self.get_club_handle(home)
        a_handle = self.get_club_handle(away)
        target_handles = [h for h in [h_handle, a_handle] if h]

        if not target_handles:
            return None, None

        h_search = self.clean_team_name_for_search(home)
        a_search = self.clean_team_name_for_search(away)
        query = f"{h_search} {a_search} highlights"

        candidates = []
        for handle in target_handles:
            c_id = youtube_client.resolve_channel_id(handle)
            if not c_id:
                continue

            # 1. Coba Playlist Uploads Scan (1 unit kuota)
            raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=20)
            matched_items = []
            for item in raw_items:
                title = html.unescape(item.get("snippet", {}).get("title", ""))
                v_id = item.get("snippet", {}).get("resourceId", {}).get("videoId")
                if v_id and (h_search in title.lower() or a_search in title.lower()):
                    matched_items.append({"id": {"videoId": v_id}, "snippet": item.get("snippet", {})})

            # 2. Fallback ke Search API jika playlist scan kosong
            if not matched_items:
                matched_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)

            if not matched_items:
                continue

            video_ids = [it["id"]["videoId"] for it in matched_items if it.get("id", {}).get("videoId")]
            durations, regions, embeddables = youtube_client.get_video_details(video_ids)

            for item in matched_items:
                vid = item["id"]["videoId"]
                title = html.unescape(item["snippet"]["title"])
                is_forbidden, reason = self.check_forbidden_title(title)
                if is_forbidden:
                    continue

                dur = durations.get(vid, 0)
                if not (settings.min_duration_seconds <= dur <= settings.max_duration_seconds):
                    continue

                candidates.append({
                    "id": vid,
                    "title": title,
                    "duration": dur,
                    "is_embeddable": embeddables.get(vid, True),
                    "region": regions.get(vid),
                    "channel": handle
                })

        if not candidates:
            return None, None

        # Filter favorit embeddable penuh
        embeddable_candidates = [c for c in candidates if c["is_embeddable"]]
        selected_pool = embeddable_candidates if embeddable_candidates else candidates

        if len(selected_pool) == 1:
            best = selected_pool[0]
            log_events.append(f"   🏆 [DAPAT] '{best['title']}' ({best['channel']})")
            return best["id"], best["region"]

        # Evaluasi AI Gemini jika ada multiple candidates
        comp_display = match.get("competition_name") or match.get("competition_id") or "League"
        best_idx = gemini_client.rerank_highlights(home, away, comp_display, selected_pool)
        if best_idx is not None and 0 <= best_idx < len(selected_pool):
            best = selected_pool[best_idx]
            log_events.append(f"   🏆 [DAPAT - AI RANKED] '{best['title']}' ({best['channel']})")
            return best["id"], best["region"]

        # Fallback default: ambil pertama
        best = selected_pool[0]
        log_events.append(f"   🏆 [DAPAT] '{best['title']}' ({best['channel']})")
        return best["id"], best["region"]

    def sync_unlinked_highlights(
        self,
        max_batch: int = 25,
        log_events: list | None = None
    ) -> int:
        if log_events is None:
            log_events = []

        res = (
            supabase_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .order("utc_date", desc=True)
            .limit(max_batch)
            .execute()
        )

        unlinked = res.data or []
        if not unlinked:
            log_events.append("⚪ YouTube: Semua laga FINISHED sudah memiliki video highlight.")
            return 0

        linked_count = 0
        now_iso = datetime.now(UTC).isoformat()

        for match in unlinked:
            vid, reg = self.search_highlight_for_match(match, log_events)
            if vid:
                supabase_client.table("matches").update({
                    "highlight_video_id": vid,
                    "youtube_region_info": reg,
                    "last_youtube_check": now_iso
                }).eq("id", match["id"]).execute()
                linked_count += 1
            else:
                supabase_client.table("matches").update({
                    "last_youtube_check": now_iso
                }).eq("id", match["id"]).execute()

        log_events.append(f"✅ Selesai pemindaian highlight. Berhasil menautkan {linked_count} video baru.")
        return linked_count

highlight_service = HighlightService()

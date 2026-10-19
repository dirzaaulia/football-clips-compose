import asyncio
from datetime import UTC, datetime, timedelta
from typing import Any

import httpx
from loguru import logger


class ESPNClient:
    """Enterprise-grade, zero-quota ESPN Soccer API client.

    Provides real-time scores, live clocks, goal events, cards, and full domestic
    cup coverage with zero authentication keys and no rate limits.
    """

    BASE_URL = "https://site.api.espn.com/apis/site/v2/sports/soccer"

    # Standard league & cup mapping
    # Maps internal competition code -> ESPN league slug & metadata
    COMPETITION_MAP: dict[str, dict[str, str]] = {
        "PL": {"slug": "eng.1", "name": "Premier League", "type": "league"},
        "PD": {"slug": "esp.1", "name": "LaLiga", "type": "league"},
        "SA": {"slug": "ita.1", "name": "Serie A", "type": "league"},
        "BL1": {"slug": "ger.1", "name": "Bundesliga", "type": "league"},
        "FL1": {"slug": "fra.1", "name": "Ligue 1", "type": "league"},
        "CL": {"slug": "uefa.champions", "name": "UEFA Champions League", "type": "cup"},
        "EL": {"slug": "uefa.europa", "name": "UEFA Europa League", "type": "cup"},
        "ECL": {"slug": "uefa.europa.conf", "name": "UEFA Conference League", "type": "cup"},
        "FAC": {"slug": "eng.fa", "name": "FA Cup", "type": "cup"},
        "EFL": {"slug": "eng.league_cup", "name": "Carabao Cup", "type": "cup"},
        "CDR": {"slug": "esp.copa_del_rey", "name": "Copa del Rey", "type": "cup"},
        "DFB": {"slug": "ger.dfb_pokal", "name": "DFB-Pokal", "type": "cup"},
        "CIT": {"slug": "ita.coppa_italia", "name": "Coppa Italia", "type": "cup"},
        "SPL": {"slug": "ksa.1", "name": "Saudi Pro League", "type": "league"},
        "MLS": {"slug": "usa.1", "name": "Major League Soccer", "type": "league"},
        "DED": {"slug": "ned.1", "name": "Eredivisie", "type": "league"},
        "PPL": {"slug": "por.1", "name": "Liga Portugal", "type": "league"},
    }

    # Inverse mapping from ESPN slug -> internal code
    SLUG_TO_CODE: dict[str, str] = {v["slug"]: k for k, v in COMPETITION_MAP.items()}

    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        # In-memory cache for league matchweek calendar clusters: slug -> list of [start_date, end_date]
        self._calendar_cache: dict[str, list[tuple[datetime.date, datetime.date]]] = {}

    @staticmethod
    def normalize_status(espn_state: str, detail: str) -> str:
        """Map ESPN status state & detail into FootballClips standard status."""
        state_upper = (espn_state or "").upper()
        detail_upper = (detail or "").upper()

        if "POSTPONED" in detail_upper:
            return "POSTPONED"
        if "CANCELLED" in detail_upper or "CANCELED" in detail_upper:
            return "CANCELLED"

        if state_upper == "PRE":
            return "TIMED"
        elif state_upper == "IN":
            if "HALFTIME" in detail_upper or detail_upper == "HT":
                return "PAUSED"
            return "IN_PLAY"
        elif state_upper == "POST":
            return "FINISHED"
        return "SCHEDULED"

    @staticmethod
    def format_round_slug(slug: str | None) -> str:
        """Format an ESPN round slug like 'fourth-round' or 'league-phase' into Title Case."""
        if not slug:
            return "Main Round"
        # If slug contains season year like '2026-27-english-premier-league', skip
        if any(char.isdigit() for char in slug) and "league" in slug.lower():
            return "Regular Season"
        words = slug.replace("-", " ").replace("_", " ").split()
        capitalized = [w.capitalize() for w in words]
        return " ".join(capitalized)

    def _cluster_calendar_matchweeks(self, calendar_dates: list[str]) -> list[tuple[datetime.date, datetime.date]]:
        """Cluster ESPN calendar dates into sequential Matchweeks (gaps <= 2 days)."""
        if not calendar_dates:
            return []
        try:
            parsed = sorted([
                datetime.fromisoformat(d.replace("Z", "+00:00")).date()
                for d in calendar_dates
            ])
            weeks: list[tuple[datetime.date, datetime.date]] = []
            current_start = parsed[0]
            current_end = parsed[0]

            for d in parsed[1:]:
                if (d - current_end).days <= 2:
                    current_end = d
                else:
                    weeks.append((current_start, current_end))
                    current_start = d
                    current_end = d
            weeks.append((current_start, current_end))
            return weeks
        except Exception as e:
            logger.warning(f"[ESPN] Failed to cluster calendar dates: {e}")
            return []

    def _determine_matchweek(self, league_slug: str, match_date: datetime.date, calendar_dates: list[str]) -> int | None:
        """Find the 1-indexed matchweek for a given date in a league season."""
        if league_slug not in self._calendar_cache and calendar_dates:
            self._calendar_cache[league_slug] = self._cluster_calendar_matchweeks(calendar_dates)

        clusters = self._calendar_cache.get(league_slug, [])
        for i, (start, end) in enumerate(clusters, 1):
            if start <= match_date <= end:
                return i
            # If match date is just 1 day off due to UTC conversion
            if abs((match_date - start).days) <= 1 or abs((match_date - end).days) <= 1:
                return i
        return None

    async def fetch_scoreboard_day(self, client: httpx.AsyncClient, league_slug: str, yyyymmdd: str | None = None) -> dict[str, Any]:
        """Fetch scoreboard data for a single league on a specific day."""
        url = f"{self.BASE_URL}/{league_slug}/scoreboard"
        params = {}
        if yyyymmdd:
            params["dates"] = yyyymmdd

        try:
            resp = await client.get(url, params=params, headers=self.headers, timeout=15.0)
            if resp.status_code == 200:
                return resp.json()
            elif resp.status_code == 400:
                # No matches scheduled for this specific date
                return {}
            else:
                logger.warning(f"[ESPN] Fetch failed for {league_slug} ({yyyymmdd}): HTTP {resp.status_code}")
                return {}
        except Exception as e:
            logger.error(f"[ESPN] Request exception for {league_slug} ({yyyymmdd}): {e}")
            return {}

    def parse_event(self, event: dict[str, Any], league_info: dict[str, str], calendar_dates: list[str]) -> dict[str, Any] | None:
        """Transform raw ESPN event into standard FootballClips match payload."""
        try:
            event_id = int(event["id"])
            competitions = event.get("competitions", [{}])
            if not competitions:
                return None
            comp = competitions[0]

            # Kickoff UTC
            raw_date = event.get("date") or comp.get("date")
            utc_date = raw_date
            match_dt = datetime.now(UTC)
            if raw_date:
                try:
                    match_dt = datetime.fromisoformat(raw_date.replace("Z", "+00:00"))
                    utc_date = match_dt.isoformat()
                except Exception:
                    pass

            # Status and display clock
            status_obj = comp.get("status", {})
            type_obj = status_obj.get("type", {})
            espn_state = type_obj.get("state", "pre")
            detail = type_obj.get("detail", "")
            status = self.normalize_status(espn_state, detail)
            display_clock = status_obj.get("displayClock") or ""

            # Competitors (Home vs Away)
            competitors = comp.get("competitors", [])
            home_team, away_team = {}, {}
            for c in competitors:
                if c.get("homeAway") == "home":
                    home_team = c
                else:
                    away_team = c

            h_team_obj = home_team.get("team", {})
            a_team_obj = away_team.get("team", {})

            home_team_id = int(h_team_obj.get("id")) if h_team_obj.get("id") else None
            home_team_name = h_team_obj.get("displayName") or h_team_obj.get("name") or "Home Team"
            home_team_crest = h_team_obj.get("logo")

            away_team_id = int(a_team_obj.get("id")) if a_team_obj.get("id") else None
            away_team_name = a_team_obj.get("displayName") or a_team_obj.get("name") or "Away Team"
            away_team_crest = a_team_obj.get("logo")

            # Scores (None if scheduled/timed)
            home_score = None
            away_score = None
            if status in ["IN_PLAY", "PAUSED", "FINISHED"]:
                try:
                    home_score = int(home_team.get("score")) if home_team.get("score") is not None else 0
                    away_score = int(away_team.get("score")) if away_team.get("score") is not None else 0
                except (ValueError, TypeError):
                    home_score, away_score = 0, 0

            # Matchday & Round Name resolution
            comp_type = league_info.get("type", "league")
            season_slug = event.get("season", {}).get("slug")
            
            matchday = None
            round_name = None

            if comp_type == "league":
                matchday = self._determine_matchweek(league_info["slug"], match_dt.date(), calendar_dates)
                round_name = f"Matchweek {matchday}" if matchday else "Regular Season"
            else:
                # Cup competition phase / round name
                if season_slug and not any(char.isdigit() for char in season_slug):
                    round_name = self.format_round_slug(season_slug)
                else:
                    round_name = comp.get("type", {}).get("text") or "Cup Round"

            return {
                "id": event_id,
                "competition_id": league_info["code"],
                "competition_name": league_info["name"],
                "season": int(event.get("season", {}).get("year", 2026)),
                "utc_date": utc_date,
                "status": status,
                "matchday": matchday,
                "round_name": round_name,
                "display_clock": display_clock,
                "home_team_id": home_team_id,
                "home_team_name": home_team_name,
                "home_team_crest": home_team_crest,
                "away_team_id": away_team_id,
                "away_team_name": away_team_name,
                "away_team_crest": away_team_crest,
                "home_score": home_score,
                "away_score": away_score,
                "provider": "espn",
            }
        except Exception as e:
            logger.error(f"[ESPN] Error parsing event {event.get('id')}: {e}")
            return None

    async def get_matches(
        self,
        competitions: list[str],
        date_from: str,
        date_to: str,
    ) -> list[dict[str, Any]]:
        """Fetch all matches for the given competitions and date window concurrently."""
        try:
            d_start = datetime.strptime(date_from, "%Y-%m-%d").date()
            d_end = datetime.strptime(date_to, "%Y-%m-%d").date()
        except Exception:
            d_start = datetime.now(UTC).date()
            d_end = d_start

        # Generate list of days to fetch
        days_to_fetch = []
        curr = d_start
        while curr <= d_end:
            days_to_fetch.append(curr.strftime("%Y%m%d"))
            curr += timedelta(days=1)

        # Filter target league metadata
        target_leagues: list[dict[str, str]] = []
        for code in competitions:
            info = self.COMPETITION_MAP.get(code)
            if info:
                target_leagues.append({"code": code, **info})

        if not target_leagues:
            logger.warning(f"[ESPN] No valid competitions mapped from: {competitions}")
            return []

        all_matches: list[dict[str, Any]] = []
        seen_ids: set[int] = set()

        sem = asyncio.Semaphore(15)

        async with httpx.AsyncClient(timeout=25.0) as client:
            async def bounded_fetch(slug: str, day: str):
                async with sem:
                    return await self.fetch_scoreboard_day(client, slug, day)

            tasks = []
            task_metadata = []

            for league in target_leagues:
                for day_str in days_to_fetch:
                    tasks.append(bounded_fetch(league["slug"], day_str))
                    task_metadata.append(league)

            # Concurrent bounded execution across all dates and leagues
            results = await asyncio.gather(*tasks, return_exceptions=True)

            for result, league_info in zip(results, task_metadata, strict=False):
                if isinstance(result, Exception) or not result:
                    continue

                calendar = result.get("leagues", [{}])[0].get("calendar", [])
                events = result.get("events", [])

                for ev in events:
                    parsed = self.parse_event(ev, league_info, calendar)
                    if parsed and parsed["id"] not in seen_ids:
                        seen_ids.add(parsed["id"])
                        all_matches.append(parsed)

        logger.info(
            f"[ESPN] Ingested {len(all_matches)} fixtures across {len(target_leagues)} competitions ({date_from} to {date_to})"
        )
        return all_matches

    async def get_standings(self, competition_code: str, season: int | None = None, client: httpx.AsyncClient | None = None) -> list[dict[str, Any]]:
        """
        Fetch league standings for a competition from ESPN.
        Supports current or historical season (e.g. season=2025).
        Returns parsed rows matching Supabase standings table schema.
        Zero quota cost, zero API keys, real-time table rankings.
        """
        info = self.COMPETITION_MAP.get(competition_code)
        if not info:
            logger.warning(f"[ESPN Standings] Unknown competition code: {competition_code}")
            return []

        slug = info["slug"]
        comp_name = info["name"]
        url = f"https://site.api.espn.com/apis/v2/sports/soccer/{slug}/standings"
        if season:
            url += f"?season={season}"

        try:
            should_close = False
            if client is None:
                client = httpx.AsyncClient(timeout=15.0, headers=self.headers)
                should_close = True

            try:
                resp = await client.get(url)
                if resp.status_code != 200:
                    logger.warning(f"[ESPN Standings] HTTP {resp.status_code} fetching standings for {competition_code}")
                    return []
                data = resp.json()
            finally:
                if should_close:
                    await client.aclose()

            season_info = data.get("season", {})
            season_year = int(season or season_info.get("year", 2026))

            children = data.get("children", [])
            entries = []
            if children:
                entries = children[0].get("standings", {}).get("entries", [])
            elif "standings" in data:
                entries = data.get("standings", {}).get("entries", [])

            standings_rows = []
            now_iso = datetime.now(UTC).isoformat()

            for entry in entries:
                team_info = entry.get("team", {})
                team_id_raw = team_info.get("id", "0")
                try:
                    team_id = int(team_id_raw)
                except ValueError:
                    team_id = abs(hash(team_id_raw)) % 1000000

                team_name = team_info.get("displayName") or team_info.get("name") or "Unknown"
                short_name = team_info.get("shortDisplayName") or team_info.get("abbreviation")

                logos = team_info.get("logos", [])
                crest_url = logos[0].get("href") if logos else None

                stats = {s.get("name"): s.get("value") for s in entry.get("stats", [])}

                position = int(stats.get("rank", 0))
                played_games = int(stats.get("gamesPlayed", 0))
                won = int(stats.get("wins", 0))
                draw = int(stats.get("ties", 0))
                lost = int(stats.get("losses", 0))
                points = int(stats.get("points", 0))
                goals_for = int(stats.get("pointsFor", 0))
                goals_against = int(stats.get("pointsAgainst", 0))
                goal_diff = int(stats.get("pointDifferential", 0))

                # Extract summary / form if present
                form = None
                for s in entry.get("stats", []):
                    if s.get("type") == "total" or s.get("name") == "overall":
                        form = s.get("summary")

                # Season 2026 retains standard ID for backward compatibility; historical uses season in ID
                row_id = f"{competition_code}_{team_id}" if season_year == 2026 else f"{competition_code}_{season_year}_{team_id}"
                standings_rows.append({
                    "id": row_id,
                    "competition_code": competition_code,
                    "season": season_year,
                    "position": position,
                    "team_id": team_id,
                    "team_name": team_name,
                    "team_short_name": short_name,
                    "crest_url": crest_url,
                    "played_games": played_games,
                    "won": won,
                    "draw": draw,
                    "lost": lost,
                    "points": points,
                    "goals_for": goals_for,
                    "goals_against": goals_against,
                    "goal_difference": goal_diff,
                    "form": form,
                    "updated_at": now_iso
                })

            return standings_rows
        except Exception as e:
            logger.error(f"[ESPN Standings] Failed to fetch standings for {competition_code}: {e}")
            return []

    async def get_match_summary(
        self,
        event_id: int,
        competition_code: str,
        client: httpx.AsyncClient | None = None
    ) -> dict[str, Any]:
        """
        Fetch full match summary from ESPN (formations, real stats, real journalistic recap).
        Zero quota cost, 100% authentic accredited data.
        """
        info = self.COMPETITION_MAP.get(competition_code)
        slug = info["slug"] if info else "eng.1"
        url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/summary?event={event_id}"

        try:
            should_close = False
            if client is None:
                client = httpx.AsyncClient(timeout=15.0, headers=self.headers)
                should_close = True

            try:
                resp = await client.get(url)
                if resp.status_code != 200:
                    return {}
                data = resp.json()
            finally:
                if should_close:
                    await client.aclose()

            # 1. Rosters & Formations
            formations = {}
            rosters = data.get("rosters", [])
            for r in rosters:
                side = "home" if r.get("homeAway") == "home" else "away"
                formation_str = r.get("formation")
                starters = []
                for p in r.get("roster", []):
                    if p.get("starter"):
                        starters.append({
                            "name": p.get("athlete", {}).get("displayName", "Player"),
                            "jersey": p.get("jersey"),
                            "position": p.get("position", {}).get("abbreviation")
                        })
                formations[side] = {
                    "formation": formation_str,
                    "starters": starters
                }

            # 2. Boxscore / Match Statistics
            statistics = {}
            boxscore = data.get("boxscore", {})
            for t in boxscore.get("teams", []):
                side = "home" if t.get("homeAway") == "home" else "away"
                stats_dict = {s.get("name"): s.get("displayValue") for s in t.get("statistics", [])}
                statistics[side] = stats_dict

            # 3. Journalistic Editorial Recap
            article = data.get("article", {})
            editorial_headline = article.get("headline")
            editorial_story = article.get("story")
            editorial_source = article.get("source") or "ESPN"

            # 4. Last Five Games Form
            last_five = data.get("lastFiveGames", [])

            # 5. Chronological Match Key Events (Goals, Cards, Substitutions)
            raw_key_events = data.get("keyEvents", [])
            header_comps = data.get("header", {}).get("competitions", [{}])
            competitors = header_comps[0].get("competitors", []) if header_comps else []
            home_team_id = next((str(c.get("team", {}).get("id")) for c in competitors if c.get("homeAway") == "home"), None)

            parsed_events = []
            for e in raw_key_events:
                t_info = e.get("type", {})
                t_type = str(t_info.get("type", "")).lower()
                t_text = str(t_info.get("text", ""))
                if any(k in t_type or k in t_text.lower() for k in ["goal", "card", "sub", "penalty", "var"]):
                    team = e.get("team", {})
                    team_id = str(team.get("id", "")) if team else None
                    side = "home" if team_id == home_team_id else ("away" if team_id else None)
                    participants = [p.get("athlete", {}).get("displayName", "") for p in e.get("participants", [])]
                    parsed_events.append({
                        "id": str(e.get("id")),
                        "type": t_type,
                        "type_text": t_text,
                        "clock": e.get("clock", {}).get("displayValue", ""),
                        "period": e.get("period", {}).get("number"),
                        "side": side,
                        "scoring_play": e.get("scoringPlay", False),
                        "player": participants[0] if participants else "",
                        "assist": participants[1] if len(participants) > 1 else None,
                        "text": e.get("shortText") or e.get("text", "")
                    })

            return {
                "formations": formations if formations else None,
                "statistics": statistics if statistics else None,
                "editorial_headline": editorial_headline,
                "editorial_story": editorial_story,
                "editorial_source": editorial_source if editorial_headline else None,
                "last_five_games": last_five if last_five else None,
                "events": parsed_events if parsed_events else None
            }
        except Exception as e:
            logger.error(f"[ESPN Summary] Failed to fetch summary for {event_id}: {e}")
            return {}


espn_client = ESPNClient()


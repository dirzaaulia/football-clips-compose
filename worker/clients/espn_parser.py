from datetime import UTC, datetime
from typing import Any

from loguru import logger

from worker.clients.espn_calendar import espn_calendar


def normalize_status(espn_state: str, detail: str) -> str:
    """Map ESPN status state & detail into FootballClips standard status."""
    state_upper = (espn_state or "").upper()
    detail_upper = (detail or "").upper()

    if "ABANDONED" in detail_upper:
        return "ABANDONED"
    if "SUSPENDED" in detail_upper or "INTERRUPTED" in detail_upper:
        return "INTERRUPTED"
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


def format_round_slug(slug: str | None) -> str:
    """Format an ESPN round slug into Title Case."""
    if not slug:
        return "Main Round"
    if any(char.isdigit() for char in slug) and "league" in slug.lower():
        return "Regular Season"
    words = slug.replace("-", " ").replace("_", " ").split()
    capitalized = [w.capitalize() for w in words]
    return " ".join(capitalized)


def parse_event(event: dict[str, Any], league_info: dict[str, str], calendar_dates: list[str]) -> dict[str, Any] | None:
    """Transform raw ESPN event into standard FootballClips match payload."""
    try:
        event_id = int(event["id"])
        competitions = event.get("competitions", [{}])
        if not competitions:
            return None
        comp = competitions[0]

        raw_date = event.get("date") or comp.get("date")
        utc_date = raw_date
        match_dt = datetime.now(UTC)
        if raw_date:
            try:
                match_dt = datetime.fromisoformat(raw_date.replace("Z", "+00:00"))
                utc_date = match_dt.isoformat()
            except Exception:
                pass

        status_obj = comp.get("status", {})
        type_obj = status_obj.get("type", {})
        status = normalize_status(type_obj.get("state", "pre"), type_obj.get("detail", ""))
        display_clock = status_obj.get("displayClock") or ""

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

        home_score, away_score = None, None
        home_penalties, away_penalties = None, None
        if status in ["IN_PLAY", "PAUSED", "FINISHED"]:
            try:
                home_score = int(home_team.get("score")) if home_team.get("score") is not None else 0
                away_score = int(away_team.get("score")) if away_team.get("score") is not None else 0
            except (ValueError, TypeError):
                home_score, away_score = 0, 0

            try:
                h_shoot = home_team.get("shootoutScore")
                if h_shoot is not None:
                    home_penalties = int(float(h_shoot))
                a_shoot = away_team.get("shootoutScore")
                if a_shoot is not None:
                    away_penalties = int(float(a_shoot))
            except (ValueError, TypeError):
                pass

        comp_type = league_info.get("type", "league")
        season_slug = event.get("season", {}).get("slug")
        matchday, round_name = None, None

        if comp_type == "league":
            matchday = espn_calendar.determine_matchweek(league_info["slug"], match_dt.date(), calendar_dates)
            round_name = f"Matchweek {matchday}" if matchday else "Regular Season"
        else:
            if season_slug and not any(char.isdigit() for char in season_slug):
                round_name = format_round_slug(season_slug)
            else:
                round_name = comp.get("type", {}).get("text") or "Cup Round"

        return {
            "id": event_id,
            "competition_id": league_info["code"],
            "competition_name": league_info["name"],
            "season": int(event.get("season", {}).get("year") or datetime.fromisoformat(utc_date.replace("Z", "+00:00")).year),
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
            "home_penalties": home_penalties,
            "away_penalties": away_penalties,
            "provider": "espn",
        }
    except Exception as e:
        logger.error(f"[ESPN] Error parsing event {event.get('id')}: {e}")
        return None


def parse_standings(data: dict[str, Any], competition_code: str, season: int | None = None) -> list[dict[str, Any]]:
    """Parse raw ESPN standings payload into Supabase standings table format."""
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

        form = None
        for s in entry.get("stats", []):
            if s.get("type") == "total" or s.get("name") == "overall":
                form = s.get("summary")

        row_id = f"{competition_code}_{team_id}" if season_year == 2026 else f"{competition_code}_{season_year}_{team_id}"
        standings_rows.append({
            "id": row_id,
            "competition_code": competition_code,
            "season": season_year,
            "position": int(stats.get("rank", 0)),
            "team_id": team_id,
            "team_name": team_name,
            "team_short_name": short_name,
            "crest_url": crest_url,
            "played_games": int(stats.get("gamesPlayed", 0)),
            "won": int(stats.get("wins", 0)),
            "draw": int(stats.get("ties", 0)),
            "lost": int(stats.get("losses", 0)),
            "points": int(stats.get("points", 0)),
            "goals_for": int(stats.get("pointsFor", 0)),
            "goals_against": int(stats.get("pointsAgainst", 0)),
            "goal_difference": int(stats.get("pointDifferential", 0)),
            "form": form,
            "updated_at": now_iso,
        })
    return standings_rows


def parse_match_summary(data: dict[str, Any]) -> dict[str, Any]:
    """Parse ESPN match summary (formations, statistics, key events, recap)."""
    formations = {}
    for r in data.get("rosters", []):
        side = "home" if r.get("homeAway") == "home" else "away"
        starters = [
            {"name": p.get("athlete", {}).get("displayName", "Player"), "jersey": p.get("jersey"), "position": p.get("position", {}).get("abbreviation")}
            for p in r.get("roster", []) if p.get("starter")
        ]
        formations[side] = {"formation": r.get("formation"), "starters": starters}

    statistics = {}
    for t in data.get("boxscore", {}).get("teams", []):
        side = "home" if t.get("homeAway") == "home" else "away"
        statistics[side] = {s.get("name"): s.get("displayValue") for s in t.get("statistics", [])}

    article = data.get("article", {})
    editorial_headline = article.get("headline")
    editorial_story = article.get("story")
    editorial_source = article.get("source") or "ESPN"
    last_five = data.get("lastFiveGames", [])

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
                "text": e.get("shortText") or e.get("text", ""),
            })

    pitch_plays = []
    for c in data.get("commentary", []):
        p = c.get("play", {})
        if not p:
            continue
        fx, fy = p.get("fieldPositionX"), p.get("fieldPositionY")
        p_type = p.get("type", {})
        clock_str = p.get("clock", {}).get("displayValue") or c.get("time", {}).get("displayValue", "")
        t_name = p.get("team", {}).get("displayName")
        parts = [pt.get("athlete", {}).get("displayName", "") for pt in p.get("participants", []) if pt.get("athlete")]
        pitch_plays.append({
            "id": str(p.get("id") or c.get("sequence", "")),
            "clock": clock_str,
            "period": p.get("period", {}).get("number", 1),
            "type": p_type.get("type") or str(p_type.get("text", "")).lower(),
            "type_text": p_type.get("text", ""),
            "text": c.get("text") or p.get("text", ""),
            "team_name": t_name,
            "player": parts[0] if parts else None,
            "x_percent": float(fx) if fx is not None else None,
            "y_percent": float(fy) if fy is not None else None,
        })

    return {
        "formations": formations if formations else None,
        "statistics": statistics if statistics else None,
        "editorial_headline": editorial_headline,
        "editorial_story": editorial_story,
        "editorial_source": editorial_source if editorial_headline else None,
        "last_five_games": last_five if last_five else None,
        "events": parsed_events if parsed_events else None,
        "pitch_plays": pitch_plays if pitch_plays else None,
    }


import { ESPN_COMPETITION_SLUGS } from '@/lib/espn';
import { database, MatchRecord, StandingRecord } from '@/lib/database';

export interface LeaguePlayerLeader {
  rank: number;
  playerId: string;
  name: string;
  shortName: string;
  jersey: string;
  teamId: string;
  teamName: string;
  teamAbbr: string;
  teamLogo: string;
  teamColor: string;
  appearances: number;
  goals: number;
  assists: number;
  contributions: number;
  perMatch: number;
}

export interface LeagueStatsPayload {
  competitionCode: string;
  seasonDisplay: string;
  scorers: LeaguePlayerLeader[];
  assists: LeaguePlayerLeader[];
  contributions: LeaguePlayerLeader[];
}

export interface ClubAnalyticsSummary {
  teamName: string;
  crestUrl?: string | null;
  position: number;
  played: number;
  won: number;
  draw: number;
  lost: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDiff: number;
  points: number;
  ppg: number;
  goalsPerMatch: number;
  concededPerMatch: number;
  estimatedXg: number;
  expectedPoints: number;
  pointsDelta: number;
  cleanSheetEst: number;
  form: string[];
  formPointsLast5: number;
}

const STATS_CACHE = new Map<string, LeagueStatsPayload>();
const PHOTO_CACHE = new Map<string, string | null>();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function parseLeaderRow(item: any, idx: number, mode: 'goals' | 'assists'): LeaguePlayerLeader {
  const ath = item.athlete || {};
  const team = ath.team || item.team || {};
  const statsArr: Array<{ name: string; value: number }> = ath.statistics || [];
  const getStat = (key: string) =>
    Number(statsArr.find((s) => s.name === key)?.value ?? 0);

  const appearances = Math.max(1, getStat('appearances') || 1);
  const goals = mode === 'goals' ? Number(item.value ?? getStat('totalGoals')) : getStat('totalGoals');
  const assists = mode === 'assists' ? Number(item.value ?? getStat('goalAssists')) : getStat('goalAssists');
  const contributions = goals + assists;
  const primaryVal = mode === 'goals' ? goals : assists;

  return {
    rank: idx + 1,
    playerId: String(ath.id || idx),
    name: ath.displayName || ath.shortName || 'Player',
    shortName: ath.shortName || ath.displayName || 'Player',
    jersey: String(ath.jersey || ''),
    teamId: String(team.id || ''),
    teamName: team.displayName || team.name || 'Club',
    teamAbbr: team.abbreviation || (team.name || 'CLB').slice(0, 3).toUpperCase(),
    teamLogo: team.logos?.[0]?.href || '',
    teamColor: team.color ? `#${team.color.replace('#', '')}` : '#DFFF00',
    appearances,
    goals,
    assists,
    contributions,
    perMatch: Number((primaryVal / appearances).toFixed(2)),
  };
}

export async function fetchLeaguePlayerStats(
  competitionCode: string,
): Promise<LeagueStatsPayload> {
  const code = competitionCode || 'PL';
  if (STATS_CACHE.has(code)) {
    return STATS_CACHE.get(code)!;
  }

  const slug = ESPN_COMPETITION_SLUGS[code] || 'eng.1';
  try {
    const res = await fetch(
      `https://site.api.espn.com/apis/site/v2/sports/soccer/${slug}/statistics`,
    );
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const goalsCat = (data.stats || []).find((s: any) => s.name === 'goalsLeaders');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const assistsCat = (data.stats || []).find((s: any) => s.name === 'assistsLeaders');

    const scorers: LeaguePlayerLeader[] = (goalsCat?.leaders || [])
      .slice(0, 30)
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      .map((row: any, i: number) => parseLeaderRow(row, i, 'goals'));

    const assists: LeaguePlayerLeader[] = (assistsCat?.leaders || [])
      .slice(0, 30)
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      .map((row: any, i: number) => parseLeaderRow(row, i, 'assists'));

    const combinedMap = new Map<string, LeaguePlayerLeader>();
    for (const s of scorers) {
      combinedMap.set(s.playerId, { ...s });
    }
    for (const a of assists) {
      const existing = combinedMap.get(a.playerId);
      if (existing) {
        existing.assists = Math.max(existing.assists, a.assists);
        existing.contributions = existing.goals + existing.assists;
        existing.perMatch = Number((existing.contributions / existing.appearances).toFixed(2));
      } else {
        combinedMap.set(a.playerId, {
          ...a,
          contributions: a.goals + a.assists,
          perMatch: Number(((a.goals + a.assists) / a.appearances).toFixed(2)),
        });
      }
    }

    const contributions = Array.from(combinedMap.values())
      .sort((a, b) => b.contributions - a.contributions || b.goals - a.goals)
      .slice(0, 25)
      .map((item, idx) => ({ ...item, rank: idx + 1 }));

    const payload: LeagueStatsPayload = {
      competitionCode: code,
      seasonDisplay: data.season?.displayName || '2026/27 Season',
      scorers,
      assists,
      contributions,
    };
    STATS_CACHE.set(code, payload);
    return payload;
  } catch {
    return {
      competitionCode: code,
      seasonDisplay: '2026/27 Season',
      scorers: [],
      assists: [],
      contributions: [],
    };
  }
}

export async function fetchPlayerCutoutPhoto(playerName: string): Promise<string | null> {
  const key = playerName.trim().toLowerCase();
  if (!key) return null;
  if (PHOTO_CACHE.has(key)) return PHOTO_CACHE.get(key) ?? null;

  try {
    const query = encodeURIComponent(playerName.trim().replace(/\s+/g, '_'));
    const res = await fetch(
      `https://www.thesportsdb.com/api/v1/json/123/searchplayers.php?p=${query}`,
    );
    if (!res.ok) {
      PHOTO_CACHE.set(key, null);
      return null;
    }
    const data = await res.json();
    const player = data?.player?.[0];
    const photo = player?.strCutout || player?.strThumb || player?.strRender || null;
    PHOTO_CACHE.set(key, photo);
    return photo;
  } catch {
    PHOTO_CACHE.set(key, null);
    return null;
  }
}

export function computeClubAnalyticsFromStandings(
  standings: StandingRecord[],
): ClubAnalyticsSummary[] {
  return standings.map((s) => {
    const played = Math.max(1, s.played_games || 1);
    const gf = s.goals_for ?? 0;
    const ga = s.goals_against ?? 0;
    const pts = s.points ?? 0;
    const ppg = Number((pts / played).toFixed(2));
    const goalsPerMatch = Number((gf / played).toFixed(2));
    const concededPerMatch = Number((ga / played).toFixed(2));

    // Pythagorean expectation for football (exponent ~1.35)
    const gfPow = Math.pow(Math.max(0.5, gf), 1.35);
    const gaPow = Math.pow(Math.max(0.5, ga), 1.35);
    const winRatio = gfPow / (gfPow + gaPow);
    const expectedPoints = Number((played * (winRatio * 2.65)).toFixed(1));
    const pointsDelta = Number((pts - expectedPoints).toFixed(1));
    const estimatedXg = Number((gf * 0.92 + s.won * 0.25).toFixed(1));
    const cleanSheetEst = Math.max(
      0,
      Math.min(played, Math.round(played * Math.exp(-concededPerMatch * 0.85))),
    );

    const form = (s.form || 'WWDWL')
      .replace(/[^WDL]/gi, '')
      .toUpperCase()
      .split('')
      .slice(-5);
    const formPointsLast5 = form.reduce(
      (acc, r) => acc + (r === 'W' ? 3 : r === 'D' ? 1 : 0),
      0,
    );

    return {
      teamName: s.team_name,
      crestUrl: s.crest_url,
      position: s.position,
      played: s.played_games,
      won: s.won,
      draw: s.draw,
      lost: s.lost,
      goalsFor: gf,
      goalsAgainst: ga,
      goalDiff: s.goal_difference,
      points: pts,
      ppg,
      goalsPerMatch,
      concededPerMatch,
      estimatedXg,
      expectedPoints,
      pointsDelta,
      cleanSheetEst,
      form,
      formPointsLast5,
    };
  });
}

export async function fetchHistoricalH2HFromDatabase(
  teamAName: string,
  teamBName: string,
): Promise<MatchRecord[]> {
  try {
    const cleanA = teamAName.split(' ')[0].trim();
    const cleanB = teamBName.split(' ')[0].trim();
    const { data, error } = await database
      .from('matches')
      .select('*')
      .or(`home_team.ilike.%${cleanA}%,away_team.ilike.%${cleanA}%`)
      .order('utc_date', { ascending: false })
      .limit(80);

    if (error || !data) return [];
    const lowerB = cleanB.toLowerCase();
    return (data as MatchRecord[]).filter((m) => {
      const h = (m.home_team_name || m.home_team || '').toLowerCase();
      const a = (m.away_team_name || m.away_team || '').toLowerCase();
      return h.includes(lowerB) || a.includes(lowerB);
    });
  } catch {
    return [];
  }
}

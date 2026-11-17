import { parseEspnSummaryPayload } from './espn-parser';

export const ESPN_COMPETITION_SLUGS: Record<string, string> = {
  CL: 'uefa.champions',
  EL: 'uefa.europa',
  ECL: 'uefa.ecl',
  PL: 'eng.1',
  PD: 'esp.1',
  SA: 'ita.1',
  BL1: 'ger.1',
  FL1: 'fra.1',
  SPL: 'ksa.1',
  MLS: 'usa.1',
  DED: 'ned.1',
  PPL: 'por.1',
  FAC: 'eng.fa',
  CDR: 'esp.copa_del_rey',
  DFB: 'ger.dfb_pokal',
  CI: 'ita.coppa_italia',
  CDF: 'fra.coupe_de_france',
};

export interface MatchEvent {
  id: string;
  type: string;
  type_text: string;
  clock: string;
  period?: number;
  side: 'home' | 'away' | null;
  scoring_play?: boolean;
  player: string;
  assist?: string | null;
  text: string;
  x_coord?: number | null;
  y_coord?: number | null;
  target_x?: number | null;
  target_y?: number | null;
}

export interface EspnH2HMatch {
  id: string;
  date: string;
  homeTeamId?: string;
  awayTeamId?: string;
  homeTeamName: string;
  awayTeamName: string;
  homeScore: string;
  awayScore: string;
  scoreText: string;
  competitionName?: string;
}

export interface EspnTeamFormMatch {
  id: string;
  date: string;
  opponentName: string;
  opponentLogo?: string;
  atVs: string;
  score: string;
  result: 'W' | 'D' | 'L';
}

export interface EspnLeaderEntry {
  category: string;
  displayName: string;
  playerName: string;
  shortName?: string;
  jersey?: string;
  position?: string;
  headshot?: string;
  displayValue: string;
  value: number;
}

export interface EspnGameInfo {
  venueName?: string;
  city?: string;
  country?: string;
  attendance?: number;
  referee?: string;
}

export interface EspnSummaryResult {
  status?: string;
  home_score?: number | null;
  away_score?: number | null;
  display_clock?: string | null;
  statistics?: {
    home?: Record<string, string>;
    away?: Record<string, string>;
  } | null;
  formations?: {
    home?: {
      formation?: string;
      color?: string;
      starters?: Array<{ name: string; jersey?: string; position?: string }>;
      substitutes?: Array<{ name: string; jersey?: string; position?: string }>;
    };
    away?: {
      formation?: string;
      color?: string;
      starters?: Array<{ name: string; jersey?: string; position?: string }>;
      substitutes?: Array<{ name: string; jersey?: string; position?: string }>;
    };
  } | null;
  editorial_headline?: string | null;
  editorial_story?: string | null;
  editorial_source?: string | null;
  events?: MatchEvent[] | null;
  gameInfo?: EspnGameInfo | null;
  headToHead?: {
    summary?: string;
    seriesScore?: string;
    matches: EspnH2HMatch[];
  } | null;
  lastFive?: {
    home: EspnTeamFormMatch[];
    away: EspnTeamFormMatch[];
  } | null;
  leaders?: {
    home: EspnLeaderEntry[];
    away: EspnLeaderEntry[];
  } | null;
}

const SUMMARY_CACHE = new Map<string, EspnSummaryResult>();

export async function fetchEspnMatchSummary(
  matchId: string | number,
  competitionId?: string | null,
  skipCache = false,
): Promise<EspnSummaryResult | null> {
  const idStr = String(matchId).replace(/^espn_/, '');
  if (!/^\d+$/.test(idStr)) return null;

  if (!skipCache && SUMMARY_CACHE.has(idStr)) {
    return SUMMARY_CACHE.get(idStr)!;
  }

  const primarySlug = competitionId ? ESPN_COMPETITION_SLUGS[competitionId] : 'eng.1';
  const candidateSlugs = Array.from(
    new Set([primarySlug || 'eng.1', 'uefa.champions', 'eng.1', 'esp.1', 'ita.1', 'ger.1', 'fra.1']),
  );

  for (const slug of candidateSlugs) {
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 6000);
      const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${slug}/summary?event=${idStr}`;
      const res = await fetch(url, { signal: controller.signal });
      clearTimeout(timeout);
      if (!res.ok) continue;

      const data = await res.json();
      if (!data || (!data.boxscore && !data.header)) continue;

      const parsed = parseEspnSummaryPayload(data);
      SUMMARY_CACHE.set(idStr, parsed);
      return parsed;
    } catch {
      continue;
    }
  }
  return null;
}

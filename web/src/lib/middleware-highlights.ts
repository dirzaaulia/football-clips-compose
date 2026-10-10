import { MatchRecord, getRecentHighlights } from './supabase';
import { BIG_LEAGUES, isLeagueMatch } from './constants';

export interface MiddlewareTeam { id: number; name: string; logo: string | null; }
export interface MiddlewareLeague { id: number; name: string; logo: string | null; season?: number; }
export interface MiddlewareCountry { code: string; name: string; logo: string | null; }
export interface MiddlewareMatch {
  id: number; date: string; round?: string;
  homeTeam: MiddlewareTeam; awayTeam: MiddlewareTeam;
  league: MiddlewareLeague; country: MiddlewareCountry;
}
export interface MiddlewareHighlight {
  id: number; title: string; category: string; channel: string;
  url: string; embedUrl: string; imgUrl: string; source: string; type: string;
  match?: MiddlewareMatch;
}
export interface MiddlewareResponse {
  status: string; total_verified: number;
  pagination: { limit: number; offset: number; totalCount: number; };
  data: MiddlewareHighlight[];
}

const MIDDLEWARE_URL = 'https://football-clips-middleware.dirzaaulia11.workers.dev';

export async function getMiddlewareHighlights(
  limit = 24,
  offset = 0,
  leagueId?: string
): Promise<MiddlewareHighlight[]> {
  try {
    const url = new URL(MIDDLEWARE_URL);
    url.searchParams.set('limit', String(limit));
    url.searchParams.set('offset', String(offset));
    if (leagueId && leagueId !== 'ALL') {
      url.searchParams.set('leagueId', leagueId);
    }

    const res = await fetch(url.toString(), {
      next: { revalidate: 60 },
    });

    if (!res.ok) {
      console.warn('Middleware highlights fetch failed:', res.status);
      return [];
    }

    const json: MiddlewareResponse = await res.json();
    return json.data || [];
  } catch (error) {
    console.error('Error fetching middleware highlights:', error);
    return [];
  }
}

export interface FilterOption {
  name: string;
  count: number;
  logo: string;
}

export interface MergedHighlightsResult {
  matches: MatchRecord[];
  hasMore: boolean;
  hasMoreSupabase: boolean;
  hasMoreMw: boolean;
  nextOffsetSupabase: number;
  nextOffsetMw: number;
}

const COMP_COUNTRY_MAP: Record<string, { country: string; flag: string }> = {
  PL: { country: 'England', flag: 'https://crests.football-data.org/770.svg' },
  PD: { country: 'Spain', flag: 'https://crests.football-data.org/760.svg' },
  BL1: { country: 'Germany', flag: 'https://crests.football-data.org/759.svg' },
  SA: { country: 'Italy', flag: 'https://crests.football-data.org/784.svg' },
  FL1: { country: 'France', flag: 'https://crests.football-data.org/773.svg' },
  DED: { country: 'Netherlands', flag: 'https://crests.football-data.org/8601.svg' },
  PPL: { country: 'Portugal', flag: 'https://crests.football-data.org/765.svg' },
  BSA: { country: 'Brazil', flag: 'https://crests.football-data.org/764.svg' },
  CL: { country: 'Europe', flag: 'https://crests.football-data.org/EUR.svg' },
  EC: { country: 'Europe', flag: 'https://crests.football-data.org/EUR.svg' },
};

function enrichSupabaseMatch(m: MatchRecord): MatchRecord {
  const comp = COMP_COUNTRY_MAP[m.competition_id];
  return {
    ...m,
    country_name: comp?.country || m.country_name || 'International',
    country_logo: comp?.flag || m.country_logo || null,
  };
}

export function middlewareHighlightToMatchRecord(item: MiddlewareHighlight): MatchRecord {
  const videoId = extractYouTubeId(item.embedUrl || item.url);
  const homeTeam = item.match?.homeTeam?.name || extractTeamsFromTitle(item.title).home;
  const awayTeam = item.match?.awayTeam?.name || extractTeamsFromTitle(item.title).away;

  return {
    id: `mw-${item.id}`,
    competition_id: item.match?.league?.id ? String(item.match.league.id) : 'MW',
    competition_name: item.match?.league?.name || item.channel || 'Official Highlight',
    utc_date: item.match?.date || new Date().toISOString(),
    status: 'FINISHED',
    home_team: homeTeam,
    away_team: awayTeam,
    home_team_name: homeTeam,
    away_team_name: awayTeam,
    home_team_crest: item.match?.homeTeam?.logo || null,
    away_team_crest: item.match?.awayTeam?.logo || null,
    home_score: null,
    away_score: null,
    highlight_video_id: videoId,
    recap_analysis: null,
    country_name: item.match?.country?.name || null,
    country_logo: item.match?.country?.logo || null,
  };
}

function extractYouTubeId(url: string): string {
  const match = url.match(/(?:embed\/|v=|vi\/|youtu\.be\/|\/v\/)([\w-]{11})/);
  return match ? match[1] : '';
}

function extractTeamsFromTitle(title: string): { home: string; away: string } {
  const clean = title.replace(/^[^:]+:\s*/, '').replace(/\s*Highlights.*$/i, '');
  const parts = clean.split(/\s+vs\.?\s+/i);
  if (parts.length >= 2) {
    return { home: parts[0].trim(), away: parts[1].trim() };
  }
  return { home: title, away: '' };
}

async function fetchAndProcessMw(
  count: number,
  offset: number,
  leagueIdParam?: string,
  selectedComp?: string,
  bigLeague?: (typeof BIG_LEAGUES)[number]
): Promise<{ records: MatchRecord[]; hasMore: boolean; nextOffset: number }> {
  const mwData = await getMiddlewareHighlights(count, offset, leagueIdParam);
  let records = mwData.map(middlewareHighlightToMatchRecord);
  if (selectedComp && selectedComp !== 'ALL' && bigLeague) {
    records = records.filter(
      (m) => isLeagueMatch(bigLeague.name, m.competition_name) || m.competition_id === selectedComp
    );
  }
  records.sort((a, b) => (new Date(b.utc_date).getTime() || 0) - (new Date(a.utc_date).getTime() || 0));
  return { records, hasMore: mwData.length >= count, nextOffset: offset + mwData.length };
}

export async function fetchMergedHighlightsPage(
  selectedComp: string,
  offsetSupabase: number,
  offsetMw: number,
  limitSupabase = 20,
  limitMw = 30
): Promise<MergedHighlightsResult> {
  const bigLeague = BIG_LEAGUES.find((b) => b.competitionId === selectedComp);
  const leagueIdParam = bigLeague?.leagueId ? String(bigLeague.leagueId) : undefined;

  let enrichedSupa: MatchRecord[] = [];
  let mwRecords: MatchRecord[] = [];
  let hasMoreSupabase = false;
  let hasMoreMw = false;
  let nextOffsetSupabase = offsetSupabase;
  let nextOffsetMw = offsetMw;

  // 1. Priority: Fetch Supabase items first (newest available) until exhausted
  if (offsetSupabase >= 0) {
    const supabaseData = await getRecentHighlights(limitSupabase, offsetSupabase, selectedComp);
    hasMoreSupabase = supabaseData.length >= limitSupabase;
    enrichedSupa = supabaseData
      .map(enrichSupabaseMatch)
      .sort((a, b) => (new Date(b.utc_date).getTime() || 0) - (new Date(a.utc_date).getTime() || 0));

    if (hasMoreSupabase) {
      nextOffsetSupabase = offsetSupabase + supabaseData.length;
      hasMoreMw = true;
    } else {
      nextOffsetSupabase = -1; // Supabase exhausted
      const deficit = limitSupabase - enrichedSupa.length;
      const mw = await fetchAndProcessMw(Math.max(deficit, limitMw), offsetMw, leagueIdParam, selectedComp, bigLeague);
      mwRecords = mw.records;
      hasMoreMw = mw.hasMore;
      nextOffsetMw = mw.nextOffset;
    }
  } else {
    // 2. Supabase already exhausted, page solely through newest Middleware items
    const mw = await fetchAndProcessMw(limitMw, offsetMw, leagueIdParam, selectedComp, bigLeague);
    mwRecords = mw.records;
    hasMoreMw = mw.hasMore;
    nextOffsetMw = mw.nextOffset;
  }

  // Combine and deduplicate
  const seen = new Set<string>();
  const combined: MatchRecord[] = [];
  for (const m of [...enrichedSupa, ...mwRecords]) {
    if (!m.highlight_video_id || !seen.has(m.highlight_video_id)) {
      if (m.highlight_video_id) seen.add(m.highlight_video_id);
      combined.push(m);
    }
  }

  return {
    matches: combined,
    hasMore: hasMoreSupabase || hasMoreMw,
    hasMoreSupabase,
    hasMoreMw,
    nextOffsetSupabase,
    nextOffsetMw,
  };
}

export async function fetchMergedHighlights(selectedComp: string): Promise<MatchRecord[]> {
  const res = await fetchMergedHighlightsPage(selectedComp, 0, 0, 30, 30);
  return res.matches;
}

export function extractFilterOptions(matches: MatchRecord[]): {
  countries: FilterOption[];
  leagues: FilterOption[];
} {
  const countryCounts = new Map<string, { count: number; logo: string }>();
  const leagueCounts = new Map<string, { count: number; logo: string }>();

  for (const m of matches) {
    if (m.country_name) {
      const cur = countryCounts.get(m.country_name) || { count: 0, logo: m.country_logo || '' };
      countryCounts.set(m.country_name, { count: cur.count + 1, logo: cur.logo || m.country_logo || '' });
    }
    if (m.competition_name) {
      const cur = leagueCounts.get(m.competition_name) || { count: 0, logo: '' };
      leagueCounts.set(m.competition_name, { count: cur.count + 1, logo: cur.logo });
    }
  }

  const countries: FilterOption[] = Array.from(countryCounts.entries())
    .map(([name, data]) => ({ name, count: data.count, logo: data.logo }))
    .sort((a, b) => b.count - a.count);

  const leagues: FilterOption[] = Array.from(leagueCounts.entries())
    .map(([name, data]) => ({ name, count: data.count, logo: data.logo }))
    .sort((a, b) => b.count - a.count);

  return { countries, leagues };
}


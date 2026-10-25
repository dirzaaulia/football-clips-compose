import { MatchRecord, getRecentHighlights } from './database';
import { isLeagueMatch } from '@/hooks/useCompetitions';
import {
  MiddlewareHighlight,
  MiddlewareFetchResult,
  MiddlewareResponse,
  MiddlewareMatch,
} from './middleware-types';

export * from './middleware-types';

const MIDDLEWARE_URL = 'https://football-clips-middleware.dirzaaulia11.workers.dev';

export async function getMiddlewareHighlights(
  limit = 30,
  offset = 0
): Promise<MiddlewareFetchResult> {
  try {
    const url = new URL(MIDDLEWARE_URL);
    url.searchParams.set('limit', String(limit));
    url.searchParams.set('offset', String(offset));

    const res = await fetch(url.toString(), {
      next: { revalidate: 60 },
    });

    if (!res.ok) {
      console.warn('Middleware highlights fetch failed:', res.status);
      return { items: [], hasMore: false, totalCount: 0 };
    }

    const json: MiddlewareResponse = await res.json();
    const data = json.data || [];
    const pagination = json.pagination || { limit, offset, totalCount: 0 };
    const hasMore = (pagination.offset + pagination.limit) < pagination.totalCount && data.length > 0;

    return {
      items: data,
      hasMore,
      totalCount: pagination.totalCount || 0,
    };
  } catch (error) {
    console.error('Error fetching middleware highlights:', error);
    return { items: [], hasMore: false, totalCount: 0 };
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
  hasMoreDb: boolean;
  hasMoreSupabase: boolean; // Backward compatibility alias
  hasMoreMw: boolean;
  nextOffsetDb: number;
  nextOffsetSupabase: number; // Backward compatibility alias
  nextOffsetMw: number;
}

const COMP_COUNTRY_MAP: Record<string, { country: string; flag: string }> = {
  PL: { country: 'England', flag: 'https://flagcdn.com/w40/gb-eng.png' },
  PD: { country: 'Spain', flag: 'https://flagcdn.com/w40/es.png' },
  BL1: { country: 'Germany', flag: 'https://flagcdn.com/w40/de.png' },
  SA: { country: 'Italy', flag: 'https://flagcdn.com/w40/it.png' },
  FL1: { country: 'France', flag: 'https://flagcdn.com/w40/fr.png' },
  DED: { country: 'Netherlands', flag: 'https://flagcdn.com/w40/nl.png' },
  PPL: { country: 'Portugal', flag: 'https://flagcdn.com/w40/pt.png' },
  BSA: { country: 'Brazil', flag: 'https://flagcdn.com/w40/br.png' },
  CL: { country: 'Europe', flag: 'https://flagcdn.com/w40/eu.png' },
  EC: { country: 'Europe', flag: 'https://flagcdn.com/w40/eu.png' },
  SPL: { country: 'Saudi Arabia', flag: 'https://flagcdn.com/w40/sa.png' },
  MLS: { country: 'USA', flag: 'https://flagcdn.com/w40/us.png' },
};

export function enrichDatabaseMatch(m: MatchRecord): MatchRecord {
  const comp = COMP_COUNTRY_MAP[m.competition_id];
  return {
    ...m,
    country_name: comp?.country || m.country_name || 'International',
    country_logo: comp?.flag || m.country_logo || null,
  };
}
const enrichSupabaseMatch = enrichDatabaseMatch;

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
    home_team: homeTeam, away_team: awayTeam,
    home_team_name: homeTeam, away_team_name: awayTeam,
    home_team_crest: item.match?.homeTeam?.logo || null,
    away_team_crest: item.match?.awayTeam?.logo || null,
    home_score: null, away_score: null,
    highlight_video_id: videoId, recap_analysis: null,
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
  selectedComp?: string,
  competition?: { name: string; competitionId: string | null }
): Promise<{ records: MatchRecord[]; hasMore: boolean; nextOffset: number }> {
  let currentOffset = offset;
  const collected: MatchRecord[] = [];
  let hasMore = true;

  // When a specific competition filter is active, scan up to 3 pages to discover matching items
  const maxAttempts = selectedComp && selectedComp !== 'ALL' ? 3 : 1;
  let attempts = 0;

  while (attempts < maxAttempts && collected.length < count && hasMore) {
    attempts++;
    const result = await getMiddlewareHighlights(count, currentOffset);
    currentOffset += count;
    hasMore = result.hasMore;

    let records = result.items.map(middlewareHighlightToMatchRecord);
    if (selectedComp && selectedComp !== 'ALL' && competition) {
      records = records.filter(
        (m) => isLeagueMatch(competition.name, m.competition_name) || m.competition_id === selectedComp
      );
    }
    collected.push(...records);

    if (result.items.length === 0 || !hasMore) {
      break;
    }
  }

  collected.sort((a, b) => (new Date(b.utc_date).getTime() || 0) - (new Date(a.utc_date).getTime() || 0));

  return {
    records: collected,
    hasMore,
    nextOffset: currentOffset,
  };
}

export async function fetchMergedHighlightsPage(
  selectedComp: string,
  offsetSupabase: number,
  offsetMw: number,
  limitSupabase = 20,
  limitMw = 30,
  season?: number,
  competitions?: { name: string; competitionId: string | null }[]
): Promise<MergedHighlightsResult> {
  const competition = competitions?.find((b) => b.competitionId === selectedComp);

  // If viewing historical archive season (e.g. 2025), fetch exclusively from Supabase archive
  if (season && season !== 2026) {
    if (offsetSupabase < 0) {
      return {
        matches: [],
        hasMore: false,
        hasMoreDb: false,
        hasMoreSupabase: false,
        hasMoreMw: false,
        nextOffsetDb: -1,
        nextOffsetSupabase: -1,
        nextOffsetMw: 0,
      };
    }
    const supabaseData = await getRecentHighlights(limitSupabase, offsetSupabase, selectedComp, season);
    const hasMore = supabaseData.length >= limitSupabase;
    const enriched = supabaseData
      .map(enrichDatabaseMatch)
      .sort((a, b) => (new Date(b.utc_date).getTime() || 0) - (new Date(a.utc_date).getTime() || 0));

    const nextDb = hasMore ? offsetSupabase + supabaseData.length : -1;
    return {
      matches: enriched,
      hasMore,
      hasMoreDb: hasMore,
      hasMoreSupabase: hasMore,
      hasMoreMw: false,
      nextOffsetDb: nextDb,
      nextOffsetSupabase: nextDb,
      nextOffsetMw: 0,
    };
  }

  let enrichedSupa: MatchRecord[] = [];
  let mwRecords: MatchRecord[] = [];
  let hasMoreSupabase = false;
  let hasMoreMw = false;
  let nextOffsetSupabase = offsetSupabase;
  let nextOffsetMw = offsetMw;

  // 1. Priority: Fetch Supabase items first (newest available) until exhausted
  if (offsetSupabase >= 0) {
    const supabaseData = await getRecentHighlights(limitSupabase, offsetSupabase, selectedComp, season);
    hasMoreSupabase = supabaseData.length >= limitSupabase;
    enrichedSupa = supabaseData
      .map(enrichSupabaseMatch)
      .sort((a, b) => (new Date(b.utc_date).getTime() || 0) - (new Date(a.utc_date).getTime() || 0));

    if (hasMoreSupabase) {
      nextOffsetSupabase = offsetSupabase + supabaseData.length;
      hasMoreMw = true;
    } else {
      nextOffsetSupabase = -1; // Supabase exhausted, transition to middleware
      const deficit = limitSupabase - enrichedSupa.length;
      const mw = await fetchAndProcessMw(Math.max(deficit, limitMw), offsetMw, selectedComp, competition);
      mwRecords = mw.records;
      hasMoreMw = mw.hasMore;
      nextOffsetMw = mw.nextOffset;
    }
  } else {
    // 2. Supabase already exhausted, page solely through newest Middleware items
    const mw = await fetchAndProcessMw(limitMw, offsetMw, selectedComp, competition);
    mwRecords = mw.records;
    hasMoreMw = mw.hasMore;
    nextOffsetMw = mw.nextOffset;
  }

  // Combine and deduplicate
  const seen = new Set<string>();
  const combined: MatchRecord[] = [];
  for (const m of [...enrichedSupa, ...mwRecords]) {
    const key = m.highlight_video_id || m.id;
    if (!seen.has(key)) {
      seen.add(key);
      combined.push(m);
    }
  }

  const overallHasMore = nextOffsetSupabase >= 0 ? (hasMoreSupabase || hasMoreMw) : hasMoreMw;

  return {
    matches: combined,
    hasMore: overallHasMore,
    hasMoreDb: hasMoreSupabase,
    hasMoreSupabase,
    hasMoreMw,
    nextOffsetDb: nextOffsetSupabase,
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


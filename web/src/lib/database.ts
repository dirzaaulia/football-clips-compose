/**
 * FootballClips PostgreSQL 16 Datastore Client
 * Target: Self-hosted PostgreSQL 16 on Oracle Cloud Super-Node via PostgREST.
 */
import { createClient } from '@supabase/supabase-js';

const databaseUrl = process.env.NEXT_PUBLIC_DATABASE_URL || process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://edge.fc.dirzaaulia.com';
const databaseApiKey = process.env.NEXT_PUBLIC_DATABASE_ANON_KEY || process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4';

export const database = createClient(databaseUrl, databaseApiKey);
export const supabase = database; // Alias for seamless backward compatibility

export interface StarterPlayer {
  name: string;
  jersey?: string;
  position?: string;
}

export interface TeamFormation {
  formation?: string;
  starters?: StarterPlayer[];
}

export interface MatchFormations {
  home?: TeamFormation;
  away?: TeamFormation;
}

export interface MatchRecord {
  id: string;
  competition_id: string;
  competition_name: string;
  utc_date: string;
  status: string;
  home_team: string;
  away_team: string;
  home_team_name?: string;
  away_team_name?: string;
  home_team_crest?: string | null;
  away_team_crest?: string | null;
  home_score?: number | null;
  away_score?: number | null;
  highlight_video_id?: string | null;
  backup_highlight_video_id?: string | null;
  recap_analysis?: string | null;
  slug?: string;
  matchday?: number;
  stage?: string;
  country_name?: string | null;
  country_logo?: string | null;
  season?: number;
  formations?: MatchFormations | null;
  statistics?: { home?: Record<string, string>; away?: Record<string, string> } | null;
  editorial_headline?: string | null;
  editorial_story?: string | null;
  editorial_source?: string | null;
  events?: MatchEvent[] | null;
  display_clock?: string | null;
  round_name?: string | null;
  youtube_region_info?: RegionRestriction | null;
  backup_youtube_region_info?: RegionRestriction | null;
  updated_at?: string | null;
}

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
}

export interface RegionRestriction {
  allowed?: string[];
  blocked?: string[];
}

export function isRegionSupported(region?: RegionRestriction | null, countryCode?: string | null): boolean {
  if (!region || !countryCode) return true;
  const upper = countryCode.toUpperCase().trim();
  if (region.allowed && region.allowed.length > 0) {
    return region.allowed.some(c => c.toUpperCase() === upper);
  }
  if (region.blocked && region.blocked.length > 0) {
    return !region.blocked.some(c => c.toUpperCase() === upper);
  }
  return true;
}

export interface StandingRecord {
  id: string;
  competition_code: string;
  competition_name: string;
  season: number;
  position: number;
  team_id: number;
  team_name: string;
  team_short_name?: string;
  crest_url?: string;
  played_games: number;
  won: number;
  draw: number;
  lost: number;
  points: number;
  goals_for: number;
  goals_against: number;
  goal_difference: number;
  form?: string;
}

export interface SupportedClubRecord {
  club_id: string;
  name: string;
  short_name: string;
  competition_code: string;
  crest_url: string;
}

function normalizeMatch(row: any): MatchRecord {
  const home = row.home_team_name || row.home_team || 'Home';
  const away = row.away_team_name || row.away_team || 'Away';
  return { ...row, id: String(row.id), home_team: home, away_team: away, home_team_name: home, away_team_name: away };
}

export async function getRecentHighlights(limit = 24, offset = 0, competitionId?: string, season?: number): Promise<MatchRecord[]> {
  let query = database
    .from('matches')
    .select('*')
    .not('highlight_video_id', 'is', null)
    .order('utc_date', { ascending: false })
    .order('id', { ascending: false })
    .range(offset, offset + limit - 1);

  if (competitionId && competitionId !== 'ALL') {
    query = query.ilike('competition_id', competitionId);
  }

  if (season) {
    query = query.eq('season', season);
  }

  const { data, error } = await query;
  if (error) {
    console.error('Error fetching highlights:', error);
    return [];
  }
  return (data || []).map(normalizeMatch);
}

export async function getMatchesBySeason(
  season: number,
  competitionId?: string,
  matchday?: number,
  limit = 50,
  offset = 0
): Promise<MatchRecord[]> {
  let query = database
    .from('matches')
    .select('*')
    .eq('season', season)
    .order('utc_date', { ascending: false })
    .order('id', { ascending: false })
    .range(offset, offset + limit - 1);

  if (competitionId && competitionId !== 'ALL') {
    query = query.ilike('competition_id', competitionId);
  }

  if (matchday && matchday > 0) {
    query = query.eq('matchday', matchday);
  }

  const { data, error } = await query;
  if (error) {
    console.error('Error fetching matches by season:', error);
    return [];
  }
  return (data || []).map(normalizeMatch);
}

export async function getMatchById(id: string): Promise<MatchRecord | null> {
  const { data, error } = await database
    .from('matches')
    .select('*')
    .eq('id', id)
    .single();

  if (error || !data) return null;
  return normalizeMatch(data);
}

export async function getLiveMatches(): Promise<MatchRecord[]> {
  const { data, error } = await database
    .from('matches')
    .select('*')
    .in('status', ['IN_PLAY', 'PAUSED', 'LIVE'])
    .order('utc_date', { ascending: true });

  if (error) {
    console.error('Error fetching live matches:', error);
    return [];
  }
  const now = Date.now();
  return (data || [])
    .filter((m) => (now - new Date(m.utc_date).getTime()) < 135 * 60 * 1000)
    .map(normalizeMatch);
}

export async function getFixturesByDate(dateStr: string): Promise<MatchRecord[]> {
  const startUtc = `${dateStr}T00:00:00Z`;
  const endUtc = `${dateStr}T23:59:59Z`;

  const { data, error } = await database
    .from('matches')
    .select('*')
    .gte('utc_date', startUtc)
    .lte('utc_date', endUtc)
    .order('utc_date', { ascending: true });

  if (error) {
    console.error('Error fetching fixtures:', error);
    return [];
  }
  return (data || []).map(normalizeMatch);
}

export async function getStandingsByCompetition(compCode: string, season = 2026): Promise<StandingRecord[]> {
  const { data, error } = await database
    .from('standings')
    .select('*')
    .ilike('competition_code', compCode)
    .eq('season', season)
    .order('position', { ascending: true });

  if (error) {
    console.error('Error fetching standings:', error);
    return [];
  }
  return (data as unknown as StandingRecord[]) || [];
}

export async function getRelatedMatches(
  competitionId: string,
  currentMatchId: string,
  limit = 8
): Promise<{ highlights: MatchRecord[]; fixtures: MatchRecord[] }> {
  try {
    const highlightsPromise = database
      .from('matches')
      .select('*')
      .eq('competition_id', competitionId)
      .neq('id', currentMatchId)
      .not('highlight_video_id', 'is', null)
      .order('utc_date', { ascending: false })
      .limit(limit);

    const fixturesPromise = database
      .from('matches')
      .select('*')
      .eq('competition_id', competitionId)
      .neq('id', currentMatchId)
      .in('status', ['IN_PLAY', 'PAUSED', 'LIVE', 'TIMED', 'SCHEDULED'])
      .order('utc_date', { ascending: true })
      .limit(limit);

    const [hRes, fRes] = await Promise.all([highlightsPromise, fixturesPromise]);

    let fixturesData = (fRes.data as unknown as MatchRecord[]) || [];
    if (fixturesData.length < 4) {
      const fb = await database.from('matches').select('*').eq('competition_id', competitionId).neq('id', currentMatchId).order('utc_date', { ascending: false }).limit(limit);
      if (fb.data) {
        const existingIds = new Set(fixturesData.map((f) => f.id));
        for (const item of (fb.data as unknown as MatchRecord[])) {
          if (!existingIds.has(item.id)) fixturesData.push(item);
        }
      }
    }

    return {
      highlights: (hRes.data || []).map(normalizeMatch),
      fixtures: fixturesData.slice(0, limit).map(normalizeMatch),
    };
  } catch (error) {
    console.error('Error fetching related matches for match centre:', error);
    return { highlights: [], fixtures: [] };
  }
}

export async function getKnockoutMatchesByCompetition(
  competitionId: string,
  season: number = 2026
): Promise<MatchRecord[]> {
  try {
    const { data, error } = await database
      .from('matches')
      .select('*')
      .eq('competition_id', competitionId)
      .eq('season', season)
      .order('utc_date', { ascending: true });

    if (error) {
      console.warn('Error fetching knockout matches:', error);
      return [];
    }

    return (data || []).map(normalizeMatch);
  } catch (err) {
    console.error('Exception in getKnockoutMatchesByCompetition:', err);
    return [];
  }
}


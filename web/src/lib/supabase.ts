import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://fc-edge-proxy.dirzaaulia11.workers.dev';
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

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
  recap_analysis?: string | null;
  slug?: string;
  matchday?: number;
  stage?: string;
  country_name?: string | null;
  country_logo?: string | null;
  season?: number;
  formations?: MatchFormations | null;
  statistics?: Record<'home' | 'away', Record<string, string>> | null;
  editorial_headline?: string | null;
  editorial_story?: string | null;
  editorial_source?: string | null;
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

export async function getRecentHighlights(limit = 24, offset = 0, competitionId?: string): Promise<MatchRecord[]> {
  let query = supabase
    .from('matches')
    .select('*')
    .not('highlight_video_id', 'is', null)
    .order('utc_date', { ascending: false })
    .order('id', { ascending: false })
    .range(offset, offset + limit - 1);

  if (competitionId && competitionId !== 'ALL') {
    query = query.ilike('competition_id', competitionId);
  }

  const { data, error } = await query;
  if (error) {
    console.error('Error fetching highlights:', error);
    return [];
  }
  return (data as unknown as MatchRecord[]) || [];
}

export async function getMatchById(id: string): Promise<MatchRecord | null> {
  const { data, error } = await supabase
    .from('matches')
    .select('*')
    .eq('id', id)
    .single();

  if (error || !data) return null;
  return (data as unknown as MatchRecord) || null;
}

export async function getLiveMatches(): Promise<MatchRecord[]> {
  const { data, error } = await supabase
    .from('matches')
    .select('*')
    .in('status', ['IN_PLAY', 'PAUSED', 'LIVE'])
    .order('utc_date', { ascending: true });

  if (error) {
    console.error('Error fetching live matches:', error);
    return [];
  }
  return (data as unknown as MatchRecord[]) || [];
}

export async function getFixturesByDate(dateStr: string): Promise<MatchRecord[]> {
  const startUtc = `${dateStr}T00:00:00Z`;
  const endUtc = `${dateStr}T23:59:59Z`;

  const { data, error } = await supabase
    .from('matches')
    .select('*')
    .gte('utc_date', startUtc)
    .lte('utc_date', endUtc)
    .order('utc_date', { ascending: true });

  if (error) {
    console.error('Error fetching fixtures:', error);
    return [];
  }
  return (data as unknown as MatchRecord[]) || [];
}

export async function getStandingsByCompetition(compCode: string, season = 2026): Promise<StandingRecord[]> {
  const { data, error } = await supabase
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
    const highlightsPromise = supabase
      .from('matches')
      .select('*')
      .eq('competition_id', competitionId)
      .neq('id', currentMatchId)
      .not('highlight_video_id', 'is', null)
      .order('utc_date', { ascending: false })
      .limit(limit);

    const fixturesPromise = supabase
      .from('matches')
      .select('*')
      .eq('competition_id', competitionId)
      .neq('id', currentMatchId)
      .order('utc_date', { ascending: false })
      .limit(limit);

    const [hRes, fRes] = await Promise.all([highlightsPromise, fixturesPromise]);

    return {
      highlights: (hRes.data as unknown as MatchRecord[]) || [],
      fixtures: (fRes.data as unknown as MatchRecord[]) || [],
    };
  } catch (error) {
    console.error('Error fetching related matches for match centre:', error);
    return { highlights: [], fixtures: [] };
  }
}


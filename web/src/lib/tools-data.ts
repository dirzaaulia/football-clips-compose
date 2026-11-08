import { supabase, StandingRecord } from '@/lib/database';

export interface SupportedCompetition {
  code: string;
  name: string;
  category: 'European Competitions' | 'Domestic Leagues' | 'Domestic Cups' | 'South American Competitions';
  emblemUrl: string;
}

export const ALL_SUPPORTED_COMPETITIONS: SupportedCompetition[] = [
  // European Competitions
  { code: 'CL', name: 'UEFA Champions League', category: 'European Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2.png' },
  { code: 'EL', name: 'UEFA Europa League', category: 'European Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2310.png' },
  { code: 'ECL', name: 'UEFA Conference League', category: 'European Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/20296.png' },

  // Big 5 & Major Global Leagues
  { code: 'PL', name: 'Premier League', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/23.png' },
  { code: 'PD', name: 'La Liga', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/15.png' },
  { code: 'SA', name: 'Serie A', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/12.png' },
  { code: 'BL1', name: 'Bundesliga', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/10.png' },
  { code: 'FL1', name: 'Ligue 1', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/9.png' },
  { code: 'SPL', name: 'Saudi Pro League', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2488.png' },
  { code: 'MLS', name: 'Major League Soccer', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/19.png' },
  { code: 'DED', name: 'Eredivisie', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/11.png' },
  { code: 'PPL', name: 'Liga Portugal', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/14.png' },
  { code: 'IDN', name: 'Liga Indonesia', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2336.png' },
  { code: 'BRA', name: 'Brasileirão Série A', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/85.png' },
  { code: 'ARG', name: 'Argentine Liga Profesional', category: 'Domestic Leagues', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/1.png' },

  // Domestic Cups
  { code: 'FAC', name: 'FA Cup', category: 'Domestic Cups', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/40.png' },
  { code: 'EFL', name: 'EFL Carabao Cup', category: 'Domestic Cups', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/41.png' },
  { code: 'CDR', name: 'Copa del Rey', category: 'Domestic Cups', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/80.png' },
  { code: 'DFB', name: 'DFB-Pokal', category: 'Domestic Cups', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2061.png' },
  { code: 'CIT', name: 'Coppa Italia', category: 'Domestic Cups', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/83.png' },

  // South American Competitions
  { code: 'LIB', name: 'CONMEBOL Libertadores', category: 'South American Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/58.png' },
  { code: 'SUD', name: 'CONMEBOL Sudamericana', category: 'South American Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/1208.png' },
  { code: 'REC', name: 'CONMEBOL Recopa', category: 'South American Competitions', emblemUrl: 'https://a.espncdn.com/i/leaguelogos/soccer/500-dark/2335.png' },
];

export interface ClubTacticalProfile {
  id: string;
  name: string;
  shortName: string;
  league: string;
  emblem: string;
  archetype: 'High Press & Direct Vertical' | 'Positional Possession' | 'Mid-Block Counter' | 'Low-Block Compact' | 'Asymmetric Wing Overload';
  goalsPerMatch: number;
  concededPerMatch: number;
  cleanSheetPct: number;
  possessionAvg: number;
  xGPerMatch: number;
  pressIntensity: 'Very High' | 'High' | 'Moderate' | 'Conservative';
  recentForm: ('W' | 'D' | 'L')[];
}

export function standingToTacticalProfile(s: StandingRecord, leagueName: string): ClubTacticalProfile {
  const games = Math.max(1, s.played_games || 1);
  const gf = s.goals_for || 0;
  const ga = s.goals_against || 0;
  const gpm = Number((gf / games).toFixed(2));
  const cpm = Number((ga / games).toFixed(2));

  let archetype: ClubTacticalProfile['archetype'] = 'Positional Possession';
  let press: ClubTacticalProfile['pressIntensity'] = 'High';

  if (gpm >= 2.2 && cpm <= 0.9) {
    archetype = 'High Press & Direct Vertical';
    press = 'Very High';
  } else if (cpm <= 0.8) {
    archetype = 'Asymmetric Wing Overload';
    press = 'Moderate';
  } else if (gpm <= 1.4 && cpm >= 1.3) {
    archetype = 'Low-Block Compact';
    press = 'Conservative';
  } else if (cpm > gpm) {
    archetype = 'Mid-Block Counter';
    press = 'Moderate';
  }

  const rawForm = (s.form || 'WDLWW').toUpperCase();
  const recentForm: ('W' | 'D' | 'L')[] = rawForm
    .split('')
    .filter((c) => c === 'W' || c === 'D' || c === 'L')
    .slice(-5) as ('W' | 'D' | 'L')[];

  if (recentForm.length === 0) {
    recentForm.push('W', 'D', 'W', 'W', 'D');
  }

  const cleanSheetPct = Math.min(85, Math.max(15, Math.round(((games * 2.2 - ga) / (games * 2.2)) * 100)));

  return {
    id: String(s.team_id || s.id),
    name: s.team_name,
    shortName: s.team_short_name || s.team_name,
    league: leagueName,
    emblem: s.crest_url || '/app_icon.png',
    archetype,
    goalsPerMatch: gpm,
    concededPerMatch: cpm,
    cleanSheetPct,
    possessionAvg: Math.min(68, Math.max(42, Math.round(50 + (gpm - cpm) * 6))),
    xGPerMatch: Number((gpm * 0.95 + 0.1).toFixed(2)),
    pressIntensity: press,
    recentForm,
  };
}

export async function fetchLiveStandings(compCode: string, season = 2026): Promise<StandingRecord[]> {
  try {
    const { data, error } = await supabase
      .from('standings')
      .select('*')
      .ilike('competition_code', compCode)
      .eq('season', season)
      .order('position', { ascending: true });

    if (error || !data) {
      console.warn(`[ToolsData] Standings fallback for ${compCode}:`, error);
      return [];
    }
    return data as StandingRecord[];
  } catch (err) {
    console.error(`[ToolsData] Exception fetching standings for ${compCode}:`, err);
    return [];
  }
}

export const FALLBACK_TACTICAL_PROFILES: ClubTacticalProfile[] = [
  {
    id: '359',
    name: 'Arsenal',
    shortName: 'Arsenal',
    league: 'Premier League',
    emblem: 'https://a.espncdn.com/i/teamlogos/soccer/500/359.png',
    archetype: 'High Press & Direct Vertical',
    goalsPerMatch: 2.15,
    concededPerMatch: 0.74,
    cleanSheetPct: 52,
    possessionAvg: 61,
    xGPerMatch: 2.10,
    pressIntensity: 'Very High',
    recentForm: ['W', 'W', 'D', 'W', 'W'],
  },
  {
    id: '382',
    name: 'Manchester City',
    shortName: 'Man City',
    league: 'Premier League',
    emblem: 'https://a.espncdn.com/i/teamlogos/soccer/500/382.png',
    archetype: 'Positional Possession',
    goalsPerMatch: 2.38,
    concededPerMatch: 0.92,
    cleanSheetPct: 44,
    possessionAvg: 65,
    xGPerMatch: 2.32,
    pressIntensity: 'High',
    recentForm: ['W', 'D', 'W', 'W', 'L'],
  },
  {
    id: '86',
    name: 'Real Madrid',
    shortName: 'Real Madrid',
    league: 'La Liga',
    emblem: 'https://a.espncdn.com/i/teamlogos/soccer/500/86.png',
    archetype: 'Asymmetric Wing Overload',
    goalsPerMatch: 2.29,
    concededPerMatch: 0.81,
    cleanSheetPct: 48,
    possessionAvg: 59,
    xGPerMatch: 2.20,
    pressIntensity: 'Moderate',
    recentForm: ['W', 'W', 'W', 'L', 'W'],
  },
  {
    id: '83',
    name: 'Barcelona',
    shortName: 'Barcelona',
    league: 'La Liga',
    emblem: 'https://a.espncdn.com/i/teamlogos/soccer/500/83.png',
    archetype: 'High Press & Direct Vertical',
    goalsPerMatch: 2.65,
    concededPerMatch: 1.05,
    cleanSheetPct: 38,
    possessionAvg: 64,
    xGPerMatch: 2.58,
    pressIntensity: 'Very High',
    recentForm: ['W', 'W', 'W', 'W', 'L'],
  },
];

export const CLUBS_PROFILES = FALLBACK_TACTICAL_PROFILES;

export interface BroadcasterDirectoryEntry {
  countryCode: string;
  countryName: string;
  flag: string;
  competitions: {
    competitionName: string;
    competitionCode: string;
    broadcasterName: string;
    platformType: 'Streaming & TV' | 'OTT Streaming' | 'Free-to-Air' | 'Linear Cable';
    officialWebsite: string;
    commentaryLanguages: string[];
  }[];
}

export const TOP_BROADCASTER_REGIONS: BroadcasterDirectoryEntry[] = [
  {
    countryCode: 'US',
    countryName: 'United States',
    flag: '🇺🇸',
    competitions: [
      { competitionName: 'UEFA Champions & Europa League', competitionCode: 'CL', broadcasterName: 'CBS Sports / Paramount+', platformType: 'Streaming & TV', officialWebsite: 'https://www.paramountplus.com', commentaryLanguages: ['English', 'Spanish'] },
      { competitionName: 'Premier League', competitionCode: 'PL', broadcasterName: 'NBC Sports & Peacock', platformType: 'Streaming & TV', officialWebsite: 'https://www.peacocktv.com', commentaryLanguages: ['English', 'Spanish'] },
      { competitionName: 'La Liga, Bundesliga & FA Cup', competitionCode: 'PD', broadcasterName: 'ESPN+ / ABC', platformType: 'OTT Streaming', officialWebsite: 'https://plus.espn.com', commentaryLanguages: ['English', 'Spanish'] },
      { competitionName: 'Serie A & Coppa Italia', competitionCode: 'SA', broadcasterName: 'CBS Sports / Paramount+', platformType: 'OTT Streaming', officialWebsite: 'https://www.paramountplus.com', commentaryLanguages: ['English', 'Italian'] },
      { competitionName: 'Major League Soccer', competitionCode: 'MLS', broadcasterName: 'Apple TV (MLS Season Pass)', platformType: 'OTT Streaming', officialWebsite: 'https://tv.apple.com', commentaryLanguages: ['English', 'Spanish', 'French'] },
      { competitionName: 'CONMEBOL Libertadores & Sudamericana', competitionCode: 'LIB', broadcasterName: 'beIN Sports USA', platformType: 'Streaming & TV', officialWebsite: 'https://www.beinsports.com/us', commentaryLanguages: ['English', 'Spanish'] },
    ],
  },
  {
    countryCode: 'GB',
    countryName: 'United Kingdom',
    flag: '🇬🇧',
    competitions: [
      { competitionName: 'UEFA Champions & Europa League', competitionCode: 'CL', broadcasterName: 'TNT Sports & discovery+', platformType: 'Streaming & TV', officialWebsite: 'https://www.discoveryplus.com/gb', commentaryLanguages: ['English'] },
      { competitionName: 'Premier League & EFL Carabao Cup', competitionCode: 'PL', broadcasterName: 'Sky Sports & TNT Sports', platformType: 'Streaming & TV', officialWebsite: 'https://www.skysports.com', commentaryLanguages: ['English'] },
      { competitionName: 'La Liga', competitionCode: 'PD', broadcasterName: 'Premier Sports & ITV', platformType: 'Streaming & TV', officialWebsite: 'https://www.premiersports.com', commentaryLanguages: ['English'] },
      { competitionName: 'Emirates FA Cup', competitionCode: 'FAC', broadcasterName: 'BBC & ITV Sport', platformType: 'Free-to-Air', officialWebsite: 'https://www.bbc.co.uk/sport', commentaryLanguages: ['English'] },
      { competitionName: 'Serie A & Ligue 1', competitionCode: 'SA', broadcasterName: 'TNT Sports', platformType: 'Streaming & TV', officialWebsite: 'https://www.tntsports.co.uk', commentaryLanguages: ['English'] },
    ],
  },
  {
    countryCode: 'ID',
    countryName: 'Indonesia',
    flag: '🇮🇩',
    competitions: [
      { competitionName: 'UEFA Champions & Europa League', competitionCode: 'CL', broadcasterName: 'beIN Sports & Vidio', platformType: 'Streaming & TV', officialWebsite: 'https://www.vidio.com', commentaryLanguages: ['Indonesian', 'English'] },
      { competitionName: 'Premier League', competitionCode: 'PL', broadcasterName: 'Vidio & SCTV', platformType: 'Streaming & TV', officialWebsite: 'https://www.vidio.com', commentaryLanguages: ['Indonesian', 'English'] },
      { competitionName: 'La Liga & FA Cup', competitionCode: 'PD', broadcasterName: 'beIN Sports', platformType: 'OTT Streaming', officialWebsite: 'https://connect-id.beinsports.com', commentaryLanguages: ['Indonesian', 'English'] },
      { competitionName: 'Serie A', competitionCode: 'SA', broadcasterName: 'Vidio', platformType: 'OTT Streaming', officialWebsite: 'https://www.vidio.com', commentaryLanguages: ['Indonesian', 'English'] },
      { competitionName: 'BRI Super League (Liga 1)', competitionCode: 'IDN', broadcasterName: 'Indosiar & Vidio', platformType: 'Streaming & TV', officialWebsite: 'https://www.vidio.com', commentaryLanguages: ['Indonesian'] },
      { competitionName: 'Saudi Pro League', competitionCode: 'SPL', broadcasterName: 'SPOTV & Vision+', platformType: 'OTT Streaming', officialWebsite: 'https://www.visionplus.id', commentaryLanguages: ['Indonesian', 'English'] },
    ],
  },
  {
    countryCode: 'ES',
    countryName: 'Spain',
    flag: '🇪🇸',
    competitions: [
      { competitionName: 'La Liga', competitionCode: 'PD', broadcasterName: 'Movistar+ & DAZN', platformType: 'Streaming & TV', officialWebsite: 'https://www.dazn.com', commentaryLanguages: ['Spanish'] },
      { competitionName: 'UEFA Champions League', competitionCode: 'CL', broadcasterName: 'Movistar Liga de Campeones', platformType: 'Linear Cable', officialWebsite: 'https://www.movistar.es', commentaryLanguages: ['Spanish'] },
      { competitionName: 'Copa del Rey', competitionCode: 'CDR', broadcasterName: 'RTVE & Movistar+', platformType: 'Free-to-Air', officialWebsite: 'https://www.rtve.es', commentaryLanguages: ['Spanish'] },
      { competitionName: 'Premier League', competitionCode: 'PL', broadcasterName: 'DAZN Spain', platformType: 'OTT Streaming', officialWebsite: 'https://www.dazn.com', commentaryLanguages: ['Spanish'] },
    ],
  },
  {
    countryCode: 'DE',
    countryName: 'Germany',
    flag: '🇩🇪',
    competitions: [
      { competitionName: 'Bundesliga', competitionCode: 'BL1', broadcasterName: 'Sky Sport & DAZN', platformType: 'Streaming & TV', officialWebsite: 'https://www.dazn.com', commentaryLanguages: ['German'] },
      { competitionName: 'UEFA Champions League', competitionCode: 'CL', broadcasterName: 'DAZN & Prime Video', platformType: 'OTT Streaming', officialWebsite: 'https://www.dazn.com', commentaryLanguages: ['German'] },
      { competitionName: 'DFB-Pokal', competitionCode: 'DFB', broadcasterName: 'ARD, ZDF & Sky Sport', platformType: 'Free-to-Air', officialWebsite: 'https://www.sportschau.de', commentaryLanguages: ['German'] },
      { competitionName: 'Premier League', competitionCode: 'PL', broadcasterName: 'Sky Sport Deutschland', platformType: 'Streaming & TV', officialWebsite: 'https://sport.sky.de', commentaryLanguages: ['German'] },
    ],
  },
];

export const BROADCAST_DIRECTORIES = TOP_BROADCASTER_REGIONS;


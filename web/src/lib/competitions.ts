import { MARKET_COMPETITIONS } from '@/lib/marketCompetitions';

export interface CompetitionMeta {
  id: string;
  name: string;
  shortName: string;
  logo: string;
}

const COMPETITION_REGISTRY: Record<string, CompetitionMeta> = {
  ...MARKET_COMPETITIONS,
  CL: {
    id: 'CL',
    name: 'UEFA Champions League',
    shortName: 'UCL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2.png',
  },
  EL: {
    id: 'EL',
    name: 'UEFA Europa League',
    shortName: 'UEL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2310.png',
  },
  ECL: {
    id: 'ECL',
    name: 'UEFA Conference League',
    shortName: 'UECL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png',
  },
  PL: {
    id: 'PL',
    name: 'Premier League',
    shortName: 'Premier League',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/23.png',
  },
  PD: {
    id: 'PD',
    name: 'La Liga',
    shortName: 'LaLiga',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/15.png',
  },
  SA: {
    id: 'SA',
    name: 'Serie A',
    shortName: 'Serie A',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/12.png',
  },
  BL1: {
    id: 'BL1',
    name: 'Bundesliga',
    shortName: 'Bundesliga',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/10.png',
  },
  FL1: {
    id: 'FL1',
    name: 'Ligue 1',
    shortName: 'Ligue 1',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/9.png',
  },
  SPL: {
    id: 'SPL',
    name: 'Saudi Pro League',
    shortName: 'Saudi Pro',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png',
  },
  MLS: {
    id: 'MLS',
    name: 'Major League Soccer',
    shortName: 'MLS',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/19.png',
  },
  DED: {
    id: 'DED',
    name: 'Eredivisie',
    shortName: 'Eredivisie',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/11.png',
  },
  PPL: {
    id: 'PPL',
    name: 'Liga Portugal',
    shortName: 'Liga Portugal',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/14.png',
  },
  IDN: {
    id: 'IDN',
    name: 'BRI Super League (Liga Indonesia)',
    shortName: 'Liga Indonesia',
    logo: 'https://fc.dirzaaulia.com/logos/competitions/idn-2026.png',
  },
  FAC: {
    id: 'FAC',
    name: 'FA Cup',
    shortName: 'FA Cup',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/40.png',
  },
  EFL: {
    id: 'EFL',
    name: 'Carabao Cup',
    shortName: 'EFL Cup',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/41.png',
  },
  CDR: {
    id: 'CDR',
    name: 'Copa del Rey',
    shortName: 'Copa del Rey',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/80.png',
  },
  DFB: {
    id: 'DFB',
    name: 'DFB-Pokal',
    shortName: 'DFB-Pokal',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png',
  },
  CIT: {
    id: 'CIT',
    name: 'Coppa Italia',
    shortName: 'Coppa Italia',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/83.png',
  },
  LIB: {
    id: 'LIB',
    name: 'CONMEBOL Libertadores',
    shortName: 'Libertadores',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/58.png',
  },
  SUD: {
    id: 'SUD',
    name: 'CONMEBOL Sudamericana',
    shortName: 'Sudamericana',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png',
  },
  REC: {
    id: 'REC',
    name: 'CONMEBOL Recopa',
    shortName: 'Recopa',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png',
  },
  BRA: {
    id: 'BRA',
    name: 'Brasileirao Serie A',
    shortName: 'Brasileirao',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/85.png',
  },
  ARG: {
    id: 'ARG',
    name: 'Argentine Liga Profesional',
    shortName: 'Liga Argentina',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/1.png',
  },
  UNL: {
    id: 'UNL',
    name: 'UEFA Nations League',
    shortName: 'Nations Lge',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2395.png',
  },
  CNL: {
    id: 'CNL',
    name: 'Concacaf Nations League',
    shortName: 'Concacaf NL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2406.png',
  },
  WCQ_CONMEBOL: {
    id: 'WCQ_CONMEBOL',
    name: 'FIFA World Cup Qualifying - CONMEBOL',
    shortName: 'WCQ CONMEBOL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/65.png',
  },
  WCQ_AFC: {
    id: 'WCQ_AFC',
    name: 'FIFA World Cup Qualifying - AFC',
    shortName: 'WCQ Asia',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/62.png',
  },
  WCQ_UEFA: {
    id: 'WCQ_UEFA',
    name: 'FIFA World Cup Qualifying - UEFA',
    shortName: 'WCQ Europe',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/67.png',
  },
  WCQ_CONCACAF: {
    id: 'WCQ_CONCACAF',
    name: 'FIFA World Cup Qualifying - Concacaf',
    shortName: 'WCQ Concacaf',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/64.png',
  },
  WCQ_CAF: {
    id: 'WCQ_CAF',
    name: 'FIFA World Cup Qualifying - CAF',
    shortName: 'WCQ Africa',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/63.png',
  },
  FRI: {
    id: 'FRI',
    name: 'International Friendlies',
    shortName: 'Friendlies',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/53.png',
  },
};

export function getCompetitionMeta(
  competitionId?: string | null,
  competitionName?: string | null,
): CompetitionMeta {
  if (competitionId && COMPETITION_REGISTRY[competitionId]) {
    return COMPETITION_REGISTRY[competitionId];
  }
  if (competitionName) {
    const raw = competitionName.toLowerCase().trim();
    for (const key of Object.keys(COMPETITION_REGISTRY)) {
      const item = COMPETITION_REGISTRY[key];
      if (
        item.name.toLowerCase() === raw ||
        item.shortName.toLowerCase() === raw ||
        raw.includes(item.name.toLowerCase()) ||
        raw.includes(item.shortName.toLowerCase())
      ) {
        return item;
      }
    }
    if (raw.includes('champions league') || raw.includes('ucl')) return COMPETITION_REGISTRY.CL;
    if (raw.includes('europa league') || raw.includes('uel')) return COMPETITION_REGISTRY.EL;
    if (raw.includes('conference') || raw.includes('uecl')) return COMPETITION_REGISTRY.ECL;
    if (raw.includes('premier') || raw.includes('epl')) return COMPETITION_REGISTRY.PL;
    if (raw.includes('la liga') || raw.includes('laliga')) return COMPETITION_REGISTRY.PD;
    if (raw.includes('serie a')) return COMPETITION_REGISTRY.SA;
    if (raw.includes('bundesliga')) return COMPETITION_REGISTRY.BL1;
    if (raw.includes('ligue 1')) return COMPETITION_REGISTRY.FL1;
    if (raw.includes('copa del rey')) return COMPETITION_REGISTRY.CDR;
    if (raw.includes('fa cup')) return COMPETITION_REGISTRY.FAC;
    if (raw.includes('concacaf')) return COMPETITION_REGISTRY.CNL;
    if (raw.includes('nations league')) return COMPETITION_REGISTRY.UNL;
    const cleanShort = competitionName
      .replace(/^(FIFA|UEFA|CONMEBOL|CONCACAF|AFC|CAF)\s+/i, '')
      .replace(/\s+Qualifying/i, ' Q.')
      .trim();
    return {
      id: competitionId || 'COMP',
      name: competitionName,
      shortName: cleanShort || competitionName,
      logo: '',
    };
  }

  return {
    id: 'COMP',
    name: 'Competition',
    shortName: 'UCL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2.png',
  };
}

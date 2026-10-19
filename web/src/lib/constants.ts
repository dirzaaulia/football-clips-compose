export interface BigLeague {
  name: string;
  leagueId: number | null;
  countryCode: string | null;
  competitionId: string | null;
  logo: string;
}

export const BIG_LEAGUES: BigLeague[] = [
  {
    name: 'All Competitions',
    leagueId: null,
    countryCode: null,
    competitionId: 'ALL',
    logo: '',
  },
  {
    name: 'Champions League',
    leagueId: 2486,
    countryCode: 'EU',
    competitionId: 'CL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2.png',
  },
  {
    name: 'Europa League',
    leagueId: 3337,
    countryCode: 'EU',
    competitionId: 'EL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2487.png',
  },
  {
    name: 'Conference League',
    leagueId: 3338,
    countryCode: 'EU',
    competitionId: 'ECL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/20349.png',
  },
  {
    name: 'Premier League',
    leagueId: 33973,
    countryCode: 'GB-ENG',
    competitionId: 'PL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/23.png',
  },
  {
    name: 'La Liga',
    leagueId: 119924,
    countryCode: 'ES',
    competitionId: 'PD',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/15.png',
  },
  {
    name: 'Serie A',
    leagueId: 115669,
    countryCode: 'IT',
    competitionId: 'SA',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/12.png',
  },
  {
    name: 'Bundesliga',
    leagueId: 67162,
    countryCode: 'DE',
    competitionId: 'BL1',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/10.png',
  },
  {
    name: 'Ligue 1',
    leagueId: 52695,
    countryCode: 'FR',
    competitionId: 'FL1',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/9.png',
  },
  {
    name: 'Saudi Pro League',
    leagueId: null,
    countryCode: 'SA',
    competitionId: 'SPL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png',
  },
  {
    name: 'MLS',
    leagueId: null,
    countryCode: 'US',
    competitionId: 'MLS',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/19.png',
  },
  {
    name: 'Eredivisie',
    leagueId: null,
    countryCode: 'NL',
    competitionId: 'DED',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/11.png',
  },
  {
    name: 'Liga Portugal',
    leagueId: null,
    countryCode: 'PT',
    competitionId: 'PPL',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/14.png',
  },
];

export function normalizeLeagueName(name: string): string {
  const trimmed = name.trim();
  const lower = trimmed.toLowerCase();

  if (
    lower === 'primera division' ||
    lower === 'primera división' ||
    lower === 'spanish primera division' ||
    lower === 'laliga' ||
    lower === 'la liga' ||
    lower === 'pd'
  ) {
    return 'La Liga';
  }

  if (lower === 'premier league' || lower === 'epl' || lower === 'pl') {
    return 'Premier League';
  }

  if (lower === 'serie a' || lower === 'sa') {
    return 'Serie A';
  }

  if (lower === 'bundesliga' || lower === 'bl1') {
    return 'Bundesliga';
  }

  if (lower === 'ligue 1' || lower === 'fl1') {
    return 'Ligue 1';
  }

  if (lower === 'uefa champions league' || lower === 'champions league' || lower === 'cl') {
    return 'Champions League';
  }

  if (lower === 'uefa europa league' || lower === 'europa league' || lower === 'el') {
    return 'Europa League';
  }

  if (
    lower === 'uefa conference league' ||
    lower === 'uefa europa conference league' ||
    lower === 'conference league' ||
    lower === 'ecl' ||
    lower === 'uecl'
  ) {
    return 'Conference League';
  }

  if (lower === 'saudi pro league' || lower === 'saudi professional league' || lower === 'spl') {
    return 'Saudi Pro League';
  }

  if (lower === 'major league soccer' || lower === 'mls') {
    return 'MLS';
  }

  if (lower === 'eredivisie' || lower === 'ded' || lower === 'ed') {
    return 'Eredivisie';
  }

  if (lower === 'liga portugal' || lower === 'primeira liga' || lower === 'ppl') {
    return 'Liga Portugal';
  }

  return trimmed;
}

export function isLeagueMatch(target?: string | null, candidate?: string | null): boolean {
  if (!target || !candidate) return false;
  const t = target.toLowerCase().trim();
  const c = candidate.toLowerCase().trim();
  if (t === c) return true;

  const normT = normalizeLeagueName(t).toLowerCase();
  const normC = normalizeLeagueName(c).toLowerCase();
  if (normT === normC) return true;

  const plRegex = /\b(pl|epl|premier league)\b/i;
  if (plRegex.test(t) && plRegex.test(c)) return true;

  const laligaRegex = /\b(la liga|laliga|primera division|primera división|pd)\b/i;
  if (laligaRegex.test(t) && laligaRegex.test(c)) return true;

  return t.includes(c) || c.includes(t);
}

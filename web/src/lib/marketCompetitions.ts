import type { CompetitionMeta } from '@/lib/competitions';

export const MARKET_COMPETITIONS: Record<string, CompetitionMeta> = {
  ACL: {
    id: 'ACL',
    name: 'AFC Champions League Elite',
    shortName: 'ACL Elite',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2200.png',
  },
  ACL2: {
    id: 'ACL2',
    name: 'AFC Champions League Two',
    shortName: 'ACL Two',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2243.png',
  },
  ENG2: {
    id: 'ENG2',
    name: 'EFL Championship',
    shortName: 'Championship',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/24.png',
  },
  JPN1: {
    id: 'JPN1',
    name: 'J1 League',
    shortName: 'J1 League',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2199.png',
  },
  TUR1: {
    id: 'TUR1',
    name: 'Turkish Super Lig',
    shortName: 'Super Lig',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/18.png',
  },
  MEX1: {
    id: 'MEX1',
    name: 'Liga MX',
    shortName: 'Liga MX',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/22.png',
  },
  BEL1: {
    id: 'BEL1',
    name: 'Belgian Pro League',
    shortName: 'Pro League',
    logo: 'https://a.espncdn.com/i/leaguelogos/soccer/500/6.png',
  },
};

import { NextResponse } from 'next/server';
import { supabase } from '@/lib/database';

export const revalidate = 3600; // 1 hour edge cache

const ESPN_COMPETITION_LOGOS: Record<string, string> = {
  CL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2.png',
  EL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2310.png',
  ECL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png',
  PL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/23.png',
  PD: 'https://a.espncdn.com/i/leaguelogos/soccer/500/15.png',
  SA: 'https://a.espncdn.com/i/leaguelogos/soccer/500/12.png',
  BL1: 'https://a.espncdn.com/i/leaguelogos/soccer/500/10.png',
  FL1: 'https://a.espncdn.com/i/leaguelogos/soccer/500/9.png',
  FAC: 'https://a.espncdn.com/i/leaguelogos/soccer/500/40.png',
  EFL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/41.png',
  CDR: 'https://a.espncdn.com/i/leaguelogos/soccer/500/80.png',
  DFB: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png',
  CIT: 'https://a.espncdn.com/i/leaguelogos/soccer/500/83.png',
  PPL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/14.png',
  DED: 'https://a.espncdn.com/i/leaguelogos/soccer/500/11.png',
  MLS: 'https://a.espncdn.com/i/leaguelogos/soccer/500/19.png',
  SPL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png',
  IDN: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2336.png',
  LIB: 'https://a.espncdn.com/i/leaguelogos/soccer/500/58.png',
  SUD: 'https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png',
  REC: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png',
  BRA: 'https://a.espncdn.com/i/leaguelogos/soccer/500/85.png',
  ARG: 'https://a.espncdn.com/i/leaguelogos/soccer/500/1.png',
};

export async function GET() {
  try {
    const edgeUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://edge.fc.dirzaaulia.com';
    const cleanUrl = edgeUrl.replace(/\/rest\/v1\/?$/, '').replace(/\/$/, '');
    const res = await fetch(`${cleanUrl}/rest/v1/competitions?is_active=eq.true&order=display_order.asc`, {
      next: { revalidate: 3600 },
      headers: {
        'Accept': 'application/json',
      },
    });

    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        return NextResponse.json(data.map((competition) => {
          const code = competition.code || competition.id;
          const espnLogo = ESPN_COMPETITION_LOGOS[code];
          const emblem = competition.emblem_url && !competition.emblem_url.includes('football-data.org')
            ? competition.emblem_url
            : (espnLogo || competition.emblem_url || '');
          return {
            ...competition,
            code: code,
            emblem_url: emblem,
          };
        }));
      }
    }
  } catch (err) {
    console.error('Failed to fetch dynamic competitions from PostgreSQL:', err);
  }

  return NextResponse.json([]);
}

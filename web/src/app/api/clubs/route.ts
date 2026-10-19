import { NextResponse } from 'next/server';
import { SUPPORTED_CLUBS } from '@/lib/clubs';

export const revalidate = 3600; // 1 hour edge cache

export async function GET() {
  try {
    const edgeUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://edge.fc.dirzaaulia.com';
    const cleanUrl = edgeUrl.replace(/\/rest\/v1\/?$/, '').replace(/\/$/, '');
    const res = await fetch(`${cleanUrl}/rest/v1/supported_clubs?is_active=eq.true&order=name.asc`, {
      next: { revalidate: 3600 },
      headers: {
        'Accept': 'application/json',
      },
    });

    if (res.ok) {
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        const mapped = data.map((dto: {
          id: string;
          name: string;
          short_name?: string;
          competition_code: string;
          competition_name: string;
          crest_url?: string;
          team_id?: number;
          aliases?: string[];
        }) => ({
          id: dto.id,
          name: dto.name,
          shortName: dto.short_name || dto.name.slice(0, 15),
          leagueCode: dto.competition_code,
          leagueName: dto.competition_name,
          crestUrl: dto.crest_url || `https://a.espncdn.com/i/teamlogos/soccer/500/${dto.team_id}.png`,
          aliases: dto.aliases || [],
        }));
        return NextResponse.json(mapped);
      }
    }
  } catch (err) {
    console.error('Failed to fetch dynamic supported clubs from PostgreSQL:', err);
  }

  return NextResponse.json(SUPPORTED_CLUBS);
}

import { NextResponse } from 'next/server';
import { BIG_LEAGUES } from '@/lib/constants';

export const revalidate = 3600; // 1 hour edge cache

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
        return NextResponse.json(data);
      }
    }
  } catch (err) {
    console.error('Failed to fetch dynamic competitions from PostgreSQL:', err);
  }

  // Graceful fallback to static bundled competitions
  const fallback = BIG_LEAGUES.filter((l) => l.competitionId !== 'ALL').map((l, idx) => ({
    id: l.competitionId,
    name: l.name,
    short_name: l.name,
    country_code: l.countryCode,
    emblem_url: l.logo,
    league_id: l.leagueId,
    is_active: true,
    display_order: idx + 1,
  }));

  return NextResponse.json(fallback);
}

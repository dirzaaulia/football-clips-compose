import { NextResponse } from 'next/server';

export const revalidate = 3600; // 1 hour edge cache

export async function GET() {
  try {
    const edgeUrl = process.env.NEXT_PUBLIC_DATABASE_URL || process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://edge.fc.dirzaaulia.com';
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
        return NextResponse.json(data.map((competition) => ({
          ...competition,
          code: competition.code || competition.id,
          emblem_url: competition.emblem_url || '',
        })));
      }
    }
  } catch (err) {
    console.error('Failed to fetch dynamic competitions from PostgreSQL:', err);
  }

  return NextResponse.json([]);
}

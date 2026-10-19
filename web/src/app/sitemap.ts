import { MetadataRoute } from 'next';
import { supabase } from '@/lib/database';

export const dynamic = 'force-static';

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://fc.dirzaaulia.com';

  const staticRoutes: MetadataRoute.Sitemap = [
    {
      url: `${baseUrl}/`,
      lastModified: new Date(),
      changeFrequency: 'hourly',
      priority: 1.0,
    },
    {
      url: `${baseUrl}/fixtures`,
      lastModified: new Date(),
      changeFrequency: 'hourly',
      priority: 0.9,
    },
    {
      url: `${baseUrl}/standings`,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 0.9,
    },
    {
      url: `${baseUrl}/articles`,
      lastModified: new Date(),
      changeFrequency: 'hourly',
      priority: 0.85,
    },
    {
      url: `${baseUrl}/about`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.7,
    },
    {
      url: `${baseUrl}/editorial-standards`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.7,
    },
    {
      url: `${baseUrl}/contact`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.6,
    },
    {
      url: `${baseUrl}/privacy`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.5,
    },
    {
      url: `${baseUrl}/terms`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.5,
    },
  ];

  try {
    const { data: matches } = await supabase
      .from('matches')
      .select('id,updated_at,utc_date')
      .order('utc_date', { ascending: false })
      .limit(500);

    if (matches && matches.length > 0) {
      const matchRoutes: MetadataRoute.Sitemap = matches.map((m) => ({
        url: `${baseUrl}/match/${m.id}`,
        lastModified: m.updated_at ? new Date(m.updated_at) : new Date(m.utc_date),
        changeFrequency: 'daily',
        priority: 0.8,
      }));
      return [...staticRoutes, ...matchRoutes];
    }
  } catch (error) {
    console.warn('Failed to fetch matches for sitemap:', error);
  }

  return staticRoutes;
}

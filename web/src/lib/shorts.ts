import { database } from './database';
import { diversifyShortsFeed } from './shortsDiversifier';

export interface FootballShortRecord {
  id: string;
  youtube_video_id: string;
  title: string;
  channel_id: string;
  channel_name: string;
  club_id?: number | null;
  category: string;
  thumbnail_url: string;
  duration_seconds: number;
  like_count: number;
  is_featured: boolean;
  published_at: string;
  created_at?: string;
  hype_score?: number;
}

export interface GetShortsOptions {
  category?: string;
  limit?: number;
  offset?: number;
}

export async function getShorts(options: GetShortsOptions = {}): Promise<FootballShortRecord[]> {
  const { category, limit = 50, offset = 0 } = options;
  const fetchLimit = Math.min(150, limit * 3);
  try {
    let query = database
      .from('football_shorts')
      .select('*')
      .order('published_at', { ascending: false })
      .order('hype_score', { ascending: false })
      .range(offset, offset + fetchLimit - 1);

    if (category && category !== 'ALL') {
      query = query.eq('category', category);
    }

    const { data, error } = await query;
    if (error) {
      console.error('Error fetching football shorts from datastore:', error);
      return [];
    }

    return diversifyShortsFeed((data || []) as FootballShortRecord[], limit, 4);
  } catch (err) {
    console.error('Exception fetching shorts:', err);
    return [];
  }
}

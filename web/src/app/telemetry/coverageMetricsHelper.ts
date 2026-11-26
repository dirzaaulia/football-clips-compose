import { database } from '@/lib/database';
import {
  CoverageFilterType,
  DbFinishedMatch,
  MatchWaterfall,
  SeasonFilterType,
} from './types';

const MATCH_COLS = [
  'id',
  'season',
  'competition_id',
  'competition_name',
  'utc_date',
  'status',
  'home_team_name',
  'away_team_name',
  'home_score',
  'away_score',
  'highlight_video_id',
  'last_youtube_check',
  'youtube_region_info',
  'highlight_videos',
].join(', ');

export interface CoverageMetricsSummary {
  totalFinished: number;
  linkedCount: number;
  unlinkedCount: number;
  belowTargetCount: number;
  worldwide99Count: number;
}

export async function fetchFinishedMatchesBySeason(
  seasonFilter: SeasonFilterType,
  extraIds: string[] = [],
): Promise<DbFinishedMatch[]> {
  const nowIso = new Date().toISOString();
  const pageSize = 2000;
  let allRows: any[] = [];
  let offset = 0;

  while (true) {
    let query = database
      .from('matches')
      .select(MATCH_COLS)
      .eq('status', 'FINISHED')
      .lte('utc_date', nowIso)
      .order('utc_date', { ascending: false })
      .range(offset, offset + pageSize - 1);

    if (seasonFilter === '2026') {
      query = query.gte('season', 2026);
    } else if (seasonFilter === '2025') {
      query = query.eq('season', 2025);
    }

    const { data, error } = await query;
    if (error || !data || data.length === 0) break;
    allRows = allRows.concat(data);
    if (data.length < pageSize || allRows.length >= 8000) break;
    offset += pageSize;
  }

  if (seasonFilter === '2026' && extraIds.length > 0) {
    const loadedIds = new Set(allRows.map((m) => String(m.id)));
    const missing = extraIds.filter((id) => !loadedIds.has(id)).slice(0, 50);
    if (missing.length > 0) {
      const { data: xRows } = await database
        .from('matches')
        .select(MATCH_COLS)
        .eq('status', 'FINISHED')
        .lte('utc_date', nowIso)
        .in('id', missing);
      if (xRows) allRows = [...allRows, ...xRows];
    }
  }

  const nowMs = Date.now();
  return allRows.filter(
    (m) => m.status === 'FINISHED' && (!m.utc_date || new Date(m.utc_date).getTime() <= nowMs),
  ) as DbFinishedMatch[];
}

export function isBelowTargetMarket(wf: MatchWaterfall): boolean {
  if (wf.status !== 'LINKED') return false;
  const avail = wf.cumulativeAvailability;
  if (!avail) return false;
  return avail.missingTargetMarkets.length > 0 || avail.targetMarketsStatus !== 'ALL_COVERED';
}

export function isWorldwide99Plus(wf: MatchWaterfall): boolean {
  if (wf.status !== 'LINKED') return false;
  const pct = wf.cumulativeAvailability?.percentage ?? wf.worldwidePercentage ?? 100;
  return pct >= 99;
}

export function computeCoverageMetrics(waterfalls: MatchWaterfall[]): CoverageMetricsSummary {
  let linkedCount = 0;
  let unlinkedCount = 0;
  let belowTargetCount = 0;
  let worldwide99Count = 0;

  for (const wf of waterfalls) {
    if (wf.status === 'LINKED') {
      linkedCount++;
      if (isBelowTargetMarket(wf)) belowTargetCount++;
      if (isWorldwide99Plus(wf)) worldwide99Count++;
    } else if (wf.status === 'UNLINKED' || wf.status === 'COOLDOWN') {
      unlinkedCount++;
    }
  }

  return {
    totalFinished: waterfalls.length,
    linkedCount,
    unlinkedCount,
    belowTargetCount,
    worldwide99Count,
  };
}

export function matchesCoverageFilter(
  wf: MatchWaterfall,
  filter: CoverageFilterType,
): boolean {
  switch (filter) {
    case 'ALL':
      return true;
    case 'LINKED':
      return wf.status === 'LINKED';
    case 'COOLDOWN':
      return wf.status === 'COOLDOWN';
    case 'UNLINKED':
      return wf.status === 'UNLINKED' || wf.status === 'COOLDOWN';
    case 'BELOW_TARGET':
      return isBelowTargetMarket(wf);
    case 'WORLDWIDE_99':
      return isWorldwide99Plus(wf);
    default:
      return true;
  }
}

export async function fetchTelemetryChannels() {
  const [{ data: compRows }, { data: clubRows }, { data: shortRows }, { data: bcastRows }] = await Promise.all([
    database.from('competitions').select('id, name, youtube_handle, youtube_channel_id, backup_channel_handles, backup_channel_ids'),
    database.from('supported_clubs').select('team_id, name, short_name, crest_url, youtube_handle, youtube_channel_id, aliases').limit(1000),
    database.from('football_shorts').select('youtube_video_id, title, channel_id, channel_name, category, duration_seconds, published_at').order('published_at', { ascending: false }).limit(50),
    database.from('broadcasters').select('broadcaster_id, name, channel_handle, channel_id, allowed_countries'),
  ]);
  const compHandles: Record<string, string[]> = {};
  const channelIdToName: Record<string, string> = {};
  for (const c of (compRows || []) as any[]) {
    const list = [c.youtube_handle, ...(Array.isArray(c.backup_channel_handles) ? c.backup_channel_handles : [])].filter(Boolean);
    if (c.id && list.length > 0) compHandles[String(c.id)] = list;
    if (c.youtube_channel_id) {
      const handle = c.youtube_handle ? (String(c.youtube_handle).startsWith('@') ? c.youtube_handle : `@${c.youtube_handle}`) : '';
      channelIdToName[String(c.youtube_channel_id)] = handle ? `${c.name || c.id} (${handle})` : (c.name || c.id);
    }
    const bIds = Array.isArray(c.backup_channel_ids) ? c.backup_channel_ids : [];
    const bHandles = Array.isArray(c.backup_channel_handles) ? c.backup_channel_handles : [];
    bIds.forEach((bid: string, idx: number) => {
      if (bid && bHandles[idx]) {
        const h = String(bHandles[idx]).startsWith('@') ? bHandles[idx] : `@${bHandles[idx]}`;
        channelIdToName[String(bid)] = h;
      }
    });
  }
  const clubHandles: Record<string, string> = {};
  const clubNamesById: Record<number, { name: string; shortName?: string; crestUrl?: string }> = {};
  for (const cl of (clubRows || []) as any[]) {
    if (cl.team_id && cl.name) {
      const tid = Number(cl.team_id);
      if (!clubNamesById[tid] || String(cl.name).length > clubNamesById[tid].name.length) {
        clubNamesById[tid] = { name: cl.name, shortName: cl.short_name, crestUrl: cl.crest_url };
      }
    }
    if (cl.name && cl.youtube_handle) {
      clubHandles[String(cl.name).toLowerCase().trim()] = String(cl.youtube_handle);
      for (const al of (Array.isArray(cl.aliases) ? cl.aliases : [])) clubHandles[String(al).toLowerCase().trim()] = String(cl.youtube_handle);
    }
    if (cl.youtube_channel_id) {
      const handle = cl.youtube_handle ? (String(cl.youtube_handle).startsWith('@') ? cl.youtube_handle : `@${cl.youtube_handle}`) : '';
      channelIdToName[String(cl.youtube_channel_id)] = handle ? `${cl.name} (${handle})` : cl.name;
    }
  }
  const broadcasters: Record<string, { name: string; handle: string; tier: number }> = {};
  for (const b of (bcastRows || []) as any[]) {
    if (b.channel_handle) {
      const isDom = Array.isArray(b.allowed_countries) && b.allowed_countries.length > 0 && b.allowed_countries.length <= 5;
      broadcasters[String(b.channel_handle).toLowerCase().trim()] = { name: b.name || b.channel_handle, handle: b.channel_handle, tier: isDom ? 3 : 2 };
    }
    if (b.channel_id) {
      const handle = b.channel_handle ? (String(b.channel_handle).startsWith('@') ? b.channel_handle : `@${b.channel_handle}`) : '';
      channelIdToName[String(b.channel_id)] = handle ? `${b.name || b.channel_handle} (${handle})` : (b.name || b.channel_handle);
    }
  }
  return { channelLookup: { compHandles, clubHandles, broadcasters, channelIdToName, clubNamesById }, shortRows };
}

import { CumulativeAvailability, DbFinishedMatch, VideoRegionCoverage } from './types';

const ALL_COUNTRIES_COUNT = 249;

export const TIER1_TARGET_MARKETS = [
  'GB', 'US', 'ID', 'ES', 'DE', 'IT', 'FR', 'BR', 'AR', 'PT', 'NL', 'MX', 'SA', 'JP', 'KR',
];

export const TIER2_TARGET_MARKETS = [
  'TR', 'GR', 'BG', 'KZ', 'PL', 'CZ', 'RO', 'HR', 'RS', 'UA',
  'SE', 'NO', 'DK', 'BE', 'AT', 'CH', 'IN', 'AU', 'NG', 'EG',
  'MA', 'ZA', 'CO', 'CA', 'CL',
];

export const CORE_TARGET_MARKETS = [...TIER1_TARGET_MARKETS, ...TIER2_TARGET_MARKETS];

function parseRegionObj(raw: any) {
  if (!raw) return null;
  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
  return raw;
}

export function resolveNextCheckEta(
  lastCheckStr?: string | null,
  utcDateStr?: string | null,
): { eta: string; timestamp: string } {
  if (!lastCheckStr) return { eta: 'Immediate', timestamp: 'Due Now' };
  const lastCheck = new Date(lastCheckStr).getTime();
  const now = Date.now();
  const minutesSinceLastCheck = (now - lastCheck) / 60000;

  const matchTime = utcDateStr ? new Date(utcDateStr).getTime() : now;
  const hoursSinceMatch = (now - matchTime) / 3600000;

  let cooldownMin = 30;
  let tierName = 'Tier 1 (<6h)';
  if (hoursSinceMatch >= 72) {
    cooldownMin = 1440;
    tierName = 'Tier 4 (>72h)';
  } else if (hoursSinceMatch >= 24) {
    cooldownMin = 360;
    tierName = 'Tier 3 (24-72h)';
  } else if (hoursSinceMatch >= 6) {
    cooldownMin = 120;
    tierName = 'Tier 2 (6-24h)';
  }

  if (minutesSinceLastCheck >= cooldownMin) {
    return { eta: `Due now (${tierName})`, timestamp: 'Scheduled for current sweep' };
  }

  const remMin = Math.round(cooldownMin - minutesSinceLastCheck);
  const nextDt = new Date(lastCheck + cooldownMin * 60000);
  const eta = remMin < 60 ? `in ${remMin}m (${tierName})` : `in ${Math.floor(remMin / 60)}h ${remMin % 60}m (${tierName})`;
  const timestamp = nextDt.toUTCString().replace('GMT', 'UTC');
  return { eta, timestamp };
}

export function resolveCumulativeAvailability(dm?: DbFinishedMatch): CumulativeAvailability {
  if (!dm) {
    return {
      text: '249/249 countries (100% Worldwide)',
      percentage: 100,
      targetMarketsStatus: 'ALL_COVERED',
      coveredTargetMarkets: [...CORE_TARGET_MARKETS],
      missingTargetMarkets: [],
      videoBreakdowns: [],
    };
  }

  const breakdowns: VideoRegionCoverage[] = [];
  const cumulativeAllowed = new Set<string>();
  let hasUniversalVideo = false;

  const addVideoCoverage = (videoId: string, role: 'PRIMARY' | 'BACKUP', regionRaw: any, channelHandle?: string) => {
    if (!videoId) return;
    const region = parseRegionObj(regionRaw);
    let allowedCount = ALL_COUNTRIES_COUNT;
    let blockedCount = 0;
    const coveredMarkets: string[] = [];

    if (!region) {
      hasUniversalVideo = true;
      coveredMarkets.push(...CORE_TARGET_MARKETS);
      for (const m of CORE_TARGET_MARKETS) cumulativeAllowed.add(m);
    } else if (region.terminal || region.status === 'UNAVAILABLE') {
      allowedCount = 0;
      blockedCount = ALL_COUNTRIES_COUNT;
    } else if (Array.isArray(region.allowed) && region.allowed.length > 0) {
      const allowedSet = new Set(region.allowed.map((c: string) => c.toUpperCase()));
      allowedCount = region.allowed.length;
      blockedCount = Math.max(0, ALL_COUNTRIES_COUNT - allowedCount);
      for (const m of CORE_TARGET_MARKETS) {
        if (allowedSet.has(m)) {
          coveredMarkets.push(m);
          cumulativeAllowed.add(m);
        }
      }
    } else if (Array.isArray(region.blocked) && region.blocked.length > 0) {
      const blockedSet = new Set(region.blocked.map((c: string) => c.toUpperCase()));
      blockedCount = region.blocked.length;
      allowedCount = Math.max(0, ALL_COUNTRIES_COUNT - blockedCount);
      for (const m of CORE_TARGET_MARKETS) {
        if (!blockedSet.has(m)) {
          coveredMarkets.push(m);
          cumulativeAllowed.add(m);
        }
      }
    } else {
      hasUniversalVideo = true;
      coveredMarkets.push(...CORE_TARGET_MARKETS);
      for (const m of CORE_TARGET_MARKETS) cumulativeAllowed.add(m);
    }

    const pct = Math.min(100, Math.round((allowedCount / ALL_COUNTRIES_COUNT) * 100));
    breakdowns.push({
      videoId,
      role,
      channelHandle,
      allowedCount,
      blockedCount,
      percentage: pct,
      targetMarketsCovered: coveredMarkets,
    });
  };

  // 1. Primary Video
  if (dm.highlight_video_id) {
    let primaryCh: string | undefined;
    if (Array.isArray(dm.highlight_videos) && dm.highlight_videos.length > 0) {
      primaryCh = dm.highlight_videos[0].channel_handle || dm.highlight_videos[0].channel_name;
    }
    addVideoCoverage(dm.highlight_video_id, 'PRIMARY', dm.youtube_region_info, primaryCh);
  }

  // 2. Backup Video
  if (dm.backup_highlight_video_id && dm.backup_highlight_video_id !== dm.highlight_video_id) {
    let backupCh: string | undefined;
    if (Array.isArray(dm.highlight_videos) && dm.highlight_videos.length > 1) {
      backupCh = dm.highlight_videos[1].channel_handle || dm.highlight_videos[1].channel_name;
    }
    addVideoCoverage(dm.backup_highlight_video_id, 'BACKUP', dm.backup_youtube_region_info, backupCh);
  }

  // 3. Highlight Videos Array
  if (Array.isArray(dm.highlight_videos)) {
    for (const hv of dm.highlight_videos) {
      const vId = hv.video_id || hv.videoId;
      if (vId && !breakdowns.some((b) => b.videoId === vId)) {
        addVideoCoverage(vId, 'BACKUP', hv.youtube_region_info || hv.region_info, hv.channel_handle || hv.channel_name);
      }
    }
  }

  const coveredMarkets = hasUniversalVideo
    ? [...CORE_TARGET_MARKETS]
    : CORE_TARGET_MARKETS.filter((m) => cumulativeAllowed.has(m));
  const missingMarkets = CORE_TARGET_MARKETS.filter((m) => !coveredMarkets.includes(m));

  const regObj = parseRegionObj(dm.youtube_region_info) || {};
  const sweepCount = typeof regObj.sweep_count === 'number' ? regObj.sweep_count : 0;
  const isTerminal = Boolean(regObj.terminal || regObj.target_market_exhausted);
  const isExhausted = isTerminal || sweepCount >= 3;

  let nextCheckEta: string | undefined;
  let nextCheckTimestamp: string | undefined;

  if (missingMarkets.length > 0 && !isExhausted) {
    const nextInfo = resolveNextCheckEta(dm.last_youtube_check, dm.utc_date);
    nextCheckEta = nextInfo.eta;
    nextCheckTimestamp = nextInfo.timestamp;
  }

  let finalPct: number;
  let text: string;

  if (breakdowns.length === 0) {
    return {
      text: 'Availability Pending Search',
      percentage: 0,
      targetMarketsStatus: isExhausted ? 'EXHAUSTED' : 'PARTIAL',
      coveredTargetMarkets: [],
      missingTargetMarkets: [...CORE_TARGET_MARKETS],
      videoBreakdowns: [],
      nextCheckEta,
      nextCheckTimestamp,
      isExhausted,
      sweepCount,
    };
  }

  if (hasUniversalVideo || missingMarkets.length === 0) {
    finalPct = 99;
    text = `99% Worldwide · ${coveredMarkets.length}/${CORE_TARGET_MARKETS.length} Target Markets Covered`;
  } else {
    const maxSingle = Math.max(...breakdowns.map((b) => b.percentage));
    finalPct = Math.min(95, maxSingle + (breakdowns.length > 1 ? 15 : 0));
    const suffix = isExhausted ? '· Broadcasters Exhausted' : '· Awaiting Backup';
    text = `${finalPct}% Worldwide · Missing: ${missingMarkets.join(', ')} ${suffix}`;
  }

  const status: CumulativeAvailability['targetMarketsStatus'] =
    missingMarkets.length === 0 ? 'ALL_COVERED' : isExhausted ? 'EXHAUSTED' : 'PARTIAL';

  return {
    text,
    percentage: finalPct,
    targetMarketsStatus: status,
    coveredTargetMarkets: coveredMarkets,
    missingTargetMarkets: missingMarkets,
    videoBreakdowns: breakdowns,
    nextCheckEta,
    nextCheckTimestamp,
    isExhausted,
    sweepCount,
  };
}

import { CandidateVerdict, MatchCycleRun } from './types';
import { ChannelLookupMap } from './mapperHelper';

export interface WaterfallTraceRow {
  event_id: number;
  timestamp: string;
  metadata: {
    match_id?: number | string;
    home_team?: string;
    away_team?: string;
    competition?: string;
    outcome?: string;
    channels_searched?: string[];
    candidates?: Array<{
      video_id: string;
      title: string;
      channel_handle?: string;
      channel_title?: string;
      duration_seconds?: number;
      watchability_pct?: number;
      source?: string;
      is_embeddable?: boolean;
      published_at?: string;
    }>;
    linked_video_id?: string;
    linked_video_title?: string;
    duration_ms?: number;
    quota_used?: number;
    global_watchability_pct?: number;
    ai_evaluations?: any;
    started_at?: string;
  };
}

export function mapTraceToCycleRun(
  row: WaterfallTraceRow,
  channelLookup?: ChannelLookupMap,
): MatchCycleRun {
  const meta = row.metadata || {};
  const channels = (meta.channels_searched || []).map((cid) => {
    if (channelLookup?.broadcasters) {
      for (const b of Object.values(channelLookup.broadcasters)) {
        if (b.handle && (b.handle === cid || (b as any).channel_id === cid)) {
          return b.handle.startsWith('@') ? b.handle : `@${b.handle}`;
        }
      }
    }
    return cid.startsWith('UC') ? cid : cid.startsWith('@') ? cid : `@${cid}`;
  });

  const candidatesList = (meta.candidates || []).map((c) => ({
    videoId: c.video_id,
    title: c.title,
    channel: c.channel_handle || c.channel_title || 'Official Channel',
    duration: c.duration_seconds,
    watchabilityPct: c.watchability_pct,
    source: c.source,
    isEmbeddable: c.is_embeddable ?? true,
    publishedAt: c.published_at,
  }));

  const step2Candidates = candidatesList.map(
    (c) => `${c.title} (${c.channel}) [${Math.round(c.duration || 0)}s]`,
  );

  const verdicts: CandidateVerdict[] = candidatesList.map((c, idx) => {
    const isAccepted = c.videoId === meta.linked_video_id || meta.outcome === 'LINKED';
    let reason = isAccepted ? 'Selected and linked by pipeline evaluation' : 'Not selected for primary link';
    if (!isAccepted) {
      if (c.watchabilityPct && c.watchabilityPct < 90) {
        reason = `Restricted reach: ${c.watchabilityPct.toFixed(1)}% WW`;
      } else if (c.duration && c.duration < 120) {
        reason = `Short duration: ${Math.round(c.duration)}s`;
      } else if (meta.outcome === 'RETAINED_SUPERIOR') {
        reason = 'Retained superior existing highlight with higher authority/reach';
      }
    }
    return {
      candidateId: c.videoId,
      index: idx + 1,
      title: c.title,
      channel: c.channel,
      url: `https://youtu.be/${c.videoId}`,
      isAccepted,
      verdictReason: reason,
      evaluator: 'ORACLE_PIPELINE',
      score: Math.round(c.watchabilityPct || 0),
      source: c.source,
    };
  });

  const rawOutcome = meta.outcome || 'NOT_FOUND';
  const isLinked = rawOutcome === 'LINKED' || Boolean(meta.linked_video_id);
  const isCooldown = rawOutcome === 'COOLDOWN_PROTECTED';
  const status: MatchCycleRun['status'] = isLinked ? 'LINKED' : isCooldown ? 'COOLDOWN' : 'UNLINKED';

  let step4Outcome = '';
  if (isLinked) {
    step4Outcome = `Official highlight linked: https://youtu.be/${meta.linked_video_id}${meta.linked_video_title ? ` (${meta.linked_video_title})` : ''}`;
  } else if (rawOutcome === 'RETAINED_SUPERIOR') {
    step4Outcome = `Retained superior existing highlight (evaluation complete in ${meta.duration_ms || 0}ms)`;
  } else if (isCooldown) {
    step4Outcome = 'Tier cooldown guardrail active (sweep deferred)';
  } else {
    step4Outcome = `No official highlight linked across ${channels.length} checked channels`;
  }

  const stoppedAtStep: 1 | 2 | 3 | 4 | undefined = isLinked
    ? undefined
    : candidatesList.length === 0 ? 2 : 3;

  return {
    cycleId: row.event_id,
    timestamp: row.timestamp ? new Date(row.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : 'Recent Run',
    step1Channels: channels.length > 0 ? channels : ['Official Competition Channels'],
    step2Candidates,
    step3Verdicts: verdicts,
    step4Outcome,
    outcomeVideoId: meta.linked_video_id,
    outcomeVideoUrl: meta.linked_video_id ? `https://youtu.be/${meta.linked_video_id}` : undefined,
    status,
    stoppedAtStep,
    stoppedReason: isLinked ? undefined : step4Outcome,
    durationMs: meta.duration_ms,
    quotaUsed: meta.quota_used,
    candidatesList,
  };
}

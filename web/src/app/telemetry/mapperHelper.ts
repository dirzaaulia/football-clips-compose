import { CandidateVerdict, DbFinishedMatch, MatchCycleRun, ScannedVideo, TelemetryEvent } from './types';

export interface ChannelLookupMap {
  compHandles?: Record<string, string[]>;
  clubHandles?: Record<string, string>;
  broadcasters?: Record<string, { name: string; handle: string; tier: number }>;
}

export function resolveDbMatchTargets(dm: DbFinishedMatch, lookup?: ChannelLookupMap): string[] {
  const handles: string[] = [];
  const addHandle = (h?: string) => {
    if (!h) return;
    const clean = h.startsWith('@') ? h : `@${h}`;
    if (!handles.some((x) => x.toLowerCase() === clean.toLowerCase())) handles.push(clean);
  };
  if (lookup?.clubHandles) {
    addHandle(lookup.clubHandles[dm.home_team_name.toLowerCase().trim()]);
    addHandle(lookup.clubHandles[dm.away_team_name.toLowerCase().trim()]);
  }
  if (dm.competition_id && lookup?.compHandles?.[dm.competition_id]) {
    for (const ch of lookup.compHandles[dm.competition_id]) addHandle(ch);
  }
  return handles;
}

const HIGHLIGHT_CODES = new Set([
  'SEARCH_START', 'CHANNEL_SCAN', 'CANDIDATE_ITEM', 'AI_EVAL', 'COOLDOWN',
  'LINKED', 'NOT_FOUND', 'DISCOVERY', 'RSS_DISCOVERY', 'API_CHANNEL_FALLBACK',
  'API_GENERAL_SEARCH', 'CANDIDATE_ACCEPTED', 'CANDIDATE_REJECTED', 'UPGRADE_ATTEMPT',
  'RESULT_EMPTY', 'RESULT_UNLINKED', 'CHANNEL_EMPTY',
]);

export function isHighlightEvent(ev: TelemetryEvent): boolean {
  const pipe = (ev.pipeline || '').toLowerCase();
  const sub = (ev.subsystem || '').toUpperCase();
  if (pipe === 'youtube' || pipe === 'highlight' || sub === 'YOUTUBE' || sub === 'HIGHLIGHT') return true;
  if (ev.code && HIGHLIGHT_CODES.has(ev.code)) return true;
  const text = ev.event_text || '';
  return text.includes('YOUTUBE') || text.includes('Searching official') || text.includes('Cooldown') ||
         text.includes('CANDIDATE') || text.includes('CHANNEL SCAN') || text.includes('RSS DISCOVERY');
}

export function parseCandidateVerdict(text: string, index: number): CandidateVerdict | null {
  const isAccepted = text.includes('[ACCEPTED]');
  const isRejected = text.includes('[REJECTED]');
  if (!isAccepted && !isRejected) return null;

  const titleMatch = text.match(/'([^']+)'/);
  const channelMatch = text.match(/\(([^)]+)\)/);
  const urlMatch = text.match(/->\s*(https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11})/);
  const url = urlMatch ? urlMatch[1] : '';
  const candidateId = url ? url.substring(url.lastIndexOf('/') + 1) : `cand-${index}`;
  const verdictReason = text.includes('| Verdict: ')
    ? text.split('| Verdict: ')[1].trim()
    : isAccepted ? 'Accepted by AI evaluation' : 'Not selected';

  return {
    candidateId,
    index,
    title: titleMatch ? titleMatch[1] : 'Candidate Video',
    channel: channelMatch ? channelMatch[1] : 'Official Channel',
    url,
    isAccepted,
    verdictReason,
    evaluator: isAccepted ? 'AI Evaluator' : 'Scorer',
    score: isAccepted ? 100 : 0,
  };
}

export function extractMatchTitleFromText(text: string): string | null {
  if (text.includes('Searching official highlights for:')) {
    return text.replace(/.*Searching official highlights for:\s*/i, '').replace(/\s*\(\d{4}-\d{2}-\d{2}\).*/, '').trim();
  }
  const cd = text.match(/(?:Cooldown guardrail active for|\[COOLDOWN\])\s*([^:]+):/i);
  if (cd) return cd[1].trim();

  // Contextual extraction for result, skip, and claimed logs
  const contextual = text.match(/(?:found for|mapped for:?|candidates \(|guardrail active for)\s*([\p{L}\p{N}\s.'-]+ vs [\p{L}\p{N}\s.'-]+?)(?:\.|\)|:|$)/iu);
  if (contextual) return contextual[1].trim();

  // Support Unicode letters to prevent slicing names like "Atlético" -> "tico"
  const vsMatch = text.match(/([\p{L}\p{N}\s.'-]+ vs [\p{L}\p{N}\s.'-]+)/iu);
  if (vsMatch) {
    let clean = vsMatch[1].replace(/\.+$/, '').trim();
    clean = clean.replace(/^(?:No eligible highlight candidates found for|All detected candidates \(|No official YouTube handles mapped for:?)\s*/i, '').trim();
    return clean;
  }

  return null;
}

export function extractTimestampFromText(text: string, fallback?: string): string {
  const match = text.match(/\[(\d{2}:\d{2}:\d{2}\s*WIB)\]/);
  if (match) return match[1];
  if (fallback) {
    try {
      return new Date(fallback).toLocaleTimeString('en-GB', { timeZone: 'Asia/Jakarta' }) + ' WIB';
    } catch {
      return fallback;
    }
  }
  return '';
}

export function resolveChannelTier(
  handle?: string,
  compId?: string,
  homeTeam?: string,
  awayTeam?: string,
  lookup?: ChannelLookupMap,
): string {
  if (!handle) return 'Official Broadcaster';
  const clean = handle.startsWith('@') ? handle.toLowerCase() : `@${handle.toLowerCase()}`;

  if (homeTeam && lookup?.clubHandles?.[homeTeam.toLowerCase().trim()]?.toLowerCase() === clean) {
    return 'Tier 1 · Official Club Channel';
  }
  if (awayTeam && lookup?.clubHandles?.[awayTeam.toLowerCase().trim()]?.toLowerCase() === clean) {
    return 'Tier 1 · Official Club Channel';
  }

  if (compId && lookup?.compHandles?.[compId]) {
    const isComp = lookup.compHandles[compId].some((h) => h.toLowerCase() === clean);
    if (isComp) return 'Tier 1 · Official League Channel';
  }

  if (lookup?.broadcasters) {
    for (const b of Object.values(lookup.broadcasters)) {
      if (b.handle.toLowerCase() === clean) {
        return b.tier === 3 ? 'Tier 3 · Domestic Broadcaster' : 'Tier 2 · Regional Broadcaster';
      }
    }
  }

  return 'Official Broadcaster';
}

export function resolveWorldwideAvailability(regionInfo: any): { text: string; percentage: number } {
  if (!regionInfo) {
    return { text: '249/249 countries (100% Worldwide)', percentage: 100 };
  }
  let info = regionInfo;
  if (typeof info === 'string') {
    try {
      info = JSON.parse(info);
    } catch {
      return { text: '249/249 countries (100% Worldwide)', percentage: 100 };
    }
  }

  if (info.terminal || info.status === 'UNAVAILABLE') {
    return { text: 'Unavailable in region', percentage: 0 };
  }

  if (Array.isArray(info.allowed) && info.allowed.length > 0) {
    const count = info.allowed.length;
    const pct = Math.min(100, Math.round((count / 249) * 100));
    return { text: `${count}/249 countries (${pct}% Worldwide)`, percentage: pct };
  }

  if (Array.isArray(info.blocked) && info.blocked.length > 0) {
    const count = Math.max(0, 249 - info.blocked.length);
    const pct = Math.round((count / 249) * 100);
    return { text: `${count}/249 countries (${pct}% Worldwide)`, percentage: pct };
  }

  return { text: '249/249 countries (100% Worldwide)', percentage: 100 };
}

export function processCycleEvents(events: TelemetryEvent[], cycleId?: number): MatchCycleRun {
  const step1Channels: string[] = [];
  const seenChannels = new Set<string>();
  const step2Candidates: string[] = [];
  const step3Verdicts: CandidateVerdict[] = [];
  let outcomeId: string | undefined;
  let outcomeUrl: string | undefined;
  let outcomeReason = '';
  let timestamp = '';
  let query: string | undefined;
  const scannedUploads: Record<string, ScannedVideo[]> = {};

  for (const ev of events) {
    const text = ev.event_text || '';
    const code = ev.code || '';
    const detail = text.replace(/^(\[[^\]]+\]\s*)+/, '').trim();
    if (!timestamp) timestamp = extractTimestampFromText(text, ev.created_at);

    if (ev.attributes?.scanned_videos && Array.isArray(ev.attributes.scanned_videos)) {
      const rawCh = String(ev.attributes.channel || ev.attributes.channel_handle || '').trim();
      const rawQuery = String(ev.attributes.query || '').trim();
      if (rawCh) {
        const chKey = rawCh.startsWith('@') ? rawCh : `@${rawCh}`;
        scannedUploads[chKey] = ev.attributes.scanned_videos as ScannedVideo[];
      } else if (rawQuery || code === 'API_GENERAL_SEARCH') {
        const qKey = rawQuery ? `API Search: '${rawQuery}'` : 'API Direct Search';
        scannedUploads[qKey] = ev.attributes.scanned_videos as ScannedVideo[];
      }
    }

    if (text.includes('Searching official') || text.includes('CHANNEL_SCAN') || text.includes('Checking channel')) {
      if (!seenChannels.has(detail)) {
        seenChannels.add(detail);
        step1Channels.push(detail);
      }
    } else if (text.includes('CANDIDATE_ACCEPTED') || text.includes('CANDIDATE_REJECTED')) {
      const v = parseCandidateVerdict(text, step3Verdicts.length + 1);
      if (v && !step3Verdicts.some((ex) => ex.candidateId === v.candidateId)) step3Verdicts.push(v);
    } else if (text.includes('CANDIDATE') || text.includes('DISCOVERY') || text.includes('API SEARCH') ||
               text.includes('API PLAYLIST') || code === 'CANDIDATE_ITEM' || code === 'RSS_DISCOVERY') {
      const cleanCand = text.trim();
      const isSummaryLine = text.includes('Scanned') || text.includes('Fetched') || text.includes('quota units');
      if (!isSummaryLine && !step2Candidates.includes(cleanCand)) {
        step2Candidates.push(cleanCand);
      }
      const qm = text.match(/Querying YouTube Data API:\s*'(.+?)'\s+for channel/i) || text.match(/'([^']+)'/);
      if (qm && text.includes('Querying YouTube')) query = qm[1];
    } else if (code === 'DB_LINKED' || code === 'RESULT_LINKED' || text.includes('HIGHLIGHT LINKED') || text.includes('LINKED')) {
      outcomeReason = detail;
      const vidMatch = text.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
      if (vidMatch) {
        outcomeId = vidMatch[1];
        outcomeUrl = `https://youtu.be/${outcomeId}`;
      }
    } else if (code === 'COOLDOWN_PROTECTED' || code === 'COOLDOWN' || text.includes('COOLDOWN')) {
      outcomeReason = detail;
    } else if (code === 'RESULT_EMPTY' || code === 'RESULT_UNLINKED' || text.includes('No eligible highlight candidates found') || text.includes('preliminary candidate(s) rejected') || text.includes('0 matching highlight videos found')) {
      outcomeReason = detail || 'No eligible highlight candidates found';
    } else if (code === 'CHANNEL_EMPTY' || text.includes('0 matching videos') || text.includes('0 public uploads')) {
      if (!outcomeReason) outcomeReason = detail || 'Official channels scanned with no highlight videos found';
    } else if (code === 'NOT_FOUND' || text.includes('NO HIGHLIGHT FOUND')) {
      outcomeReason = detail || 'No verified highlight video found';
    }
  }

  const matchedVideoLines = step2Candidates.filter((c) => c.includes('https://youtu.be/'));
  const hasMatchedCandidates = matchedVideoLines.length > 0 || step3Verdicts.length > 0;
  const isCd = outcomeReason.toLowerCase().includes('cooldown');
  const status: MatchCycleRun['status'] = outcomeId ? 'LINKED' : isCd ? 'COOLDOWN' : outcomeReason ? 'UNLINKED' : 'SEARCHED';
  const stoppedAtStep = status === 'UNLINKED' || status === 'SEARCHED' ? (hasMatchedCandidates ? 3 : 2) : undefined;

  let stoppedReason: string | undefined;
  if (stoppedAtStep) {
    if (stoppedAtStep === 3 && step3Verdicts.length > 0 && (!outcomeReason || outcomeReason.startsWith('No eligible highlight candidates found'))) {
      const rejSummary = step3Verdicts
        .filter((v) => !v.isAccepted)
        .slice(0, 2)
        .map((v) => `${v.candidateId} (${v.channel}): ${v.verdictReason}`)
        .join('; ');
      stoppedReason = rejSummary
        ? `${step3Verdicts.length} candidate(s) rejected in Step 3 — ${rejSummary}`
        : outcomeReason;
    } else {
      stoppedReason = outcomeReason || (stoppedAtStep === 2 ? '0 matching videos found for teams across scanned official channels' : 'No verified official highlights accepted');
    }
  }

  return {
    cycleId,
    timestamp: timestamp || 'Recently',
    query,
    step1Channels,
    step2Candidates,
    step3Verdicts,
    step4Outcome: outcomeReason || (outcomeId ? `Linked video ${outcomeId}` : 'No highlight found yet'),
    outcomeVideoId: outcomeId,
    outcomeVideoUrl: outcomeUrl,
    status,
    stoppedAtStep,
    stoppedReason,
    scannedUploads: Object.keys(scannedUploads).length > 0 ? scannedUploads : undefined,
  };
}

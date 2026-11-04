import { CandidateVerdict, MatchCycleRun, MatchWaterfall, TelemetryEvent } from './types';

const HIGHLIGHT_CODES = new Set([
  'SEARCH_START', 'CHANNEL_SCAN', 'CANDIDATE_ITEM', 'AI_EVAL', 'COOLDOWN',
  'LINKED', 'NOT_FOUND', 'DISCOVERY', 'RSS_DISCOVERY', 'API_CHANNEL_FALLBACK',
  'API_GENERAL_SEARCH', 'CANDIDATE_ACCEPTED', 'CANDIDATE_REJECTED', 'UPGRADE_ATTEMPT',
]);

function isHighlightEvent(ev: TelemetryEvent): boolean {
  const pipe = (ev.pipeline || '').toLowerCase();
  if (pipe === 'youtube' || pipe === 'highlight') return true;
  const sub = (ev.subsystem || '').toUpperCase();
  if (sub === 'YOUTUBE' || sub === 'HIGHLIGHT') return true;
  if (ev.code && HIGHLIGHT_CODES.has(ev.code)) return true;
  const text = ev.event_text || '';
  return (
    text.includes('YOUTUBE') || text.includes('Searching official highlights') ||
    text.includes('Cooldown guardrail active') || text.includes('CANDIDATE') ||
    text.includes('CHANNEL SCAN') || text.includes('RSS DISCOVERY') || text.includes('[COOLDOWN]')
  );
}

function parseCandidateVerdict(text: string, index: number): CandidateVerdict | null {
  const isAccepted = text.includes('[ACCEPTED]');
  const isRejected = text.includes('[REJECTED]');
  if (!isAccepted && !isRejected) return null;

  const titleMatch = text.match(/'([^']+)'/);
  const title = titleMatch ? titleMatch[1] : 'Candidate Video';
  const channelMatch = text.match(/\(([^)]+)\)/);
  const channel = channelMatch ? channelMatch[1] : 'Official Channel';
  const urlMatch = text.match(/->\s*(https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11})/);
  const url = urlMatch ? urlMatch[1] : '';
  const candidateId = url ? url.substring(url.lastIndexOf('/') + 1) : `cand-${index}`;
  const verdictReason = text.includes('| Verdict: ')
    ? text.split('| Verdict: ')[1].trim()
    : isAccepted ? 'Accepted by AI evaluation' : 'Not selected';

  return {
    candidateId,
    index,
    title,
    channel,
    url,
    isAccepted,
    verdictReason,
    evaluator: isAccepted ? 'AI / Gateway Evaluator' : 'Scorer',
    score: isAccepted ? 100 : 0,
  };
}

function extractMatchTitleFromText(text: string): string | null {
  if (text.includes('Searching official highlights for:')) {
    return text.replace(/.*Searching official highlights for:\s*/i, '').replace(/\s*\(\d{4}-\d{2}-\d{2}\).*/, '').trim();
  }
  const cooldownMatch = text.match(/Cooldown guardrail active for ([^:]+):/i);
  if (cooldownMatch) return cooldownMatch[1].trim();

  const directCooldown = text.match(/\[COOLDOWN\]\s*([^:]+):/i);
  if (directCooldown) return directCooldown[1].trim();

  // Support Unicode letters to prevent slicing names like "Atlético" -> "tico"
  const vsMatch = text.match(/([\p{L}\p{N}\s.'-]+ vs [\p{L}\p{N}\s.'-]+)/iu);
  if (vsMatch) return vsMatch[1].replace(/\.+$/, '').trim();

  return null;
}

function extractTimestampFromText(text: string, fallback?: string): string {
  const match = text.match(/\[(\d{2}:\d{2}:\d{2}\s*WIB)\]/);
  if (match) return match[1];
  if (fallback) {
    try {
      const d = new Date(fallback);
      return d.toLocaleTimeString('en-GB', { timeZone: 'Asia/Jakarta' }) + ' WIB';
    } catch {
      return fallback;
    }
  }
  return '';
}

function processCycleEvents(events: TelemetryEvent[], cycleId?: number): MatchCycleRun {
  const step1Channels: string[] = [];
  const seenChannels = new Set<string>();
  const step2Candidates: string[] = [];
  const step3Verdicts: CandidateVerdict[] = [];
  let outcomeId: string | undefined;
  let outcomeUrl: string | undefined;
  let outcomeReason = '';
  let timestamp = '';
  let query: string | undefined;

  for (const ev of events) {
    const text = ev.event_text || '';
    const code = ev.code || '';
    const detail = text.replace(/^(\[[^\]]+\]\s*)+/, '').trim();

    if (!timestamp) {
      timestamp = extractTimestampFromText(text, ev.created_at);
    }

    if (text.includes('Searching official highlights for:')) {
      if (!seenChannels.has(detail)) {
        seenChannels.add(detail);
        step1Channels.push(detail);
      }
    } else if (text.includes('CHANNEL_SCAN') || text.includes('Checking channel') || text.includes('CHANNEL SCAN')) {
      if (!seenChannels.has(detail)) {
        seenChannels.add(detail);
        step1Channels.push(detail);
      }
    } else if (text.includes('CANDIDATE_ACCEPTED') || text.includes('CANDIDATE_REJECTED')) {
      const v = parseCandidateVerdict(text, step3Verdicts.length + 1);
      if (v && !step3Verdicts.some((ex) => ex.candidateId === v.candidateId)) {
        step3Verdicts.push(v);
      }
    } else if (
      text.includes('CANDIDATE') || text.includes('DISCOVERY') || text.includes('API SEARCH') ||
      text.includes('API RESULT') || text.includes('API PLAYLIST') || code === 'CANDIDATE_ITEM' ||
      code === 'RSS_DISCOVERY' || code === 'API_CHANNEL_FALLBACK' || code === 'API_GENERAL_SEARCH'
    ) {
      const cleanCand = text.replace(/^\s+/, '').trim();
      if (!step2Candidates.includes(cleanCand)) {
        step2Candidates.push(cleanCand);
      }
      if (text.includes('Querying YouTube Data API:')) {
        const qm = text.match(/'([^']+)'/);
        if (qm) query = qm[1];
      }
    } else if (code === 'DB_LINKED' || code === 'RESULT_LINKED' || text.includes('HIGHLIGHT LINKED') || text.includes('LINKED')) {
      outcomeReason = detail;
      const vidMatch = text.match(/youtu\.be\/([a-zA-Z0-9_-]{11})/);
      if (vidMatch) {
        outcomeId = vidMatch[1];
        outcomeUrl = `https://youtu.be/${outcomeId}`;
      }
    } else if (code === 'COOLDOWN_PROTECTED' || code === 'COOLDOWN' || text.includes('COOLDOWN')) {
      outcomeReason = detail;
    } else if (code === 'NOT_FOUND' || code === 'RESULT_EMPTY' || text.includes('NO HIGHLIGHT FOUND') || text.includes('NOT AVAILABLE YET')) {
      outcomeReason = detail || 'No verified highlight video found';
    }
  }

  const isCooldownStatus = outcomeReason.toLowerCase().includes('cooldown');
  const status: MatchCycleRun['status'] = outcomeId ? 'LINKED' : isCooldownStatus ? 'COOLDOWN' : outcomeReason ? 'UNLINKED' : 'SEARCHED';

  let stoppedAtStep: 1 | 2 | 3 | 4 | undefined;
  let stoppedReason: string | undefined;

  if (status === 'UNLINKED' || status === 'SEARCHED') {
    if (step1Channels.length <= 1) {
      stoppedAtStep = 1;
      stoppedReason = 'No official YouTube channels mapped in database for clubs or league.';
    } else if (step2Candidates.length === 0) {
      stoppedAtStep = 2;
      stoppedReason = 'Zero candidate videos uploaded by official channels within match window (-2h to +96h).';
    } else if (step3Verdicts.length > 0 && !step3Verdicts.some((v) => v.isAccepted)) {
      stoppedAtStep = 3;
      stoppedReason = 'All detected candidate videos rejected during AI semantic reranking.';
    } else {
      stoppedAtStep = step2Candidates.length === 0 ? 2 : 3;
      stoppedReason = outcomeReason || 'No verified official highlights matched this fixture.';
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
  };
}

export function buildMatchWaterfalls(events: TelemetryEvent[]): MatchWaterfall[] {
  const matchMap = new Map<string, TelemetryEvent[]>();

  for (const ev of events) {
    if (!isHighlightEvent(ev)) continue;
    const text = ev.event_text || '';
    if (text.includes('WEBSUB') || text.includes('[UNLINKED & UPGRADE SWEEP]') || text.includes('[CLEANUP]')) continue;

    const key = ev.match_id ? String(ev.match_id) : extractMatchTitleFromText(text);
    if (key) {
      if (!matchMap.has(key)) matchMap.set(key, []);
      matchMap.get(key)!.push(ev);
    }
  }

  const results: MatchWaterfall[] = [];

  for (const [key, rawEvList] of matchMap.entries()) {
    // Group events by cycle_id or search boundary
    const cycleMap = new Map<number | string, TelemetryEvent[]>();
    let currentSyntheticCycle = 1;

    for (let i = 0; i < rawEvList.length; i++) {
      const ev = rawEvList[i];
      const text = ev.event_text || '';
      const code = ev.code || '';
      if (i > 0 && (code === 'SEARCH_START' || text.includes('Searching official highlights for:'))) {
        currentSyntheticCycle++;
      }
      const cId = ev.cycle_id ?? `synth-${currentSyntheticCycle}`;
      if (!cycleMap.has(cId)) cycleMap.set(cId, []);
      cycleMap.get(cId)!.push(ev);
    }

    const cycles: MatchCycleRun[] = [];
    for (const [cId, cEvents] of cycleMap.entries()) {
      const numericCycle = typeof cId === 'number' ? cId : undefined;
      cycles.push(processCycleEvents(cEvents, numericCycle));
    }

    // Sort newest cycle first
    cycles.reverse();
    const latest = cycles[0] || processCycleEvents(rawEvList);

    let title = /^\d+$/.test(key) ? '' : key;
    let matchDate = '';
    let compId: string | undefined;
    let compName: string | undefined;
    let homeScore: number | null = null;
    let awayScore: number | null = null;

    for (const ev of rawEvList) {
      const attrs = ev.attributes as Record<string, any> | undefined;
      if (attrs) {
        if (!title && attrs.match_title) title = attrs.match_title;
        if (!title && attrs.home_team && attrs.away_team) title = `${attrs.home_team} vs ${attrs.away_team}`;
        if (!compId && attrs.competition_id) compId = String(attrs.competition_id);
        if (!compName && attrs.competition_name) compName = String(attrs.competition_name);
        if (homeScore === null && attrs.home_score !== undefined) homeScore = Number(attrs.home_score);
        if (awayScore === null && attrs.away_score !== undefined) awayScore = Number(attrs.away_score);
      }
      if (!matchDate) {
        const dateMatch = (ev.event_text || '').match(/\b\d{4}-\d{2}-\d{2}\b/);
        if (dateMatch) matchDate = dateMatch[0];
      }
    }

    if (!title) {
      const fromText = extractMatchTitleFromText(rawEvList[0]?.event_text || '');
      if (fromText) title = fromText;
    }

    if (title && !/^\d+$/.test(title) && title !== 'General Routine' && title !== 'global') {
      results.push({
        matchId: key,
        title,
        date: matchDate,
        timestamp: latest.timestamp,
        homeScore,
        awayScore,
        competitionId: compId,
        competitionName: compName,
        status: latest.status,
        step1Channels: latest.step1Channels,
        step2Candidates: latest.step2Candidates,
        step3Verdicts: latest.step3Verdicts,
        step4Outcome: latest.step4Outcome,
        outcomeVideoId: latest.outcomeVideoId,
        outcomeVideoUrl: latest.outcomeVideoUrl,
        stoppedAtStep: latest.stoppedAtStep,
        stoppedReason: latest.stoppedReason,
        cycles,
      });
    }
  }

  return results;
}

import { CandidateVerdict, MatchWaterfall, TelemetryEvent } from './types';

const HIGHLIGHT_CODES = new Set([
  'SEARCH_START',
  'CHANNEL_SCAN',
  'CANDIDATE_ITEM',
  'AI_EVAL',
  'COOLDOWN',
  'LINKED',
  'NOT_FOUND',
  'DISCOVERY',
  'RSS_DISCOVERY',
  'API_CHANNEL_FALLBACK',
  'API_GENERAL_SEARCH',
  'CANDIDATE_ACCEPTED',
  'CANDIDATE_REJECTED',
  'UPGRADE_ATTEMPT',
]);

function isHighlightEvent(ev: TelemetryEvent): boolean {
  const pipe = (ev.pipeline || '').toLowerCase();
  if (pipe === 'youtube' || pipe === 'highlight') return true;

  const sub = (ev.subsystem || '').toUpperCase();
  if (sub === 'YOUTUBE' || sub === 'HIGHLIGHT') return true;

  if (ev.code && HIGHLIGHT_CODES.has(ev.code)) return true;

  const text = ev.event_text || '';
  return (
    text.includes('YOUTUBE') ||
    text.includes('Searching official highlights') ||
    text.includes('Cooldown guardrail active') ||
    text.includes('CANDIDATE') ||
    text.includes('CHANNEL SCAN') ||
    text.includes('RSS DISCOVERY') ||
    text.includes('[COOLDOWN]')
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
    : isAccepted
      ? 'Accepted by AI evaluation'
      : 'Not selected';

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

  const vsMatch = text.match(/([A-Za-z0-9 .'-]+ vs [A-Za-z0-9 .'-]+)/i);
  if (vsMatch) return vsMatch[1].trim();

  return null;
}

export function buildMatchWaterfalls(events: TelemetryEvent[]): MatchWaterfall[] {
  const matchMap = new Map<string, TelemetryEvent[]>();

  for (const ev of events) {
    // Strictly isolate: Only process events that went through the YouTube highlight pipeline
    if (!isHighlightEvent(ev)) continue;

    const text = ev.event_text || '';
    if (text.includes('WEBSUB')) continue;

    // Filter out batch summary sweeps from leaking into cards
    const isSweepOrSystem =
      text.includes('[UNLINKED & UPGRADE SWEEP]') ||
      text.includes('Identified') ||
      text.includes('[CLEANUP]') ||
      text.includes('[MAINTENANCE]');

    if (isSweepOrSystem) continue;

    let key = '';
    if (ev.match_id) {
      key = String(ev.match_id);
    } else {
      const extracted = extractMatchTitleFromText(text);
      if (extracted) key = extracted;
    }

    if (key) {
      if (!matchMap.has(key)) {
        matchMap.set(key, []);
      }
      matchMap.get(key)!.push(ev);
    }
  }

  const results: MatchWaterfall[] = [];

  for (const [key, rawEvList] of matchMap.entries()) {
    // Isolate events to the latest execution run using the last SEARCH_START boundary
    let lastStartIndex = -1;
    for (let i = rawEvList.length - 1; i >= 0; i--) {
      const t = rawEvList[i].event_text || '';
      const c = rawEvList[i].code || '';
      if (c === 'SEARCH_START' || t.includes('Searching official highlights for:') || c === 'COOLDOWN' || c === 'SKIP_UNFINISHED') {
        lastStartIndex = i;
        break;
      }
    }
    const evList = lastStartIndex >= 0 ? rawEvList.slice(lastStartIndex) : rawEvList;

    let title = /^\d+$/.test(key) ? '' : key;
    let matchDate = '';
    let outcomeId: string | undefined;
    let outcomeUrl: string | undefined;
    let outcomeReason = '';
    let cooldownRemainingMinutes: number | undefined;
    let cooldownReason: string | undefined;

    const step1Channels: string[] = [];
    const seenChannels = new Set<string>();
    const step2Candidates: string[] = [];
    const step3Verdicts: CandidateVerdict[] = [];

    for (const ev of evList) {
      const text = ev.event_text || '';
      const code = ev.code || '';
      const detail = text.replace(/^(\[[^\]]+\]\s*)+/, '').trim();
      const attrs = ev.attributes;

      // Resolve Title from event attributes
      if (!title) {
        const home = (attrs?.home_team || attrs?.home) as string | undefined;
        const away = (attrs?.away_team || attrs?.away) as string | undefined;
        const attrTitle = (attrs?.match_title as string) || (home && away ? `${home} vs ${away}` : '');
        if (attrTitle && !attrTitle.includes('None')) {
          title = attrTitle;
        } else {
          const fromText = extractMatchTitleFromText(text);
          if (fromText) title = fromText;
        }
      }

      if (text.includes('Searching official highlights for:')) {
        const dateMatch = text.match(/\b\d{4}-\d{2}-\d{2}\b/);
        if (dateMatch) matchDate = dateMatch[0];
        if (!seenChannels.has(detail)) {
          seenChannels.add(detail);
          step1Channels.push(detail);
        }
      } else if (text.includes('CHANNEL_SCAN') || text.includes('Checking official') || text.includes('CHANNEL SCAN')) {
        if (!seenChannels.has(detail)) {
          seenChannels.add(detail);
          step1Channels.push(detail);
        }
      } else if (text.includes('CANDIDATE_ACCEPTED') || text.includes('CANDIDATE_REJECTED')) {
        const v = parseCandidateVerdict(text, step3Verdicts.length + 1);
        if (v && !step3Verdicts.some((ex) => ex.candidateId === v.candidateId)) {
          step3Verdicts.push(v);
        }
      } else if (text.includes('CANDIDATE') || text.includes('DISCOVERY')) {
        if (!step2Candidates.includes(detail)) {
          step2Candidates.push(detail);
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
        const cdMatch = text.match(/(\d+(?:\.\d+)?)\s*m(?:in)?(?:\s+remaining)?/i);
        if (cdMatch) cooldownRemainingMinutes = Math.round(parseFloat(cdMatch[1]));
        const reasonMatch = text.match(/(?:Tier \d+[^.]+)/i);
        if (reasonMatch) cooldownReason = reasonMatch[0];
      } else if (code === 'NOT_FOUND' || code === 'RESULT_EMPTY' || text.includes('NO HIGHLIGHT FOUND') || text.includes('NOT AVAILABLE YET')) {
        outcomeReason = detail || 'No verified highlight video found';
      } else if (code === 'SKIP_UNFINISHED' || text.includes('SKIP UNFINISHED')) {
        outcomeReason = detail || 'Match in play (not finished yet)';
      } else if (code === 'NO_HANDLE' || text.includes('NO_HANDLE')) {
        outcomeReason = detail || 'No official YouTube handles mapped for clubs';
      }
    }

    const isCooldownStatus = outcomeReason.toLowerCase().includes('cooldown') || Boolean(cooldownReason);
    const isSkippedStatus = outcomeReason.toLowerCase().includes('in play') || outcomeReason.toLowerCase().includes('unfinished');
    const isNoHandleStatus = outcomeReason.toLowerCase().includes('no official');

    const status: MatchWaterfall['status'] = outcomeId
      ? 'LINKED'
      : isCooldownStatus
        ? 'COOLDOWN'
        : outcomeReason
          ? 'UNLINKED'
          : 'SEARCHED';

    let stoppedAtStep: 1 | 2 | 3 | 4 | undefined;
    let stoppedReason: string | undefined;

    if (status === 'UNLINKED' || status === 'SEARCHED') {
      if (isNoHandleStatus || step1Channels.length <= 1) {
        stoppedAtStep = 1;
        stoppedReason = 'No official YouTube channels mapped in database for clubs or league.';
      } else if (step2Candidates.length === 0) {
        stoppedAtStep = 2;
        stoppedReason = 'Zero candidate videos uploaded by official channels within match window (-2h to +96h).';
      } else if (step3Verdicts.length > 0 && !step3Verdicts.some((v) => v.isAccepted)) {
        stoppedAtStep = 3;
        stoppedReason = 'All detected candidate videos rejected during AI semantic reranking.';
      } else if (outcomeReason.toLowerCase().includes('geo') || outcomeReason.toLowerCase().includes('embed')) {
        stoppedAtStep = 4;
        stoppedReason = 'Candidate video embed is restricted or geo-blocked outside target markets.';
      } else {
        stoppedAtStep = step2Candidates.length === 0 ? 2 : 3;
        stoppedReason = outcomeReason || 'No verified official highlights matched this fixture.';
      }
    }

    const defaultOutcome = outcomeId
      ? `Linked video ${outcomeId}`
      : isCooldownStatus
        ? 'Protected by Tier Cooldown'
        : isSkippedStatus
          ? 'Match in play (eligible when finished)'
          : isNoHandleStatus
            ? 'No official broadcaster channel mapped'
            : 'No verified highlight video found yet';

    // Strictly discard any phantom matches with missing or purely numeric titles
    if (title && !/^\d+$/.test(title) && title !== 'General Routine' && title !== 'global') {
      results.push({
        matchId: key,
        title,
        date: matchDate,
        status,
        step1Channels,
        step2Candidates,
        step3Verdicts,
        step4Outcome: outcomeReason || defaultOutcome,
        outcomeVideoId: outcomeId,
        outcomeVideoUrl: outcomeUrl,
        cooldownReason: cooldownReason || (isCooldownStatus ? 'Tier Cooldown Protection Active' : undefined),
        cooldownRemainingMinutes,
        stoppedAtStep,
        stoppedReason,
      });
    }
  }

  return results;

}

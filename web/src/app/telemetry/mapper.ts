import { CandidateVerdict, DbFinishedMatch, MatchCycleRun, MatchWaterfall, ScannedVideo, TelemetryEvent } from './types';
import { computeNextCycleSchedule } from './scheduleHelper';
import {
  isHighlightEvent,
  parseCandidateVerdict,
  extractMatchTitleFromText,
  extractTimestampFromText,
} from './mapperHelper';

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
  const scannedUploads: Record<string, ScannedVideo[]> = {};

  for (const ev of events) {
    const text = ev.event_text || '';
    const code = ev.code || '';
    const detail = text.replace(/^(\[[^\]]+\]\s*)+/, '').trim();
    if (!timestamp) timestamp = extractTimestampFromText(text, ev.created_at);

    if (ev.attributes?.scanned_videos && Array.isArray(ev.attributes.scanned_videos)) {
      const rawCh = String(ev.attributes.channel || ev.attributes.channel_handle || '').trim();
      if (rawCh) {
        const chKey = rawCh.startsWith('@') ? rawCh : `@${rawCh}`;
        scannedUploads[chKey] = ev.attributes.scanned_videos as ScannedVideo[];
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
    } else if (code === 'RESULT_EMPTY' || code === 'RESULT_UNLINKED' || text.includes('No eligible highlight candidates found')) {
      outcomeReason = detail || 'No eligible highlight candidates found';
    } else if (code === 'CHANNEL_EMPTY' || text.includes('0 matching videos') || text.includes('0 public uploads')) {
      if (!outcomeReason) outcomeReason = detail || 'Official channels scanned with no highlight videos found';
    } else if (code === 'NOT_FOUND' || text.includes('NO HIGHLIGHT FOUND')) {
      outcomeReason = detail || 'No verified highlight video found';
    }
  }

  const totalCandCount = Object.values(scannedUploads).reduce((acc, list) => acc + list.length, 0) + step2Candidates.length;
  const isCd = outcomeReason.toLowerCase().includes('cooldown');
  const status: MatchCycleRun['status'] = outcomeId ? 'LINKED' : isCd ? 'COOLDOWN' : outcomeReason ? 'UNLINKED' : 'SEARCHED';
  const stoppedAtStep = status === 'UNLINKED' || status === 'SEARCHED' ? (totalCandCount === 0 ? 2 : 3) : undefined;
  const stoppedReason = stoppedAtStep
    ? (outcomeReason || (status === 'SEARCHED' ? 'Search completed with no candidate accepted' : 'No verified official highlights matched'))
    : undefined;

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

export function buildMatchWaterfalls(
  events: TelemetryEvent[],
  dbMatches: DbFinishedMatch[] = [],
): MatchWaterfall[] {
  const dbMap = new Map<string, DbFinishedMatch>();
  const dbTitleMap = new Map<string, DbFinishedMatch>();
  for (const dm of dbMatches) {
    dbMap.set(String(dm.id), dm);
    const titleKey = `${dm.home_team_name} vs ${dm.away_team_name}`.toLowerCase().trim();
    dbTitleMap.set(titleKey, dm);
  }

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
    const cycleMap = new Map<number | string, TelemetryEvent[]>();
    let currentSyntheticCycle = 1;

    for (let i = 0; i < rawEvList.length; i++) {
      const ev = rawEvList[i];
      if (i > 0 && (ev.code === 'SEARCH_START' || (ev.event_text || '').includes('Searching official'))) {
        currentSyntheticCycle++;
      }
      const cId = ev.cycle_id ?? `synth-${currentSyntheticCycle}`;
      if (!cycleMap.has(cId)) cycleMap.set(cId, []);
      cycleMap.get(cId)!.push(ev);
    }

    const cycles: MatchCycleRun[] = [];
    for (const [cId, cEvents] of cycleMap.entries()) {
      cycles.push(processCycleEvents(cEvents, typeof cId === 'number' ? cId : undefined));
    }
    cycles.reverse();
    const latest = cycles[0] || processCycleEvents(rawEvList);

    let dm = dbMap.get(key);
    if (!dm) {
      dm = dbTitleMap.get(key.toLowerCase().trim());
    }

    let title = dm ? `${dm.home_team_name} vs ${dm.away_team_name}` : (/^\d+$/.test(key) ? '' : key);
    let matchDate = dm?.utc_date ? dm.utc_date.split('T')[0] : '';
    let compId = dm?.competition_id;
    let compName = dm?.competition_name;
    let homeScore = dm?.home_score ?? null;
    let awayScore = dm?.away_score ?? null;

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
        const dmMatch = (ev.event_text || '').match(/\b\d{4}-\d{2}-\d{2}\b/);
        if (dmMatch) matchDate = dmMatch[0];
      }
    }

    if (!title) {
      for (const ev of rawEvList) {
        const t = extractMatchTitleFromText(ev.event_text || '');
        if (t) { title = t; break; }
      }
    }

    const dbOutcomeId = dm?.highlight_video_id || undefined;
    const outcomeId = latest.outcomeVideoId || dbOutcomeId;
    const isLinked = Boolean(outcomeId);

    if (isLinked) {
      latest.status = 'LINKED';
      latest.outcomeVideoId = outcomeId;
      latest.outcomeVideoUrl = `https://youtu.be/${outcomeId}`;
      latest.stoppedAtStep = undefined;
      latest.stoppedReason = undefined;
    }

    const finalStatus: MatchWaterfall['status'] = isLinked ? 'LINKED' : latest.status;
    const sched = !isLinked ? computeNextCycleSchedule(dm?.utc_date, dm?.last_youtube_check) : undefined;
    if (sched && latest) {
      latest.nextCycleEta = sched.nextCycleEta;
      latest.nextCycleTimestamp = sched.nextCycleTimestamp;
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
        status: finalStatus,
        step1Channels: latest.step1Channels,
        step2Candidates: latest.step2Candidates,
        step3Verdicts: latest.step3Verdicts,
        step4Outcome: latest.step4Outcome,
        outcomeVideoId: outcomeId,
        outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
        stoppedAtStep: latest.stoppedAtStep,
        stoppedReason: latest.stoppedReason,
        nextCycleEta: sched?.nextCycleEta,
        nextCycleTimestamp: sched?.nextCycleTimestamp,
        cycles,
      });
    }
  }

  // Synthesize finished matches from database that are not yet in telemetry events buffer
  for (const dm of dbMatches) {
    const idStr = String(dm.id);
    if (matchMap.has(idStr)) continue;

    const title = `${dm.home_team_name} vs ${dm.away_team_name}`;
    const date = dm.utc_date ? dm.utc_date.split('T')[0] : '';
    const outcomeId = dm.highlight_video_id || undefined;
    const isLinked = Boolean(outcomeId);
    const sched = !isLinked ? computeNextCycleSchedule(dm.utc_date, dm.last_youtube_check) : undefined;

    results.push({
      matchId: idStr,
      title,
      date,
      timestamp: date ? `${date} Final` : 'Recently',
      homeScore: dm.home_score,
      awayScore: dm.away_score,
      competitionId: dm.competition_id,
      competitionName: dm.competition_name,
      status: isLinked ? 'LINKED' : 'UNLINKED',
      step1Channels: isLinked ? ['Official Broadcaster Linked in Database'] : ['Awaiting Highlight Sweep'],
      step2Candidates: [],
      step3Verdicts: [],
      step4Outcome: isLinked ? `Linked video ${outcomeId}` : 'Match finished in DB, awaiting next highlight processor run',
      outcomeVideoId: outcomeId,
      outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
      stoppedAtStep: isLinked ? undefined : 1,
      stoppedReason: isLinked ? undefined : 'Scheduled for upcoming background highlight search',
      nextCycleEta: sched?.nextCycleEta,
      nextCycleTimestamp: sched?.nextCycleTimestamp,
      cycles: [{
        cycleId: undefined,
        timestamp: date || 'Recently',
        step1Channels: isLinked ? ['Official Broadcaster Linked in Database'] : ['Awaiting Highlight Sweep'],
        step2Candidates: [],
        step3Verdicts: [],
        step4Outcome: isLinked ? `Linked video ${outcomeId}` : 'Match finished in DB, awaiting next highlight processor run',
        outcomeVideoId: outcomeId,
        outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
        status: isLinked ? 'LINKED' : 'UNLINKED',
        stoppedAtStep: isLinked ? undefined : 1,
        stoppedReason: isLinked ? undefined : 'Scheduled for upcoming background highlight search',
        nextCycleEta: sched?.nextCycleEta,
        nextCycleTimestamp: sched?.nextCycleTimestamp,
      }],
    });
  }

  return results;
}

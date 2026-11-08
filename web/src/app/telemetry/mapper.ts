import { DbFinishedMatch, MatchCycleRun, MatchWaterfall, TelemetryEvent } from './types';
import { computeNextCycleSchedule } from './scheduleHelper';
import {
  isHighlightEvent,
  extractMatchTitleFromText,
  ChannelLookupMap,
  resolveDbMatchTargets,
  processCycleEvents,
  resolveChannelTier,
  resolveWorldwideAvailability,
} from './mapperHelper';

export function buildMatchWaterfalls(
  events: TelemetryEvent[],
  dbMatches: DbFinishedMatch[] = [],
  channelLookup?: ChannelLookupMap,
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

    let linkedChannel: string | undefined;
    if (dm?.highlight_videos && Array.isArray(dm.highlight_videos) && dm.highlight_videos.length > 0) {
      linkedChannel = dm.highlight_videos[0].channel_handle || dm.highlight_videos[0].channel_name;
    }
    if (!linkedChannel && outcomeId) {
      const v = latest.step3Verdicts.find((c) => c.candidateId === outcomeId);
      if (v) linkedChannel = v.channel;
    }
    if (!linkedChannel) {
      const matchOutcomeCh = latest.step4Outcome.match(/channel\s+(@[a-zA-Z0-9_-]+)/i);
      if (matchOutcomeCh) linkedChannel = matchOutcomeCh[1];
    }
    const homeTeam = dm?.home_team_name || (title.includes(' vs ') ? title.split(' vs ')[0].trim() : '');
    const awayTeam = dm?.away_team_name || (title.includes(' vs ') ? title.split(' vs ')[1].trim() : '');
    const linkedTier = isLinked ? resolveChannelTier(linkedChannel, compId, homeTeam, awayTeam, channelLookup) : undefined;
    const avail = isLinked ? resolveWorldwideAvailability(dm?.youtube_region_info) : undefined;

    if (isLinked) {
      latest.status = 'LINKED';
      latest.outcomeVideoId = outcomeId;
      latest.outcomeVideoUrl = `https://youtu.be/${outcomeId}`;
      latest.linkedChannelHandle = linkedChannel;
      latest.linkedChannelTier = linkedTier;
      latest.worldwideAvailabilityText = avail?.text;
      latest.worldwidePercentage = avail?.percentage;
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
        linkedChannelHandle: linkedChannel,
        linkedChannelTier: linkedTier,
        worldwideAvailabilityText: avail?.text,
        worldwidePercentage: avail?.percentage,
        stoppedAtStep: latest.stoppedAtStep,
        stoppedReason: latest.stoppedReason,
        nextCycleEta: sched?.nextCycleEta,
        nextCycleTimestamp: sched?.nextCycleTimestamp,
        cycles,
      });
    }
  }

  for (const dm of dbMatches) {
    const idStr = String(dm.id);
    if (matchMap.has(idStr)) continue;

    const title = `${dm.home_team_name} vs ${dm.away_team_name}`;
    const date = dm.utc_date ? dm.utc_date.split('T')[0] : '';
    const outcomeId = dm.highlight_video_id || undefined;
    const isLinked = Boolean(outcomeId);
    let linkedChannel: string | undefined;
    if (dm.highlight_videos && Array.isArray(dm.highlight_videos) && dm.highlight_videos.length > 0) {
      linkedChannel = dm.highlight_videos[0].channel_handle || dm.highlight_videos[0].channel_name;
    }
    const linkedTier = isLinked ? resolveChannelTier(linkedChannel, dm.competition_id, dm.home_team_name, dm.away_team_name, channelLookup) : undefined;
    const avail = isLinked ? resolveWorldwideAvailability(dm.youtube_region_info) : undefined;
    const sched = !isLinked ? computeNextCycleSchedule(dm.utc_date, dm.last_youtube_check) : undefined;
    const resolvedTargets = resolveDbMatchTargets(dm, channelLookup);
    const defaultChannels = resolvedTargets.length > 0
      ? [`Configured targets: ${resolvedTargets.join(', ')}`]
      : isLinked ? ['Official Broadcaster Linked in Database'] : ['Awaiting Highlight Sweep'];

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
      step1Channels: defaultChannels,
      step2Candidates: [],
      step3Verdicts: [],
      step4Outcome: isLinked ? `Linked video ${outcomeId}` : 'Match finished in DB, awaiting next highlight processor run',
      outcomeVideoId: outcomeId,
      outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
      linkedChannelHandle: linkedChannel,
      linkedChannelTier: linkedTier,
      worldwideAvailabilityText: avail?.text,
      worldwidePercentage: avail?.percentage,
      stoppedAtStep: isLinked ? undefined : 1,
      stoppedReason: isLinked ? undefined : 'Scheduled for upcoming background highlight search',
      nextCycleEta: sched?.nextCycleEta,
      nextCycleTimestamp: sched?.nextCycleTimestamp,
      cycles: [{
        cycleId: undefined,
        timestamp: date || 'Recently',
        step1Channels: defaultChannels,
        step2Candidates: [],
        step3Verdicts: [],
        step4Outcome: isLinked ? `Linked video ${outcomeId}` : 'Match finished in DB, awaiting next highlight processor run',
        outcomeVideoId: outcomeId,
        outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
        linkedChannelHandle: linkedChannel,
        linkedChannelTier: linkedTier,
        worldwideAvailabilityText: avail?.text,
        worldwidePercentage: avail?.percentage,
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


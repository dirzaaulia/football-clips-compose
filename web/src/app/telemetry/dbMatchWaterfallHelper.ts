import { DbFinishedMatch, MatchCycleRun, MatchWaterfall } from './types';
import { computeNextCycleSchedule } from './scheduleHelper';
import {
  ChannelLookupMap,
  resolveDbMatchTargets,
  resolveChannelTier,
  resolveWorldwideAvailability,
} from './mapperHelper';
import { resolveCumulativeAvailability } from './availabilityHelper';
import { WaterfallTraceRow, mapTraceToCycleRun } from './waterfallTraceHelper';

export function buildDbMatchWaterfall(
  dm: DbFinishedMatch,
  traceRows: WaterfallTraceRow[],
  channelLookup?: ChannelLookupMap,
): MatchWaterfall {
  const idStr = String(dm.id);
  const title = `${dm.home_team_name} vs ${dm.away_team_name}`;
  const date = dm.utc_date ? dm.utc_date.split('T')[0] : '';
  const outcomeId = dm.highlight_video_id || undefined;
  const isLinked = Boolean(outcomeId);
  let linkedChannel: string | undefined;
  let ingestionSource: MatchWaterfall['ingestionSource'] = undefined;
  if (dm.highlight_videos && Array.isArray(dm.highlight_videos) && dm.highlight_videos.length > 0) {
    linkedChannel = dm.highlight_videos[0].channel_handle || dm.highlight_videos[0].channel_name;
    const src = dm.highlight_videos[0].source;
    if (src === 'WEBSUB' || src === 'RSS' || src === 'API' || src === 'MANUAL') ingestionSource = src;
  }
  if (!ingestionSource && isLinked) ingestionSource = 'RSS';

  const linkedTier = isLinked ? resolveChannelTier(linkedChannel, dm.competition_id, dm.home_team_name, dm.away_team_name, channelLookup) : undefined;
  const cumulativeAvail = isLinked ? resolveCumulativeAvailability(dm) : undefined;
  const avail = cumulativeAvail || (isLinked ? resolveWorldwideAvailability(dm.youtube_region_info) : undefined);
  const sched = !isLinked ? computeNextCycleSchedule(dm.utc_date, dm.last_youtube_check) : undefined;
  const resolvedTargets = resolveDbMatchTargets(dm, channelLookup);
  const traceCycles = traceRows.map((tr) => mapTraceToCycleRun(tr, channelLookup));
  const latestTrace = traceCycles[0];

  const defaultChannels = latestTrace?.step1Channels?.length ? latestTrace.step1Channels : (resolvedTargets.length > 0
    ? resolvedTargets
    : isLinked ? [linkedChannel || 'Official Broadcaster Linked in Database'] : ['Awaiting Highlight Sweep']);

  const defaultCandidates = latestTrace?.step2Candidates?.length ? latestTrace.step2Candidates : (isLinked && outcomeId ? [outcomeId] : []);
  const defaultCandidatesList = latestTrace?.candidatesList?.length ? latestTrace.candidatesList : (isLinked && outcomeId ? [{
    videoId: outcomeId,
    title: `${title} Highlights`,
    channel: linkedChannel || 'Official Broadcaster',
    watchabilityPct: avail?.percentage ?? 100,
    source: ingestionSource,
    isEmbeddable: true,
  }] : []);

  const defaultVerdicts = latestTrace?.step3Verdicts?.length ? latestTrace.step3Verdicts : (isLinked && outcomeId ? [{
    candidateId: outcomeId,
    index: 1,
    title: `${title} Highlights`,
    channel: linkedChannel || 'Official Broadcaster',
    url: `https://youtu.be/${outcomeId}`,
    isAccepted: true,
    verdictReason: 'Verified official match highlight in datastore',
    evaluator: 'PIPELINE_LINKED',
    score: 100,
    source: ingestionSource,
  }] : []);

  const defaultOutcome = (isLinked && latestTrace?.status === 'LINKED' && latestTrace.step4Outcome)
    ? latestTrace.step4Outcome
    : isLinked
    ? `Official highlight linked: https://youtu.be/${outcomeId}${linkedChannel ? ` (${linkedChannel})` : ''}`
    : latestTrace?.step4Outcome || 'Match finished in DB, awaiting next highlight processor run';

  const syntheticCycle: MatchCycleRun = {
    timestamp: date || 'Recently',
    step1Channels: defaultChannels,
    step2Candidates: defaultCandidates,
    candidatesList: defaultCandidatesList,
    step3Verdicts: defaultVerdicts,
    step4Outcome: defaultOutcome,
    outcomeVideoId: outcomeId,
    outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
    linkedChannelHandle: linkedChannel,
    linkedChannelTier: linkedTier,
    worldwideAvailabilityText: avail?.text,
    worldwidePercentage: avail?.percentage,
    cumulativeAvailability: cumulativeAvail,
    ingestionSource,
    status: isLinked ? 'LINKED' : 'UNLINKED',
    stoppedAtStep: isLinked ? undefined : 1,
    stoppedReason: isLinked ? undefined : 'Scheduled for upcoming background highlight search',
    nextCycleEta: sched?.nextCycleEta,
    nextCycleTimestamp: sched?.nextCycleTimestamp,
  };

  const fallbackCycles: MatchCycleRun[] = traceCycles.length === 0
    ? [syntheticCycle]
    : (isLinked && traceCycles[0].status !== 'LINKED')
    ? [syntheticCycle, ...traceCycles]
    : traceCycles;

  return {
    matchId: idStr, title, date, timestamp: date ? `${date} Final` : 'Recently',
    homeScore: dm.home_score, awayScore: dm.away_score,
    competitionId: dm.competition_id, competitionName: dm.competition_name,
    status: isLinked ? 'LINKED' : 'UNLINKED',
    step1Channels: defaultChannels, step2Candidates: defaultCandidates,
    step3Verdicts: defaultVerdicts, step4Outcome: defaultOutcome,
    outcomeVideoId: outcomeId,
    outcomeVideoUrl: outcomeId ? `https://youtu.be/${outcomeId}` : undefined,
    linkedChannelHandle: linkedChannel, linkedChannelTier: linkedTier,
    worldwideAvailabilityText: avail?.text, worldwidePercentage: avail?.percentage,
    cumulativeAvailability: cumulativeAvail, ingestionSource,
    stoppedAtStep: isLinked ? undefined : 1,
    stoppedReason: isLinked ? undefined : 'Scheduled for upcoming background highlight search',
    nextCycleEta: sched?.nextCycleEta, nextCycleTimestamp: sched?.nextCycleTimestamp,
    cycles: fallbackCycles,
  };
}

import { MatchRecord } from '@/lib/database';
import { Competition } from '@/hooks/useCompetitions';

export interface FeedCompetitionGroup {
  compId: string;
  name: string;
  emblem?: string;
  matches: MatchRecord[];
}

const INTERNATIONAL_COMP_IDS = new Set([
  'UNL', 'WCQ_CONMEBOL', 'WCQ_AFC', 'WCQ_UEFA', 'CNL', 'WCQ_CONCACAF', 'WCQ_CAF', 'FRI',
]);

function getGroupPriority(
  group: FeedCompetitionGroup,
  competitions: Competition[],
): number {
  const compInfo = competitions.find((c) => c.competitionId === group.compId);
  const isIntl = compInfo?.type === 'international' || INTERNATIONAL_COMP_IDS.has(group.compId);

  let latestDateMs = 0;
  for (const m of group.matches) {
    if (m.utc_date) {
      const ms = new Date(m.utc_date).getTime();
      if (ms > latestDateMs) latestDateMs = ms;
    }
  }
  const isRecent = latestDateMs > 0 && Date.now() - latestDateMs < 7 * 24 * 60 * 60 * 1000;

  if (isIntl && isRecent) {
    const idx = competitions.findIndex((c) => c.competitionId === group.compId);
    return -1000 + (idx !== -1 ? idx : 0);
  }

  const idx = competitions.findIndex((c) => c.competitionId === group.compId);
  return idx !== -1 ? idx : 999;
}

export function groupFeedMatchesByCompetition(
  matches: MatchRecord[],
  competitions: Competition[],
): FeedCompetitionGroup[] {
  const map = new Map<string, FeedCompetitionGroup>();
  const groups: FeedCompetitionGroup[] = [];

  matches.forEach((m) => {
    const compId = m.competition_id || 'OTHER';
    const name = m.competition_name || 'Other Competitions';
    let entry = map.get(compId);
    if (!entry) {
      const compInfo = competitions.find((c) => c.competitionId === compId);
      entry = {
        compId,
        name,
        emblem: compInfo?.logo || m.country_logo || undefined,
        matches: [],
      };
      map.set(compId, entry);
      groups.push(entry);
    }
    entry.matches.push(m);
  });

  groups.sort((a, b) => {
    return getGroupPriority(a, competitions) - getGroupPriority(b, competitions);
  });

  return groups;
}

import { MatchRecord } from '@/lib/database';
import { Competition } from '@/hooks/useCompetitions';

export interface FeedCompetitionGroup {
  compId: string;
  name: string;
  emblem?: string;
  matches: MatchRecord[];
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
    const idxA = competitions.findIndex((c) => c.competitionId === a.compId);
    const idxB = competitions.findIndex((c) => c.competitionId === b.compId);
    return (idxA !== -1 ? idxA : 999) - (idxB !== -1 ? idxB : 999);
  });

  return groups;
}

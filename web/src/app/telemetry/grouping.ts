import { MatchWaterfall, WeekCompetitionGroup } from './types';

export function getCompetitionPriority(codeOrName?: string): number {
  if (!codeOrName) return 99;
  const upper = codeOrName.toUpperCase();
  if (upper.includes('CHAMPIONS LEAGUE') || upper === 'CL') return 1;
  if (upper.includes('EUROPA LEAGUE') || upper === 'EL') return 2;
  if (upper.includes('CONFERENCE LEAGUE') || upper === 'ECL') return 3;
  if (upper.includes('PREMIER LEAGUE') || upper === 'PL') return 4;
  if (upper.includes('LALIGA') || upper.includes('PRIMERA DIVISION') || upper === 'PD') return 5;
  if (upper.includes('SERIE A') || upper === 'SA') return 6;
  if (upper.includes('BUNDESLIGA') || upper === 'BL1') return 7;
  if (upper.includes('LIGUE 1') || upper === 'FL1') return 8;
  return 99;
}

function getWeekOfMonthInfo(dateStr: string): { monthWeekKey: string; weekStart: string; weekEnd: string; sortKey: string } {
  const d = dateStr ? new Date(dateStr) : new Date();
  const year = isNaN(d.getFullYear()) ? 2026 : d.getFullYear();
  const month = isNaN(d.getMonth()) ? 10 : d.getMonth();
  const day = isNaN(d.getDate()) ? 1 : d.getDate();

  const weekNum = Math.min(5, Math.floor((day - 1) / 7) + 1);
  const monthName = new Intl.DateTimeFormat('en-US', { month: 'long' }).format(new Date(year, month, 1));
  const monthShort = new Intl.DateTimeFormat('en-US', { month: 'short' }).format(new Date(year, month, 1));

  const startDay = (weekNum - 1) * 7 + 1;
  const lastDayOfMonth = new Date(year, month + 1, 0).getDate();
  const endDay = Math.min(startDay + 6, lastDayOfMonth);

  const monthWeekKey = `${monthName} ${year} · Week ${weekNum}`;
  const weekStart = `${monthShort} ${startDay}`;
  const weekEnd = `${monthShort} ${endDay}, ${year}`;
  const sortKey = `${year}-${String(month + 1).padStart(2, '0')}-W${weekNum}`;

  return { monthWeekKey, weekStart, weekEnd, sortKey };
}

export function groupWaterfallsByWeekAndCompetition(
  waterfalls: MatchWaterfall[],
): WeekCompetitionGroup[] {
  const weekMap = new Map<string, {
    info: ReturnType<typeof getWeekOfMonthInfo>;
    matches: MatchWaterfall[];
  }>();

  for (const wf of waterfalls) {
    const info = getWeekOfMonthInfo(wf.date);
    if (!weekMap.has(info.sortKey)) {
      weekMap.set(info.sortKey, { info, matches: [] });
    }
    weekMap.get(info.sortKey)!.matches.push(wf);
  }

  // Sort weeks descending (newest week first)
  const sortedWeeks = Array.from(weekMap.entries()).sort((a, b) => b[0].localeCompare(a[0]));

  const result: WeekCompetitionGroup[] = [];

  for (const [, { info, matches }] of sortedWeeks) {
    const compMap = new Map<string, { competitionId: string; competitionName: string; priority: number; matches: MatchWaterfall[] }>();

    for (const m of matches) {
      const compName = m.competitionName || 'Other Competitions';
      const compId = m.competitionId || compName;
      if (!compMap.has(compId)) {
        compMap.set(compId, {
          competitionId: compId,
          competitionName: compName,
          priority: getCompetitionPriority(compName),
          matches: [],
        });
      }
      compMap.get(compId)!.matches.push(m);
    }

    const sortedCompetitions = Array.from(compMap.values()).sort((a, b) => {
      if (a.priority !== b.priority) return a.priority - b.priority;
      return a.competitionName.localeCompare(b.competitionName);
    });

    result.push({
      monthWeekKey: info.monthWeekKey,
      weekStart: info.weekStart,
      weekEnd: info.weekEnd,
      competitions: sortedCompetitions,
    });
  }

  return result;
}

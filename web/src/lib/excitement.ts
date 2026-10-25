import { MatchRecord } from './database';

export const BIG_CLUB_PATTERNS = [
  'real madrid',
  'barcelona',
  'atletico madrid',
  'manchester city',
  'man city',
  'arsenal',
  'liverpool',
  'manchester united',
  'man utd',
  'chelsea',
  'tottenham',
  'newcastle',
  'bayern',
  'dortmund',
  'leverkusen',
  'juventus',
  'inter',
  'milan',
  'napoli',
  'roma',
  'psg',
  'paris',
];

export function isBigClub(name?: string | null): boolean {
  if (!name) return false;
  const n = name.toLowerCase();
  return BIG_CLUB_PATTERNS.some((p) => n.includes(p));
}

export interface RankedMatch {
  match: MatchRecord;
  score: number;
  totalGoals: number;
  isHighGoal: boolean;
  isClashOfTitans: boolean;
  isBigMatch: boolean;
  tag: string;
}

export function calculateExcitementRank(m: MatchRecord): RankedMatch {
  const home = m.home_team_name || m.home_team || '';
  const away = m.away_team_name || m.away_team || '';
  const hBig = isBigClub(home);
  const aBig = isBigClub(away);
  const isClashOfTitans = hBig && aBig;
  const isBigMatch = hBig || aBig;

  const hScore = m.home_score ?? 0;
  const aScore = m.away_score ?? 0;
  const hasScores = m.home_score !== null && m.away_score !== null;
  const totalGoals = hasScores ? hScore + aScore : 0;
  const goalDiff = hasScores ? Math.abs(hScore - aScore) : 99;

  let score = 0;
  let tag = 'Must-Watch';

  if (isClashOfTitans) {
    score += 65;
    tag = 'Clash of Titans';
  } else if (isBigMatch) {
    score += 40;
    tag = 'Heavyweight Duel';
  }

  if (totalGoals >= 5) {
    score += 50;
    tag = `Goal Fest • ${totalGoals} Goals`;
  } else if (totalGoals === 4) {
    score += 35;
    tag = `4-Goal Thriller`;
  } else if (totalGoals === 3) {
    score += 20;
  }

  if (hasScores && goalDiff <= 1 && totalGoals >= 3) {
    score += 15;
    if (tag === 'Must-Watch') tag = 'Dramatic Decider';
  }

  return {
    match: m,
    score,
    totalGoals,
    isHighGoal: totalGoals >= 4,
    isClashOfTitans,
    isBigMatch,
    tag,
  };
}

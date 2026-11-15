import { MatchRecord } from './database';

const BIG_CLUBS = [
  'real madrid', 'barcelona', 'atletico madrid',
  'manchester city', 'man city', 'arsenal', 'liverpool',
  'manchester united', 'man utd', 'chelsea', 'tottenham',
  'newcastle', 'bayern', 'dortmund', 'leverkusen',
  'juventus', 'inter', 'milan', 'napoli', 'roma', 'psg', 'paris',
];

function isBigClub(name?: string | null): boolean {
  if (!name) return false;
  const n = name.toLowerCase();
  return BIG_CLUBS.some((c) => n.includes(c));
}

export interface RankedExcitementItem {
  match: MatchRecord;
  score: number;
  tag: string;
}

export type ExcitementSlide =
  | { type: 'match'; match: MatchRecord; tag: string }
  | { type: 'ad' };

export function calculateExcitementRank(
  match: MatchRecord,
  isSpoilerFree = false
): RankedExcitementItem {
  const home = match.home_team_name || match.home_team;
  const away = match.away_team_name || match.away_team;
  const homeScore = match.home_score;
  const awayScore = match.away_score;

  const hBig = isBigClub(home);
  const aBig = isBigClub(away);
  const isClashOfTitans = hBig && aBig;
  const isBigMatch = hBig || aBig;

  const hasScores = homeScore !== null && awayScore !== null && homeScore !== undefined && awayScore !== undefined;
  const totalGoals = hasScores ? (homeScore ?? 0) + (awayScore ?? 0) : 0;
  const goalDiff = hasScores ? Math.abs((homeScore ?? 0) - (awayScore ?? 0)) : 99;

  let score = 0;
  let tag = '🔥 Must-Watch';

  if (isClashOfTitans) {
    score += 65;
    tag = '👑 Clash of Titans';
  } else if (isBigMatch) {
    score += 40;
    tag = '🌟 Heavyweight Duel';
  }

  if (totalGoals >= 5) {
    score += 50;
    tag = isSpoilerFree ? '🔥 Goal Fest' : `🔥 Goal Fest • ${totalGoals} Goals`;
  } else if (totalGoals === 4) {
    score += 35;
    tag = isSpoilerFree ? '⚡ High-Scoring Thriller' : '⚡ 4-Goal Thriller';
  } else if (totalGoals === 3) {
    score += 20;
  }

  if (hasScores && goalDiff <= 1 && totalGoals >= 3) {
    score += 15;
    if (tag === '🔥 Must-Watch') tag = '⚡ Dramatic Decider';
  }

  return { match, score, tag };
}

export function arrangeExcitementSlides(
  matches: MatchRecord[],
  isPro = false,
  isSpoilerFree = false
): ExcitementSlide[] {
  const valid = matches.filter((m) => !!m.highlight_video_id || !!m.external_video_id);
  const pool = valid.length > 0 ? valid : matches;
  if (pool.length === 0) return [];

  const ranked = pool
    .map((m) => calculateExcitementRank(m, isSpoilerFree))
    .sort((a, b) => b.score - a.score)
    .slice(0, 6);

  if (isPro) {
    return ranked.map((r) => ({ type: 'match', match: r.match, tag: r.tag }));
  }

  const slides: ExcitementSlide[] = [];
  ranked.forEach((r, idx) => {
    slides.push({ type: 'match', match: r.match, tag: r.tag });
    if ((idx + 1) % 2 === 0 && idx < ranked.length - 1) {
      slides.push({ type: 'ad' });
    }
  });

  return slides;
}

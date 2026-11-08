import { describe, expect, it } from 'vitest';
import { calculateExcitementRank, isBigClub } from '@/lib/excitement';
import { MatchRecord } from '@/lib/database';

describe('Excitement Ranking & Spoiler-Free Mode', () => {
  const thrillerMatch: MatchRecord = {
    id: '401915001',
    competition_id: 'PL',
    competition_name: 'Premier League',
    utc_date: '2026-11-08T16:30:00Z',
    status: 'FINISHED',
    home_team: 'Arsenal',
    away_team: 'Chelsea',
    home_team_name: 'Arsenal',
    away_team_name: 'Chelsea',
    home_score: 3,
    away_score: 2,
    highlight_video_id: 'vid_123',
  };

  it('Correctly identifies European giant clubs', () => {
    expect(isBigClub('Arsenal')).toBe(true);
    expect(isBigClub('Real Madrid')).toBe(true);
    expect(isBigClub('Luton Town')).toBe(false);
  });

  it('Calculates Clash of Titans + 5-goal thriller score and reveals goal count when spoiler-free is off', () => {
    const ranked = calculateExcitementRank(thrillerMatch, false);
    expect(ranked.isClashOfTitans).toBe(true);
    expect(ranked.isHighGoal).toBe(true);
    expect(ranked.score).toBe(130); // 65 (Clash) + 50 (5 goals) + 15 (1-goal margin)
    expect(ranked.tag).toBe('Goal Fest • 5 Goals');
  });

  it('Masks exact goal count in tag when Spoiler-Free mode is active', () => {
    const rankedSpoilerFree = calculateExcitementRank(thrillerMatch, true);
    expect(rankedSpoilerFree.score).toBe(130);
    expect(rankedSpoilerFree.tag).toBe('Goal Fest');
    expect(rankedSpoilerFree.tag).not.toContain('5 Goals');
  });
});

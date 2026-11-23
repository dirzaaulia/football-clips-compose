import { MatchRecord } from '@/lib/database';
import { EspnSummaryResult } from '@/lib/espn';
import { WebPitchPlay } from './WebPitchRadar';
import { extractScorerDisplayName } from './MatchScrubberStrip';

export function derivePitchPlays(
  match: MatchRecord,
  espnSummary?: EspnSummaryResult | null,
): WebPitchPlay[] {
  const rawEvents = (espnSummary?.events && espnSummary.events.length > 0)
    ? espnSummary.events
    : (match.events || []);
  const homeLabel = match.home_team_name || match.home_team;
  const awayLabel = match.away_team_name || match.away_team;

  if (rawEvents.length > 0) {
    return rawEvents.map((e, idx) => {
      const isHome = e.side !== 'away';
      const t = (e.type || e.type_text || 'Play').toLowerCase();
      const isGoal = Boolean(
        e.scoring_play || (t.includes('goal') && !t.includes('kick') && !t.includes('attempt')),
      );
      const isShot = t.includes('shot') || t.includes('attempt') || t.includes('saved');

      const defaultX = isGoal
        ? (isHome ? 87 : 13)
        : isShot
        ? (isHome ? 78 : 22)
        : (isHome ? 45 : 55);
      const defaultY = isGoal ? (idx % 2 === 0 ? 38 : 31) : (25 + ((idx * 7) % 20));

      const xPct = e.x_coord != null ? Math.round(e.x_coord > 1 ? e.x_coord : e.x_coord * 100) : defaultX;
      const yPct = e.y_coord != null ? Math.round(e.y_coord > 1 ? e.y_coord : e.y_coord * 100) : defaultY;
      const txPct = e.target_x != null
        ? Math.round(e.target_x > 1 ? e.target_x : e.target_x * 100)
        : (isGoal || isShot ? (isHome ? 99 : 1) : null);
      const tyPct = e.target_y != null
        ? Math.round(e.target_y > 1 ? e.target_y : e.target_y * 100)
        : (isGoal || isShot ? 50 : null);

      const cleanPlayer = extractScorerDisplayName(e);
      return {
        id: e.id || `evt-${idx}`,
        clock: e.clock || '',
        type: isGoal ? 'Goal' : isShot ? 'Shot' : e.type || 'Play',
        type_text: isGoal ? 'Goal' : isShot ? 'Shot' : e.type_text || e.type || 'Play',
        text: e.text || e.type_text || 'Match Event',
        player: cleanPlayer !== 'Scorer' ? cleanPlayer : isHome ? homeLabel : awayLabel,
        assist: e.assist || undefined,
        team_name: isHome ? homeLabel : awayLabel,
        x_percent: Math.max(4, Math.min(96, xPct)),
        y_percent: Math.max(6, Math.min(94, yPct)),
        target_x: txPct,
        target_y: tyPct,
        goal_position_y: e.goal_position_y,
        period: e.period,
        seconds: e.seconds,
      };
    });
  }

  // Synthesize goals from match score if no granular event stream
  const hGoals = match.home_score ?? 0;
  const aGoals = match.away_score ?? 0;
  const scorePlays: WebPitchPlay[] = [];
  for (let i = 0; i < hGoals; i++) {
    scorePlays.push({
      id: `hg-${i}`, clock: `${22 + i * 24}'`, type: 'Goal', type_text: 'Goal',
      text: `Goal — ${homeLabel}`, player: homeLabel, team_name: homeLabel,
      x_percent: 87, y_percent: 42 + (i % 3) * 8, target_x: 98, target_y: 50,
    });
  }
  for (let i = 0; i < aGoals; i++) {
    scorePlays.push({
      id: `ag-${i}`, clock: `${34 + i * 23}'`, type: 'Goal', type_text: 'Goal',
      text: `Goal — ${awayLabel}`, player: awayLabel, team_name: awayLabel,
      x_percent: 13, y_percent: 44 + (i % 3) * 8, target_x: 2, target_y: 50,
    });
  }
  return scorePlays;
}

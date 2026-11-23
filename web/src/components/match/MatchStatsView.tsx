'use client';

import React, { useMemo } from 'react';
import { MatchRecord } from '@/lib/database';
import { ensureLegibleHex } from '@/lib/colors';
import MatchMomentumChart from './MatchMomentumChart';
import { GoalBallIcon, extractScorerDisplayName } from './MatchScrubberStrip';

interface MatchStatsViewProps {
  match: MatchRecord;
  homeName: string;
  awayName: string;
  isLive: boolean;
  onOpenRadar?: () => void;
}

const STAT_METRICS: Array<{ key: string; label: string; isPct?: boolean }> = [
  { key: 'possessionPct', label: 'Possession %', isPct: true },
  { key: 'totalShots', label: 'Total Shots' },
  { key: 'shotsOnTarget', label: 'Shots on Target' },
  { key: 'cornerKicks', label: 'Corners' },
  { key: 'foulsCommitted', label: 'Fouls' },
  { key: 'yellowCards', label: 'Yellow Cards' },
  { key: 'redCards', label: 'Red Cards' },
  { key: 'passes', label: 'Passes' },
  { key: 'tackles', label: 'Tackles' },
];

function formatStatDisplay(val: string, isPct?: boolean, key?: string) {
  const num = parseFloat(val);
  if (isNaN(num) || !isPct) return val;
  if (key === 'possessionPct') return `${num.toFixed(1)}%`;
  return `${Math.round(num <= 1.0 && num > 0 ? num * 100 : num)}%`;
}

class StatsChartErrorBoundary extends React.Component<{ children: React.ReactNode }, { hasError: boolean }> {
  state = { hasError: false };
  static getDerivedStateFromError() { return { hasError: true }; }
  render() {
    if (this.state.hasError) {
      return (
        <div className="p-4 rounded-xl bg-[#0A0A0C] border border-white/10 text-center text-xs text-white/50 font-tech">
          Match momentum telemetry is temporarily unavailable for this fixture.
        </div>
      );
    }
    return this.props.children;
  }
}

export default function MatchStatsView({
  match,
  homeName,
  awayName,
  isLive,
  onOpenRadar,
}: MatchStatsViewProps) {
  const homeColor = ensureLegibleHex(match.home_color, '#DFFF00');
  const awayColor = ensureLegibleHex(match.away_color, '#38BDF8');

  const resolvedStats = useMemo(() => {
    const rawH = { ...(match.statistics?.home || {}) };
    const rawA = { ...(match.statistics?.away || {}) };
    const evs = match.events || [];

    // Backfill basic tallies from events if boxscore statistics are sparse
    if (Object.keys(rawH).length === 0 && Object.keys(rawA).length === 0) {
      let hShots = match.home_score ?? 0;
      let aShots = match.away_score ?? 0;
      let hYC = 0;
      let aYC = 0;
      let hRC = 0;
      let aRC = 0;

      for (const e of evs) {
        const t = (e.type || e.type_text || '').toLowerCase();
        const isH = e.side !== 'away';
        if (t.includes('shot') || t.includes('attempt') || e.scoring_play) {
          isH ? hShots++ : aShots++;
        } else if (t.includes('yellow')) {
          isH ? hYC++ : aYC++;
        } else if (t.includes('red')) {
          isH ? hRC++ : aRC++;
        }
      }

      rawH.possessionPct = '50';
      rawA.possessionPct = '50';
      rawH.totalShots = String(Math.max(hShots, match.home_score ?? 0));
      rawA.totalShots = String(Math.max(aShots, match.away_score ?? 0));
      rawH.shotsOnTarget = String(match.home_score ?? 0);
      rawA.shotsOnTarget = String(match.away_score ?? 0);
      rawH.yellowCards = String(hYC);
      rawA.yellowCards = String(aYC);
      if (hRC > 0 || aRC > 0) {
        rawH.redCards = String(hRC);
        rawA.redCards = String(aRC);
      }
    }
    return { home: rawH, away: rawA };
  }, [match.statistics, match.events, match.home_score, match.away_score]);

  const goalEvents = useMemo(
    () =>
      (match.events || []).filter((e) => {
        const t = (e.type || e.type_text || '').toLowerCase();
        return e.scoring_play || (t.includes('goal') && !t.includes('kick') && !t.includes('attempt'));
      }),
    [match.events],
  );

  const rawPossHome = parseFloat(resolvedStats.home.possessionPct || '50');
  const rawPossAway = parseFloat(resolvedStats.away.possessionPct || '50');
  const possHome = isNaN(rawPossHome) ? 50 : Math.round(rawPossHome);
  const possAway = isNaN(rawPossAway) ? 50 : Math.round(rawPossAway);

  return (
    <div className="space-y-5 bg-[#121316] p-4 sm:p-5 select-none">
      <StatsChartErrorBoundary>
        <MatchMomentumChart
          homeTeam={homeName}
          awayTeam={awayName}
          homeScore={match.home_score}
          awayScore={match.away_score}
          homeColor={homeColor}
          awayColor={awayColor}
          events={match.events}
          possHome={possHome}
        />
      </StatsChartErrorBoundary>

      {/* Possession Bar */}
      <div className="space-y-2 p-4 rounded-xl bg-[#0A0A0C] border border-white/10">
        <div className="flex items-center justify-between text-xs sm:text-sm font-tech">
          <span className="font-bold uppercase tracking-wider text-white/80">
            Ball Possession
          </span>
          <span className="text-[#DFFF00] font-mono text-xs font-black">
            {isLive ? 'LIVE MATCH' : 'FULL TIME'}
          </span>
        </div>
        <div className="flex justify-between items-baseline font-scoreboard text-2xl sm:text-3xl text-white">
          <span style={{ color: homeColor }}>{possHome}%</span>
          <span className="font-tech text-xs text-white/70 uppercase font-bold tracking-widest">
            Possession Share
          </span>
          <span style={{ color: awayColor }}>{possAway}%</span>
        </div>
        <div className="w-full h-2.5 rounded-full bg-[#1A1C22] overflow-hidden flex">
          <div className="h-full transition-all duration-500" style={{ width: `${possHome}%`, backgroundColor: homeColor }} />
          <div className="h-full transition-all duration-500" style={{ width: `${possAway}%`, backgroundColor: awayColor }} />
        </div>
      </div>

      {/* Metrics & Shot Placement */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-5 items-start">
        <div className="md:col-span-7 space-y-3">
          <div className="flex items-center justify-between text-xs sm:text-sm font-extrabold text-white px-1">
            <span style={{ color: homeColor }}>{homeName}</span>
            <span className="text-white/70 text-xs uppercase font-tech font-bold tracking-wider">Match Stats</span>
            <span className="text-right" style={{ color: awayColor }}>{awayName}</span>
          </div>

          <div className="space-y-3">
            {STAT_METRICS.filter((m) => m.key !== 'possessionPct').map(({ key, label, isPct }) => {
              const rawH = resolvedStats.home[key];
              const rawA = resolvedStats.away[key];
              if (rawH === undefined && rawA === undefined) return null;

              const numH = parseFloat(rawH || '0');
              const numA = parseFloat(rawA || '0');
              const sum = numH + numA;
              const homeWidth = sum > 0 ? (numH / sum) * 100 : 50;
              const awayWidth = sum > 0 ? (numA / sum) * 100 : 50;

              return (
                <div key={key} className="space-y-1.5">
                  <div className="flex justify-between items-center text-xs sm:text-sm font-tech">
                    <span className="font-mono font-black text-sm sm:text-base" style={{ color: homeColor }}>
                      {formatStatDisplay(rawH || '0', isPct, key)}
                    </span>
                    <span className="text-xs sm:text-sm font-semibold text-white/85">{label}</span>
                    <span className="font-mono font-black text-sm sm:text-base" style={{ color: awayColor }}>
                      {formatStatDisplay(rawA || '0', isPct, key)}
                    </span>
                  </div>
                  <div className="h-2 w-full bg-[#0A0A0C] rounded-full overflow-hidden flex gap-0.5">
                    <div className="h-full rounded-l-full transition-all duration-500" style={{ width: `${homeWidth}%`, backgroundColor: homeColor }} />
                    <div className="h-full rounded-r-full transition-all duration-500" style={{ width: `${awayWidth}%`, backgroundColor: awayColor }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Mini Goal Map */}
        <div className="md:col-span-5 flex flex-col gap-2.5">
          <div className="flex items-center justify-between text-xs sm:text-sm font-tech">
            <span className="uppercase tracking-wider text-white/80 font-bold">
              Goal &amp; Shot Locations
            </span>
            <span className="text-xs text-[#DFFF00] font-black font-mono">
              PITCH OVERVIEW
            </span>
          </div>

          <div className="relative w-full aspect-[4/3] rounded-xl bg-[#0A0A0C] border border-white/10 p-3 overflow-hidden shadow-inner flex flex-col justify-between">
            <div className="absolute inset-2 rounded pointer-events-none opacity-20 border border-white">
              <div className="absolute top-0 bottom-0 left-1/2 w-0.5 bg-white -translate-x-1/2" />
              <div className="absolute top-1/2 left-1/2 w-12 h-12 rounded-full border border-white -translate-x-1/2 -translate-y-1/2" />
              <div className="absolute left-0 top-1/4 bottom-1/4 w-7 border-t border-b border-r border-white" />
              <div className="absolute right-0 top-1/4 bottom-1/4 w-7 border-t border-b border-l border-white" />
            </div>

            <div className="relative z-10 w-full h-full">
              {goalEvents.map((g, idx) => {
                const isHome = g.side !== 'away';
                const leftPct = g.x_coord != null ? Math.round(g.x_coord * 100) : isHome ? 84 - (idx * 6) % 14 : 16 + (idx * 6) % 14;
                const topPct = g.y_coord != null ? Math.round(g.y_coord * 100) : 32 + ((idx * 17) % 36);
                return (
                  <div
                    key={g.id || idx}
                    style={{ top: `${topPct}%`, left: `${leftPct}%` }}
                    className="absolute -translate-x-1/2 -translate-y-1/2 group/marker cursor-pointer"
                  >
                    <span className="w-6 h-6 rounded-full bg-[#1A1C22] border border-[#DFFF00] flex items-center justify-center shadow-lg">
                      <GoalBallIcon className="w-3.5 h-3.5 text-[#DFFF00]" />
                    </span>
                    <div className="absolute right-7 top-0 hidden group-hover/marker:flex flex-col bg-[#1A1C22] border border-white/10 px-2.5 py-1.5 rounded-lg shadow-xl whitespace-nowrap z-30">
                      <span className="font-tech text-xs sm:text-sm text-[#DFFF00] font-black">
                        {g.clock} • {extractScorerDisplayName(g)}
                      </span>
                      <span className="text-xs text-white/80 font-bold">
                        {isHome ? homeName : awayName}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="relative z-10 flex items-center justify-between text-xs font-tech text-white/85 bg-[#121316]/95 px-3 py-1.5 rounded-lg border border-white/10">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: homeColor }} />
                <span className="font-bold">{homeName}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: awayColor }} />
                <span className="font-bold">{awayName}</span>
              </div>
            </div>
          </div>

          {onOpenRadar && (
            <button
              type="button"
              onClick={onOpenRadar}
              className="w-full py-2.5 rounded-xl bg-[#1A1C22] hover:bg-[#252830] text-[#DFFF00] font-tech font-black transition-colors cursor-pointer border border-white/10 text-xs sm:text-sm"
            >
              OPEN FULL 2D PITCH VIEW &rarr;
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

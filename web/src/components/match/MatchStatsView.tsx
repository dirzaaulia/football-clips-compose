'use client';

import React from 'react';
import { MatchRecord } from '@/lib/database';
import { ensureLegibleHex } from '@/lib/colors';
import MatchMomentumChart from './MatchMomentumChart';

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
  if (isNaN(num)) return val;
  if (isPct) {
    if (key === 'possessionPct') return `${num.toFixed(1)}%`;
    if (num <= 1.0 && num > 0) return `${Math.round(num * 100)}%`;
    return `${Math.round(num)}%`;
  }
  return val;
}

export default function MatchStatsView({
  match,
  homeName,
  awayName,
  isLive,
  onOpenRadar,
}: MatchStatsViewProps) {
  const stats = match.statistics;
  const hasStats = Boolean(
    stats &&
      ((stats.home && Object.keys(stats.home).length > 0) ||
        (stats.away && Object.keys(stats.away).length > 0))
  );

  const homeColor = ensureLegibleHex(match.home_color, '#DFFF00');
  const awayColor = ensureLegibleHex(match.away_color, '#38BDF8');

  const goalEvents = (match.events || []).filter(
    (e) => (e.type?.toLowerCase().includes('goal') || e.text?.toLowerCase().includes('goal'))
  );

  if (!hasStats && !isLive) {
    return (
      <div className="py-6 text-center text-xs font-tech text-white/50 bg-[#121316] rounded-2xl border border-white/10">
        No detailed statistics recorded for this fixture.
      </div>
    );
  }

  const rawPossHome = parseFloat(stats?.home?.possessionPct || '50');
  const rawPossAway = parseFloat(stats?.away?.possessionPct || '50');
  const possHome = isNaN(rawPossHome) ? 50 : Math.round(rawPossHome);
  const possAway = isNaN(rawPossAway) ? 50 : Math.round(rawPossAway);

  return (
    <div className="space-y-5 bg-[#121316] rounded-2xl p-4 sm:p-5 border border-white/10 shadow-xl select-none">
      <MatchMomentumChart
        homeTeam={homeName}
        awayTeam={awayName}
        homeScore={match.home_score}
        awayScore={match.away_score}
        homeColor={homeColor}
        awayColor={awayColor}
      />

      {/* Dual Possession & Dominance Banner */}
      <div className="space-y-1.5 p-3 rounded-xl bg-[#0A0A0C] border border-white/10">
        <div className="flex items-center justify-between text-xs font-tech">
          <span className="font-bold uppercase tracking-wider text-white/60">
            Possession &amp; Dominance
          </span>
          <span className="text-[#DFFF00] font-mono text-[11px] font-bold">
            {isLive ? 'LIVE MATCH TEMPO' : 'FULL 90\' REGULATION'}
          </span>
        </div>
        <div className="flex justify-between items-baseline font-scoreboard text-xl sm:text-2xl text-white">
          <span style={{ color: homeColor }}>{possHome}%</span>
          <span className="font-tech text-[10px] text-white/40 uppercase font-bold tracking-widest">
            Ball Possession
          </span>
          <span style={{ color: awayColor }}>{possAway}%</span>
        </div>
        <div className="w-full h-2 rounded-full bg-[#1A1C22] overflow-hidden flex">
          <div className="h-full transition-all duration-500" style={{ width: `${possHome}%`, backgroundColor: homeColor }} />
          <div className="h-full transition-all duration-500" style={{ width: `${possAway}%`, backgroundColor: awayColor }} />
        </div>
      </div>

      {/* 2-Column Split: Telemetry Metrics Bars (7 cols) + Mini Tactical Pitch Map (5 cols) */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-5 items-start">
        {/* Left: Metric Comparison Bars */}
        <div className="md:col-span-7 space-y-3">
          <div className="flex items-center justify-between text-xs font-black text-white px-1">
            <span className="truncate max-w-[45%]" style={{ color: homeColor }}>{homeName}</span>
            <span className="text-white/40 text-[10px] uppercase font-tech tracking-wider">Metrics</span>
            <span className="truncate max-w-[45%] text-right" style={{ color: awayColor }}>{awayName}</span>
          </div>

          <div className="space-y-2.5">
            {STAT_METRICS.filter((m) => m.key !== 'possessionPct').map(({ key, label, isPct }) => {
              const rawH = stats?.home?.[key];
              const rawA = stats?.away?.[key];
              if (!rawH && !rawA) return null;

              const numH = parseFloat(rawH || '0');
              const numA = parseFloat(rawA || '0');
              const sum = numH + numA;
              const homeWidth = sum > 0 ? (numH / sum) * 100 : 50;
              const awayWidth = sum > 0 ? (numA / sum) * 100 : 50;

              return (
                <div key={key} className="space-y-1">
                  <div className="flex justify-between items-center text-xs font-tech">
                    <span className="font-mono font-bold" style={{ color: homeColor }}>
                      {formatStatDisplay(rawH || '0', isPct, key)}
                    </span>
                    <span className="text-[11px] text-white/50">{label}</span>
                    <span className="font-mono font-bold" style={{ color: awayColor }}>
                      {formatStatDisplay(rawA || '0', isPct, key)}
                    </span>
                  </div>
                  <div className="h-1.5 w-full bg-[#0A0A0C] rounded-full overflow-hidden flex gap-0.5">
                    <div className="h-full rounded-l-full transition-all duration-500" style={{ width: `${homeWidth}%`, backgroundColor: homeColor }} />
                    <div className="h-full rounded-r-full transition-all duration-500" style={{ width: `${awayWidth}%`, backgroundColor: awayColor }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right: Mini Tactical Pitch Heatmap & Shot Map */}
        <div className="md:col-span-5 flex flex-col gap-2">
          <div className="flex items-center justify-between text-xs font-tech">
            <span className="uppercase tracking-wider text-white/60 font-bold">
              Shot Map &amp; Pitch View
            </span>
            <span className="text-[10px] text-[#DFFF00] font-bold font-mono">
              EVENT PLACEMENT
            </span>
          </div>

          <div className="relative w-full aspect-[4/3] rounded-xl bg-[#0A0A0C] border border-white/10 p-3 overflow-hidden shadow-inner flex flex-col justify-between">
            {/* Field lines */}
            <div className="absolute inset-2 rounded pointer-events-none opacity-20 border border-white">
              <div className="absolute top-1/2 left-0 right-0 h-0.5 bg-white -translate-y-1/2" />
              <div className="absolute top-1/2 left-1/2 w-14 h-14 rounded-full border border-white -translate-x-1/2 -translate-y-1/2" />
              <div className="absolute top-0 left-1/4 right-1/4 h-8 border-b border-l border-r border-white" />
              <div className="absolute bottom-0 left-1/4 right-1/4 h-8 border-t border-l border-r border-white" />
            </div>

            {/* Glowing heatmaps */}
            <div className="absolute top-6 right-6 w-20 h-20 rounded-full bg-[#DFFF00]/15 blur-xl pointer-events-none" />
            <div className="absolute bottom-8 left-8 w-16 h-16 rounded-full bg-[#38BDF8]/15 blur-lg pointer-events-none" />

            {/* Goal Markers */}
            <div className="relative z-10 w-full h-full">
              {goalEvents.map((g, idx) => {
                const isHome = g.side === 'home';
                const topPct = isHome ? 25 + (idx * 20) % 40 : 60 + (idx * 15) % 30;
                const leftPct = isHome ? 35 + (idx * 25) % 50 : 40 + (idx * 20) % 40;
                return (
                  <div
                    key={g.id || idx}
                    style={{ top: `${topPct}%`, left: `${leftPct}%` }}
                    className="absolute -translate-x-1/2 -translate-y-1/2 group/marker cursor-pointer"
                  >
                    <span
                      className={`w-5 h-5 rounded-full flex items-center justify-center font-tech text-[9px] font-bold shadow-lg ${
                        isHome ? 'bg-[#DFFF00] text-[#0A0A0C]' : 'bg-[#38BDF8] text-white'
                      }`}
                    >
                      ⚽
                    </span>
                    <div className="absolute left-6 top-0 hidden group-hover/marker:flex flex-col bg-[#1A1C22] border border-white/10 px-2 py-1 rounded shadow-xl whitespace-nowrap z-30">
                      <span className="font-tech text-xs text-[#DFFF00] font-bold">
                        {g.clock} • {g.player || g.text}
                      </span>
                      <span className="text-[10px] text-white/60">
                        {isHome ? homeName : awayName}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Bottom Legend */}
            <div className="relative z-10 flex items-center justify-between text-[10px] font-tech text-white/60 bg-[#121316]/90 px-2 py-1 rounded border border-white/5">
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: homeColor }} />
                <span className="truncate max-w-[80px]">{homeName}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: awayColor }} />
                <span className="truncate max-w-[80px]">{awayName}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Starting XI Quick Teaser */}
      {match.formations?.home?.starters && (
        <div className="w-full bg-[#0A0A0C] border border-white/10 rounded-xl p-3 flex flex-wrap items-center justify-between gap-3 text-xs font-tech">
          <div className="flex items-center gap-2 truncate min-w-0">
            <span className="px-2 py-0.5 rounded bg-white/10 text-[#DFFF00] font-bold">
              {match.formations.home.formation || 'XI'}
            </span>
            <span className="font-bold text-white truncate">
              {homeName} Starting XI:
            </span>
            <span className="text-white/50 truncate hidden sm:inline">
              {match.formations.home.starters.slice(0, 7).map((p: any) => p.name).join(', ')}...
            </span>
          </div>

          {onOpenRadar && (
            <button
              onClick={onOpenRadar}
              className="px-3 py-1.5 rounded-lg bg-[#1A1C22] hover:bg-[#252830] text-[#DFFF00] font-bold transition-colors cursor-pointer shrink-0 border border-white/10 text-[11px]"
            >
              LAUNCH 2D PITCH RADAR &rarr;
            </button>
          )}
        </div>
      )}
    </div>
  );
}

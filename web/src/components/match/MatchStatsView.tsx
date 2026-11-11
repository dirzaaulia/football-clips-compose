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
}: MatchStatsViewProps) {
  const stats = match.statistics;
  const hasStats = Boolean(
    stats &&
      ((stats.home && Object.keys(stats.home).length > 0) ||
        (stats.away && Object.keys(stats.away).length > 0))
  );

  const homeColor = ensureLegibleHex(match.home_color, '#7098F8');
  const awayColor = ensureLegibleHex(match.away_color, '#4FC3F7');

  if (!hasStats) {
    if (isLive) {
      return (
        <div className="py-6 px-4 text-center rounded-2xl bg-[#101418] border border-[#334155]/60 space-y-1.5">
          <div className="flex items-center justify-center gap-1.5 text-red-400 text-xs font-black uppercase tracking-wider">
            <span className="w-2 h-2 rounded-full bg-red-500 animate-ping" />
            <span>Telemetry Initializing</span>
          </div>
          <p className="text-xs text-[#94A3B8]">
            Collecting live possession, shot accuracy, and pitch metrics directly from stadium feeds...
          </p>
        </div>
      );
    }
    return (
      <div className="py-6 text-center text-xs text-[#94A3B8]">
        No detailed statistics recorded for this fixture.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <MatchMomentumChart
        homeTeam={homeName}
        awayTeam={awayName}
        homeScore={match.home_score}
        awayScore={match.away_score}
      />
      <div className="flex items-center justify-between text-xs font-black text-white px-2">
        <span className="truncate max-w-[45%]" style={{ color: homeColor }}>{homeName}</span>
        <span className="text-[#94A3B8] text-[10px] uppercase tracking-wider">Metrics</span>
        <span className="truncate max-w-[45%] text-right" style={{ color: awayColor }}>{awayName}</span>
      </div>

      <div className="space-y-3">
        {STAT_METRICS.map(({ key, label, isPct }) => {
          const rawHome = stats?.home?.[key];
          const rawAway = stats?.away?.[key];
          if (!rawHome && !rawAway) return null;

          const numH = parseFloat(rawHome || '0');
          const numA = parseFloat(rawAway || '0');
          const sum = numH + numA;
          const homeWidth = sum > 0 ? (numH / sum) * 100 : 50;
          const awayWidth = sum > 0 ? (numA / sum) * 100 : 50;

          return (
            <div key={key} className="space-y-1">
              <div className="flex justify-between items-center text-xs">
                <span className="font-mono" style={{ color: homeColor }}>{formatStatDisplay(rawHome || '0', isPct, key)}</span>
                <span className="text-[11px] text-[#94A3B8] font-medium">{label}</span>
                <span className="font-mono" style={{ color: awayColor }}>{formatStatDisplay(rawAway || '0', isPct, key)}</span>
              </div>
              <div className="h-1.5 w-full bg-[#101418] rounded-full overflow-hidden flex gap-0.5">
                <div className="h-full rounded-l-full transition-all duration-500" style={{ width: `${homeWidth}%`, backgroundColor: homeColor }} />
                <div className="h-full rounded-r-full transition-all duration-500" style={{ width: `${awayWidth}%`, backgroundColor: awayColor }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

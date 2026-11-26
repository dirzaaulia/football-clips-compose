'use client';

import React from 'react';
import {
  Activity,
  CheckCircle2,
  AlertTriangle,
  Target,
  Globe,
  Calendar,
  X,
} from 'lucide-react';
import { CoverageFilterType, SeasonFilterType } from '../types';
import { CoverageMetricsSummary } from '../coverageMetricsHelper';

interface CoverageMetricsDeckProps {
  metrics: CoverageMetricsSummary;
  activeFilter: CoverageFilterType;
  onSelectFilter: (filter: CoverageFilterType) => void;
  seasonFilter: SeasonFilterType;
  onSeasonChange: (season: SeasonFilterType) => void;
  isLoadingSeason?: boolean;
}

function pctText(part: number, whole: number): string {
  if (whole <= 0) return '0.0%';
  return `${((part / whole) * 100).toFixed(1)}%`;
}

export function CoverageMetricsDeck({
  metrics,
  activeFilter,
  onSelectFilter,
  seasonFilter,
  onSeasonChange,
  isLoadingSeason = false,
}: CoverageMetricsDeckProps) {
  const cards: {
    key: CoverageFilterType;
    label: string;
    count: number;
    sub: string;
    icon: React.ReactNode;
    accent: string;
    ring: string;
    badgeBg: string;
  }[] = [
    {
      key: 'ALL',
      label: 'Total Finished',
      count: metrics.totalFinished,
      sub: seasonFilter === '2026' ? '2026+ Season' : seasonFilter === '2025' ? '2025 Archive' : 'All Seasons',
      icon: <Activity className="w-4 h-4 text-indigo-400" />,
      accent: 'border-indigo-500/30 hover:border-indigo-400/60',
      ring: 'ring-2 ring-indigo-500 border-indigo-400 bg-indigo-950/30',
      badgeBg: 'bg-indigo-500/15 text-indigo-300',
    },
    {
      key: 'LINKED',
      label: 'Linked Highlights',
      count: metrics.linkedCount,
      sub: `${pctText(metrics.linkedCount, metrics.totalFinished)} covered`,
      icon: <CheckCircle2 className="w-4 h-4 text-emerald-400" />,
      accent: 'border-emerald-500/30 hover:border-emerald-400/60',
      ring: 'ring-2 ring-emerald-500 border-emerald-400 bg-emerald-950/30',
      badgeBg: 'bg-emerald-500/15 text-emerald-300',
    },
    {
      key: 'UNLINKED',
      label: 'Unlinked / Missing',
      count: metrics.unlinkedCount,
      sub: `${pctText(metrics.unlinkedCount, metrics.totalFinished)} unlinked`,
      icon: <AlertTriangle className="w-4 h-4 text-rose-400" />,
      accent: 'border-rose-500/30 hover:border-rose-400/60',
      ring: 'ring-2 ring-rose-500 border-rose-400 bg-rose-950/30',
      badgeBg: 'bg-rose-500/15 text-rose-300',
    },
    {
      key: 'BELOW_TARGET',
      label: 'Below Target Market',
      count: metrics.belowTargetCount,
      sub: `${pctText(metrics.belowTargetCount, metrics.linkedCount)} missing targets`,
      icon: <Target className="w-4 h-4 text-amber-400" />,
      accent: 'border-amber-500/30 hover:border-amber-400/60',
      ring: 'ring-2 ring-amber-500 border-amber-400 bg-amber-950/30',
      badgeBg: 'bg-amber-500/15 text-amber-300',
    },
    {
      key: 'WORLDWIDE_99',
      label: 'Worldwide 99%+',
      count: metrics.worldwide99Count,
      sub: `${pctText(metrics.worldwide99Count, metrics.linkedCount)} global reach`,
      icon: <Globe className="w-4 h-4 text-cyan-400" />,
      accent: 'border-cyan-500/30 hover:border-cyan-400/60',
      ring: 'ring-2 ring-cyan-500 border-cyan-400 bg-cyan-950/30',
      badgeBg: 'bg-cyan-500/15 text-cyan-300',
    },
  ];

  const seasons: { key: SeasonFilterType; label: string }[] = [
    { key: '2026', label: '2026+ Current' },
    { key: '2025', label: '2025 Archive' },
    { key: 'ALL', label: 'All Seasons' },
  ];

  return (
    <div className="bg-slate-900/70 border border-slate-800 rounded-2xl p-4 space-y-3">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div className="flex items-center gap-2 text-xs font-mono text-slate-300">
          <Calendar className="w-3.5 h-3.5 text-indigo-400" />
          <span className="font-bold uppercase tracking-wider">Season Coverage & Reach</span>
          {activeFilter !== 'ALL' && (
            <button
              type="button"
              onClick={() => onSelectFilter('ALL')}
              className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 hover:bg-indigo-500/30 transition text-[11px]"
            >
              <span>Filter: {activeFilter}</span>
              <X className="w-3 h-3" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-1 bg-slate-950 border border-slate-800 rounded-lg p-0.5 self-start sm:self-auto">
          {seasons.map((s) => (
            <button
              key={s.key}
              type="button"
              disabled={isLoadingSeason}
              onClick={() => onSeasonChange(s.key)}
              className={`px-2.5 py-1 rounded-md text-[11px] font-mono font-semibold transition ${
                seasonFilter === s.key
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              } disabled:opacity-50`}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2.5">
        {isLoadingSeason ? (
          [1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="p-3 rounded-xl border border-slate-800 bg-slate-950/70 space-y-2.5 animate-pulse">
              <div className="flex items-center justify-between gap-1.5">
                <div className="w-24 h-3 bg-slate-800 rounded" />
                <div className="w-4 h-4 bg-slate-800 rounded-full" />
              </div>
              <div className="w-16 h-7 bg-slate-800/90 rounded" />
              <div className="flex items-center justify-between gap-1">
                <div className="w-20 h-4 bg-slate-800/70 rounded" />
                <div className="w-10 h-3 bg-slate-800/50 rounded" />
              </div>
            </div>
          ))
        ) : (
          cards.map((c) => {
          const isActive = activeFilter === c.key;
          return (
            <button
              key={c.key}
              type="button"
              onClick={() => onSelectFilter(isActive && c.key !== 'ALL' ? 'ALL' : c.key)}
              className={`text-left p-3 rounded-xl border transition-all duration-150 bg-slate-950/70 ${
                isActive ? c.ring : c.accent
              }`}
            >
              <div className="flex items-center justify-between gap-1.5">
                <span className="text-[11px] font-mono text-slate-400 font-semibold truncate">
                  {c.label}
                </span>
                {c.icon}
              </div>
              <div className="mt-1.5 flex items-baseline justify-between gap-1">
                <span className="text-xl sm:text-2xl font-bold font-mono tabular-nums text-white">
                  {c.count.toLocaleString()}
                </span>
              </div>
              <div className="mt-1.5 flex items-center justify-between gap-1">
                <span className={`px-1.5 py-0.5 rounded text-[10px] font-mono font-semibold ${c.badgeBg}`}>
                  {c.sub}
                </span>
                <span className="text-[10px] font-mono text-slate-500">
                  {isActive ? 'Active' : 'Click →'}
                </span>
              </div>
            </button>
          );
        })
        )}
      </div>
    </div>
  );
}

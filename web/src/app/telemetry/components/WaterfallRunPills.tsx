'use client';

import React from 'react';
import { ExternalLink, Edit3, Code, Clock } from 'lucide-react';
import { MatchCycleRun } from '../types';

interface WaterfallRunPillsProps {
  cycles: MatchCycleRun[];
  selectedCycleIdx: number;
  onSelectCycle?: (idx: number) => void;
  cycle: MatchCycleRun;
  onOpenOverride: () => void;
  showRawJson: boolean;
  onToggleRawJson: () => void;
}

export function WaterfallRunPills({
  cycles,
  selectedCycleIdx,
  onSelectCycle,
  cycle,
  onOpenOverride,
  showRawJson,
  onToggleRawJson,
}: WaterfallRunPillsProps) {
  return (
    <div className="space-y-2.5 pb-2 border-b border-slate-800/80">
      {cycles.length > 1 && onSelectCycle && (
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-thin">
          <span className="text-[11px] font-mono text-slate-400 font-semibold shrink-0 uppercase tracking-wider pr-1">
            Runs ({cycles.length}):
          </span>
          {cycles.map((c, idx) => {
            const isSelected = idx === selectedCycleIdx;
            const isL = c.status === 'LINKED';
            const isCd = c.status === 'COOLDOWN';
            const runNum = cycles.length - idx;
            const label = idx === 0 ? `Run #${runNum} (Latest)` : `Run #${runNum}`;

            return (
              <button
                key={idx}
                type="button"
                onClick={() => onSelectCycle(idx)}
                className={`px-2.5 py-1 rounded-full text-xs font-mono font-bold transition flex items-center gap-1.5 shrink-0 border ${
                  isSelected
                    ? isL
                      ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/50 shadow-sm'
                      : isCd
                      ? 'bg-amber-500/20 text-amber-300 border-amber-500/50 shadow-sm'
                      : 'bg-rose-500/20 text-rose-300 border-rose-500/50 shadow-sm'
                    : 'bg-slate-900/80 text-slate-400 border-slate-800 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <span className={`w-1.5 h-1.5 rounded-full ${isL ? 'bg-emerald-400' : isCd ? 'bg-amber-400' : 'bg-rose-400'}`} />
                <span>{label}</span>
                <span className="text-[10px] opacity-75">({c.status})</span>
              </button>
            );
          })}
        </div>
      )}

      <div className="flex items-center justify-between gap-2 flex-wrap text-xs font-mono">
        <div className="flex items-center gap-2 text-slate-400">
          <span className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-slate-500" />
            <span>{cycle.timestamp}</span>
          </span>
          {cycle.durationMs !== undefined && cycle.durationMs > 0 && (
            <span className="text-slate-500">• {cycle.durationMs}ms</span>
          )}
          {cycle.quotaUsed !== undefined && (
            <span className="text-slate-500">• {cycle.quotaUsed} quota</span>
          )}
        </div>

        <div className="flex items-center gap-1.5 shrink-0">
          {cycle.outcomeVideoUrl && (
            <a
              href={cycle.outcomeVideoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="px-2 py-1 rounded bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20 border border-emerald-500/30 flex items-center gap-1 transition text-[11px]"
            >
              <ExternalLink className="w-3 h-3" />
              <span>Watch</span>
            </a>
          )}
          <button
            type="button"
            onClick={onOpenOverride}
            className="px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 flex items-center gap-1 transition text-[11px]"
          >
            <Edit3 className="w-3 h-3" />
            <span>Override</span>
          </button>
          <button
            type="button"
            onClick={onToggleRawJson}
            className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-slate-200 border border-slate-700 transition"
            title="Inspect raw JSON"
          >
            <Code className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}

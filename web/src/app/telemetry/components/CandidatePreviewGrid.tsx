'use client';

import React from 'react';
import { Play, Globe, Lock, ShieldCheck, Film } from 'lucide-react';
import { CandidateDetail } from '../types';

interface CandidatePreviewGridProps {
  candidates?: CandidateDetail[];
  fallbackCandidates: string[];
}

export function CandidatePreviewGrid({
  candidates = [],
  fallbackCandidates = [],
}: CandidatePreviewGridProps) {
  if (candidates.length > 0) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
        {candidates.map((c, idx) => {
          const isGoodReach = (c.watchabilityPct ?? 100) >= 90;
          return (
            <div
              key={c.videoId || idx}
              className="p-2.5 rounded-lg bg-slate-900 border border-slate-800 hover:border-slate-700 transition space-y-1.5"
            >
              <div className="flex items-start justify-between gap-1.5">
                <span className="text-[11px] font-semibold text-slate-200 line-clamp-2 leading-tight">
                  {c.title}
                </span>
                <span className="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-mono bg-slate-800 text-slate-400 border border-slate-700">
                  {c.duration ? `${Math.round(c.duration)}s` : 'Video'}
                </span>
              </div>
              <div className="flex items-center justify-between text-[11px] font-mono text-slate-400 pt-1 border-t border-slate-800/60">
                <span className="truncate max-w-[140px] text-indigo-300">
                  {c.channel}
                </span>
                <div className="flex items-center gap-1.5">
                  <span
                    className={`inline-flex items-center gap-0.5 px-1.5 py-0.2 rounded text-[10px] font-bold border ${
                      isGoodReach
                        ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30'
                        : 'bg-amber-500/15 text-amber-300 border-amber-500/30'
                    }`}
                  >
                    <Globe className="w-2.5 h-2.5" />
                    <span>{(c.watchabilityPct ?? 100).toFixed(0)}%</span>
                  </span>
                  {c.isEmbeddable && (
                    <span className="text-slate-500" title="Embeddable">
                      <Film className="w-3 h-3" />
                    </span>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  return (
    <div className="space-y-1">
      {fallbackCandidates.map((cand, idx) => (
        <div key={idx} className="p-2 rounded bg-slate-900 border border-slate-800 text-slate-300 text-xs font-mono">
          {cand}
        </div>
      ))}
    </div>
  );
}

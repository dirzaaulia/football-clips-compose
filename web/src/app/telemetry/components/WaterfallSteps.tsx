'use client';

import React from 'react';
import { ExternalLink, Edit3, ShieldAlert } from 'lucide-react';
import { MatchCycleRun } from '../types';
import { CandidateCard } from './CandidateCard';

interface WaterfallStepsProps {
  cycle: MatchCycleRun;
  matchId: string;
  matchTitle: string;
  onOpenOverride: () => void;
}

export function WaterfallSteps({ cycle, matchId, matchTitle, onOpenOverride }: WaterfallStepsProps) {
  const isLinked = cycle.status === 'LINKED';
  const isCooldown = cycle.status === 'COOLDOWN';

  return (
    <div className="space-y-4 pt-2">
      {/* Tier Cooldown Guardrail Card */}
      {isCooldown && (
        <div className="bg-amber-950/30 border border-amber-800/50 rounded-xl p-3.5 space-y-2 text-xs">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2 text-amber-400 font-semibold uppercase tracking-wide text-[11px]">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>Tier Cooldown Guardrail Active</span>
            </div>
            <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-mono text-[11px] font-bold border border-amber-500/30">
              Cooldown Active
            </span>
          </div>
          <p className="text-slate-300 text-[12px] leading-relaxed">
            {cycle.step4Outcome || 'YouTube search bypassed to conserve daily API quota.'}
          </p>
          <div className="pt-1 flex items-center justify-end">
            <button
              onClick={onOpenOverride}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <Edit3 className="w-3.5 h-3.5" />
              <span>Manual Override / Force Search</span>
            </button>
          </div>
        </div>
      )}

      {/* Step 1: Channels */}
      <div className="text-xs space-y-1.5">
        <div className="flex items-center justify-between">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
            <span className="w-5 h-5 rounded-full bg-blue-950 border border-blue-500/40 inline-flex items-center justify-center text-[10px] text-blue-400 font-mono font-bold">1</span>
            Official Broadcaster Targets ({cycle.step1Channels.length})
          </p>
          {cycle.query && (
            <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-800 text-slate-300 border border-slate-700 truncate max-w-[200px]" title={cycle.query}>
              Query: {cycle.query}
            </span>
          )}
        </div>
        <div className="bg-slate-900/90 rounded-lg p-2.5 text-slate-300 font-mono text-xs border border-slate-800 flex flex-wrap gap-1.5">
          {cycle.step1Channels.length > 0 ? (
            cycle.step1Channels.map((ch, idx) => (
              <span key={idx} className="px-2 py-1 rounded bg-slate-800 text-slate-300 text-[11px] border border-slate-700/60 font-sans">
                {ch.replace(/^Checking\s+(channel\s+)?/i, '').replace(/\s+uploads\.\.\.$/i, '')}
              </span>
            ))
          ) : (
            <p className="text-slate-500 italic">Target channels awaiting sweep</p>
          )}
        </div>
      </div>

      {/* Step 2: Candidates */}
      <div className="text-xs space-y-1.5">
        <div className="flex items-center justify-between">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
            <span className="w-5 h-5 rounded-full bg-indigo-950 border border-indigo-500/40 inline-flex items-center justify-center text-[10px] text-indigo-400 font-mono font-bold">2</span>
            Discovered Candidates ({cycle.step2Candidates.length})
          </p>
          {cycle.stoppedAtStep === 2 && (
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-300 border border-amber-500/30">
              Stopped at Step 2
            </span>
          )}
        </div>
        {cycle.step2Candidates.length > 0 ? (
          <div className="bg-slate-900/90 rounded-lg p-2.5 text-slate-300 font-mono space-y-2 border border-slate-800 max-h-48 overflow-y-auto">
            {cycle.step2Candidates.map((cand, idx) => {
              const isRss = cand.includes('RSS');
              const isApi = cand.includes('API');
              const urlMatch = cand.match(/->\s*(https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11})/);
              let displayLine = cand
                .replace(/^(\[[^\]]+\]\s*)+/, '')
                .replace(/\[(RSS|API)[^\]]*\]\s*/i, '')
                .trim();
              if (urlMatch) {
                displayLine = displayLine.replace(/->\s*https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11}/, '').trim();
              }
              return (
                <div key={idx} className="flex items-center justify-between gap-2 p-1.5 rounded hover:bg-slate-800/60 text-[11px]">
                  <div className="flex items-center gap-2 min-w-0 flex-1">
                    {isRss && (
                      <span className="px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[9px] font-bold border border-emerald-500/30 shrink-0">
                        RSS 0-quota
                      </span>
                    )}
                    {isApi && (
                      <span className="px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 text-[9px] font-bold border border-blue-500/30 shrink-0">
                        API Data
                      </span>
                    )}
                    <span className="truncate text-slate-300 font-sans">{displayLine}</span>
                  </div>
                  {urlMatch && (
                    <a
                      href={urlMatch[1]}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-slate-800 text-blue-400 hover:text-blue-300 hover:bg-slate-700 text-[10px] font-bold shrink-0 border border-slate-700"
                    >
                      <span>Watch</span>
                      <ExternalLink className="w-2.5 h-2.5" />
                    </a>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <p className="text-slate-500 italic pl-6 text-xs">Zero preliminary uploads detected</p>
        )}
      </div>

      {/* Step 3: Verdicts */}
      <div className="text-xs space-y-1.5">
        <div className="flex items-center justify-between">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
            <span className="w-5 h-5 rounded-full bg-emerald-950 border border-emerald-500/40 inline-flex items-center justify-center text-[10px] text-emerald-400 font-mono font-bold">3</span>
            AI Semantic Reranking Verdicts ({cycle.step3Verdicts.length})
          </p>
        </div>
        {cycle.step3Verdicts.length > 0 ? (
          <div className="space-y-1.5">
            {cycle.step3Verdicts.map((verdict, idx) => (
              <CandidateCard key={idx} verdict={verdict} />
            ))}
          </div>
        ) : (
          <p className="text-slate-500 italic pl-6 text-xs">No candidate videos submitted for evaluation</p>
        )}
      </div>

      {/* Step 4: Outcome */}
      <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
        <div className="flex items-center justify-between">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
            <span className="w-5 h-5 rounded-full bg-purple-950 border border-purple-500/40 inline-flex items-center justify-center text-[10px] text-purple-400 font-mono font-bold">4</span>
            Final Outcome & Video Linking
          </p>
        </div>
        <div className="bg-slate-900/90 rounded-lg p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 border border-slate-800">
          <div className="space-y-0.5">
            <p className="font-mono text-slate-200 text-xs">{cycle.step4Outcome}</p>
            {cycle.outcomeVideoId && (
              <p className="text-slate-400 font-mono text-[11px]">YouTube ID: {cycle.outcomeVideoId}</p>
            )}
          </div>
          <div className="flex items-center gap-2">
            {cycle.outcomeVideoUrl && (
              <a
                href={cycle.outcomeVideoUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600/20 text-indigo-400 hover:bg-indigo-600/30 border border-indigo-500/30 rounded-lg text-xs font-semibold transition"
              >
                <ExternalLink className="w-3.5 h-3.5" />
                <span>Watch Highlight</span>
              </a>
            )}
            <button
              onClick={onOpenOverride}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 text-slate-300 hover:text-white hover:bg-slate-700 border border-slate-700 rounded-lg text-xs font-semibold transition"
            >
              <Edit3 className="w-3.5 h-3.5" />
              <span>{isLinked ? 'Change Video' : 'Manual Link'}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

'use client';

import React from 'react';
import { ExternalLink, Edit3, ShieldAlert, AlertCircle } from 'lucide-react';
import { MatchCycleRun } from '../types';
import { CandidateCard } from './CandidateCard';
import { CandidateGroupList } from './CandidateGroupList';

interface WaterfallStepsProps {
  cycle: MatchCycleRun;
  matchId: string;
  matchTitle: string;
  onOpenOverride: () => void;
}

export function WaterfallSteps({ cycle, matchId, matchTitle, onOpenOverride }: WaterfallStepsProps) {
  const isLinked = cycle.status === 'LINKED';
  const isCooldown = cycle.status === 'COOLDOWN';

  const totalScanned = Object.values(cycle.scannedUploads || {}).reduce((sum, list) => sum + list.length, 0);
  const totalCandidatesCount = totalScanned + cycle.step2Candidates.length;

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
          {cycle.nextCycleEta && (
            <div className="flex items-center gap-1.5 text-sky-400 font-mono text-[11px] pt-0.5">
              <span>⏳ {cycle.nextCycleEta}</span>
              {cycle.nextCycleTimestamp && (
                <span className="text-slate-400">· {cycle.nextCycleTimestamp}</span>
              )}
            </div>
          )}
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

      {/* Unlinked / Stopped Step Diagnostics Alert */}
      {!isLinked && !isCooldown && (
        <div className="bg-rose-950/20 border border-rose-800/40 rounded-xl p-3 flex items-start gap-2.5 text-xs text-rose-300">
          <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
          <div className="space-y-1 flex-1">
            <div className="font-semibold text-rose-200 flex items-center justify-between flex-wrap gap-1">
              <span>
                {cycle.stoppedAtStep
                  ? `Search Stopped at Step ${cycle.stoppedAtStep}`
                  : cycle.status === 'SEARCHED'
                    ? 'Search Completed (No Highlight Linked)'
                    : 'Highlight Search Incomplete'}
              </span>
              {(cycle.nextCycleEta || cycle.nextCycleTimestamp) && (
                <span className="text-[10px] font-mono text-sky-400 font-normal">
                  ⏳ {cycle.nextCycleEta || 'Scheduled'}
                </span>
              )}
            </div>
            <p className="text-[11px] text-rose-300/90 leading-relaxed font-mono">
              {cycle.stoppedReason || cycle.step4Outcome || 'No eligible highlight candidates found yet.'}
            </p>
            {cycle.nextCycleTimestamp && (
              <p className="text-[10px] text-slate-400 font-mono pt-0.5">
                {cycle.nextCycleTimestamp}
              </p>
            )}
          </div>
        </div>
      )}

      {/* Step 1: Channels */}
      <div className="text-xs space-y-1.5">
        <div className="flex items-center justify-between flex-wrap gap-1">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
            <span className="w-5 h-5 rounded-full bg-blue-950 border border-blue-500/40 inline-flex items-center justify-center text-[10px] text-blue-400 font-mono font-bold">1</span>
            Official Broadcaster Targets ({cycle.step1Channels.length})
          </p>
          {cycle.query && (
            <span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-800 text-slate-300 border border-slate-700 max-w-[280px] sm:max-w-md truncate" title={cycle.query}>
              Query: &quot;{cycle.query}&quot;
            </span>
          )}
        </div>
        <div className="bg-slate-900/90 rounded-lg p-2.5 text-slate-300 font-mono text-xs border border-slate-800 flex flex-wrap gap-2">
          {cycle.step1Channels.length > 0 ? (
            cycle.step1Channels.map((ch, idx) => {
              const handle = ch.replace(/^Checking\s+(channel\s+)?/i, '').replace(/\s+uploads\.\.\.$/i, '').trim();
              return (
                <span
                  key={idx}
                  className="px-2.5 py-1 rounded-lg bg-slate-800/80 text-slate-300 text-[11px] border border-slate-700/60 font-mono"
                >
                  {handle}
                </span>
              );
            })
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
            Discovered Candidates ({totalCandidatesCount})
          </p>
          {cycle.stoppedAtStep === 2 && (
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-300 border border-amber-500/30">
              Stopped at Step 2
            </span>
          )}
        </div>
        <CandidateGroupList
          scannedUploads={cycle.scannedUploads}
          directCandidates={cycle.step2Candidates}
        />
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

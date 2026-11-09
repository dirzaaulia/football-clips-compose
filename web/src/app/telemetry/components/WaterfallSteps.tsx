'use client';

import React, { useState } from 'react';
import { ExternalLink, Edit3, ShieldAlert, AlertCircle, ChevronDown, ChevronUp, Layers } from 'lucide-react';
import { MatchCycleRun, MatchWaterfall } from '../types';
import { CandidateCard } from './CandidateCard';
import { CandidateGroupList } from './CandidateGroupList';
import { WaterfallExecutiveSummary } from './WaterfallExecutiveSummary';

interface WaterfallStepsProps {
  cycle: MatchCycleRun;
  matchId: string;
  matchTitle: string;
  waterfall?: MatchWaterfall;
  onOpenOverride: () => void;
}

export function WaterfallSteps({
  cycle,
  matchId,
  matchTitle,
  waterfall,
  onOpenOverride,
}: WaterfallStepsProps) {
  const isLinked = cycle.status === 'LINKED';
  const isCooldown = cycle.status === 'COOLDOWN';

  // Auto-expand deep trace if stopped or unlinked with issues, collapse by default if cleanly linked
  const [showDeepTrace, setShowDeepTrace] = useState<boolean>(!isLinked && !isCooldown);

  const totalScanned = Object.values(cycle.scannedUploads || {}).reduce((sum, list) => sum + list.length, 0);
  const totalCandidatesCount = totalScanned + cycle.step2Candidates.length;

  return (
    <div className="space-y-3 pt-1">
      {/* 1. Executive Summary for High-Level Operators */}
      <WaterfallExecutiveSummary
        cycle={cycle}
        waterfall={waterfall}
        onOpenOverride={onOpenOverride}
      />

      {/* 2. Tier Cooldown Guardrail Card */}
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

      {/* 3. Unlinked / Stopped Step Diagnostics Alert */}
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
          </div>
        </div>
      )}

      {/* 4. Deep Pipeline Trace (Collapsible Accordion Section) */}
      <div className="border border-slate-800/80 rounded-xl overflow-hidden bg-slate-950/60">
        <button
          onClick={() => setShowDeepTrace((prev) => !prev)}
          className="w-full px-3.5 py-2.5 bg-slate-900/60 hover:bg-slate-900/90 flex items-center justify-between text-xs text-slate-300 font-mono transition"
        >
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-slate-400" />
            <span className="font-semibold">
              Deep Pipeline Trace ({cycle.step1Channels.length} Channels, {totalCandidatesCount} Candidates, {cycle.step3Verdicts.length} AI Verdicts)
            </span>
          </div>
          <div className="flex items-center gap-1.5 text-slate-400 text-[11px]">
            <span>{showDeepTrace ? 'Hide Trace' : 'Inspect Trace'}</span>
            {showDeepTrace ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </div>
        </button>

        {showDeepTrace && (
          <div className="p-3.5 space-y-4 border-t border-slate-800/80">
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

            {/* Step 4: Outcome & Action */}
            <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
              <div className="flex items-center justify-between">
                <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
                  <span className="w-5 h-5 rounded-full bg-purple-950 border border-purple-500/40 inline-flex items-center justify-center text-[10px] text-purple-400 font-mono font-bold">4</span>
                  Raw Linking Result
                </p>
              </div>
              <div className="bg-slate-900/90 rounded-lg p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 border border-slate-800">
                <p className="font-mono text-slate-300 text-xs">{cycle.step4Outcome}</p>
                <div className="flex items-center gap-2">
                  {cycle.outcomeVideoUrl && (
                    <a
                      href={cycle.outcomeVideoUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600/20 text-indigo-400 hover:bg-indigo-600/30 border border-indigo-500/30 rounded-lg text-xs font-semibold transition"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                      <span>Watch</span>
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
        )}
      </div>
    </div>
  );
}

'use client';

import React, { useState } from 'react';
import { ShieldAlert, AlertCircle, CheckCircle2 } from 'lucide-react';
import { MatchCycleRun, MatchWaterfall } from '../types';
import { CandidateCard } from './CandidateCard';
import { CandidatePreviewGrid } from './CandidatePreviewGrid';
import { WaterfallRunPills } from './WaterfallRunPills';
import { RegionalTargetMarketsDeck } from './RegionalTargetMarketsDeck';

interface WaterfallStepsProps {
  cycle: MatchCycleRun;
  matchId: string;
  matchTitle: string;
  waterfall?: MatchWaterfall;
  cycles?: MatchCycleRun[];
  selectedCycleIdx?: number;
  onSelectCycle?: (idx: number) => void;
  onOpenOverride: () => void;
}

export function WaterfallSteps({
  cycle,
  matchId,
  matchTitle,
  waterfall,
  cycles = [],
  selectedCycleIdx = 0,
  onSelectCycle,
  onOpenOverride,
}: WaterfallStepsProps) {
  const [showRawJson, setShowRawJson] = useState(false);

  const isLinked = cycle.status === 'LINKED';
  const isCooldown = cycle.status === 'COOLDOWN';
  const totalCandidatesCount = cycle.candidatesList?.length ?? cycle.step2Candidates.length;

  return (
    <div className="space-y-3.5 pt-1 font-sans">
      {/* 1. Run History Pill Switcher & Quick Action Toolbar */}
      <WaterfallRunPills
        cycles={cycles}
        selectedCycleIdx={selectedCycleIdx}
        onSelectCycle={onSelectCycle}
        cycle={cycle}
        onOpenOverride={onOpenOverride}
        showRawJson={showRawJson}
        onToggleRawJson={() => setShowRawJson((prev) => !prev)}
      />

      {/* 2. Primary 4-Step Waterfall Deck */}
      <div className="space-y-3 p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
        {/* Step 1: Official Channels Searched */}
        <div className="text-xs space-y-1.5">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
            <span className="w-5 h-5 rounded-full bg-blue-950 border border-blue-500/40 inline-flex items-center justify-center text-[10px] text-blue-400 font-bold">
              1
            </span>
            <span>Official Channels Checked ({cycle.step1Channels.length})</span>
          </p>
          <div className="bg-slate-900/90 rounded-lg p-2 text-slate-300 font-mono text-xs border border-slate-800 flex flex-wrap gap-1.5">
            {cycle.step1Channels.length > 0 ? (
              cycle.step1Channels.map((ch, idx) => (
                <span
                  key={idx}
                  className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 text-[11px] border border-slate-700"
                >
                  {ch.replace(/^Checking\s+(channel\s+)?/i, '').replace(/\s+uploads\.\.\.$/i, '').trim()}
                </span>
              ))
            ) : (
              <span className="text-slate-500 italic">Target channels awaiting sweep</span>
            )}
          </div>
        </div>

        {/* Step 2: Discovered Candidates */}
        <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
            <span className="w-5 h-5 rounded-full bg-indigo-950 border border-indigo-500/40 inline-flex items-center justify-center text-[10px] text-indigo-400 font-bold">
              2
            </span>
            <span>Discovered Candidates ({totalCandidatesCount})</span>
          </p>
          <CandidatePreviewGrid
            candidates={cycle.candidatesList}
            fallbackCandidates={cycle.step2Candidates}
          />
        </div>

        {/* Step 3: AI Evaluation Verdicts */}
        <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
            <span className="w-5 h-5 rounded-full bg-emerald-950 border border-emerald-500/40 inline-flex items-center justify-center text-[10px] text-emerald-400 font-bold">
              3
            </span>
            <span>AI Evaluation Verdicts ({cycle.step3Verdicts.length})</span>
          </p>
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

        {/* Step 4: Outcome & Regional Availability */}
        <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
          <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
            <span className="w-5 h-5 rounded-full bg-amber-950 border border-amber-500/40 inline-flex items-center justify-center text-[10px] text-amber-400 font-bold">
              4
            </span>
            <span>Cycle Outcome & Regional Availability</span>
          </p>

          <div className="p-3 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-slate-400">Verdict:</span>
              <span
                className={`px-2 py-0.5 rounded font-bold border text-[11px] ${
                  isLinked
                    ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                    : isCooldown
                    ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                    : 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                }`}
              >
                {cycle.status}
              </span>
            </div>
            <p className="text-slate-300 text-[11px] leading-relaxed break-words">
              {cycle.step4Outcome}
            </p>
          </div>

          {isLinked && waterfall?.cumulativeAvailability && (
            <div className="pt-2">
              <RegionalTargetMarketsDeck
                coveredMarkets={waterfall.cumulativeAvailability.coveredTargetMarkets || []}
              />
            </div>
          )}

          {isCooldown && (
            <div className="bg-amber-950/20 border border-amber-800/40 rounded-lg p-2.5 flex items-center gap-2 text-xs text-amber-300">
              <ShieldAlert className="w-4 h-4 shrink-0 text-amber-400" />
              <span>Cooldown guardrail active — next check scheduled automatically</span>
            </div>
          )}
        </div>
      </div>

      {/* 3. Raw JSON Viewer Drawer */}
      {showRawJson && (
        <pre className="p-3 rounded-lg bg-black/70 border border-slate-800 text-[10px] font-mono text-slate-300 overflow-x-auto max-h-72">
          {JSON.stringify(cycle, null, 2)}
        </pre>
      )}
    </div>
  );
}

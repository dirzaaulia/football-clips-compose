'use client';

import React, { useState } from 'react';
import { ShieldAlert, AlertCircle, Edit3, ChevronLeft, ChevronRight, Code } from 'lucide-react';
import { MatchCycleRun, MatchWaterfall } from '../types';
import { CandidateCard } from './CandidateCard';
import { CandidateGroupList } from './CandidateGroupList';
import { WaterfallExecutiveSummary } from './WaterfallExecutiveSummary';
import { WaterfallTabs, WaterfallTab } from './WaterfallTabs';

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
  const [activeTab, setActiveTab] = useState<WaterfallTab>('overview');
  const [showRawJson, setShowRawJson] = useState(false);

  const isLinked = cycle.status === 'LINKED';
  const isCooldown = cycle.status === 'COOLDOWN';

  const totalScanned = Object.values(cycle.scannedUploads || {}).reduce((sum, list) => sum + list.length, 0);
  const totalCandidatesCount = totalScanned + cycle.step2Candidates.length;

  return (
    <div className="space-y-3 pt-1">
      {/* Segmented 3-Tab Bar */}
      <WaterfallTabs
        activeTab={activeTab}
        onTabChange={setActiveTab}
        candidatesCount={totalCandidatesCount}
        cyclesCount={cycles.length}
      />

      {/* Tab 1: Overview & Video */}
      {activeTab === 'overview' && (
        <div className="space-y-3 animate-in fade-in duration-200">
          <WaterfallExecutiveSummary
            cycle={cycle}
            waterfall={waterfall}
            onOpenOverride={onOpenOverride}
          />

          {isCooldown && (
            <div className="bg-amber-950/30 border border-amber-800/50 rounded-xl p-3 space-y-2 text-xs">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-1.5 text-amber-400 font-semibold text-[11px] uppercase tracking-wide">
                  <ShieldAlert className="w-4 h-4" />
                  <span>Tier 1 Cooldown Guardrail Active</span>
                </div>
                {cycle.nextCycleEta && (
                  <span className="font-mono text-[11px] text-sky-400">
                    ⏳ Next Check: {cycle.nextCycleEta}
                  </span>
                )}
              </div>
              <p className="text-slate-300 text-[12px] leading-relaxed">
                {cycle.step4Outcome || 'Search cooldown active. Last 14 days priority matches are checked every 30m.'}
              </p>
            </div>
          )}

          {!isLinked && !isCooldown && (
            <div className="bg-rose-950/20 border border-rose-800/40 rounded-xl p-3 flex items-start gap-2 text-xs text-rose-300">
              <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
              <div className="space-y-1 flex-1">
                <div className="font-semibold text-rose-200 flex items-center justify-between">
                  <span>Search Completed (No Highlight Linked)</span>
                  {cycle.nextCycleEta && (
                    <span className="text-[10px] font-mono text-sky-400">⏳ {cycle.nextCycleEta}</span>
                  )}
                </div>
                <p className="text-[11px] text-rose-300/90 font-mono">
                  {cycle.stoppedReason || cycle.step4Outcome || 'No official highlight video available yet.'}
                </p>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: AI & Candidates Trace */}
      {activeTab === 'candidates' && (
        <div className="space-y-3.5 p-3 rounded-xl bg-slate-950/60 border border-slate-800/80 animate-in fade-in duration-200">
          <div className="text-xs space-y-1.5">
            <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
              <span className="w-5 h-5 rounded-full bg-blue-950 border border-blue-500/40 inline-flex items-center justify-center text-[10px] text-blue-400 font-bold">1</span>
              Official Broadcaster Channels ({cycle.step1Channels.length})
            </p>
            <div className="bg-slate-900/90 rounded-lg p-2 text-slate-300 font-mono text-xs border border-slate-800 flex flex-wrap gap-1.5">
              {cycle.step1Channels.length > 0 ? (
                cycle.step1Channels.map((ch, idx) => (
                  <span key={idx} className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-[11px] border border-slate-700">
                    {ch.replace(/^Checking\s+(channel\s+)?/i, '').replace(/\s+uploads\.\.\.$/i, '').trim()}
                  </span>
                ))
              ) : (
                <span className="text-slate-500 italic">Target channels awaiting sweep</span>
              )}
            </div>
          </div>

          <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
            <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
              <span className="w-5 h-5 rounded-full bg-indigo-950 border border-indigo-500/40 inline-flex items-center justify-center text-[10px] text-indigo-400 font-bold">2</span>
              Discovered Candidates ({totalCandidatesCount})
            </p>
            <CandidateGroupList scannedUploads={cycle.scannedUploads} directCandidates={cycle.step2Candidates} />
          </div>

          <div className="text-xs space-y-1.5 pt-2 border-t border-slate-800/80">
            <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5 font-mono">
              <span className="w-5 h-5 rounded-full bg-emerald-950 border border-emerald-500/40 inline-flex items-center justify-center text-[10px] text-emerald-400 font-bold">3</span>
              AI Evaluation Verdicts ({cycle.step3Verdicts.length})
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
        </div>
      )}

      {/* Tab 3: Cycles & Execution Logs */}
      {activeTab === 'cycles' && (
        <div className="space-y-3 p-3 rounded-xl bg-slate-950/60 border border-slate-800/80 animate-in fade-in duration-200">
          {cycles.length > 1 && onSelectCycle && (
            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono">
              <span className="text-slate-400 text-[11px]">Execution Runs ({cycles.length}):</span>
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  disabled={selectedCycleIdx >= cycles.length - 1}
                  onClick={() => onSelectCycle(selectedCycleIdx + 1)}
                  className="p-1 rounded bg-slate-800 text-slate-300 hover:text-white disabled:opacity-40 transition"
                  title="Older Run"
                >
                  <ChevronLeft className="w-3.5 h-3.5" />
                </button>
                <select
                  value={selectedCycleIdx}
                  onChange={(e) => onSelectCycle(Number(e.target.value))}
                  className="bg-slate-950 border border-slate-700 text-slate-200 px-2 py-1 rounded text-xs"
                >
                  {cycles.map((c, idx) => (
                    <option key={idx} value={idx}>
                      {idx === 0 ? 'Latest: ' : `Run #${cycles.length - idx}: `}
                      {c.timestamp} ({c.status})
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  disabled={selectedCycleIdx <= 0}
                  onClick={() => onSelectCycle(selectedCycleIdx - 1)}
                  className="p-1 rounded bg-slate-800 text-slate-300 hover:text-white disabled:opacity-40 transition"
                  title="Newer Run"
                >
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          <div className="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono space-y-1 text-slate-300">
            <div className="flex justify-between">
              <span className="text-slate-500">Run Timestamp:</span>
              <span>{cycle.timestamp || 'Latest'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Status:</span>
              <span className="text-emerald-400 font-bold">{cycle.status}</span>
            </div>
            {cycle.stoppedAtStep && (
              <div className="flex justify-between">
                <span className="text-slate-500">Stopped At:</span>
                <span className="text-amber-400">Step {cycle.stoppedAtStep}</span>
              </div>
            )}
            <div className="flex justify-between">
              <span className="text-slate-500">Raw Outcome:</span>
              <span className="truncate max-w-[280px] sm:max-w-md">{cycle.step4Outcome}</span>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setShowRawJson((prev) => !prev)}
            className="w-full py-1.5 px-3 rounded-lg bg-slate-900 hover:bg-slate-800/80 border border-slate-800 text-xs font-mono text-slate-400 flex items-center justify-center gap-1.5 transition"
          >
            <Code className="w-3.5 h-3.5" />
            <span>{showRawJson ? 'Hide Raw Cycle JSON' : 'Inspect Raw Cycle JSON'}</span>
          </button>

          {showRawJson && (
            <pre className="p-3 rounded-lg bg-black/60 border border-slate-800 text-[10px] font-mono text-slate-300 overflow-x-auto max-h-60">
              {JSON.stringify(cycle, null, 2)}
            </pre>
          )}
        </div>
      )}
    </div>
  );
}

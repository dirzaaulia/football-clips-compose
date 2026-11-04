'use client';

import React, { useState } from 'react';
import {
  ChevronDown, ChevronUp, CheckCircle, Clock, AlertTriangle,
  Copy, Check, Calendar, ChevronLeft, ChevronRight,
} from 'lucide-react';
import { MatchWaterfall } from '../types';
import { WaterfallSteps } from './WaterfallSteps';
import { ManualOverrideModal } from './ManualOverrideModal';

interface WaterfallCardProps {
  waterfall: MatchWaterfall;
  onOverrideSuccess?: () => void;
}

export function WaterfallCard({ waterfall, onOverrideSuccess }: WaterfallCardProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isOverrideOpen, setIsOverrideOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const [selectedCycleIdx, setSelectedCycleIdx] = useState(0);

  const cycles = waterfall.cycles && waterfall.cycles.length > 0
    ? waterfall.cycles
    : [{
        cycleId: undefined,
        timestamp: waterfall.timestamp || 'Recently',
        step1Channels: waterfall.step1Channels,
        step2Candidates: waterfall.step2Candidates,
        step3Verdicts: waterfall.step3Verdicts,
        step4Outcome: waterfall.step4Outcome,
        outcomeVideoId: waterfall.outcomeVideoId,
        outcomeVideoUrl: waterfall.outcomeVideoUrl,
        status: waterfall.status,
        stoppedAtStep: waterfall.stoppedAtStep,
        stoppedReason: waterfall.stoppedReason,
      }];

  const activeCycle = cycles[selectedCycleIdx] || cycles[0];
  const isLinked = activeCycle.status === 'LINKED';
  const isCooldown = activeCycle.status === 'COOLDOWN';

  const handleCopyCardJson = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(JSON.stringify(waterfall, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const statusBadge = isLinked ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
      <CheckCircle className="w-3.5 h-3.5" />
      LINKED
    </span>
  ) : isCooldown ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
      <Clock className="w-3.5 h-3.5" />
      COOLDOWN
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-500/15 text-slate-400 border border-slate-500/30">
      <AlertTriangle className="w-3.5 h-3.5" />
      {activeCycle.status}
    </span>
  );

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden transition-all duration-200">
      <div
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between p-4 cursor-pointer hover:bg-slate-800/40 select-none transition"
      >
        <div className="flex flex-col gap-1 min-w-0 pr-3">
          <div className="flex items-center gap-2 flex-wrap">
            <h4 className="text-base font-bold text-slate-100 truncate">
              {waterfall.title}
            </h4>
            {waterfall.homeScore !== null && waterfall.homeScore !== undefined && (
              <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 font-mono text-xs font-bold border border-slate-700">
                {waterfall.homeScore} - {waterfall.awayScore ?? 0}
              </span>
            )}
          </div>
          <div className="flex items-center gap-2 text-xs font-mono text-slate-400 flex-wrap">
            <span>Match ID: {waterfall.matchId}</span>
            {waterfall.date && (
              <span className="flex items-center gap-1">
                <Calendar className="w-3 h-3 text-slate-500" />
                {waterfall.date}
              </span>
            )}
            {activeCycle.timestamp && (
              <span className="px-1.5 py-0.5 rounded bg-slate-800/80 text-blue-400 border border-blue-500/20 text-[10px] font-semibold">
                🕒 {activeCycle.timestamp}
              </span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <button
            onClick={handleCopyCardJson}
            title="Copy this match's trace JSON"
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition border border-slate-800/80"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
          {statusBadge}
          {isOpen ? <ChevronUp className="w-5 h-5 text-slate-400" /> : <ChevronDown className="w-5 h-5 text-slate-400" />}
        </div>
      </div>

      {isOpen && (
        <div className="px-4 pb-4 pt-2 border-t border-slate-800/80 space-y-3 bg-slate-950/40">
          {/* Cycle Selector Bar: Visible when multiple execution runs exist */}
          {cycles.length > 1 && (
            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900 border border-slate-800 text-xs font-mono">
              <span className="text-slate-400 text-[11px] font-semibold">
                Execution Cycle History ({cycles.length} runs):
              </span>
              <div className="flex items-center gap-1.5">
                <button
                  disabled={selectedCycleIdx >= cycles.length - 1}
                  onClick={(e) => { e.stopPropagation(); setSelectedCycleIdx((i) => i + 1); }}
                  className="p-1 rounded bg-slate-800 text-slate-300 hover:text-white disabled:opacity-40 transition"
                  title="Older Run"
                >
                  <ChevronLeft className="w-3.5 h-3.5" />
                </button>
                <select
                  value={selectedCycleIdx}
                  onChange={(e) => setSelectedCycleIdx(Number(e.target.value))}
                  className="bg-slate-950 border border-slate-700 text-slate-200 px-2 py-1 rounded text-xs focus:outline-none"
                >
                  {cycles.map((c, idx) => (
                    <option key={idx} value={idx}>
                      {idx === 0 ? 'Latest: ' : `Run #${cycles.length - idx}: `}
                      {c.timestamp} ({c.status})
                    </option>
                  ))}
                </select>
                <button
                  disabled={selectedCycleIdx <= 0}
                  onClick={(e) => { e.stopPropagation(); setSelectedCycleIdx((i) => i - 1); }}
                  className="p-1 rounded bg-slate-800 text-slate-300 hover:text-white disabled:opacity-40 transition"
                  title="Newer Run"
                >
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          <WaterfallSteps
            cycle={activeCycle}
            matchId={waterfall.matchId}
            matchTitle={waterfall.title}
            onOpenOverride={() => setIsOverrideOpen(true)}
          />
        </div>
      )}

      {isOverrideOpen && (
        <ManualOverrideModal
          isOpen={isOverrideOpen}
          matchId={waterfall.matchId}
          matchTitle={waterfall.title}
          onClose={() => setIsOverrideOpen(false)}
          onSuccess={() => {
            setIsOverrideOpen(false);
            if (onOverrideSuccess) onOverrideSuccess();
          }}
        />
      )}
    </div>
  );
}

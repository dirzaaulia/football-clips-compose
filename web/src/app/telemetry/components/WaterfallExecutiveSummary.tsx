'use client';

import React from 'react';
import { ExternalLink, CheckCircle2, AlertTriangle, ShieldCheck, Video, Clock } from 'lucide-react';
import { MatchCycleRun, MatchWaterfall } from '../types';
import { CORE_TARGET_MARKETS } from '../availabilityHelper';
import { RegionalTargetMarketsDeck } from './RegionalTargetMarketsDeck';

interface WaterfallExecutiveSummaryProps {
  cycle: MatchCycleRun;
  waterfall?: MatchWaterfall;
  onOpenOverride: () => void;
}

export function WaterfallExecutiveSummary({
  cycle,
  waterfall,
  onOpenOverride,
}: WaterfallExecutiveSummaryProps) {
  const isLinked = cycle.status === 'LINKED';
  const availability = waterfall?.cumulativeAvailability;
  const winningVerdict = cycle.step3Verdicts.find((v) => v.isAccepted);
  const breakdowns = availability?.videoBreakdowns || [];

  if (!isLinked && breakdowns.length === 0) {
    return null;
  }

  const isAllCovered = availability?.targetMarketsStatus === 'ALL_COVERED' || (availability?.percentage ?? 0) >= 95;

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-3.5 space-y-3 font-sans">
      <div className="flex items-center justify-between flex-wrap gap-2">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded-lg bg-indigo-500/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center">
            <Video className="w-3.5 h-3.5" />
          </div>
          <span className="text-xs font-bold text-slate-200 tracking-wide uppercase font-mono">
            Broadcast & Worldwide Availability Summary
          </span>
        </div>
        <div className="flex items-center gap-1.5">
          {cycle.outcomeVideoUrl && (
            <a
              href={cycle.outcomeVideoUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 px-2.5 py-1 bg-indigo-600/20 text-indigo-300 hover:bg-indigo-600/30 border border-indigo-500/30 rounded-lg text-[11px] font-semibold transition"
            >
              <ExternalLink className="w-3 h-3" />
              <span>Watch</span>
            </a>
          )}
          <button
            onClick={onOpenOverride}
            className="px-2.5 py-1 bg-slate-800 text-slate-300 hover:text-white hover:bg-slate-700 border border-slate-700 rounded-lg text-[11px] font-semibold transition"
          >
            {isLinked ? 'Edit Video' : 'Manual Link'}
          </button>
        </div>
      </div>

      {/* Cumulative Availability Status */}
      <div
        className={`p-2.5 rounded-lg border flex items-start gap-2.5 text-xs ${
          isAllCovered
            ? 'bg-emerald-950/20 border-emerald-800/40 text-emerald-300'
            : 'bg-amber-950/20 border-amber-800/40 text-amber-300'
        }`}
      >
        {isAllCovered ? (
          <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400 mt-0.5" />
        ) : (
          <AlertTriangle className="w-4 h-4 shrink-0 text-amber-400 mt-0.5" />
        )}
        <div className="space-y-1 flex-1">
          <div className="font-semibold flex items-center justify-between flex-wrap gap-1">
            <span>{availability?.text || 'Universal Global Availability (99% Worldwide)'}</span>
            <span className="font-mono text-[10px] px-1.5 py-0.5 rounded bg-slate-900 border border-current">
              {availability?.percentage ?? 99}% WW
            </span>
          </div>
          {/* Aesthetic Regional Target Markets Deck */}
          <RegionalTargetMarketsDeck coveredMarkets={availability?.coveredTargetMarkets ?? CORE_TARGET_MARKETS} />

          {/* Target Market Next Check or Exhaustion Status */}
          {!isAllCovered && (
            <div className="pt-2 border-t border-slate-800/60 flex items-center justify-between flex-wrap gap-2 text-[11px] font-mono">
              {availability?.isExhausted ? (
                <div className="flex items-center gap-1.5 text-rose-300">
                  <span className="px-1.5 py-0.5 rounded bg-rose-500/20 border border-rose-500/40 text-[10px] font-bold">
                    🚫 Broadcasters Exhausted
                  </span>
                  <span>{availability.sweepCount ?? 3}/3 sweeps finished across official channels · No further rights holders</span>
                </div>
              ) : (
                <div className="flex items-center gap-1.5 text-sky-400">
                  <Clock className="w-3.5 h-3.5 shrink-0" />
                  <span className="font-semibold">Next Check: {availability?.nextCheckEta || 'Scheduled for current sweep'}</span>
                  {availability?.nextCheckTimestamp && (
                    <span className="text-slate-400">· {availability.nextCheckTimestamp}</span>
                  )}
                </div>
              )}
              {availability?.missingTargetMarkets && availability.missingTargetMarkets.length > 0 && !availability.isExhausted && (
                <span className="text-[10px] text-slate-400">
                  Sweeps: {availability?.sweepCount ?? 0}/3 · Seeking: {availability.missingTargetMarkets.join(', ')}
                </span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Linked Videos Breakdown */}
      {breakdowns.length > 0 && (
        <div className="space-y-1.5 pt-1">
          <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wide font-mono">
            Active Highlight Video Sources ({breakdowns.length})
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {breakdowns.map((v, i) => (
              <div
                key={v.videoId + i}
                className="bg-slate-950/70 border border-slate-800/80 rounded-lg p-2.5 flex flex-col justify-between gap-1.5 text-xs"
              >
                <div className="flex items-center justify-between gap-1">
                  <span className={`text-[10px] font-bold font-mono px-1.5 py-0.2 rounded border ${
                    v.role === 'PRIMARY'
                      ? 'bg-indigo-500/15 text-indigo-300 border-indigo-500/30'
                      : 'bg-slate-800 text-slate-300 border-slate-700'
                  }`}>
                    {v.role}
                  </span>
                  <span className="text-slate-400 font-mono text-[11px] truncate">
                    {v.channelHandle || 'Official Partner'}
                  </span>
                  <a
                    href={`https://youtu.be/${v.videoId}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-sky-400 hover:text-sky-300 font-mono text-[11px] underline inline-flex items-center gap-0.5"
                  >
                    <span>{v.videoId}</span>
                    <ExternalLink className="w-2.5 h-2.5" />
                  </a>
                </div>
                <div className="flex items-center justify-between text-[11px] font-mono text-slate-400 pt-0.5 border-t border-slate-900">
                  <span>Watchable in {v.allowedCount} countries</span>
                  <span className="text-emerald-400 font-semibold">{v.percentage}% WW</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Winning AI Verdict Highlight */}
      {winningVerdict && (
        <div className="bg-slate-950/60 border border-slate-800 rounded-lg p-2 text-xs flex items-start gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div className="space-y-0.5 flex-1">
            <div className="flex items-center justify-between text-[11px]">
              <span className="font-semibold text-slate-200">
                Selected Video: {winningVerdict.title}
              </span>
              <span className="font-mono text-emerald-400 font-bold">
                AI Score: {winningVerdict.score}/100
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-mono leading-relaxed">
              {winningVerdict.verdictReason}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

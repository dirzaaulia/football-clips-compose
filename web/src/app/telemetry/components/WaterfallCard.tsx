'use client';

import React, { useState } from 'react';
import {
  ChevronDown, ChevronUp, CheckCircle, Clock, AlertTriangle,
  Copy, Check, Calendar, RotateCcw, Loader2,
} from 'lucide-react';
import { MatchWaterfall } from '../types';
import { WaterfallSteps } from './WaterfallSteps';
import { ManualOverrideModal } from './ManualOverrideModal';
import { database } from '@/lib/database';

interface WaterfallCardProps {
  waterfall: MatchWaterfall;
  onOverrideSuccess?: () => void;
}

export function WaterfallCard({ waterfall, onOverrideSuccess }: WaterfallCardProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isOverrideOpen, setIsOverrideOpen] = useState(false);
  const [copied, setCopied] = useState(false);
  const [isReSearching, setIsReSearching] = useState(false);
  const [isReset, setIsReset] = useState(false);
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
  const isLinked = !isReset && (activeCycle.status === 'LINKED' || (selectedCycleIdx === 0 && waterfall.status === 'LINKED'));
  const isCooldown = !isReset && activeCycle.status === 'COOLDOWN';

  const handleCopyCardJson = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(JSON.stringify(waterfall, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleReSearch = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isReSearching) return;
    setIsReSearching(true);
    try {
      const { error } = await database
        .from('matches')
        .update({
          highlight_video_id: null,
          highlight_videos: [],
          last_youtube_check: null,
          youtube_region_info: null,
          updated_at: new Date().toISOString(),
        })
        .eq('id', waterfall.matchId);

      if (error) throw new Error(error.message);
      fetch('https://edge.fc.dirzaaulia.com/api/purge-cache', { method: 'POST' }).catch(() => {});
      setIsReset(true);
      if (onOverrideSuccess) onOverrideSuccess();
    } catch (err) {
      console.error('Failed to queue re-search:', err);
    } finally {
      setIsReSearching(false);
    }
  };

  const statusBadge = isReset ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-sky-500/15 text-sky-400 border border-sky-500/30">
      <Clock className="w-3 h-3" />
      DUE NOW
    </span>
  ) : isLinked ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
      <CheckCircle className="w-3 h-3" />
      LINKED
    </span>
  ) : isCooldown ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
      <Clock className="w-3 h-3" />
      COOLDOWN
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-500/15 text-slate-400 border border-slate-500/30">
      <AlertTriangle className="w-3 h-3" />
      {activeCycle.status}
    </span>
  );

  const sourceName = waterfall.ingestionSource || activeCycle.ingestionSource;
  const wwText = waterfall.cumulativeAvailability?.text || waterfall.worldwideAvailabilityText || activeCycle.worldwideAvailabilityText;
  const isWorldwideGood = waterfall.cumulativeAvailability?.targetMarketsStatus === 'ALL_COVERED' || (waterfall.worldwidePercentage ?? 0) >= 90;

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-xl transition-all duration-200">
      {/* Clean 2-Line Header */}
      <div
        onClick={() => setIsOpen(!isOpen)}
        className={`p-3.5 sm:p-4 cursor-pointer select-none transition ${
          isOpen
            ? 'sticky top-24 z-10 bg-slate-900/95 backdrop-blur-md border-b border-slate-800/90 rounded-t-xl shadow-md'
            : 'hover:bg-slate-800/40 rounded-xl'
        }`}
      >
        {/* Line 1: Match Title, Score, Status Badge & Chevron */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2 min-w-0">
            <h4 className="text-sm sm:text-base font-bold text-slate-100 truncate">
              {waterfall.title}
            </h4>
            {waterfall.homeScore !== null && waterfall.homeScore !== undefined && (
              <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 font-mono text-xs font-bold border border-slate-700 shrink-0">
                {waterfall.homeScore} - {waterfall.awayScore ?? 0}
              </span>
            )}
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {statusBadge}
            {isOpen ? <ChevronUp className="w-4 h-4 text-slate-400" /> : <ChevronDown className="w-4 h-4 text-slate-400" />}
          </div>
        </div>

        {/* Line 2: Match ID, Date, Source, Worldwide % badge, Quick Re-search & Copy */}
        <div className="flex items-center justify-between gap-2 mt-2 pt-2 border-t border-slate-800/60 text-xs font-mono text-slate-400">
          <div className="flex items-center gap-2 flex-wrap min-w-0">
            <span>ID: {waterfall.matchId}</span>
            {waterfall.date && (
              <span className="flex items-center gap-1">
                <Calendar className="w-3 h-3 text-slate-500" />
                {waterfall.date}
              </span>
            )}
            {isLinked && sourceName && (
              <span className="px-1.5 py-0.2 rounded text-[10px] font-semibold bg-slate-800 text-slate-300 border border-slate-700">
                {sourceName === 'WEBSUB' ? '⚡ WebSub' : sourceName === 'RSS' ? '📡 RSS' : sourceName === 'API' ? '🔑 API' : sourceName}
              </span>
            )}
            {isLinked && (waterfall.linkedChannelHandle || activeCycle.linkedChannelHandle) && (
              <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/30 flex items-center gap-1">
                <span>📺</span>
                <span className="truncate max-w-[140px] sm:max-w-[200px]">
                  {waterfall.linkedChannelHandle || activeCycle.linkedChannelHandle}
                </span>
              </span>
            )}
            {isLinked && (waterfall.cumulativeAvailability?.percentage ?? waterfall.worldwidePercentage ?? activeCycle.worldwidePercentage) !== undefined && (
              <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold border ${
                isWorldwideGood
                  ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30'
                  : 'bg-amber-500/15 text-amber-300 border-amber-500/30'
              }`}>
                🌍 {waterfall.cumulativeAvailability?.percentage ?? waterfall.worldwidePercentage ?? activeCycle.worldwidePercentage}% WW
              </span>
            )}
            {!isLinked && !isReset && (activeCycle.nextCycleEta || waterfall.nextCycleEta) && (
              <span className="text-sky-400 text-[11px] flex items-center gap-1">
                <Clock className="w-3 h-3" />
                <span>{activeCycle.nextCycleEta || waterfall.nextCycleEta}</span>
              </span>
            )}
          </div>

          <div className="flex items-center gap-1 shrink-0">
            <button
              type="button"
              onClick={handleReSearch}
              disabled={isReSearching}
              title="Reset highlight and re-queue search"
              className="p-1 rounded text-slate-400 hover:text-amber-300 hover:bg-amber-500/10 transition border border-slate-800 disabled:opacity-50"
            >
              {isReSearching ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin text-amber-400" />
              ) : isReset ? (
                <Check className="w-3.5 h-3.5 text-emerald-400" />
              ) : (
                <RotateCcw className="w-3.5 h-3.5" />
              )}
            </button>
            <button
              type="button"
              onClick={handleCopyCardJson}
              title="Copy match JSON"
              className="p-1 rounded text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition border border-slate-800"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Expanded Segmented Accordion Body */}
      {isOpen && (
        <div className="px-3.5 sm:px-4 pb-4 pt-2 border-t border-slate-800/80 bg-slate-950/40 rounded-b-xl">
          <WaterfallSteps
            cycle={activeCycle}
            matchId={waterfall.matchId}
            matchTitle={waterfall.title}
            waterfall={waterfall}
            cycles={cycles}
            selectedCycleIdx={selectedCycleIdx}
            onSelectCycle={setSelectedCycleIdx}
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

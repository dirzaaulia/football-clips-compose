'use client';

import React, { useState } from 'react';
import { Sparkles, EyeOff, ChevronDown } from 'lucide-react';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface MatchRecapSectionProps {
  recapAnalysis?: string | null;
  status?: string;
}

export default function MatchRecapSection({ recapAnalysis, status }: MatchRecapSectionProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  if (!recapAnalysis || !recapAnalysis.trim()) return null;

  const isFinished = status === 'FINISHED';
  const isLive = status === 'IN_PLAY' || status === 'PAUSED' || status === 'LIVE';
  const isUpcoming = !isFinished && !isLive;

  // Never spoiler-guard upcoming preview summaries; only guard finished/live games when spoiler-free is on
  const isGuarded = !isUpcoming && isSpoilerFree && !isRevealed;
  const sectionTitle = isUpcoming
    ? 'Match Preview & Tactical Setup'
    : 'Match Summary & Tactical Breakdown';

  return (
    <section className="rounded-3xl bg-[#181C20] border border-[#334155] p-6 sm:p-8 space-y-4 shadow-xl">
      <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60">
        <div className="flex items-center gap-2 text-[#7098F8]">
          <Sparkles className="w-4 h-4 text-[#4FC3F7]" />
          <h2 className="text-sm font-extrabold uppercase tracking-wider text-white">
            {sectionTitle}
          </h2>
        </div>
        {!isUpcoming && isSpoilerFree && (
          <span className="text-[10px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
            Spoiler Guarded
          </span>
        )}
      </div>

      {isGuarded ? (
        <div className="py-6 px-4 rounded-2xl bg-[#101418] border border-[#334155] text-center space-y-3">
          <div className="flex items-center justify-center gap-2 text-sm font-bold text-[#94A3B8]">
            <EyeOff className="w-4 h-4 text-[#7098F8]" />
            <span>Analysis contains match outcome, tactical moments, and goal details.</span>
          </div>
          <button
            onClick={() => setIsRevealed(true)}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white text-xs font-bold transition-all shadow-md cursor-pointer"
          >
            <span>Reveal Match Analysis</span>
            <ChevronDown className="w-3.5 h-3.5" />
          </button>
        </div>
      ) : (
        <div className="prose prose-invert max-w-none text-[#F1F5F9] text-xs sm:text-sm leading-relaxed space-y-3 whitespace-pre-line">
          {recapAnalysis}
        </div>
      )}
    </section>
  );
}

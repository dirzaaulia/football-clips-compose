'use client';

import React, { useState } from 'react';
import { EyeOff } from 'lucide-react';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface MatchScoreSectionProps {
  homeScore?: number | null;
  awayScore?: number | null;
  status: string;
}

export default function MatchScoreSection({
  homeScore,
  awayScore,
  status,
}: MatchScoreSectionProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  const isLive = status === 'IN_PLAY' || status === 'PAUSED' || status === 'LIVE';
  const isFinished = status === 'FINISHED';
  const hasScore = homeScore != null && awayScore != null;

  const shouldHideScore = isSpoilerFree && !isRevealed && (isFinished || isLive);

  const statusLabel =
    status === 'PAUSED'
      ? 'Half Time'
      : isLive
      ? 'Live In-Play'
      : isFinished
      ? 'Full Time'
      : 'Scheduled';

  if (shouldHideScore) {
    return (
      <button
        onClick={() => setIsRevealed(true)}
        title="Spoiler protected. Click to reveal score."
        className="px-5 py-2.5 rounded-2xl text-center bg-[#101418] border border-[#334155] hover:border-[#7098F8]/60 transition-all shadow-inner shrink-0 group cursor-pointer"
      >
        <div className="flex items-center justify-center gap-1.5 text-xl sm:text-2xl font-black text-[#94A3B8] group-hover:text-white">
          <EyeOff className="w-5 h-5 text-[#7098F8] group-hover:scale-110 transition-transform" />
          <span className="tracking-widest">? - ?</span>
        </div>
        <div className="text-[10px] font-bold uppercase tracking-wider text-[#7098F8] mt-0.5">
          Tap to Reveal
        </div>
      </button>
    );
  }

  return (
    <div
      className={`px-5 py-2.5 rounded-2xl text-center shadow-inner shrink-0 ${
        isLive
          ? 'bg-[#101418] border border-red-500/50 shadow-[0_0_12px_rgba(239,68,68,0.25)]'
          : 'bg-[#101418] border border-[#7098F8]/40'
      }`}
    >
      <span className="text-xl sm:text-2xl font-black text-white">
        {hasScore ? `${homeScore} - ${awayScore}` : 'vs'}
      </span>
      <div
        className={`text-[10px] font-bold uppercase tracking-wider mt-0.5 ${
          isLive ? 'text-red-400 animate-pulse' : 'text-[#4FC3F7]'
        }`}
      >
        {statusLabel}
      </div>
    </div>
  );
}

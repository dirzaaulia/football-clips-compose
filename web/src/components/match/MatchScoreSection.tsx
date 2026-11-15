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

  const isFinished = status === 'FINISHED' || status === 'AWARDED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(status);
  const shouldHideScore = isSpoilerFree && isFinished && !isRevealed;
  const hasScore = homeScore !== null && awayScore !== null && homeScore !== undefined && awayScore !== undefined;

  const statusLabel = isFinished
    ? 'Full Time'
    : isLive
    ? 'Live'
    : 'Scheduled';

  if (shouldHideScore) {
    return (
      <button
        onClick={() => setIsRevealed(true)}
        title="Spoiler protected. Click to reveal score."
        className="px-5 py-2.5 rounded-2xl text-center bg-[#121316] border border-white/10 hover:border-[#DFFF00]/60 transition-all shadow-inner shrink-0 group cursor-pointer font-tech"
      >
        <div className="flex items-center justify-center gap-1.5 text-xl sm:text-2xl font-black text-white/50 group-hover:text-white">
          <EyeOff className="w-5 h-5 text-[#DFFF00] group-hover:scale-110 transition-transform" />
          <span className="tracking-widest">? - ?</span>
        </div>
        <div className="text-[10px] font-bold uppercase tracking-wider text-[#DFFF00] mt-0.5">
          Tap to Reveal
        </div>
      </button>
    );
  }

  return (
    <div
      className={`px-5 py-2.5 rounded-2xl text-center shadow-inner shrink-0 font-tech ${
        isLive
          ? 'bg-[#121316] border border-red-500/50 shadow-[0_0_12px_rgba(239,68,68,0.25)]'
          : 'bg-[#121316] border border-white/10'
      }`}
    >
      <span className="text-xl sm:text-2xl font-black text-white">
        {hasScore ? `${homeScore} - ${awayScore}` : 'vs'}
      </span>
      <div className="text-[10px] font-bold uppercase tracking-wider text-white/50 mt-0.5">
        {statusLabel}
      </div>
    </div>
  );
}

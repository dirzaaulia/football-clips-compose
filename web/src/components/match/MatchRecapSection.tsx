'use client';

import React, { useState, useMemo } from 'react';
import ReactMarkdown from 'react-markdown';
import { Newspaper, EyeOff, ChevronDown } from 'lucide-react';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface MatchRecapSectionProps {
  recapAnalysis?: string | null;
  status?: string;
}

export default function MatchRecapSection({ recapAnalysis, status }: MatchRecapSectionProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  const cleanedAnalysis = useMemo(() => {
    if (!recapAnalysis) return '';
    return recapAnalysis
      .replace(/(?:\.\.\.)?\s*Continue\s+reading\.{0,3}/gi, '')
      .trim();
  }, [recapAnalysis]);

  if (!cleanedAnalysis) return null;

  const isFinished = status === 'FINISHED';
  const isLive = status === 'IN_PLAY' || status === 'PAUSED' || status === 'LIVE';
  const isUpcoming = !isFinished && !isLive;

  const isGuarded = isFinished && isSpoilerFree && !isRevealed;
  const sectionTitle = isUpcoming
    ? 'Accredited Match Preview & Tactical Setup'
    : 'Accredited Match Report & Tactical Breakdown';

  return (
    <section className="rounded-2xl bg-[#121316] border border-white/10 p-5 sm:p-7 space-y-4 shadow-xl select-none">
      <div className="flex items-center justify-between pb-3 border-b border-white/10">
        <div className="flex items-center gap-2">
          <Newspaper className="w-4 h-4 text-[#DFFF00]" />
          <h2 className="font-scoreboard text-sm sm:text-base uppercase tracking-wider text-white">
            {sectionTitle}
          </h2>
        </div>
        {isGuarded && (
          <span className="text-[10px] font-tech font-bold px-2.5 py-0.5 rounded-full bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30">
            Spoiler Guarded
          </span>
        )}
      </div>

      {isGuarded ? (
        <div className="py-6 px-4 rounded-xl bg-[#0A0A0C] border border-white/10 text-center space-y-3 font-tech">
          <div className="flex items-center justify-center gap-2 text-xs sm:text-sm text-white/60">
            <EyeOff className="w-4 h-4 text-[#DFFF00]" />
            <span>Report contains match outcome, key tactical moments, and goal details.</span>
          </div>
          <button
            onClick={() => setIsRevealed(true)}
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] text-xs font-tech font-black uppercase tracking-wider transition-all shadow-[0_0_15px_rgba(223,255,0,0.3)] cursor-pointer"
          >
            <span>Reveal Match Report</span>
            <ChevronDown className="w-3.5 h-3.5" />
          </button>
        </div>
      ) : (
        <div className="prose prose-invert prose-headings:text-white prose-headings:font-scoreboard prose-h3:text-sm prose-h3:uppercase prose-h3:tracking-wider prose-h3:mt-4 prose-h3:mb-1 prose-strong:text-[#DFFF00] prose-em:text-white/60 prose-li:text-white/80 prose-ul:my-2 max-w-none text-white/80 text-xs sm:text-sm leading-relaxed font-tech">
          <ReactMarkdown>{cleanedAnalysis}</ReactMarkdown>
        </div>
      )}
    </section>
  );
}

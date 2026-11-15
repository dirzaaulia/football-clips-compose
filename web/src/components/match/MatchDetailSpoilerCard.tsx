'use client';

import React from 'react';
import { Eye, EyeOff } from 'lucide-react';

interface MatchDetailSpoilerCardProps {
  onRevealScore: () => void;
}

export default function MatchDetailSpoilerCard({ onRevealScore }: MatchDetailSpoilerCardProps) {
  return (
    <div className="py-8 px-6 rounded-2xl bg-[#121316] border border-white/10 text-center space-y-3 shadow-xl select-none">
      <div className="inline-flex p-3 rounded-full bg-[#DFFF00]/10 text-[#DFFF00] border border-[#DFFF00]/30 mb-1">
        <EyeOff className="w-6 h-6" />
      </div>
      <h3 className="font-scoreboard text-base sm:text-lg uppercase tracking-wider text-white">
        Spoiler-Free Mode Active
      </h3>
      <p className="text-xs font-tech text-white/50 max-w-md mx-auto">
        Match scoreline, incident timeline, stats, and match recap are hidden to preserve match excitement.
      </p>
      <button
        onClick={onRevealScore}
        className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider transition-all shadow-[0_0_15px_rgba(223,255,0,0.3)] cursor-pointer mt-2"
      >
        <Eye className="w-4 h-4 fill-[#0A0A0C]" />
        <span>Reveal Match Details &amp; Score</span>
      </button>
    </div>
  );
}

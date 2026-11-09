'use client';

import React from 'react';
import { Eye, EyeOff } from 'lucide-react';

interface MatchDetailSpoilerCardProps {
  onRevealScore: () => void;
}

export default function MatchDetailSpoilerCard({ onRevealScore }: MatchDetailSpoilerCardProps) {
  return (
    <div className="py-8 px-6 rounded-3xl bg-[#181C20] border border-[#334155] text-center space-y-3 shadow-xl">
      <div className="inline-flex p-3 rounded-full bg-[#1E3A8A]/30 text-[#7098F8] border border-[#7098F8]/40 mb-1">
        <EyeOff className="w-6 h-6" />
      </div>
      <h3 className="text-base font-extrabold text-white">Spoiler Free Mode Active</h3>
      <p className="text-xs text-[#94A3B8] max-w-md mx-auto">
        Match scoreline, incident timeline, stats, and match recap are hidden to preserve match excitement.
      </p>
      <button
        onClick={onRevealScore}
        className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white text-xs font-bold transition-all shadow-lg hover:shadow-blue-500/25 cursor-pointer mt-2"
      >
        <Eye className="w-4 h-4" />
        <span>Reveal Match Details &amp; Score</span>
      </button>
    </div>
  );
}

'use client';

import React from 'react';
import { Zap, Sparkles, Flame, Bookmark, Compass } from 'lucide-react';

interface ShortsChannelRailProps {
  activeCategory: string;
  onSelectCategory: (cat: string) => void;
}

const POPULAR_CHANNELS = [
  { code: 'RM', name: 'Real Madrid CF', color: 'bg-white/10 text-white' },
  { code: 'FCB', name: 'FC Barcelona', color: 'bg-[#FF2E4D]/20 text-[#FF2E4D]' },
  { code: 'MC', name: 'Manchester City', color: 'bg-[#38BDF8]/20 text-[#38BDF8]' },
  { code: 'LIV', name: 'Liverpool FC', color: 'bg-rose-500/20 text-rose-400' },
  { code: 'BAY', name: 'Bayern Munich', color: 'bg-red-600/20 text-red-400' },
];

export default function ShortsChannelRail({
  activeCategory,
  onSelectCategory,
}: ShortsChannelRailProps) {
  return (
    <aside className="hidden lg:flex flex-col gap-2 w-56 shrink-0 pt-2 select-none">
      {/* Category Links */}
      <button
        onClick={() => onSelectCategory('all')}
        className={`flex items-center gap-3 px-4 py-3 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
          activeCategory === 'all'
            ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
            : 'text-white/60 hover:text-white hover:bg-white/5'
        }`}
      >
        <Zap className="w-4 h-4 fill-current text-[#DFFF00]" />
        <span>Active Feed</span>
      </button>

      <button
        onClick={() => onSelectCategory('trending')}
        className={`flex items-center gap-3 px-4 py-3 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
          activeCategory === 'trending'
            ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
            : 'text-white/60 hover:text-white hover:bg-white/5'
        }`}
      >
        <Flame className="w-4 h-4 text-[#FF2E4D]" />
        <span>Trending Goals</span>
      </button>

      <button
        onClick={() => onSelectCategory('tactical')}
        className={`flex items-center gap-3 px-4 py-3 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
          activeCategory === 'tactical'
            ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
            : 'text-white/60 hover:text-white hover:bg-white/5'
        }`}
      >
        <Compass className="w-4 h-4 text-[#38BDF8]" />
        <span>Tactical Skills</span>
      </button>

      <button
        onClick={() => onSelectCategory('saved')}
        className={`flex items-center gap-3 px-4 py-3 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
          activeCategory === 'saved'
            ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
            : 'text-white/60 hover:text-white hover:bg-white/5'
        }`}
      >
        <Bookmark className="w-4 h-4 text-white/50" />
        <span>Saved Reels</span>
      </button>

      <div className="border-t border-white/5 my-2" />

      {/* Popular Channels Section */}
      <span className="px-4 py-1 text-[10px] font-tech uppercase tracking-wider text-white/40 font-bold">
        Popular Channels
      </span>
      <div className="flex flex-col gap-1">
        {POPULAR_CHANNELS.map((ch) => (
          <div
            key={ch.code}
            className="flex items-center gap-3 px-4 py-2 hover:bg-white/5 rounded-xl transition-colors cursor-pointer"
          >
            <div
              className={`w-7 h-7 rounded-full flex items-center justify-center font-tech font-bold text-[10px] ${ch.color}`}
            >
              {ch.code}
            </div>
            <span className="text-xs text-white/80 font-medium truncate">
              {ch.name}
            </span>
          </div>
        ))}
      </div>
    </aside>
  );
}

'use client';

import React from 'react';
import { EyeOff, Eye, SlidersHorizontal } from 'lucide-react';
import SeasonSelector from '@/components/filter/SeasonSelector';

interface SubNavBarProps {
  selectedComp: string;
  onSelectComp: (comp: string) => void;
  isSpoilerFree: boolean;
  onToggleSpoilerFree: () => void;
  onOpenFilter?: () => void;
  activeFilterCount?: number;
}

const PRIMARY_LEAGUES = [
  { id: 'ALL', name: 'All Competitions' },
  { id: 'CL', name: 'Champions League' },
  { id: 'PL', name: 'Premier League' },
  { id: 'PD', name: 'La Liga' },
  { id: 'SA', name: 'Serie A' },
  { id: 'BL1', name: 'Bundesliga' },
  { id: 'FL1', name: 'Ligue 1' },
];

export default function SubNavBar({
  selectedComp,
  onSelectComp,
  isSpoilerFree,
  onToggleSpoilerFree,
  onOpenFilter,
  activeFilterCount = 0,
}: SubNavBarProps) {
  return (
    <div className="sticky top-0 z-30 bg-[#0A0A0C]/90 backdrop-blur-xl py-3 border-b border-white/10 mb-6 space-y-3">
      {/* Top Bar: Spoiler-Free Segmented Pill & Season Filter */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        {/* Segmented Spoiler-Free Pill Switch */}
        <div className="flex items-center gap-2">
          <div className="inline-flex p-1 rounded-full bg-[#121316] border border-white/10 font-tech text-xs select-none">
            <button
              onClick={() => isSpoilerFree && onToggleSpoilerFree()}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-full transition-all cursor-pointer ${
                !isSpoilerFree
                  ? 'bg-[#1A1C22] text-white font-bold shadow-sm'
                  : 'text-white/60 hover:text-white'
              }`}
            >
              <Eye className="w-3.5 h-3.5 text-[#38BDF8]" />
              <span>Show Scores</span>
            </button>
            <button
              onClick={() => !isSpoilerFree && onToggleSpoilerFree()}
              className={`flex items-center gap-1.5 px-3 py-1 rounded-full transition-all cursor-pointer ${
                isSpoilerFree
                  ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-[0_0_12px_rgba(223,255,0,0.3)]'
                  : 'text-white/60 hover:text-white'
              }`}
            >
              <EyeOff className="w-3.5 h-3.5 fill-current" />
              <span>Hide Scores (No Spoilers)</span>
            </button>
          </div>
        </div>

        {/* Season & Quick Filter Trigger */}
        <div className="flex items-center gap-2">
          <SeasonSelector variant="compact" />
          {onOpenFilter && (
            <button
              onClick={onOpenFilter}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[#121316] hover:bg-[#1A1C22] text-xs font-tech font-bold text-white/80 border border-white/10 transition-colors cursor-pointer"
            >
              <SlidersHorizontal className="w-3.5 h-3.5 text-[#DFFF00]" />
              <span>Filters</span>
              {activeFilterCount > 0 && (
                <span className="w-4 h-4 rounded-full bg-[#DFFF00] text-[#0A0A0C] text-[10px] font-black flex items-center justify-center">
                  {activeFilterCount}
                </span>
              )}
            </button>
          )}
        </div>
      </div>

      {/* Bottom Horizontal League Jump Capsule Pills */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
        {PRIMARY_LEAGUES.map((league) => {
          const isSelected = selectedComp === league.id;
          return (
            <button
              key={league.id}
              onClick={() => onSelectComp(league.id)}
              className={`shrink-0 px-4 py-1.5 rounded-full text-xs font-tech font-bold transition-all cursor-pointer ${
                isSelected
                  ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_15px_rgba(223,255,0,0.35)]'
                  : 'bg-[#121316] hover:bg-[#1A1C22] text-white/80 hover:text-white border border-white/10'
              }`}
            >
              {league.name}
            </button>
          );
        })}
      </div>
    </div>
  );
}

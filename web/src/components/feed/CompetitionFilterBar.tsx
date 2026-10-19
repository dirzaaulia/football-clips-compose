'use client';

import React from 'react';
import Image from 'next/image';
import { Trophy, Star, SlidersHorizontal } from 'lucide-react';
import { useCompetitions } from '@/hooks/useCompetitions';

interface CompetitionFilterBarProps {
  selectedComp: string;
  onSelectComp: (comp: string) => void;
  isMyClubsActive: boolean;
  onToggleMyClubs: () => void;
  favoriteCount: number;
  mounted: boolean;
  onOpenFilter: () => void;
  activeFilterCount: number;
  scrollRef: React.RefObject<HTMLDivElement | null>;
}

export default function CompetitionFilterBar({
  selectedComp,
  onSelectComp,
  isMyClubsActive,
  onToggleMyClubs,
  favoriteCount,
  mounted,
  onOpenFilter,
  activeFilterCount,
  scrollRef,
}: CompetitionFilterBarProps) {
  const competitions = useCompetitions();

  return (
    <section className="mb-6">
      <div
        ref={scrollRef}
        className="flex items-center gap-2 overflow-x-auto scrollbar-none py-1 cursor-grab active:cursor-grabbing select-none"
      >
        {/* Advanced Filters Button */}
        <button
          onClick={onOpenFilter}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
            activeFilterCount > 0
              ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
          }`}
        >
          <SlidersHorizontal className="w-3.5 h-3.5 text-[#7098F8]" />
          <span>Filters</span>
          {activeFilterCount > 0 && (
            <span className="w-4 h-4 rounded-full bg-[#7098F8] text-[#101418] text-[10px] font-black flex items-center justify-center ml-0.5">
              {activeFilterCount}
            </span>
          )}
        </button>

        {/* All Highlights Pill */}
        <button
          onClick={() => {
            onSelectComp('ALL');
          }}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
            !isMyClubsActive && selectedComp === 'ALL'
              ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
          }`}
        >
          <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
          <span>All Highlights</span>
        </button>

        {/* My Clubs Pill */}
        <button
          onClick={onToggleMyClubs}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
            isMyClubsActive && favoriteCount > 0
              ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
          }`}
        >
          <Star
            className={`w-3.5 h-3.5 ${
              isMyClubsActive && mounted && favoriteCount > 0
                ? 'text-[#7098F8] fill-[#7098F8]'
                : 'text-[#7098F8]'
            }`}
          />
          <span>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
        </button>

        {/* Dynamic Competitions Pills */}
        {competitions.map((c) => {
          const isSelected = !isMyClubsActive && selectedComp === c.competitionId;
          return (
            <button
              key={c.competitionId || c.name}
              onClick={() => {
                onSelectComp(c.competitionId || 'ALL');
              }}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
                isSelected
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                  : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
              }`}
            >
              {c.logo && (
                <div className="relative w-4 h-4 shrink-0">
                  <Image src={c.logo} alt={c.name} fill className="object-contain" unoptimized />
                </div>
              )}
              <span>{c.name}</span>
            </button>
          );
        })}
      </div>
    </section>
  );
}

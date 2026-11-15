'use client';

import React, { useState } from 'react';
import { Search, Star, SlidersHorizontal, Trophy, ChevronRight } from 'lucide-react';
import SeasonSelector from '@/components/filter/SeasonSelector';
import { useCompetitions } from '@/hooks/useCompetitions';
import CompetitionDrawer from '@/components/layout/CompetitionDrawer';

interface LeagueFilterSubBarProps {
  selectedComp: string;
  onSelectComp: (comp: string) => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  favoriteCount: number;
  isMyClubsActive: boolean;
  onToggleMyClubs: () => void;
  onOpenFilter?: () => void;
  activeFilterCount?: number;
}

const TOP_QUICK_LEAGUES = [
  { id: 'ALL', name: 'All' },
  { id: 'CL', name: 'UCL' },
  { id: 'PL', name: 'PL' },
  { id: 'PD', name: 'LaLiga' },
  { id: 'SA', name: 'Serie A' },
  { id: 'BL1', name: 'Bundesliga' },
];

export default function LeagueFilterSubBar({
  selectedComp,
  onSelectComp,
  searchQuery,
  onSearchChange,
  favoriteCount,
  isMyClubsActive,
  onToggleMyClubs,
  onOpenFilter,
  activeFilterCount = 0,
}: LeagueFilterSubBarProps) {
  const dynamicCompetitions = useCompetitions();
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  const activeCompetition = dynamicCompetitions.find(
    (c) => (c.competitionId || 'ALL') === selectedComp
  );
  const activeLabel = activeCompetition?.shortName || activeCompetition?.name || 'All Competitions';

  return (
    <>
      <div className="w-full bg-[#121316] border-y border-white/10 px-4 sm:px-6 lg:px-8 py-3.5 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3.5 select-none relative z-30">
        {/* Left: Search Input & Advanced Filter Trigger */}
        <div className="relative flex-1 max-w-sm flex items-center bg-[#0A0A0C] rounded-xl px-3.5 py-2 border border-white/10 focus-within:border-[#DFFF00] transition-colors">
          <Search className="w-4 h-4 text-white/40 mr-2.5 shrink-0" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search clubs or matches"
            className="bg-transparent text-xs sm:text-sm text-white placeholder:text-white/40 focus:outline-none w-full font-tech"
          />
          {onOpenFilter && (
            <button
              onClick={onOpenFilter}
              className="p-1 hover:text-[#DFFF00] text-white/50 transition-colors ml-1 cursor-pointer"
              title="Advanced Filters"
            >
              <SlidersHorizontal className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Center: Master Competition Drawer Button & Generous Quick Chips */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Master Competition Side Sheet Trigger */}
          <button
            onClick={() => setIsDrawerOpen(true)}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl border text-xs font-tech font-bold uppercase tracking-wider transition-all cursor-pointer ${
              selectedComp !== 'ALL' && !isMyClubsActive
                ? 'bg-[#DFFF00] text-[#0A0A0C] border-[#DFFF00] shadow-[0_0_15px_rgba(223,255,0,0.3)]'
                : 'bg-[#1A1C22] text-white/90 hover:text-white border-white/10 hover:border-white/20'
            }`}
          >
            <Trophy className={`w-3.5 h-3.5 ${selectedComp !== 'ALL' && !isMyClubsActive ? 'text-[#0A0A0C]' : 'text-[#DFFF00]'}`} />
            <span className="max-w-[140px] truncate">{activeLabel}</span>
            <ChevronRight className="w-3.5 h-3.5 text-white/40" />
          </button>

          {/* Quick Generous Chips */}
          <div className="flex flex-wrap items-center gap-1.5">
            {TOP_QUICK_LEAGUES.map((league) => {
              const isSelected = selectedComp === league.id && !isMyClubsActive;
              return (
                <button
                  key={league.id}
                  onClick={() => onSelectComp(league.id)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-tech font-bold uppercase tracking-wider transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                      : 'bg-[#1A1C22] hover:bg-[#252830] text-white/70 hover:text-white border border-white/10'
                  }`}
                >
                  {league.name}
                </button>
              );
            })}
          </div>
        </div>

        {/* Right: Season & My Clubs Button */}
        <div className="flex items-center gap-2 shrink-0 self-end md:self-auto">
          <SeasonSelector variant="compact" />

          <button
            onClick={onToggleMyClubs}
            className={`px-3.5 py-2 rounded-xl border font-tech text-xs uppercase tracking-wider flex items-center gap-2 transition-all cursor-pointer ${
              isMyClubsActive
                ? 'bg-[#DFFF00] text-[#0A0A0C] border-[#DFFF00] font-black shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                : 'bg-[#1A1C22] hover:bg-[#252830] text-white/80 border-white/10'
            }`}
          >
            <Star className={`w-3.5 h-3.5 ${isMyClubsActive ? 'fill-[#0A0A0C]' : 'text-[#DFFF00]'}`} />
            <span>My Clubs</span>
            <span className="px-1.5 py-0.5 rounded bg-black/40 text-[10px] font-mono">
              {favoriteCount}
            </span>
          </button>

          {activeFilterCount > 0 && onOpenFilter && (
            <button
              onClick={onOpenFilter}
              className="px-2.5 py-1.5 rounded-xl bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/40 text-xs font-tech font-bold flex items-center gap-1 cursor-pointer"
            >
              <span>{activeFilterCount} active</span>
            </button>
          )}
        </div>
      </div>

      {/* Smooth Sliding Side Sheet Drawer */}
      <CompetitionDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        competitions={dynamicCompetitions}
        selectedComp={selectedComp}
        onSelectComp={onSelectComp}
      />
    </>
  );
}

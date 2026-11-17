'use client';

import React from 'react';
import { Search, Star, Trophy, X } from 'lucide-react';
import CustomDropdown from '@/components/common/CustomDropdown';
import CompetitionLogo from '@/components/common/CompetitionLogo';

export type FixtureTab = 'ALL' | 'LIVE' | 'RESULTS' | 'UPCOMING';

interface CompetitionOption {
  competitionId?: string | null;
  name: string;
  shortName?: string;
  logo?: string;
}

interface FixturesFilterBarProps {
  searchQuery: string;
  onSearchChange: (val: string) => void;
  competitions: CompetitionOption[];
  selectedComp: string;
  isMyClubsActive: boolean;
  favoriteCount: number;
  mounted: boolean;
  onSelectCompetition: (val: string) => void;
  onToggleMyClubs: () => void;
}

export default function FixturesFilterBar({
  searchQuery,
  onSearchChange,
  competitions,
  selectedComp,
  isMyClubsActive,
  favoriteCount,
  mounted,
  onSelectCompetition,
  onToggleMyClubs,
}: FixturesFilterBarProps) {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-3">
      {/* Left 7 Cols: Search Input */}
      <div className="lg:col-span-7 relative flex items-center bg-[#1B1B1F] rounded-xl border border-white/10 focus-within:border-[#DFFF00]/60 px-4 py-2.5 transition-colors">
        <Search className="w-4 h-4 text-white/40 mr-3 shrink-0" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search teams, venues, competitions, or matchdays..."
          className="bg-transparent border-none outline-none text-xs sm:text-sm text-white placeholder:text-white/40 w-full font-tech"
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => onSearchChange('')}
            className="text-white/40 hover:text-white ml-2 cursor-pointer"
            aria-label="Clear search"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Right 5 Cols: League Dropdown & My Clubs */}
      <div className="lg:col-span-5 flex items-center gap-2.5">
        <div className="flex-1 min-w-0">
          <CustomDropdown<string>
            items={competitions.map((c) => ({
              value: c.competitionId || 'ALL',
              label: c.name,
              shortLabel: c.shortName || c.name,
              icon: c.logo ? (
                <div className="relative w-4 h-4 rounded bg-white/10 p-0.5 flex items-center justify-center shrink-0 border border-white/10">
                  <CompetitionLogo src={c.logo} name={c.name} />
                </div>
              ) : (
                <Trophy className="w-3.5 h-3.5 text-[#DFFF00]" />
              ),
            }))}
            value={isMyClubsActive ? '' : selectedComp}
            onChange={onSelectCompetition}
            prefix="League"
            variant="compact"
          />
        </div>

        <button
          type="button"
          onClick={onToggleMyClubs}
          className={`flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl font-tech text-xs font-bold uppercase tracking-wider transition-all shrink-0 cursor-pointer ${
            isMyClubsActive && favoriteCount > 0
              ? 'bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/40 shadow-[0_0_12px_rgba(223,255,0,0.2)]'
              : 'bg-[#1B1B1F] hover:bg-[#252830] text-white/80 hover:text-white border border-white/10'
          }`}
        >
          <Star
            className={`w-3.5 h-3.5 ${
              isMyClubsActive && mounted && favoriteCount > 0
                ? 'text-[#DFFF00] fill-[#DFFF00]'
                : 'text-[#DFFF00]'
            }`}
          />
          <span suppressHydrationWarning>
            My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}
          </span>
        </button>
      </div>
    </div>
  );
}

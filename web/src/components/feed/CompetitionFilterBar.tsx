'use client';

import React, { useMemo, useState, useEffect } from 'react';
import CompetitionLogo from '@/components/common/CompetitionLogo';
import { Trophy, Star, SlidersHorizontal, Search, X, ChevronUp } from 'lucide-react';
import { useCompetitions } from '@/hooks/useCompetitions';
import CustomDropdown from '@/components/common/CustomDropdown';

interface CompetitionFilterBarProps {
  selectedComp: string;
  onSelectComp: (comp: string) => void;
  isMyClubsActive: boolean;
  onToggleMyClubs: () => void;
  favoriteCount: number;
  mounted: boolean;
  onOpenFilter: () => void;
  activeFilterCount: number;
  scrollRef?: React.RefObject<HTMLDivElement | null>;
  searchQuery?: string;
  onSearchChange?: (q: string) => void;
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
  searchQuery = '',
  onSearchChange,
}: CompetitionFilterBarProps) {
  const competitions = useCompetitions();
  const [showScrollTop, setShowScrollTop] = useState(false);

  useEffect(() => {
    function handleScroll() {
      setShowScrollTop(window.scrollY > 400);
    }
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const dropdownItems = useMemo(() => {
    return competitions.map((c) => ({
      value: c.competitionId || 'ALL',
      label: c.name,
      shortLabel: c.shortName || c.name,
      icon: c.logo ? (
        <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
          <div className="relative w-full h-full">
            <CompetitionLogo src={c.logo} name={c.name} />
          </div>
        </div>
      ) : (
        <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
      ),
    }));
  }, [competitions]);

  const activeCompLogo = useMemo(() => {
    const found = competitions.find((c) => c.competitionId === selectedComp);
    return found?.logo || '';
  }, [competitions, selectedComp]);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <>
      <section className="sticky top-14 sm:top-16 z-30 mb-5 bg-[#101418]/95 backdrop-blur-xl border border-[#334155]/70 shadow-[0_8px_32px_rgba(0,0,0,0.5)] rounded-2xl p-2.5 transition-all duration-200">
        <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-2.5">
          {/* Quick Filters Row */}
          <div className="flex flex-wrap items-center gap-2">
            {/* League Dropdown Selector */}
            <CustomDropdown<string>
              items={dropdownItems}
              value={isMyClubsActive ? '' : selectedComp}
              onChange={(val) => onSelectComp(val)}
              leadingIcon={
                activeCompLogo ? (
                  <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
                    <div className="relative w-full h-full">
                      <CompetitionLogo src={activeCompLogo} name="Selected League" />
                    </div>
                  </div>
                ) : (
                  <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
                )
              }
              prefix="League"
              variant="compact"
              ariaLabel="Filter highlights by league"
            />

            {/* Quick Filter: All Competitions */}
            <button
              onClick={() => onSelectComp('ALL')}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
                !isMyClubsActive && selectedComp === 'ALL'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-[0_0_10px_rgba(112,152,248,0.3)]'
                  : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
              }`}
            >
              <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
              <span>All Competitions</span>
            </button>

            {/* Quick Filter: My Clubs */}
            <button
              onClick={onToggleMyClubs}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
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
              <span>My Clubs</span>
              {mounted && favoriteCount > 0 && (
                <span className="px-1.5 py-0.5 rounded-full text-[10px] font-black bg-[#7098F8]/20 text-[#7098F8] border border-[#7098F8]/30">
                  {favoriteCount}
                </span>
              )}
            </button>

            {/* Advanced Filters Button */}
            <button
              onClick={onOpenFilter}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer shrink-0 ${
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
          </div>

          {/* Inline Search Bar */}
          {onSearchChange && (
            <div className="relative flex-1 max-w-full md:max-w-xs">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#94A3B8] pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => onSearchChange(e.target.value)}
                placeholder="Search highlights, clubs..."
                className="w-full bg-[#181C20] border border-[#334155] focus:border-[#7098F8] text-white text-xs rounded-xl pl-8 pr-7 py-1.5 placeholder-[#64748B] focus:outline-none focus:ring-1 focus:ring-[#7098F8] transition-all"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => onSearchChange('')}
                  className="absolute right-2 top-1/2 -translate-y-1/2 p-0.5 text-[#94A3B8] hover:text-white cursor-pointer"
                  aria-label="Clear search"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>
          )}
        </div>
      </section>

      {/* Mobile Floating Quick-Return Button */}
      {showScrollTop && (
        <button
          onClick={scrollToTop}
          className="fixed bottom-20 right-4 z-40 sm:hidden flex items-center gap-1.5 px-3 py-2 rounded-full bg-[#1E3A8A] text-white border border-[#7098F8]/60 shadow-[0_4px_20px_rgba(0,0,0,0.6)] text-xs font-bold animate-in fade-in slide-in-from-bottom-2 cursor-pointer"
          aria-label="Quick return to filters"
        >
          <ChevronUp className="w-4 h-4" />
          <span>Filters</span>
        </button>
      )}
    </>
  );
}

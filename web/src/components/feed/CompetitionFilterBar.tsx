'use client';

import React, { useMemo } from 'react';
import CompetitionLogo from '@/components/common/CompetitionLogo';
import { Trophy, Star, SlidersHorizontal } from 'lucide-react';
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
}: CompetitionFilterBarProps) {
  const competitions = useCompetitions();

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

  return (
    <section className="mb-5">
      <div className="flex flex-wrap items-center justify-between gap-2.5">
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

          {/* Quick Filter: All Highlights */}
          <button
            onClick={() => onSelectComp('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
              !isMyClubsActive && selectedComp === 'ALL'
                ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-[0_0_10px_rgba(112,152,248,0.3)]'
                : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
            }`}
          >
            All
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
            <span>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
          </button>
        </div>

        {/* Advanced Filters Button */}
        <button
          onClick={onOpenFilter}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
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
    </section>
  );
}



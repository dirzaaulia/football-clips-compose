'use client';

import React from 'react';
import { MatchRecord } from '@/lib/database';
import { FilterOption } from '@/lib/middleware-highlights';
import FilterSideSheet from '@/components/filter/FilterSideSheet';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import SpoilerEducationDialog from '@/components/spoiler/SpoilerEducationDialog';
import FloatingActionControls from '@/components/feed/FloatingActionControls';

interface HomeModalsAndOverlaysProps {
  activeVideoMatch: MatchRecord | null;
  activeMatch: MatchRecord | null;
  isSideSheetOpen: boolean;
  onClosePlayer: () => void;
  matches: MatchRecord[];
  onSelectMatch: (m: MatchRecord) => void;
  isFilterOpen: boolean;
  onCloseFilter: () => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  availableCountries: FilterOption[];
  selectedCountries: string[];
  onToggleCountry: (c: string) => void;
  availableLeagues: FilterOption[];
  selectedLeagues: string[];
  onToggleLeague: (l: string) => void;
  onResetFilters: () => void;
  displayedMatchesCount: number;
  hasMore: boolean;
  isLoadingMore: boolean;
  onLoadMore: () => void;
  isMyClubsModalOpen: boolean;
  onCloseMyClubs: () => void;
  showEducation: boolean;
  onCloseEducation: () => void;
  activeFilterCount: number;
  onOpenFilter: () => void;
}

export default function HomeModalsAndOverlays({
  activeVideoMatch,
  activeMatch,
  isSideSheetOpen,
  onClosePlayer,
  matches,
  onSelectMatch,
  isFilterOpen,
  onCloseFilter,
  searchQuery,
  onSearchChange,
  availableCountries,
  selectedCountries,
  onToggleCountry,
  availableLeagues,
  selectedLeagues,
  onToggleLeague,
  onResetFilters,
  displayedMatchesCount,
  hasMore,
  isLoadingMore,
  onLoadMore,
  isMyClubsModalOpen,
  onCloseMyClubs,
  showEducation,
  onCloseEducation,
  activeFilterCount,
  onOpenFilter,
}: HomeModalsAndOverlaysProps) {
  const currentMatch = activeVideoMatch || activeMatch;

  return (
    <>
      <FilterSideSheet
        isOpen={isFilterOpen}
        onClose={onCloseFilter}
        searchQuery={searchQuery}
        onSearchChange={onSearchChange}
        availableCountries={availableCountries}
        selectedCountries={selectedCountries}
        onToggleCountry={onToggleCountry}
        availableLeagues={availableLeagues}
        selectedLeagues={selectedLeagues}
        onToggleLeague={onToggleLeague}
        onReset={onResetFilters}
        matchesFound={displayedMatchesCount}
        hasMore={hasMore}
        isLoadingMore={isLoadingMore}
        onLoadMore={onLoadMore}
      />
      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={onCloseMyClubs} />
      <SpoilerEducationDialog isOpen={showEducation} onClose={onCloseEducation} />
      <FloatingActionControls activeFilterCount={activeFilterCount} onOpenFilter={onOpenFilter} />
    </>
  );
}

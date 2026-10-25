'use client';

import React from 'react';
import { MatchRecord } from '@/lib/database';
import { FilterOption } from '@/lib/middleware-highlights';
import VideoPlayerSideSheet from '@/components/player/VideoPlayerSideSheet';
import FilterSideSheet from '@/components/filter/FilterSideSheet';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import SpoilerEducationDialog from '@/components/spoiler/SpoilerEducationDialog';
import FloatingActionControls from '@/components/feed/FloatingActionControls';

interface HomeModalsAndOverlaysProps {
  currentMatch: MatchRecord | null;
  isSideSheetOpen: boolean;
  onClosePlayer: () => void;
  matches: MatchRecord[];
  onSelectMatch: (match: MatchRecord) => void;
  isFilterOpen: boolean;
  onCloseFilter: () => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  availableLeagues: FilterOption[];
  selectedLeagues: string[];
  onToggleLeague: (league: string) => void;
  onResetFilters: () => void;
  displayedMatchesCount: number;
  isMyClubsModalOpen: boolean;
  onCloseMyClubs: () => void;
  showEducation: boolean;
  onCloseEducation: () => void;
  activeFilterCount: number;
  onOpenFilter: () => void;
}

export default function HomeModalsAndOverlays({
  currentMatch,
  isSideSheetOpen,
  onClosePlayer,
  matches,
  onSelectMatch,
  isFilterOpen,
  onCloseFilter,
  searchQuery,
  onSearchChange,
  availableLeagues,
  selectedLeagues,
  onToggleLeague,
  onResetFilters,
  displayedMatchesCount,
  isMyClubsModalOpen,
  onCloseMyClubs,
  showEducation,
  onCloseEducation,
  activeFilterCount,
  onOpenFilter,
}: HomeModalsAndOverlaysProps) {
  return (
    <>
      <VideoPlayerSideSheet
        match={currentMatch}
        isOpen={isSideSheetOpen}
        onClose={onClosePlayer}
        relatedMatches={matches}
        onSelectMatch={onSelectMatch}
      />
      <FilterSideSheet
        isOpen={isFilterOpen}
        onClose={onCloseFilter}
        searchQuery={searchQuery}
        onSearchChange={onSearchChange}
        availableLeagues={availableLeagues}
        selectedLeagues={selectedLeagues}
        onToggleLeague={onToggleLeague}
        onReset={onResetFilters}
        matchesFound={displayedMatchesCount}
      />
      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={onCloseMyClubs} />
      <SpoilerEducationDialog isOpen={showEducation} onClose={onCloseEducation} />
      <FloatingActionControls activeFilterCount={activeFilterCount} onOpenFilter={onOpenFilter} />
    </>
  );
}

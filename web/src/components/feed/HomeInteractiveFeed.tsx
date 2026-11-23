'use client';

import React, { useState, useEffect, useMemo, useRef } from 'react';
import { MatchRecord } from '@/lib/database';
import { extractFilterOptions } from '@/lib/middleware-highlights';
import { useCompetitions } from '@/hooks/useCompetitions';
import { isMatchForFavoriteClubs, useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useInfiniteHighlights } from '@/hooks/useCatalogHighlights';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import HeroBillboard from '@/components/hero/HeroBillboard';
import LeagueFilterSubBar from '@/components/layout/LeagueFilterSubBar';
import FeedLoadingSkeleton from '@/components/feed/FeedLoadingSkeleton';
import HomeHighlightsFeed from '@/components/feed/HomeHighlightsFeed';
import HomeModalsAndOverlays from '@/components/feed/HomeModalsAndOverlays';
import InfiniteScrollSentinel from '@/components/feed/InfiniteScrollSentinel';
import MyClubsStoreShelf from '@/components/affiliate/MyClubsStoreShelf';
import { useSeason } from '@/hooks/useSeason';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';
import { trackScreenView, trackSessionStart } from '@/lib/analytics';

interface HomeInteractiveFeedProps {
  initialMatches?: MatchRecord[];
  initialLiveMatches?: MatchRecord[];
  recentMatches?: MatchRecord[];
}

export default function HomeInteractiveFeed({
  initialMatches = [],
  recentMatches,
}: HomeInteractiveFeedProps) {
  const { selectedSeason } = useSeason();
  const { playVideo, closePlayer, isSideSheetOpen, activeMatch } = useVideoPlayer();
  const { isSpoilerFree } = useSpoilerFree();
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [showEducation, setShowEducation] = useState(false);
  const [activeVideoMatch, setActiveVideoMatch] = useState<MatchRecord | null>(null);
  const [isMountEntrance, setIsMountEntrance] = useState(true);

  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCountries, setSelectedCountries] = useState<string[]>([]);
  const [selectedLeagues, setSelectedLeagues] = useState<string[]>([]);
  const { favoriteClubs, favoriteCount } = useMyClubs();
  const { isMerchandiseEnabled } = useMerchandisePreference();
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    trackSessionStart();
    trackScreenView('Home');
    if (typeof window !== 'undefined' && !localStorage.getItem('is_spoiler_free_configured')) {
      setShowEducation(true);
    }
    const timer = setTimeout(() => setIsMountEntrance(false), 300);
    return () => clearTimeout(timer);
  }, []);

  const competitions = useCompetitions();
  const dynamicCompIds = useMemo(
    () => competitions.map((c) => c.competitionId).filter((id): id is string => Boolean(id && id !== 'ALL')),
    [competitions],
  );

  const isDefaultFeed = selectedComp === 'ALL' && selectedSeason === 2026 && !searchQuery.trim();
  const catalog = useInfiniteHighlights(selectedComp, selectedSeason, 20, searchQuery, dynamicCompIds);

  const matches = catalog.highlights.length > 0
    ? catalog.highlights
    : (isDefaultFeed ? initialMatches : []);

  const isFeedLoading = (catalog.isLoading || catalog.isFetching) && matches.length === 0;
  const loading = { initial: isFeedLoading, more: catalog.isLoadingMore, hasMore: catalog.hasMore };
  const handleLoadMore = catalog.loadMore;

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !loading.hasMore || loading.initial || catalog.error) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) handleLoadMore();
    }, { rootMargin: '400px' });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [handleLoadMore, loading.hasMore, loading.initial, catalog.error]);

  const { leagues } = useMemo(() => extractFilterOptions(matches), [matches]);
  const supportedClubs = useSupportedClubs();

  const displayedMatches = useMemo(() => {
    return matches.filter((m) => {
      if (isMyClubsActive && favoriteClubs.size > 0 && !isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs, supportedClubs)) return false;
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const home = (m.home_team_name || m.home_team).toLowerCase();
        const away = (m.away_team_name || m.away_team).toLowerCase();
        if (!home.includes(q) && !away.includes(q)) return false;
      }
      if (selectedCountries.length > 0 && (!m.country_name || !selectedCountries.includes(m.country_name))) return false;
      if (selectedLeagues.length > 0 && !selectedLeagues.includes(m.competition_name)) return false;
      return true;
    });
  }, [matches, isMyClubsActive, favoriteClubs, searchQuery, selectedCountries, selectedLeagues, supportedClubs]);

  const activeFilterCount = (searchQuery ? 1 : 0) + selectedCountries.length + selectedLeagues.length;

  const heroCandidateMatches = useMemo(() => {
    if (initialMatches.length > 0) return initialMatches;
    return recentMatches || [];
  }, [initialMatches, recentMatches]);

  return (
    <div className="w-full">
      {/* 1. Full-Bleed Edge-to-Edge DAZN Hero Billboard */}
      <HeroBillboard
        matches={heroCandidateMatches}
        match={heroCandidateMatches[0]}
        onPlay={(m) => { setActiveVideoMatch(m); playVideo(m); }}
        isLoading={isMountEntrance && heroCandidateMatches.length === 0}
      />

      {/* 2. Horizontal Supported Competitions Filter Bar */}
      <LeagueFilterSubBar
        selectedComp={selectedComp}
        onSelectComp={(comp) => { setIsMyClubsActive(false); setSelectedComp(comp); }}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        favoriteCount={favoriteCount}
        isMyClubsActive={isMyClubsActive}
        onToggleMyClubs={() => setIsMyClubsActive((prev) => !prev)}
        onOpenFilter={() => setIsFilterOpen(true)}
        activeFilterCount={activeFilterCount}
      />

      {/* 3. Modern Vertical Grid List with Infinite Scroll (Chronological Newest Matches) */}
      <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-4 space-y-6">
        {isMerchandiseEnabled && isMyClubsActive && (
          <MyClubsStoreShelf onOpenMyClubs={() => setIsMyClubsModalOpen(true)} className="mb-4" />
        )}

        {loading.initial ? (
          <FeedLoadingSkeleton isGrouped={false} />
        ) : (
          <HomeHighlightsFeed
            displayedMatches={displayedMatches}
            onPlay={(sel) => {
              setActiveVideoMatch(sel);
              playVideo(sel);
            }}
          />
        )}

        <InfiniteScrollSentinel
          sentinelRef={sentinelRef}
          loadingMore={loading.more}
          hasMore={loading.hasMore}
          hasMatches={displayedMatches.length > 0}
          initialLoading={loading.initial}
        />
      </div>

      <HomeModalsAndOverlays
        currentMatch={activeVideoMatch || activeMatch}
        isSideSheetOpen={isSideSheetOpen}
        onClosePlayer={() => { setActiveVideoMatch(null); closePlayer(); }}
        matches={matches}
        onSelectMatch={(sel) => { setActiveVideoMatch(sel); playVideo(sel); }}
        isFilterOpen={isFilterOpen}
        onCloseFilter={() => setIsFilterOpen(false)}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        availableLeagues={leagues}
        selectedLeagues={selectedLeagues}
        onToggleLeague={(l) => setSelectedLeagues((prev) => prev.includes(l) ? prev.filter((x) => x !== l) : [...prev, l])}
        onResetFilters={() => { setSearchQuery(''); setSelectedLeagues([]); }}
        displayedMatchesCount={displayedMatches.length}
        isMyClubsModalOpen={isMyClubsModalOpen}
        onCloseMyClubs={() => setIsMyClubsModalOpen(false)}
        showEducation={showEducation}
        onCloseEducation={() => setShowEducation(false)}
        activeFilterCount={activeFilterCount}
        onOpenFilter={() => setIsFilterOpen(true)}
      />
    </div>
  );
}

'use client';

import React, { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import { MatchRecord } from '@/lib/database';
import { extractFilterOptions } from '@/lib/middleware-highlights';
import { useCompetitions, isLeagueMatch } from '@/hooks/useCompetitions';
import { isMatchForFavoriteClubs, useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useInfiniteHighlights } from '@/hooks/useCatalogHighlights';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useDragScroll } from '@/hooks/useDragScroll';
import ExcitementCarousel from '@/components/hero/ExcitementCarousel';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import HighlightCard from '@/components/feed/HighlightCard';
import HighlightCardSkeleton from '@/components/feed/HighlightCardSkeleton';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import CompetitionSectionHeader from '@/components/feed/CompetitionSectionHeader';
import HomeModalsAndOverlays from '@/components/feed/HomeModalsAndOverlays';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CompetitionFilterBar from '@/components/feed/CompetitionFilterBar';
import InfiniteScrollSentinel from '@/components/feed/InfiniteScrollSentinel';
import MyClubsStoreShelf from '@/components/affiliate/MyClubsStoreShelf';
import { groupFeedMatchesByCompetition } from '@/components/feed/feedGroupHelpers';
import { useSeason } from '@/hooks/useSeason';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';
import { Trophy } from 'lucide-react';
import { trackScreenView, trackSessionStart } from '@/lib/analytics';

interface HomeInteractiveFeedProps {
  initialMatches?: MatchRecord[];
  initialLiveMatches?: MatchRecord[];
}

export default function HomeInteractiveFeed({
  initialMatches = [],
  initialLiveMatches = [],
}: HomeInteractiveFeedProps) {
  const { selectedSeason } = useSeason();
  const { playVideo, closePlayer, isSideSheetOpen, activeMatch } = useVideoPlayer();
  const [liveMatches, setLiveMatches] = useState<MatchRecord[]>(initialLiveMatches);
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [showEducation, setShowEducation] = useState(false);
  const [activeVideoMatch, setActiveVideoMatch] = useState<MatchRecord | null>(null);
  const [mounted, setMounted] = useState(false);

  // Search & Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCountries, setSelectedCountries] = useState<string[]>([]);
  const [selectedLeagues, setSelectedLeagues] = useState<string[]>([]);
  const { favoriteClubs, favoriteCount } = useMyClubs();
  const { isMerchandiseEnabled } = useMerchandisePreference();
  const scrollRef = useDragScroll<HTMLDivElement>();
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const [collapsedCompIds, setCollapsedCompIds] = useState<Set<string>>(new Set());

  const toggleCollapse = useCallback((compId: string) => {
    setCollapsedCompIds((prev) => {
      const next = new Set(prev);
      if (next.has(compId)) next.delete(compId);
      else next.add(compId);
      return next;
    });
  }, []);

  useEffect(() => {
    trackSessionStart();
    trackScreenView('Home');
    const timer = setTimeout(() => {
      setMounted(true);
      if (typeof window !== 'undefined' && !localStorage.getItem('is_spoiler_free_configured')) {
        setShowEducation(true);
      }
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  const filterKey = `${selectedComp}-${selectedSeason}-${searchQuery.trim()}`;
  const prevFilterKeyRef = useRef(filterKey);
  const catalog = useInfiniteHighlights(selectedComp, selectedSeason, 20, searchQuery);
  const isDefaultFeed = selectedComp === 'ALL' && selectedSeason === 2026 && !searchQuery.trim();
  const isTransitioning = filterKey !== prevFilterKeyRef.current;
  const isFilterLoading = (isTransitioning || catalog.isFetching || catalog.isLoading || catalog.isPending) && (!isDefaultFeed || isTransitioning);

  useEffect(() => {
    if (!catalog.isFetching && !catalog.isLoading) {
      prevFilterKeyRef.current = filterKey;
    }
  }, [catalog.isFetching, catalog.isLoading, filterKey]);

  const matches = (isFilterLoading && isTransitioning) ? [] : catalog.highlights.length > 0 ? catalog.highlights : (isDefaultFeed ? initialMatches : []);
  const isInitialLoading = (catalog.isLoading && initialMatches.length === 0) || isFilterLoading;
  const loading = { initial: isInitialLoading, more: catalog.isLoadingMore, hasMore: catalog.hasMore };
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
  const competitions = useCompetitions();
  const supportedClubs = useSupportedClubs();
  const competition = useMemo(() => competitions.find((b) => b.competitionId === selectedComp), [competitions, selectedComp]);

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

  const displayedLiveMatches = useMemo(() => {
    return liveMatches.filter((m) => {
      if (isMyClubsActive && favoriteClubs.size > 0) {
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs, supportedClubs)) return false;
      } else if (selectedComp !== 'ALL' && competition) {
        const matchComp = m.competition_id === selectedComp || isLeagueMatch(competition.name, m.competition_name);
        if (!matchComp) return false;
      }
      return true;
    });
  }, [liveMatches, isMyClubsActive, favoriteClubs, selectedComp, competition, supportedClubs]);

  const groupedFeedMatches = useMemo(() => {
    if (selectedComp !== 'ALL' || searchQuery.trim() || selectedCountries.length > 0 || selectedLeagues.length > 0 || isMyClubsActive) {
      return null;
    }
    return groupFeedMatchesByCompetition(displayedMatches, competitions);
  }, [displayedMatches, selectedComp, searchQuery, selectedCountries, selectedLeagues, isMyClubsActive, competitions]);

  const activeFilterCount = (searchQuery ? 1 : 0) + selectedCountries.length + selectedLeagues.length;

  return (
    <div className="space-y-6">
      <ExcitementCarousel
        matches={matches}
        onPlay={(m) => { setActiveVideoMatch(m); playVideo(m); }}
        isLoading={loading.initial}
      />

      <LiveMatchSection liveMatches={displayedLiveMatches} className="mt-6" />

      {/* Filter & Competition Bar (Sticky) */}
      <section className="sticky top-0 z-30 bg-[#101418]/95 backdrop-blur-xl py-2 my-4 border-b border-white/5 shadow-md">
        <div className="flex items-center justify-between pb-2 border-b border-[#334155]/40 mb-3">
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-[#7098F8]" />
            <h2 className="text-base font-extrabold text-white tracking-tight">Recent Highlights</h2>
          </div>
          <div className="flex items-center gap-3">
            <SeasonSelector variant="compact" />
            <span className="hidden sm:inline text-xs text-[#94A3B8] font-medium">Verified Publisher Feeds</span>
          </div>
        </div>

        <CompetitionFilterBar
          selectedComp={selectedComp}
          onSelectComp={(comp) => { setIsMyClubsActive(false); setSelectedComp(comp); }}
          isMyClubsActive={isMyClubsActive}
          onToggleMyClubs={() => favoriteCount === 0 ? setIsMyClubsModalOpen(true) : setIsMyClubsActive(!isMyClubsActive)}
          favoriteCount={favoriteCount}
          mounted={mounted}
          onOpenFilter={() => setIsFilterOpen(true)}
          activeFilterCount={activeFilterCount}
          scrollRef={scrollRef}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
        />
      </section>

      {isMerchandiseEnabled && isMyClubsActive && (
        <MyClubsStoreShelf onOpenMyClubs={() => setIsMyClubsModalOpen(true)} className="mb-6" />
      )}

      {/* Highlights Feed: Grouped by Competition when ALL, else Flat Grid */}
      {loading.initial || isFilterLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-5 my-6">
          {Array.from({ length: 8 }).map((_, i) => (
            <HighlightCardSkeleton key={`skeleton-${i}`} />
          ))}
        </div>
      ) : displayedMatches.length === 0 ? (
        <div className="flex flex-col items-center justify-center p-12 text-center rounded-2xl bg-[#181C20] border border-[#334155]/60 my-6">
          <Trophy className="w-10 h-10 text-[#94A3B8] mb-3 opacity-40" />
          <h3 className="text-base font-bold text-white">No Highlights Found</h3>
          <p className="text-xs text-[#94A3B8] mt-1 max-w-sm">
            No highlights match your selected competition or filters. Try clearing your filters or selecting another league.
          </p>
        </div>
      ) : groupedFeedMatches ? (
        <div className="space-y-6 my-6">
          {groupedFeedMatches.map((group, groupIdx) => {
            const displayItems = group.matches.slice(0, 20);
            const isExpanded = !collapsedCompIds.has(group.compId);
            return (
              <React.Fragment key={group.compId}>
                <div className="space-y-4">
                  <CompetitionSectionHeader
                    name={group.name}
                    emblemUrl={group.emblem}
                    isExpanded={isExpanded}
                    onToggleExpand={() => toggleCollapse(group.compId)}
                    onSeeAll={() => setSelectedComp(group.compId)}
                  />
                  {isExpanded && (
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-5 animate-in fade-in duration-200">
                      {displayItems.map((m) => (
                        <HighlightCard
                          key={m.id}
                          match={m}
                          onPlay={(sel) => {
                            setActiveVideoMatch(sel);
                            playVideo(sel);
                          }}
                        />
                      ))}
                    </div>
                  )}
                </div>

                {/* Ad between competition sections every 2-3 sections */}
                {(groupIdx + 1) % 3 === 0 && groupIdx < groupedFeedMatches.length - 1 && (
                  <AdSenseBanner slotId="5616816386" format="auto" className="my-6" />
                )}
              </React.Fragment>
            );
          })}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-5 my-6 animate-in fade-in duration-300">
          {displayedMatches.map((m, idx) => (
            <React.Fragment key={m.id}>
              <HighlightCard
                match={m}
                onPlay={(sel) => {
                  setActiveVideoMatch(sel);
                  playVideo(sel);
                }}
              />
              {idx > 0 && idx % 6 === 0 && <NativeInFeedAdCard slotId="5616816386" />}
            </React.Fragment>
          ))}
        </div>
      )}

      <InfiniteScrollSentinel
        sentinelRef={sentinelRef}
        loadingMore={loading.more}
        hasMore={loading.hasMore}
        hasMatches={displayedMatches.length > 0}
        initialLoading={loading.initial}
      />

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

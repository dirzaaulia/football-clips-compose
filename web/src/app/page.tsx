'use client';

import React, { useState, useEffect, useMemo, useRef } from 'react';
import { MatchRecord, getLiveMatches } from '@/lib/database';
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
import HomeModalsAndOverlays from '@/components/feed/HomeModalsAndOverlays';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CompetitionFilterBar from '@/components/feed/CompetitionFilterBar';
import InfiniteScrollSentinel from '@/components/feed/InfiniteScrollSentinel';
import MyClubsStoreShelf from '@/components/affiliate/MyClubsStoreShelf';
import { useSeason } from '@/hooks/useSeason';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';
import { Trophy, Loader2, CheckCircle2, Archive } from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';

export default function HomePage() {
  const { selectedSeason, setSeason, isArchiveSeason, currentOption } = useSeason();
  const { playVideo, closePlayer, isSideSheetOpen, activeMatch } = useVideoPlayer();
  const [liveMatches, setLiveMatches] = useState<MatchRecord[]>([]);
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [showEducation, setShowEducation] = useState(false);
  const [activeVideoMatch, setActiveVideoMatch] = useState<MatchRecord | null>(null);
  const [mounted, setMounted] = useState(false);

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCountries, setSelectedCountries] = useState<string[]>([]);
  const [selectedLeagues, setSelectedLeagues] = useState<string[]>([]);

  const { favoriteClubs, favoriteCount } = useMyClubs();
  const { isMerchandiseEnabled } = useMerchandisePreference();
  const scrollRef = useDragScroll<HTMLDivElement>();
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => {
      setMounted(true);
      if (typeof window !== 'undefined' && !localStorage.getItem('is_spoiler_free_configured')) {
        setShowEducation(true);
      }
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  // Fetch live matches and set 30s interval for real-time score updates
  useEffect(() => {
    let isCancelled = false;
    async function fetchLive() {
      try {
        const live = await getLiveMatches();
        if (!isCancelled) setLiveMatches(live);
      } catch (err) {
        console.error('Failed to fetch live matches:', err);
      }
    }
    fetchLive();
    const interval = setInterval(fetchLive, 10_000);
    return () => { isCancelled = true; clearInterval(interval); };
  }, []);

  const catalog = useInfiniteHighlights(selectedComp, selectedSeason, 20, searchQuery);
  const matches = catalog.highlights;
  const loading = { initial: catalog.isLoading, more: catalog.isLoadingMore, hasMore: catalog.hasMore };
  const handleLoadMore = catalog.loadMore;

  // Infinite scroll intersection observer
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !loading.hasMore || loading.initial || catalog.error) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) handleLoadMore();
    }, { rootMargin: '400px' });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [handleLoadMore, loading.hasMore, loading.initial, catalog.error]);

  const { countries, leagues } = useMemo(() => extractFilterOptions(matches), [matches]);

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
  }, [matches, isMyClubsActive, favoriteClubs, searchQuery, selectedCountries, selectedLeagues]);

  const competitions = useCompetitions();
  const supportedClubs = useSupportedClubs();
  const competition = useMemo(() => competitions.find((b) => b.competitionId === selectedComp), [competitions, selectedComp]);

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

  const activeFilterCount = (searchQuery ? 1 : 0) + selectedCountries.length + selectedLeagues.length;
  const feedMatches = displayedMatches;

  return (
    <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-4">
      {catalog.error ? <div role="alert" className="mb-4 rounded-xl border border-amber-700 p-4 text-amber-100">
        Highlights could not load. Your last results are still available.
        <button onClick={catalog.refresh} className="ml-3 underline min-h-12">Retry</button>
      </div> : null}
      <ExcitementCarousel
        matches={matches}
        onPlay={(m) => {
          setActiveVideoMatch(m);
          playVideo(m);
        }}
        isLoading={loading.initial}
      />

      {/* Pinned Live Match Section */}
      <LiveMatchSection liveMatches={displayedLiveMatches} className="mt-6" />

      {/* Filter & Competition Bar */}
      <section className="my-6">
        <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60 mb-4">
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-[#7098F8]" />
            <h2 className="text-base font-extrabold text-white tracking-tight">Recent Highlights</h2>
          </div>
          <div className="flex items-center gap-3">
            <SeasonSelector variant="compact" />
            <span className="hidden sm:inline text-xs text-[#94A3B8] font-medium">Saved Highlights & Publisher Links</span>
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
        />
      </section>

      {isMerchandiseEnabled && isMyClubsActive && (
        <MyClubsStoreShelf onOpenMyClubs={() => setIsMyClubsModalOpen(true)} className="mb-6" />
      )}

      {/* Archive Season Alert Banner */}
      {isArchiveSeason && (
        <div className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3.5 sm:p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-200 shadow-sm animate-in fade-in duration-200">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-amber-500/20 text-amber-400 shrink-0">
              <Archive className="w-5 h-5" />
            </div>
            <div>
              <div className="text-sm font-black flex items-center gap-2">
                <span>Archive Season {currentOption.label}</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-amber-500/20 border border-amber-500/40 text-amber-300 uppercase">
                  Archive
                </span>
              </div>
              <p className="text-xs text-amber-300/80 mt-0.5">
                Displaying verified archive match highlights and historical scorelines.
              </p>
            </div>
          </div>
          <button
            onClick={() => setSeason(2026)}
            className="self-start sm:self-auto px-3.5 py-1.5 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 border border-amber-500/40 text-xs font-bold text-amber-200 transition-all cursor-pointer whitespace-nowrap"
          >
            Switch to Current Season
          </button>
        </div>
      )}

      {/* Highlights Grid */}
      {loading.initial ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 my-6">
          {[...Array(6)].map((_, i) => (
            <HighlightCardSkeleton key={i} />
          ))}
        </div>
      ) : feedMatches.length === 0 ? (
        <div className="text-center py-20 bg-[#181C20] rounded-3xl border border-[#334155] my-6 space-y-3">
          <p className="text-[#94A3B8] text-sm">No highlights found matching your criteria.</p>
          <button
            onClick={() => { setSearchQuery(''); setSelectedCountries([]); setSelectedLeagues([]); setIsMyClubsActive(false); }}
            className="px-4 py-2 rounded-xl bg-[#1E2227] border border-[#7098F8]/40 text-xs font-bold text-[#7098F8] hover:bg-[#7098F8]/10 cursor-pointer"
          >
            Reset All Filters
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 my-6">
          {feedMatches.map((m, idx) => (
            <React.Fragment key={m.id}>
              <HighlightCard
                match={m}
                onPlay={(selected) => {
                  setActiveVideoMatch(selected);
                  playVideo(selected);
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

      {/* Side Sheets & Modals */}
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

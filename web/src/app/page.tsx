'use client';

import React, { useState, useEffect, useMemo, useTransition, useRef, useCallback } from 'react';
import { MatchRecord, getLiveMatches } from '@/lib/supabase';
import { fetchMergedHighlightsPage, extractFilterOptions } from '@/lib/middleware-highlights';
import { BIG_LEAGUES, isLeagueMatch } from '@/lib/constants';
import { isMatchForFavoriteClubs } from '@/lib/clubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useDragScroll } from '@/hooks/useDragScroll';
import HeroCarousel from '@/components/hero/HeroCarousel';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import HighlightCard from '@/components/feed/HighlightCard';
import HighlightCardSkeleton from '@/components/feed/HighlightCardSkeleton';
import VideoPlayerSideSheet from '@/components/player/VideoPlayerSideSheet';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import FilterSideSheet from '@/components/filter/FilterSideSheet';
import SpoilerEducationDialog from '@/components/spoiler/SpoilerEducationDialog';
import FloatingActionControls from '@/components/feed/FloatingActionControls';
import { Trophy, Star, SlidersHorizontal, Loader2 } from 'lucide-react';
import Image from 'next/image';

export default function HomePage() {
  const [matches, setMatches] = useState<MatchRecord[]>([]);
  const [liveMatches, setLiveMatches] = useState<MatchRecord[]>([]);
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [showEducation, setShowEducation] = useState(false);
  const [activeVideoMatch, setActiveVideoMatch] = useState<MatchRecord | null>(null);
  const [loading, setLoading] = useState({ initial: true, more: false, hasMore: true });
  const [offsets, setOffsets] = useState({ supa: 0, mw: 0 });
  const [mounted, setMounted] = useState(false);
  const [, startTransition] = useTransition();

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCountries, setSelectedCountries] = useState<string[]>([]);
  const [selectedLeagues, setSelectedLeagues] = useState<string[]>([]);

  const { favoriteClubs, favoriteCount } = useMyClubs();
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
        if (!isCancelled) {
          setLiveMatches(live);
        }
      } catch (err) {
        console.error('Failed to fetch live matches:', err);
      }
    }

    fetchLive();
    const interval = setInterval(fetchLive, 30_000);
    return () => {
      isCancelled = true;
      clearInterval(interval);
    };
  }, []);

  // Reset and load initial page when competition changes
  useEffect(() => {
    let isCancelled = false;
    async function loadInitial() {
      setLoading((prev) => ({ ...prev, initial: true }));
      const res = await fetchMergedHighlightsPage(selectedComp, 0, 0, 24, 30);
      if (!isCancelled) {
        startTransition(() => {
          setMatches(res.matches);
          setOffsets({ supa: res.nextOffsetSupabase, mw: res.nextOffsetMw });
          setLoading({ initial: false, more: false, hasMore: res.hasMore });
        });
      }
    }
    loadInitial();
    return () => { isCancelled = true; };
  }, [selectedComp]);

  // Infinite scroll load more handler
  const handleLoadMore = useCallback(async () => {
    if (loading.more || !loading.hasMore) return;
    setLoading((prev) => ({ ...prev, more: true }));
    const res = await fetchMergedHighlightsPage(selectedComp, offsets.supa, offsets.mw, 20, 30);
    startTransition(() => {
      setMatches((prev) => {
        const seen = new Set(prev.map((m) => m.highlight_video_id || m.id));
        return [...prev, ...res.matches.filter((m) => !seen.has(m.highlight_video_id || m.id))];
      });
      setOffsets({ supa: res.nextOffsetSupabase, mw: res.nextOffsetMw });
      setLoading((prev) => ({ ...prev, more: false, hasMore: res.hasMore }));
    });
  }, [loading.more, loading.hasMore, selectedComp, offsets]);

  // Infinite scroll intersection observer
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !loading.hasMore || loading.initial) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) handleLoadMore();
    }, { rootMargin: '400px' });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [handleLoadMore, loading.hasMore, loading.initial]);

  const { countries, leagues } = useMemo(() => extractFilterOptions(matches), [matches]);

  const displayedMatches = useMemo(() => {
    return matches.filter((m) => {
      if (isMyClubsActive && favoriteClubs.size > 0 && !isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs)) return false;
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

  const bigLeague = useMemo(() => BIG_LEAGUES.find((b) => b.competitionId === selectedComp), [selectedComp]);

  const displayedLiveMatches = useMemo(() => {
    return liveMatches.filter((m) => {
      if (isMyClubsActive && favoriteClubs.size > 0) {
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs)) return false;
      } else if (selectedComp !== 'ALL' && bigLeague) {
        const matchComp = m.competition_id === selectedComp || isLeagueMatch(bigLeague.name, m.competition_name);
        if (!matchComp) return false;
      }
      return true;
    });
  }, [liveMatches, isMyClubsActive, favoriteClubs, selectedComp, bigLeague]);

  const activeFilterCount = (searchQuery ? 1 : 0) + selectedCountries.length + selectedLeagues.length;
  const featuredMatch = displayedMatches[0] || null;
  const trendingMatches = displayedMatches.slice(1, 3);
  const feedMatches = displayedMatches.length > 3 ? displayedMatches.slice(3) : displayedMatches.slice(1);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
      <HeroCarousel
        featuredMatch={featuredMatch}
        trendingMatches={trendingMatches}
        onPlay={setActiveVideoMatch}
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
          <span className="text-xs text-[#94A3B8] font-medium">Verified Official Broadcasts</span>
        </div>

        <div ref={scrollRef} className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none touch-pan-x overscroll-x-contain py-1 select-none">
          <button
            onClick={() => setIsFilterOpen(true)}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
              activeFilterCount > 0 ? 'bg-[#1E3A8A]/60 text-[#4FC3F7] border border-[#7098F8]' : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
            }`}
          >
            <SlidersHorizontal className="w-3.5 h-3.5 text-[#7098F8]" />
            <span>Filter</span>
            {activeFilterCount > 0 && (
              <span className="px-1.5 py-0.2 bg-[#7098F8] text-[#101418] rounded-full text-[10px] font-black">{activeFilterCount}</span>
            )}
          </button>

          <button
            onClick={() => favoriteCount === 0 ? setIsMyClubsModalOpen(true) : setIsMyClubsActive(!isMyClubsActive)}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
              isMyClubsActive && favoriteCount > 0 ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]' : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
            }`}
          >
            <Star className={`w-3.5 h-3.5 ${isMyClubsActive && mounted && favoriteCount > 0 ? 'text-[#7098F8] fill-[#7098F8]' : 'text-[#7098F8]'}`} />
            <span>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
          </button>

          {BIG_LEAGUES.map((c) => {
            const isSelected = !isMyClubsActive && selectedComp === c.competitionId;
            return (
              <button
                key={c.competitionId || c.name}
                onClick={() => { setIsMyClubsActive(false); setSelectedComp(c.competitionId || 'ALL'); }}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
                  isSelected ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]' : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
                }`}
              >
                {c.logo && <div className="relative w-4 h-4 shrink-0"><Image src={c.logo} alt={c.name} fill className="object-contain" unoptimized /></div>}
                <span>{c.name}</span>
              </button>
            );
          })}
        </div>
      </section>

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
              <HighlightCard match={m} onPlay={setActiveVideoMatch} />
              {idx > 0 && idx % 6 === 0 && <NativeInFeedAdCard slotId="5616816386" />}
            </React.Fragment>
          ))}
        </div>
      )}

      {/* Infinite Scroll Sentinel */}
      <div ref={sentinelRef} className="py-6 flex flex-col items-center justify-center">
        {loading.more && (
          <div className="flex items-center gap-2 text-xs text-[#7098F8] font-bold">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span>Fetching more highlights...</span>
          </div>
        )}
      </div>

      {/* Side Sheets & Modals */}
      <VideoPlayerSideSheet
        match={activeVideoMatch}
        isOpen={Boolean(activeVideoMatch)}
        onClose={() => setActiveVideoMatch(null)}
        relatedMatches={matches.filter((m) => m.id !== activeVideoMatch?.id)}
        onSelectMatch={setActiveVideoMatch}
      />
      <FilterSideSheet
        isOpen={isFilterOpen}
        onClose={() => setIsFilterOpen(false)}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        availableCountries={countries}
        selectedCountries={selectedCountries}
        onToggleCountry={(c) => setSelectedCountries((prev) => prev.includes(c) ? prev.filter((x) => x !== c) : [...prev, c])}
        availableLeagues={leagues}
        selectedLeagues={selectedLeagues}
        onToggleLeague={(l) => setSelectedLeagues((prev) => prev.includes(l) ? prev.filter((x) => x !== l) : [...prev, l])}
        onReset={() => { setSearchQuery(''); setSelectedCountries([]); setSelectedLeagues([]); }}
        matchesFound={displayedMatches.length}
        hasMore={loading.hasMore}
        isLoadingMore={loading.more}
        onLoadMore={handleLoadMore}
      />
      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={() => setIsMyClubsModalOpen(false)} />
      <SpoilerEducationDialog isOpen={showEducation} onClose={() => setShowEducation(false)} />
      <FloatingActionControls activeFilterCount={activeFilterCount} onOpenFilter={() => setIsFilterOpen(true)} />
    </div>
  );
}

'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { getFixturesByDate, getLiveMatches, getMatchesBySeason, MatchRecord } from '@/lib/supabase';
import { BIG_LEAGUES, isLeagueMatch } from '@/lib/constants';
import { isMatchForFavoriteClubs } from '@/lib/clubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useDragScroll } from '@/hooks/useDragScroll';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, CheckCircle2, Clock, Star, Radio, Trophy, Film } from 'lucide-react';
import Image from 'next/image';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import FixtureItemCard from '@/components/fixtures/FixtureItemCard';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import MyClubsModal from '@/components/clubs/MyClubsModal';

type FixtureTab = 'ALL' | 'LIVE' | 'RESULTS' | 'UPCOMING';

export default function FixturesPage() {
  const [selectedSeason, setSelectedSeason] = useState<number>(2026);
  const [archiveFilter, setArchiveFilter] = useState<'ALL' | 'HIGHLIGHTS'>('ALL');
  const [selectedDate, setSelectedDate] = useState('2026-10-08');
  const [activeTab, setActiveTab] = useState<FixtureTab>('ALL');
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [fixtures, setFixtures] = useState<MatchRecord[]>([]);
  const [liveMatches, setLiveMatches] = useState<MatchRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [mounted, setMounted] = useState(false);

  const { favoriteClubs, favoriteCount } = useMyClubs();
  const scrollRef = useDragScroll<HTMLDivElement>();

  useEffect(() => {
    const today = new Date().toISOString().split('T')[0];
    const timer = setTimeout(() => {
      setMounted(true);
      setSelectedDate(today);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    let isCancelled = false;

    async function load() {
      if (selectedSeason === 2026) {
        const [fixData, liveData] = await Promise.all([
          getFixturesByDate(selectedDate),
          getLiveMatches(),
        ]);
        if (!isCancelled) {
          setFixtures(fixData);
          setLiveMatches(liveData);
          setIsLoading(false);
        }
      } else {
        const archiveData = await getMatchesBySeason(
          selectedSeason,
          selectedComp !== 'ALL' ? selectedComp : undefined,
          undefined,
          100
        );
        if (!isCancelled) {
          setFixtures(archiveData);
          setLiveMatches([]);
          setIsLoading(false);
        }
      }
    }

    setIsLoading(true);
    load();

    if (selectedSeason === 2026) {
      // Auto-refresh every 30s to keep live scores and match statuses semi-live
      const interval = setInterval(load, 30_000);
      return () => {
        isCancelled = true;
        clearInterval(interval);
      };
    } else {
      return () => {
        isCancelled = true;
      };
    }
  }, [selectedDate, selectedSeason, selectedComp]);

  const changeDay = (days: number) => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + days);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const formattedDate = new Date(selectedDate).toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  const bigLeague = BIG_LEAGUES.find((b) => b.competitionId === selectedComp);

  // Filter based on activeTab (status), competition (league), and My Clubs
  const allCandidateMatches = useMemo(() => {
    if (selectedSeason !== 2026) {
      if (archiveFilter === 'HIGHLIGHTS') {
        return fixtures.filter((m) => !!m.highlight_video_id);
      }
      return fixtures;
    }

    if (activeTab === 'LIVE') return liveMatches;

    const seen = new Set<string>();
    const list: MatchRecord[] = [];

    // In ALL tab, pin currently live matches to the top
    if (activeTab === 'ALL') {
      liveMatches.forEach((m) => {
        seen.add(m.id);
        list.push(m);
      });
    }

    fixtures.forEach((m) => {
      if (!seen.has(m.id)) {
        seen.add(m.id);
        list.push(m);
      }
    });

    return list;
  }, [activeTab, liveMatches, fixtures, selectedSeason, archiveFilter]);

  const filteredFixtures = useMemo(() => {
    return allCandidateMatches.filter((m) => {
      if (selectedSeason === 2026) {
        const isFinished = m.status === 'FINISHED';
        const isLive = m.status === 'IN_PLAY' || m.status === 'PAUSED' || m.status === 'LIVE';

        if (activeTab === 'LIVE' && !isLive) return false;
        if (activeTab === 'RESULTS' && !isFinished) return false;
        if (activeTab === 'UPCOMING' && (isFinished || isLive)) return false;
      }

      if (isMyClubsActive && favoriteClubs.size > 0) {
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs)) return false;
      } else if (selectedComp !== 'ALL' && bigLeague) {
        const matchComp = m.competition_id === selectedComp || isLeagueMatch(bigLeague.name, m.competition_name);
        if (!matchComp) return false;
      }

      return true;
    });
  }, [allCandidateMatches, activeTab, isMyClubsActive, favoriteClubs, selectedComp, bigLeague, selectedSeason]);

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-6">
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Scores & Match Schedules</h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">Live scores, scheduled fixtures, and verified results.</p>
        </div>

        {/* Season Switcher Pill */}
        <div className="flex items-center bg-[#181C20] border border-[#334155] p-1 rounded-xl shadow-sm self-start sm:self-auto">
          <button
            onClick={() => setSelectedSeason(2026)}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              selectedSeason === 2026
                ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            2026/27 (Current)
          </button>
          <button
            onClick={() => setSelectedSeason(2025)}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              selectedSeason === 2025
                ? 'bg-gradient-to-r from-amber-600 to-amber-500 text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white'
            }`}
          >
            2025/26 (Archive)
          </button>
        </div>
      </header>

      {/* Control Deck */}
      {selectedSeason === 2026 ? (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#181C20] border border-[#334155] rounded-2xl p-2 mb-3">
          {/* Status Segmented Tabs */}
          <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-none">
            <button
              onClick={() => setActiveTab('ALL')}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'ALL'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              All ({allCandidateMatches.length})
            </button>
            <button
              onClick={() => setActiveTab('LIVE')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'LIVE'
                  ? 'bg-gradient-to-r from-red-700 to-red-500 text-white border border-red-500 shadow-[0_0_12px_rgba(239,68,68,0.35)]'
                  : liveMatches.length > 0
                  ? 'text-red-400 hover:text-white hover:bg-[#1E2227] border border-red-500/30'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <span className={`w-2 h-2 rounded-full ${liveMatches.length > 0 ? 'bg-red-500 animate-pulse' : 'bg-gray-500'}`} />
              <span>Live {liveMatches.length > 0 ? `(${liveMatches.length})` : ''}</span>
            </button>
            <button
              onClick={() => setActiveTab('RESULTS')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'RESULTS'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
              <span>Results</span>
            </button>
            <button
              onClick={() => setActiveTab('UPCOMING')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'UPCOMING'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <Clock className="w-3.5 h-3.5 text-[#4FC3F7]" />
              <span>Upcoming</span>
            </button>
          </div>

          {/* Compact Date Stepper */}
          <div className="flex items-center justify-between sm:justify-end gap-1.5 bg-[#1E2227]/80 rounded-xl px-2 py-1 border border-[#334155]/60">
            <button
              onClick={() => changeDay(-1)}
              aria-label="Previous day"
              className="p-1 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#252B33] transition-all cursor-pointer"
            >
              <ChevronLeft className="w-3.5 h-3.5" />
            </button>
            <div className="flex items-center gap-1.5 px-2 text-xs font-bold text-white">
              <CalendarIcon className="w-3.5 h-3.5 text-[#7098F8]" />
              <span>{formattedDate}</span>
            </div>
            <button
              onClick={() => changeDay(1)}
              aria-label="Next day"
              className="p-1 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#252B33] transition-all cursor-pointer"
            >
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#181C20] border border-amber-500/30 rounded-2xl p-2 mb-3">
          <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-none">
            <button
              onClick={() => setArchiveFilter('ALL')}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                archiveFilter === 'ALL'
                  ? 'bg-gradient-to-r from-amber-600 to-amber-500 text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              All Matches ({fixtures.length})
            </button>
            <button
              onClick={() => setArchiveFilter('HIGHLIGHTS')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                archiveFilter === 'HIGHLIGHTS'
                  ? 'bg-gradient-to-r from-red-600 to-red-500 text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <Film className="w-3.5 h-3.5 text-red-400" />
              <span>With Highlights ({fixtures.filter((m) => !!m.highlight_video_id).length})</span>
            </button>
          </div>

          <div className="flex items-center gap-2 bg-amber-500/10 border border-amber-500/30 rounded-xl px-3 py-1.5 text-xs font-bold text-amber-400">
            <Trophy className="w-3.5 h-3.5 text-amber-400" />
            <span>Archive Season 2025/26 &bull; {fixtures.length} Fixtures Loaded</span>
          </div>
        </div>
      )}

      {/* Row 2: Competition Dropdown & My Clubs Quick Toggle (Compact & Space Efficient) */}
      <div className="flex flex-wrap items-center justify-between gap-2.5 mb-5">
        <div className="flex items-center gap-2">
          {/* League Dropdown Selector */}
          <div className="flex items-center gap-2 bg-[#181C20] border border-[#334155] hover:border-[#7098F8]/50 rounded-xl px-3 py-1.5 transition-all shadow-sm">
            <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
            <select
              value={isMyClubsActive ? '' : selectedComp}
              onChange={(e) => {
                setIsMyClubsActive(false);
                setSelectedComp(e.target.value);
              }}
              className="bg-transparent text-white text-xs font-bold focus:outline-none cursor-pointer pr-1"
            >
              {BIG_LEAGUES.map((c) => (
                <option key={c.competitionId || c.name} value={c.competitionId || 'ALL'} className="bg-[#181C20] text-white">
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* My Clubs Chip */}
          <button
            suppressHydrationWarning
            onClick={() => {
              if (favoriteCount === 0) {
                setIsMyClubsModalOpen(true);
              } else {
                setIsMyClubsActive(!isMyClubsActive);
              }
            }}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap transition-all cursor-pointer ${
              isMyClubsActive && favoriteCount > 0
                ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                : 'bg-[#181C20] text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-[#334155]'
            }`}
          >
            <Star className={`w-3.5 h-3.5 shrink-0 ${isMyClubsActive && mounted && favoriteCount > 0 ? 'text-[#7098F8] fill-[#7098F8]' : 'text-[#7098F8]'}`} />
            <span suppressHydrationWarning>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
          </button>
        </div>

        {/* Active League Badge / Summary */}
        <div className="hidden sm:flex items-center gap-2 text-xs text-[#94A3B8]">
          <span>Showing:</span>
          <span className="font-bold text-white bg-[#1E2227] border border-[#334155]/60 px-2.5 py-1 rounded-lg">
            {isMyClubsActive ? `My Clubs (${favoriteCount})` : (bigLeague?.name || 'All Competitions')}
          </span>
        </div>
      </div>

      {/* Pinned Live Match Section when in ALL mode */}
      {activeTab === 'ALL' && liveMatches.length > 0 && (
        <LiveMatchSection liveMatches={liveMatches} className="mb-6" />
      )}

      {/* Fixtures List */}
      {isLoading ? (
        <div className="space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-20 rounded-2xl bg-[#1E2227]/60 animate-pulse border border-[#334155]" />
          ))}
        </div>
      ) : filteredFixtures.length === 0 ? (
        <div className="text-center py-16 bg-[#181C20] rounded-2xl border border-[#334155] space-y-2">
          <p className="text-[#94A3B8] text-sm">
            {isMyClubsActive
              ? 'No scheduled matches for your favorite clubs on this date.'
              : 'No fixtures scheduled for this date matching the selected filters.'}
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredFixtures.map((m) => (
            <FixtureItemCard key={m.id} match={m} />
          ))}
        </div>
      )}

      <div className="mt-8">
        <AdSenseBanner slotId="5616816386" format="auto" />
      </div>

      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={() => setIsMyClubsModalOpen(false)} />
    </div>
  );
}

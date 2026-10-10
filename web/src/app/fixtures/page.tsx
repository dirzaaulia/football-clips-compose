'use client';

import React, { useState, useEffect } from 'react';
import { getFixturesByDate, MatchRecord } from '@/lib/supabase';
import { BIG_LEAGUES, isLeagueMatch } from '@/lib/constants';
import { isMatchForFavoriteClubs } from '@/lib/clubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useDragScroll } from '@/hooks/useDragScroll';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, CheckCircle2, Clock, Star } from 'lucide-react';
import Image from 'next/image';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import FixtureItemCard from '@/components/fixtures/FixtureItemCard';
import MyClubsModal from '@/components/clubs/MyClubsModal';

type FixtureTab = 'ALL' | 'RESULTS' | 'UPCOMING';

export default function FixturesPage() {
  const [selectedDate, setSelectedDate] = useState('2026-10-08');
  const [activeTab, setActiveTab] = useState<FixtureTab>('ALL');
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [fixtures, setFixtures] = useState<MatchRecord[]>([]);
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
      setIsLoading(true);
      const data = await getFixturesByDate(selectedDate);
      if (!isCancelled) {
        setFixtures(data);
        setIsLoading(false);
      }
    }

    load();

    return () => {
      isCancelled = true;
    };
  }, [selectedDate]);

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
  const filteredFixtures = fixtures.filter((m) => {
    const isFinished = m.status === 'FINISHED';
    if (activeTab === 'RESULTS' && !isFinished) return false;
    if (activeTab === 'UPCOMING' && isFinished) return false;

    if (isMyClubsActive && favoriteClubs.size > 0) {
      if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs)) return false;
    } else if (selectedComp !== 'ALL' && bigLeague) {
      const matchComp = m.competition_id === selectedComp || isLeagueMatch(bigLeague.name, m.competition_name);
      if (!matchComp) return false;
    }

    return true;
  });

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-6">
      <header className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Scores & Match Schedules</h1>
        <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">Live scores, scheduled fixtures, and verified results.</p>
      </header>

      {/* Sleek Integrated Control Deck (Row 1: Status & Date Stepper) */}
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
            All ({fixtures.length})
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

      {/* Row 2: Competition & My Clubs Horizontal Scrollable Chips */}
      <div ref={scrollRef} className="flex items-center gap-2 overflow-x-auto pb-1.5 scrollbar-none touch-pan-x overscroll-x-contain py-1 select-none mb-5">
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
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
            isMyClubsActive && favoriteCount > 0
              ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'bg-[#181C20] text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-[#334155]'
          }`}
        >
          <Star className={`w-3.5 h-3.5 shrink-0 ${isMyClubsActive && mounted && favoriteCount > 0 ? 'text-[#7098F8] fill-[#7098F8]' : 'text-[#7098F8]'}`} />
          <span suppressHydrationWarning>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
        </button>

        {BIG_LEAGUES.map((c) => {
          const isSelected = !isMyClubsActive && selectedComp === c.competitionId;
          return (
            <button
              key={c.competitionId || c.name}
              onClick={() => {
                setIsMyClubsActive(false);
                setSelectedComp(c.competitionId || 'ALL');
              }}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 transition-all cursor-pointer ${
                isSelected
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
                  : 'bg-[#181C20] text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-[#334155]'
              }`}
            >
              {c.logo ? (
                <div className="relative w-4 h-4 shrink-0">
                  <Image
                    src={c.logo}
                    alt={c.name}
                    fill
                    className="object-contain"
                    unoptimized
                  />
                </div>
              ) : null}
              <span>{c.name}</span>
            </button>
          );
        })}
      </div>

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

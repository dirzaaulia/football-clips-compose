'use client';

import React, { useState, useEffect, useMemo } from 'react';
import {
  getFixturesByDate,
  getLiveMatches,
  getMatchesBySeason,
  MatchRecord,
} from '@/lib/database';
import { useCompetitions, isLeagueMatch } from '@/hooks/useCompetitions';
import { isMatchForFavoriteClubs, useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import TopAdSenseLeaderboard from '@/components/ads/TopAdSenseLeaderboard';
import FixturesDateCarousel from '@/components/fixtures/FixturesDateCarousel';
import FixturesFilterBar, { FixtureTab } from '@/components/fixtures/FixturesFilterBar';
import FixturesGroupList, {
  CompetitionFixtureGroup,
} from '@/components/fixtures/FixturesGroupList';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import GooglePlayBadge from '@/components/common/GooglePlayBadge';
import { useSeason } from '@/hooks/useSeason';

function groupMatchesByCompetition(
  matches: MatchRecord[],
  competitions: Array<{ competitionId?: string | null; name: string; logo?: string }>,
): CompetitionFixtureGroup[] {
  const map = new Map<string, CompetitionFixtureGroup>();
  matches.forEach((m) => {
    const matchedLeague = competitions.find(
      (b) =>
        b.competitionId !== 'ALL' &&
        (b.competitionId === m.competition_id ||
          b.name === m.competition_name ||
          isLeagueMatch(b.name, m.competition_name)),
    );
    if (!matchedLeague && !m.competition_id) return;
    const normName = matchedLeague?.name || m.competition_name || m.competition_id;
    if (!map.has(normName)) {
      map.set(normName, {
        competitionId: matchedLeague?.competitionId || m.competition_id,
        competitionName: normName,
        logo: matchedLeague?.logo,
        matches: [],
      });
    }
    map.get(normName)!.matches.push(m);
  });
  return Array.from(map.values());
}

const STATUS_TABS: Array<{ id: FixtureTab; label: string }> = [
  { id: 'ALL', label: 'All Matches' },
  { id: 'LIVE', label: 'Live' },
  { id: 'RESULTS', label: 'Finished' },
  { id: 'UPCOMING', label: 'Upcoming' },
];

export default function FixturesPage() {
  const { selectedSeason } = useSeason();
  const [selectedDate, setSelectedDate] = useState('2026-10-08');
  const [activeTab, setActiveTab] = useState<FixtureTab>('ALL');
  const [selectedComp, setSelectedComp] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isMyClubsActive, setIsMyClubsActive] = useState(false);
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);
  const [fixtures, setFixtures] = useState<MatchRecord[]>([]);
  const [liveMatches, setLiveMatches] = useState<MatchRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [mounted, setMounted] = useState(false);

  const { favoriteClubs, favoriteCount } = useMyClubs();
  const competitions = useCompetitions();
  const supportedClubs = useSupportedClubs();
  const competition = useMemo(
    () => competitions.find((b) => b.competitionId === selectedComp),
    [competitions, selectedComp],
  );

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
    async function load(isSilent = false) {
      if (!isSilent) setIsLoading(true);
      if (selectedSeason === 2026) {
        const [fixData, liveData] = await Promise.all([
          getFixturesByDate(selectedDate),
          getLiveMatches(),
        ]);
        if (!isCancelled) {
          setFixtures(fixData);
          setLiveMatches(liveData);
          if (!isSilent) setIsLoading(false);
        }
      } else {
        const archiveData = await getMatchesBySeason(
          selectedSeason,
          selectedComp !== 'ALL' ? selectedComp : undefined,
          undefined,
          100,
        );
        if (!isCancelled) {
          setFixtures(archiveData);
          setLiveMatches([]);
          if (!isSilent) setIsLoading(false);
        }
      }
    }
    load(false);
    if (selectedSeason === 2026) {
      const interval = setInterval(() => load(true), 10_000);
      return () => {
        isCancelled = true;
        clearInterval(interval);
      };
    }
  }, [selectedDate, selectedSeason, selectedComp]);

  const filteredFixtures = useMemo(() => {
    const seen = new Set<string>();
    const list: MatchRecord[] = [];
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
    const q = searchQuery.trim().toLowerCase();
    return list.filter((m) => {
      if (q) {
        const home = (m.home_team_name || m.home_team || '').toLowerCase();
        const away = (m.away_team_name || m.away_team || '').toLowerCase();
        const comp = (m.competition_name || '').toLowerCase();
        if (!home.includes(q) && !away.includes(q) && !comp.includes(q)) return false;
      }
      if (selectedSeason === 2026) {
        const isFinished = m.status === 'FINISHED';
        const isLive = m.status === 'IN_PLAY' || m.status === 'PAUSED' || m.status === 'LIVE';
        if (activeTab === 'LIVE' && !isLive) return false;
        if (activeTab === 'RESULTS' && !isFinished) return false;
        if (activeTab === 'UPCOMING' && (isFinished || isLive)) return false;
      }
      if (isMyClubsActive && favoriteClubs.size > 0) {
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs, supportedClubs))
          return false;
      } else if (selectedComp !== 'ALL' && competition) {
        if (
          m.competition_id !== selectedComp &&
          !isLeagueMatch(competition.name, m.competition_name)
        )
          return false;
      }
      return true;
    });
  }, [
    fixtures,
    liveMatches,
    activeTab,
    selectedSeason,
    isMyClubsActive,
    favoriteClubs,
    selectedComp,
    competition,
    supportedClubs,
    searchQuery,
  ]);

  const groupedFixtures = useMemo(
    () => groupMatchesByCompetition(filteredFixtures, competitions),
    [filteredFixtures, competitions],
  );

  const tabCounts = useMemo(() => {
    const all =
      fixtures.length + liveMatches.filter((m) => !fixtures.some((f) => f.id === m.id)).length;
    const live = liveMatches.length;
    const results = fixtures.filter((m) => m.status === 'FINISHED').length;
    const upcoming = fixtures.filter(
      (m) => !['FINISHED', 'IN_PLAY', 'PAUSED', 'LIVE'].includes(m.status),
    ).length;
    return { ALL: all, LIVE: live, RESULTS: results, UPCOMING: upcoming };
  }, [fixtures, liveMatches]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6">
      <TopAdSenseLeaderboard />

      {/* Stitch Header & Status Pills */}
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-white/10 pb-5">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="w-2 h-2 rounded-full bg-[#DFFF00] animate-pulse" />
            <span className="font-tech text-xs uppercase tracking-widest text-[#DFFF00] font-bold">
              Direct Live Telemetry Engine {selectedSeason - 1}/{String(selectedSeason).slice(-2)}
            </span>
          </div>
          <h1 className="font-scoreboard text-3xl sm:text-5xl uppercase text-white tracking-wide leading-none">
            SCORES &amp; MATCH SCHEDULES
          </h1>
          <p className="font-tech text-xs sm:text-sm text-white/60 mt-1">
            Real-time pitch telemetry, official referee assignments, and instantaneous European broadcast VOD links.
          </p>
        </div>

        {/* Status Filter Pills */}
        <div className="flex flex-wrap items-center gap-2">
          {STATUS_TABS.map((t) => {
            const isActive = activeTab === t.id;
            const count = tabCounts[t.id];
            return (
              <button
                key={t.id}
                type="button"
                onClick={() => setActiveTab(t.id)}
                className={`px-4 py-2 rounded-full font-tech text-xs font-bold uppercase tracking-wider transition-all flex items-center gap-2 cursor-pointer ${
                  isActive
                    ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_15px_rgba(223,255,0,0.25)]'
                    : 'bg-[#1B1B1F] hover:bg-[#292A2E] text-white/80 border border-white/10'
                }`}
              >
                {t.id === 'LIVE' && (
                  <span
                    className={`w-2 h-2 rounded-full ${
                      isActive ? 'bg-[#0A0A0C]' : 'bg-red-500 animate-ping'
                    }`}
                  />
                )}
                <span>{t.label}</span>
                <span
                  className={`px-1.5 py-0.5 rounded-full text-[10px] ${
                    isActive ? 'bg-black/20 text-[#0A0A0C]' : 'bg-white/10 text-white/70'
                  }`}
                >
                  {count}
                </span>
              </button>
            );
          })}
        </div>
      </header>

      <FixturesFilterBar
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        competitions={competitions}
        selectedComp={selectedComp}
        isMyClubsActive={isMyClubsActive}
        favoriteCount={favoriteCount}
        mounted={mounted}
        onSelectCompetition={(val) => {
          setIsMyClubsActive(false);
          setSelectedComp(val);
        }}
        onToggleMyClubs={() => {
          if (favoriteCount === 0) setIsMyClubsModalOpen(true);
          else setIsMyClubsActive(!isMyClubsActive);
        }}
      />

      <FixturesDateCarousel
        selectedDate={selectedDate}
        onSelectDate={setSelectedDate}
        onPrevDay={() => {
          const d = new Date(selectedDate);
          d.setDate(d.getDate() - 1);
          setSelectedDate(d.toISOString().split('T')[0]);
        }}
        onNextDay={() => {
          const d = new Date(selectedDate);
          d.setDate(d.getDate() + 1);
          setSelectedDate(d.toISOString().split('T')[0]);
        }}
      />

      {liveMatches.length > 0 && activeTab !== 'RESULTS' && activeTab !== 'UPCOMING' && (
        <LiveMatchSection liveMatches={liveMatches} />
      )}

      <FixturesGroupList
        isLoading={isLoading}
        totalCount={filteredFixtures.length}
        groups={groupedFixtures}
      />

      <section className="rounded-2xl bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] border border-white/10 p-5 sm:p-6 shadow-xl flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="space-y-1">
          <span className="text-[10px] font-tech font-bold uppercase tracking-wider text-[#DFFF00]">
            Native Android App • Target SDK 37
          </span>
          <h3 className="font-scoreboard text-xl sm:text-2xl text-white uppercase tracking-wider leading-none">
            Get Real-Time Goal Alerts &amp; Spoiler Guard on Mobile
          </h3>
          <p className="text-xs text-white/60 font-tech">
            Instant push notifications for kickoff, goals, and verified full-time match highlights.
          </p>
        </div>
        <GooglePlayBadge height={40} />
      </section>

      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={() => setIsMyClubsModalOpen(false)} />
    </div>
  );
}

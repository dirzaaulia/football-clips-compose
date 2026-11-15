'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { getFixturesByDate, getLiveMatches, getMatchesBySeason, MatchRecord } from '@/lib/database';
import { useCompetitions, isLeagueMatch } from '@/hooks/useCompetitions';
import { isMatchForFavoriteClubs, useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import { Star, Trophy } from 'lucide-react';
import Image from 'next/image';
import FixtureItemCard from '@/components/fixtures/FixtureItemCard';
import FixturesPartnerBanner from '@/components/fixtures/FixturesPartnerBanner';
import FixturesDateCarousel from '@/components/fixtures/FixturesDateCarousel';
import FixturesSkeleton from '@/components/fixtures/FixturesSkeleton';
import FixturesInFeedAd from '@/components/fixtures/FixturesInFeedAd';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
import CompetitionLogo from '@/components/common/CompetitionLogo';
import { useSeason } from '@/hooks/useSeason';

type FixtureTab = 'ALL' | 'LIVE' | 'RESULTS' | 'UPCOMING';

export default function FixturesPage() {
  const { selectedSeason } = useSeason();
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
  const competitions = useCompetitions();
  const supportedClubs = useSupportedClubs();
  const competition = useMemo(() => competitions.find((b) => b.competitionId === selectedComp), [competitions, selectedComp]);

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
        const [fixData, liveData] = await Promise.all([getFixturesByDate(selectedDate), getLiveMatches()]);
        if (!isCancelled) {
          setFixtures(fixData);
          setLiveMatches(liveData);
          if (!isSilent) setIsLoading(false);
        }
      } else {
        const archiveData = await getMatchesBySeason(selectedSeason, selectedComp !== 'ALL' ? selectedComp : undefined, undefined, 100);
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
      return () => { isCancelled = true; clearInterval(interval); };
    }
  }, [selectedDate, selectedSeason, selectedComp]);

  const changeDay = (delta: number) => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + delta);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const filteredFixtures = useMemo(() => {
    const list: MatchRecord[] = [];
    const seen = new Set<string>();
    if (activeTab === 'ALL') {
      liveMatches.forEach((m) => { seen.add(m.id); list.push(m); });
    }
    fixtures.forEach((m) => { if (!seen.has(m.id)) { seen.add(m.id); list.push(m); } });

    return list.filter((m) => {
      if (selectedSeason === 2026) {
        const isFinished = m.status === 'FINISHED';
        const isLive = m.status === 'IN_PLAY' || m.status === 'PAUSED' || m.status === 'LIVE';
        if (activeTab === 'LIVE' && !isLive) return false;
        if (activeTab === 'RESULTS' && !isFinished) return false;
        if (activeTab === 'UPCOMING' && (isFinished || isLive)) return false;
      }
      if (isMyClubsActive && favoriteClubs.size > 0) {
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs, supportedClubs)) return false;
      } else if (selectedComp !== 'ALL' && competition) {
        if (m.competition_id !== selectedComp && !isLeagueMatch(competition.name, m.competition_name)) return false;
      }
      return true;
    });
  }, [fixtures, liveMatches, activeTab, selectedSeason, isMyClubsActive, favoriteClubs, selectedComp, competition, supportedClubs]);

  const groupedFixtures = useMemo(() => {
    const map = new Map<string, { competitionId: string; competitionName: string; logo?: string; matches: MatchRecord[] }>();
    filteredFixtures.forEach((m) => {
      const matchedLeague = competitions.find((b) => b.competitionId !== 'ALL' && (b.competitionId === m.competition_id || b.name === m.competition_name || isLeagueMatch(b.name, m.competition_name)));
      if (!matchedLeague && !m.competition_id) return;
      const normName = matchedLeague?.name || m.competition_name || m.competition_id;
      if (!map.has(normName)) {
        map.set(normName, { competitionId: matchedLeague?.competitionId || m.competition_id, competitionName: normName, logo: matchedLeague?.logo, matches: [] });
      }
      map.get(normName)!.matches.push(m);
    });
    return Array.from(map.values());
  }, [filteredFixtures, competitions]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6">
      {/* Top Sponsored Partner Spotlight */}
      <FixturesPartnerBanner onExplore={() => window.open('https://www.playstation.com', '_blank')} />

      {/* Header & Season Filter */}
      <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <span className="text-[10px] font-tech uppercase tracking-widest text-[#DFFF00] font-bold">
            DIRECT LIVE TELEMETRY ENGINE 2026/27
          </span>
          <h1 className="font-scoreboard text-3xl sm:text-4xl uppercase text-white tracking-wide leading-none mt-1">
            SCORES &amp; MATCH SCHEDULES
          </h1>
          <p className="font-tech text-xs sm:text-sm text-white/50 mt-1">
            Live real-time match telemetry, official fixtures, and verified European broadcast outcomes.
          </p>
        </div>
        <SeasonSelector variant="pill" className="self-start sm:self-auto" />
      </header>

      {/* Date Scroller Carousel */}
      <FixturesDateCarousel
        selectedDate={selectedDate}
        onSelectDate={setSelectedDate}
        onPrevDay={() => changeDay(-1)}
        onNextDay={() => changeDay(1)}
      />

      {/* Filter Tabs & Competition Controls */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-white/5">
        <div className="flex items-center gap-1.5 bg-[#121316] p-1 rounded-xl border border-white/10">
          {(['ALL', 'LIVE', 'RESULTS', 'UPCOMING'] as FixtureTab[]).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                activeTab === tab
                  ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40 shadow-sm'
                  : 'text-white/60 hover:text-white'
              }`}
            >
              {tab === 'ALL' ? 'All Matches' : tab}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <CustomDropdown<string>
            items={competitions.map((c) => ({
              value: c.competitionId || 'ALL',
              label: c.name,
              shortLabel: c.shortName || c.name,
              icon: c.logo ? (
                <div className="relative w-4 h-4 rounded bg-white/10 p-0.5 flex items-center justify-center shrink-0 border border-white/10">
                  <CompetitionLogo src={c.logo} name={c.name} />
                </div>
              ) : (
                <Trophy className="w-3.5 h-3.5 text-[#DFFF00]" />
              ),
            }))}
            value={isMyClubsActive ? '' : selectedComp}
            onChange={(val) => { setIsMyClubsActive(false); setSelectedComp(val); }}
            prefix="League"
            variant="compact"
          />

          <button
            onClick={() => { if (favoriteCount === 0) setIsMyClubsModalOpen(true); else setIsMyClubsActive(!isMyClubsActive); }}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-tech text-xs font-bold transition-all cursor-pointer ${
              isMyClubsActive && favoriteCount > 0
                ? 'bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/40 shadow-[0_0_10px_rgba(223,255,0,0.2)]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/10'
            }`}
          >
            <Star className={`w-3.5 h-3.5 ${isMyClubsActive && mounted && favoriteCount > 0 ? 'text-[#DFFF00] fill-[#DFFF00]' : 'text-white/40'}`} />
            <span>My Clubs {mounted && favoriteCount > 0 ? `(${favoriteCount})` : ''}</span>
          </button>
        </div>
      </div>

      {activeTab === 'ALL' && liveMatches.length > 0 && <LiveMatchSection liveMatches={liveMatches} className="mb-6" />}

      {isLoading ? (
        <FixturesSkeleton />
      ) : filteredFixtures.length === 0 ? (
        <div className="text-center py-16 bg-[#121316] rounded-2xl border border-white/10 space-y-2">
          <p className="font-tech text-white/50 text-sm">No fixtures scheduled matching the selected filters.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {groupedFixtures.map((group, groupIdx) => (
            <React.Fragment key={group.competitionName}>
              <div className="space-y-3">
                <div className="flex items-center justify-between px-1">
                  <div className="flex items-center gap-2">
                    {group.logo ? (
                      <div className="relative w-6 h-6 rounded-lg bg-white/10 p-1 flex items-center justify-center shrink-0 border border-white/10">
                        <Image src={group.logo} alt={group.competitionName} fill className="object-contain" unoptimized />
                      </div>
                    ) : (
                      <Trophy className="w-5 h-5 text-[#DFFF00]" />
                    )}
                    <h3 className="font-scoreboard text-lg sm:text-xl font-bold text-white uppercase tracking-wider">
                      {group.competitionName}
                    </h3>
                  </div>
                  <span className="font-tech text-xs font-bold text-white/60 bg-[#121316] px-3 py-1 rounded-full border border-white/10">
                    {group.matches.length} matches
                  </span>
                </div>

                <div className="grid grid-cols-1 gap-2.5">
                  {group.matches.map((m) => (
                    <FixtureItemCard key={m.id} match={m} />
                  ))}
                </div>
              </div>

              {(groupIdx + 1) % 2 === 0 && groupIdx < groupedFixtures.length - 1 && (
                <FixturesInFeedAd />
              )}
            </React.Fragment>
          ))}
        </div>
      )}

      <MyClubsModal isOpen={isMyClubsModalOpen} onClose={() => setIsMyClubsModalOpen(false)} />
    </div>
  );
}

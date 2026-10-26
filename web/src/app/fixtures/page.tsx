'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { getFixturesByDate, getLiveMatches, getMatchesBySeason, MatchRecord } from '@/lib/database';
import { useCompetitions, isLeagueMatch } from '@/hooks/useCompetitions';
import { isMatchForFavoriteClubs, useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useMyClubs } from '@/hooks/useMyClubs';
import { Star, Trophy } from 'lucide-react';
import Image from 'next/image';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import FixtureItemCard from '@/components/fixtures/FixtureItemCard';
import LiveMatchSection from '@/components/live/LiveMatchSection';
import MyClubsModal from '@/components/clubs/MyClubsModal';
import FixturesDateAndTabs, { FixtureTab } from '@/components/fixtures/FixturesDateAndTabs';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
import CompetitionLogo from '@/components/common/CompetitionLogo';
import { useSeason } from '@/hooks/useSeason';

export default function FixturesPage() {
  const { selectedSeason, currentOption } = useSeason();
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
          100
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
      // Auto-refresh every 10s silently to keep live scores real-time without layout flickers
      const interval = setInterval(() => load(true), 10_000);
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

  const competitions = useCompetitions();
  const supportedClubs = useSupportedClubs();
  const competition = competitions.find((b) => b.competitionId === selectedComp);

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
        if (!isMatchForFavoriteClubs(m.home_team, m.away_team, favoriteClubs, supportedClubs)) return false;
      } else if (selectedComp !== 'ALL' && competition) {
        const matchComp = m.competition_id === selectedComp || isLeagueMatch(competition.name, m.competition_name);
        if (!matchComp) return false;
      }

      return true;
    });
  }, [allCandidateMatches, activeTab, isMyClubsActive, favoriteClubs, selectedComp, competition, supportedClubs, selectedSeason]);

  const groupedFixtures = useMemo(() => {
    const map = new Map<
      string,
      {
        competitionId: string;
        competitionName: string;
        logo?: string;
        matches: MatchRecord[];
      }
    >();

    filteredFixtures.forEach((m) => {
      const rawName = m.competition_name || m.competition_id || 'Other Competitions';
      const normName = rawName;

      if (!map.has(normName)) {
        const matchedLeague = competitions.find(
          (b) => b.competitionId !== 'ALL' && (b.name === normName || isLeagueMatch(b.name, normName))
        );
        map.set(normName, {
          competitionId: m.competition_id || 'OTHER',
          competitionName: normName,
          logo: matchedLeague?.logo,
          matches: [],
        });
      }

      map.get(normName)!.matches.push(m);
    });

    return Array.from(map.values());
  }, [filteredFixtures]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 2xl:px-10 py-6">
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">Scores & Match Schedules</h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">Live scores, scheduled fixtures, and verified results.</p>
        </div>

        {/* Global Synchronized Season Switcher */}
        <SeasonSelector variant="pill" className="self-start sm:self-auto" />
      </header>

      {/* Control Deck */}
      <FixturesDateAndTabs
        selectedSeason={selectedSeason}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        formattedDate={formattedDate}
        changeDay={changeDay}
        archiveFilter={archiveFilter}
        setArchiveFilter={setArchiveFilter}
        currentOption={currentOption}
      />

      {/* Row 2: Competition Dropdown & My Clubs Quick Toggle (Compact & Space Efficient) */}
      <div className="flex flex-wrap items-center justify-between gap-2.5 mb-5">
        <div className="flex items-center gap-2">
          {/* League Dropdown Selector */}
          <CustomDropdown<string>
            items={competitions.map((c) => ({
              value: c.competitionId || 'ALL',
              label: c.name,
              shortLabel: c.shortName || c.name,
              icon: c.logo ? (
                <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
                  <div className="relative w-full h-full">
                    <CompetitionLogo src={c.logo} name={c.name} />
                  </div>
                </div>
              ) : (
                <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
              ),
            }))}
            value={isMyClubsActive ? '' : selectedComp}
            onChange={(val) => {
              setIsMyClubsActive(false);
              setSelectedComp(val);
            }}
            leadingIcon={
              competition?.logo ? (
                <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
                  <div className="relative w-full h-full">
                    <CompetitionLogo src={competition.logo} name={competition.name} />
                  </div>
                </div>
              ) : (
                <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />
              )
            }
            prefix="League"
            variant="compact"
            ariaLabel="Filter fixtures by competition"
          />

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
          <span className="font-bold text-white bg-[#1E2227] border border-[#334155]/60 px-2.5 py-1 rounded-lg truncate max-w-[220px]">
            {isMyClubsActive ? `My Clubs (${favoriteCount})` : (competition?.name || 'All Competitions')}
          </span>
        </div>
      </div>

      {/* Pinned Live Match Section when in ALL mode */}
      {activeTab === 'ALL' && liveMatches.length > 0 && (
        <LiveMatchSection liveMatches={liveMatches} className="mb-6" />
      )}

      {/* Fixtures List */}
      {isLoading ? (
        <div className="space-y-4">
          {[...Array(2)].map((_, groupIdx) => (
            <div key={groupIdx} className="space-y-2">
              <div className="h-5 w-40 bg-[#1E2227] rounded-md animate-pulse" />
              <div className="bg-[#181C20] border border-[#334155]/70 rounded-2xl divide-y divide-[#334155]/40 overflow-hidden">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="h-12 bg-[#1E2227]/50 animate-pulse" />
                ))}
              </div>
            </div>
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
        <div className="space-y-5">
          {groupedFixtures.map((group) => (
            <div key={group.competitionName} className="space-y-2">
              {/* League Header */}
              <div className="flex items-center justify-between px-1">
                <div className="flex items-center gap-2">
                  {group.logo ? (
                    <div className="relative w-6 h-6 rounded-full bg-white p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-slate-200/20">
                      <div className="relative w-full h-full">
                        <Image
                          src={group.logo}
                          alt={group.competitionName}
                          fill
                          className="object-contain"
                          unoptimized
                        />
                      </div>
                    </div>
                  ) : (
                    <Trophy className="w-4 h-4 text-[#7098F8]" />
                  )}
                  <h3 className="text-xs sm:text-sm font-black text-white uppercase tracking-wider">
                    {group.competitionName}
                  </h3>
                </div>
                <span className="text-[10px] font-bold text-[#94A3B8] bg-[#1E2227] px-2 py-0.5 rounded-full border border-[#334155]/60">
                  {group.matches.length} {group.matches.length === 1 ? 'match' : 'matches'}
                </span>
              </div>

              {/* Fixture Items Connected Container */}
              <div className="bg-[#181C20] border border-[#334155]/70 rounded-2xl overflow-hidden divide-y divide-[#334155]/40 shadow-sm">
                {group.matches.map((m) => (
                  <FixtureItemCard key={m.id} match={m} showLeagueHeader={false} />
                ))}
              </div>
            </div>
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

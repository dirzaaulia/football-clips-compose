'use client';

import React, { useState, useEffect, useMemo } from 'react';
import {
  getStandingsByCompetition,
  getKnockoutMatchesByCompetition,
  StandingRecord,
  MatchRecord,
} from '@/lib/database';
import { fetchLeaguePlayerStats, LeagueStatsPayload } from '@/lib/free-stats';
import KnockoutBracketView from '@/components/standings/KnockoutBracketView';
import TopAdSenseLeaderboard from '@/components/ads/TopAdSenseLeaderboard';
import StandingsTable from '@/components/standings/StandingsTable';
import StandingsTableSkeleton from '@/components/standings/StandingsTableSkeleton';
import StandingsPlayerStatsTab from '@/components/standings/StandingsPlayerStatsTab';
import StandingsClubAnalyticsTab from '@/components/standings/StandingsClubAnalyticsTab';
import StandingsBottomWidgets from '@/components/standings/StandingsBottomWidgets';
import StandingsLegend from '@/components/standings/StandingsLegend';
import { useSeason } from '@/hooks/useSeason';
import { useCompetitions } from '@/hooks/useCompetitions';
import CompetitionLogo from '@/components/common/CompetitionLogo';

type StandingsViewTab = 'TABLE' | 'PLAYER_STATS' | 'CLUB_STATS' | 'BRACKETS';

export default function StandingsPage() {
  const { selectedSeason } = useSeason();
  const competitions = useCompetitions();
  const [activeView, setActiveView] = useState<StandingsViewTab>('TABLE');
  const [selectedCompCode, setSelectedCompCode] = useState('PL');
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [knockoutMatches, setKnockoutMatches] = useState<MatchRecord[]>([]);
  const [playerStats, setPlayerStats] = useState<LeagueStatsPayload | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isStatsLoading, setIsStatsLoading] = useState(true);

  const selectableLeagues = useMemo(
    () => competitions.filter((c) => c.competitionId && c.competitionId !== 'ALL'),
    [competitions],
  );

  const selectedComp = useMemo(
    () => selectableLeagues.find((c) => c.competitionId === selectedCompCode) || selectableLeagues[0],
    [selectableLeagues, selectedCompCode],
  );

  useEffect(() => {
    let isCancelled = false;
    async function loadStandings() {
      setIsLoading(true);
      const [standingsData, bracketData] = await Promise.all([
        getStandingsByCompetition(selectedCompCode, selectedSeason),
        getKnockoutMatchesByCompetition(selectedCompCode, selectedSeason),
      ]);
      if (!isCancelled) {
        setStandings(standingsData);
        setKnockoutMatches(bracketData);
        setIsLoading(false);
      }
    }
    loadStandings();
    return () => {
      isCancelled = true;
    };
  }, [selectedCompCode, selectedSeason]);

  useEffect(() => {
    let isCancelled = false;
    async function loadPlayerStats() {
      setIsStatsLoading(true);
      const data = await fetchLeaguePlayerStats(selectedCompCode);
      if (!isCancelled) {
        setPlayerStats(data);
        setIsStatsLoading(false);
      }
    }
    loadPlayerStats();
    return () => {
      isCancelled = true;
    };
  }, [selectedCompCode]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6">
      <TopAdSenseLeaderboard />

      {/* Stitch Header & View Switcher */}
      <header className="flex flex-col lg:flex-row lg:items-end justify-between gap-4 border-b border-white/10 pb-5">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-block w-2 h-2 rounded-full bg-[#DFFF00] animate-pulse" />
            <span className="font-tech text-xs uppercase tracking-widest text-[#DFFF00] font-bold">
              Live Broadcast Feed • Standings &amp; Statistical Telemetry
            </span>
          </div>
          <h1 className="font-scoreboard text-3xl sm:text-5xl uppercase text-white tracking-wide leading-none">
            LEAGUE STANDINGS &amp; STATS HUB
          </h1>
          <p className="font-tech text-xs sm:text-sm text-white/60 mt-1">
            Official {selectedSeason - 1}/{String(selectedSeason).slice(-2)} European tables, Golden Boot leaderboards, playmaker assists, and club xPTS telemetry.
          </p>
        </div>

        {/* Segmented View Switcher */}
        <div className="flex flex-wrap items-center gap-1.5 bg-[#1B1B1F] p-1 rounded-xl border border-white/10 self-start lg:self-auto">
          {[
            { id: 'TABLE' as StandingsViewTab, label: 'League Table' },
            { id: 'PLAYER_STATS' as StandingsViewTab, label: 'Player Stats' },
            { id: 'CLUB_STATS' as StandingsViewTab, label: 'Club Telemetry' },
            { id: 'BRACKETS' as StandingsViewTab, label: 'Knockout Brackets' },
          ].map((tab) => {
            const active = activeView === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveView(tab.id)}
                className={`px-3.5 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                  active
                    ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                    : 'text-white/65 hover:text-white'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </header>

      {/* Stitch League Switcher Pills & Zone Legend Bar */}
      <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <div className="flex items-center gap-2 overflow-x-auto no-scrollbar pb-1">
          {selectableLeagues.slice(0, 9).map((c) => {
            const code = c.competitionId || 'PL';
            const isSelected = selectedCompCode === code;
            return (
              <button
                key={code}
                type="button"
                onClick={() => setSelectedCompCode(code)}
                className={`px-4 py-2 rounded-xl font-tech text-xs font-bold uppercase tracking-wider flex items-center gap-2 shrink-0 transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_15px_rgba(223,255,0,0.2)]'
                    : 'bg-[#1B1B1F] hover:bg-[#292A2E] text-white/80 border border-white/10'
                }`}
              >
                {c.logo && (
                  <div className="relative w-4 h-4 rounded bg-black/20 p-0.5 flex items-center justify-center shrink-0">
                    <CompetitionLogo src={c.logo} name={c.name} />
                  </div>
                )}
                <span>{c.shortName || c.name}</span>
              </button>
            );
          })}
        </div>

        {/* Dynamic Competition Zone Legend Strip */}
        <StandingsLegend competitionCode={selectedCompCode} />
      </div>

      {/* Main Content Area by Active View */}
      {activeView === 'PLAYER_STATS' ? (
        <StandingsPlayerStatsTab stats={playerStats} isLoading={isStatsLoading} />
      ) : activeView === 'CLUB_STATS' ? (
        <StandingsClubAnalyticsTab
          standings={standings}
          competitionCode={selectedCompCode}
        />
      ) : activeView === 'BRACKETS' ? (
        <KnockoutBracketView
          matches={knockoutMatches}
          competitionName={selectedComp?.name || 'Tournament'}
        />
      ) : isLoading ? (
        <StandingsTableSkeleton />
      ) : standings.length === 0 ? (
        <div className="text-center py-16 bg-[#1B1B1F] rounded-xl border border-white/10">
          <p className="font-tech text-white/50 text-sm">
            No standings data available for this competition season.
          </p>
        </div>
      ) : (
        <div className="space-y-8">
          <StandingsTable standings={standings} competitionCode={selectedCompCode} />
          <StandingsBottomWidgets
            standings={standings}
            playerStats={playerStats}
            onSelectUcl={() => setSelectedCompCode('CL')}
            onOpenPlayerStats={() => setActiveView('PLAYER_STATS')}
          />
        </div>
      )}
    </div>
  );
}

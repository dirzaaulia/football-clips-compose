'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { Trophy } from 'lucide-react';
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
import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
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

  const isInternational = useMemo(
    () => selectedComp?.type === 'international' || ['UNL', 'WCQ_CONMEBOL', 'WCQ_AFC', 'WCQ_UEFA', 'CNL', 'WCQ_CONCACAF', 'WCQ_CAF', 'FRI'].includes(selectedCompCode),
    [selectedComp, selectedCompCode],
  );

  const isCup = useMemo(
    () => !isInternational && (selectedComp?.type === 'cup' || ['FAC', 'EFL', 'CDR', 'DFB', 'CIT'].includes(selectedCompCode)),
    [isInternational, selectedComp, selectedCompCode],
  );

  const isHybrid = useMemo(
    () => selectedComp?.type === 'hybrid' || ['CL', 'EL', 'ECL', 'UNL', 'CNL', 'WCQ_CONMEBOL', 'WCQ_AFC', 'WCQ_UEFA'].includes(selectedCompCode),
    [selectedComp, selectedCompCode],
  );

  const hasBrackets = isCup || isHybrid;
  const hasTable = !isCup;

  useEffect(() => {
    if (isCup && activeView === 'TABLE') {
      setActiveView('BRACKETS');
    } else if (!hasBrackets && activeView === 'BRACKETS') {
      setActiveView('TABLE');
    }
  }, [isCup, hasBrackets, activeView]);

  const availableTabs = useMemo(() => {
    const tabs: { id: StandingsViewTab; label: string }[] = [];
    if (hasTable) {
      tabs.push({
        id: 'TABLE',
        label: isInternational ? 'Tournament Standings' : (isHybrid ? 'League Phase (36 Teams)' : 'League Table'),
      });
    }
    if (hasBrackets) {
      tabs.push({ id: 'BRACKETS', label: isInternational ? 'Tournament Stages' : 'Knockout Brackets' });
    }
    tabs.push({ id: 'PLAYER_STATS', label: 'Player Stats' });
    tabs.push({ id: 'CLUB_STATS', label: 'Club Telemetry' });
    return tabs;
  }, [hasTable, hasBrackets, isHybrid, isInternational]);

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

        {/* Dynamic Segmented View Switcher */}
        <div className="flex flex-wrap items-center gap-1.5 bg-[#1B1B1F] p-1 rounded-xl border border-white/10 self-start lg:self-auto">
          {availableTabs.map((tab) => {
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

      {/* Filter Toolbar: Unified League Dropdown + Season Selector + Dynamic Legend */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-2.5">
          <div className="w-full sm:w-64">
            <CustomDropdown<string>
              items={selectableLeagues.map((c) => ({
                value: c.competitionId || 'PL',
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
              value={selectedCompCode}
              onChange={setSelectedCompCode}
              prefix="League"
              variant="compact"
            />
          </div>
          <SeasonSelector variant="compact" />
        </div>

        {/* Dynamic Competition Zone Legend Strip (Shown only for league table view) */}
        {activeView === 'TABLE' && hasTable && (
          <StandingsLegend competitionCode={selectedCompCode} />
        )}
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

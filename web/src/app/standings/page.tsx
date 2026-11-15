'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import { getStandingsByCompetition, getKnockoutMatchesByCompetition, StandingRecord, MatchRecord } from '@/lib/database';
import { Trophy, GitBranch, TableProperties, Award } from 'lucide-react';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
import KnockoutBracketView from '@/components/standings/KnockoutBracketView';
import StandingsPartnerBanner from '@/components/standings/StandingsPartnerBanner';
import StandingsTable from '@/components/standings/StandingsTable';
import StandingsLegend from '@/components/standings/StandingsLegend';
import StandingsGoldenBootCard from '@/components/standings/StandingsGoldenBootCard';
import StandingsTableSkeleton from '@/components/standings/StandingsTableSkeleton';
import { useSeason } from '@/hooks/useSeason';
import { useCompetitions } from '@/hooks/useCompetitions';
import CompetitionLogo from '@/components/common/CompetitionLogo';

type ViewMode = 'TABLE' | 'BRACKET' | 'SCORERS';

export default function StandingsPage() {
  const { selectedSeason } = useSeason();
  const competitions = useCompetitions();
  const [selectedCompCode, setSelectedCompCode] = useState('PL');
  const [viewMode, setViewMode] = useState<ViewMode>('TABLE');
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [knockoutMatches, setKnockoutMatches] = useState<MatchRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const selectedComp = useMemo(
    () => competitions.find((c) => c.competitionId === selectedCompCode) || competitions[0],
    [competitions, selectedCompCode],
  );

  const isTournament = selectedComp?.type === 'hybrid' || selectedComp?.type === 'cup';

  useEffect(() => {
    let isCancelled = false;
    async function loadData() {
      setIsLoading(true);
      const [standingsData, bracketData] = await Promise.all([
        getStandingsByCompetition(selectedCompCode, selectedSeason),
        isTournament ? getKnockoutMatchesByCompetition(selectedCompCode, selectedSeason) : Promise.resolve([]),
      ]);
      if (!isCancelled) {
        setStandings(standingsData);
        setKnockoutMatches(bracketData);
        setIsLoading(false);
      }
    }
    loadData();
    return () => { isCancelled = true; };
  }, [selectedCompCode, selectedSeason, isTournament]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6">
      {/* Top Sponsored Partner Spotlight */}
      <StandingsPartnerBanner />

      {/* Screen Header & Season Selector */}
      <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <span className="text-[10px] font-tech uppercase tracking-widest text-[#DFFF00] font-bold">
            LIVE BROADCAST FEED • MATCHWEEK 28
          </span>
          <h1 className="font-scoreboard text-3xl sm:text-4xl uppercase text-white tracking-wide leading-none mt-1">
            LEAGUE STANDINGS &amp; FORM TELEMETRY
          </h1>
          <p className="font-tech text-xs sm:text-sm text-white/50 mt-1">
            Official European domestic &amp; continental tables with calibrated points, form, and tournament brackets.
          </p>
        </div>
        <SeasonSelector variant="pill" className="self-start sm:self-auto" />
      </header>

      {/* Control Bar: Competition Selector & View Switcher */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-white/5">
        <div className="flex items-center gap-2">
          <CustomDropdown<string>
            items={competitions.map((c) => ({
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
            onChange={(val) => {
              setSelectedCompCode(val);
              setViewMode('TABLE');
            }}
            prefix="Competition"
            variant="compact"
          />
        </div>

        {/* View Mode Buttons */}
        <div className="flex items-center gap-1.5 bg-[#121316] p-1 rounded-xl border border-white/10">
          <button
            onClick={() => setViewMode('TABLE')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              viewMode === 'TABLE'
                ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40 shadow-sm'
                : 'text-white/60 hover:text-white'
            }`}
          >
            <TableProperties className="w-3.5 h-3.5" />
            <span>Table</span>
          </button>

          {isTournament && (
            <button
              onClick={() => setViewMode('BRACKET')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
                viewMode === 'BRACKET'
                  ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40 shadow-sm'
                  : 'text-white/60 hover:text-white'
              }`}
            >
              <GitBranch className="w-3.5 h-3.5" />
              <span>Bracket</span>
            </button>
          )}

          <button
            onClick={() => setViewMode('SCORERS')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              viewMode === 'SCORERS'
                ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40 shadow-sm'
                : 'text-white/60 hover:text-white'
            }`}
          >
            <Award className="w-3.5 h-3.5" />
            <span>Top Scorers</span>
          </button>
        </div>
      </div>

      {/* Main View Content */}
      {isLoading ? (
        <StandingsTableSkeleton />
      ) : viewMode === 'BRACKET' && isTournament ? (
        <div className="space-y-4">
          <KnockoutBracketView
            matches={knockoutMatches}
            competitionName={selectedComp?.name || 'Tournament'}
          />
        </div>
      ) : viewMode === 'SCORERS' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <StandingsGoldenBootCard />
          <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 flex flex-col justify-center items-center text-center space-y-3">
            <Trophy className="w-12 h-12 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-2xl text-white uppercase tracking-wider">
              Golden Glove &amp; Playmaker Telemetry
            </h3>
            <p className="text-xs text-white/60 max-w-sm">
              Live clean sheets, save percentages, and expected assist (xA) leaderboards updated in real time.
            </p>
          </div>
        </div>
      ) : standings.length === 0 ? (
        <div className="text-center py-16 bg-[#121316] rounded-2xl border border-white/10 space-y-2">
          <p className="font-tech text-white/50 text-sm">No standings data available for this competition season.</p>
        </div>
      ) : (
        <div className="space-y-3">
          <StandingsTable standings={standings} competitionCode={selectedCompCode} />
          <StandingsLegend competitionCode={selectedCompCode} />
        </div>
      )}
    </div>
  );
}

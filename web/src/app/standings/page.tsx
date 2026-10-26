'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import { getStandingsByCompetition, getKnockoutMatchesByCompetition, StandingRecord, MatchRecord } from '@/lib/database';
import { Trophy, GitBranch, ListFilter, TableProperties, Sparkles } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
import KnockoutBracketView from '@/components/standings/KnockoutBracketView';
import KnockoutRoundsView from '@/components/standings/KnockoutRoundsView';
import { useSeason } from '@/hooks/useSeason';
import { useCompetitions } from '@/hooks/useCompetitions';
import CompetitionLogo from '@/components/common/CompetitionLogo';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface CompetitionDef {
  code: string;
  name: string;
  type: 'league' | 'hybrid' | 'cup';
  category: string;
  logo?: string;
}

const ALL_COMPETITIONS: CompetitionDef[] = [
  // Domestic Leagues
  { code: 'PL', name: 'Premier League', type: 'league', category: 'Domestic Leagues' },
  { code: 'PD', name: 'La Liga', type: 'league', category: 'Domestic Leagues' },
  { code: 'SA', name: 'Serie A', type: 'league', category: 'Domestic Leagues' },
  { code: 'BL1', name: 'Bundesliga', type: 'league', category: 'Domestic Leagues' },
  { code: 'FL1', name: 'Ligue 1', type: 'league', category: 'Domestic Leagues' },
  { code: 'SPL', name: 'Saudi Pro League', type: 'league', category: 'Domestic Leagues' },
  { code: 'MLS', name: 'MLS', type: 'league', category: 'Domestic Leagues' },
  { code: 'DED', name: 'Eredivisie', type: 'league', category: 'Domestic Leagues' },
  { code: 'PPL', name: 'Liga Portugal', type: 'league', category: 'Domestic Leagues' },
  { code: 'IDN', name: 'Liga Indonesia', type: 'league', category: 'Domestic Leagues' },

  // European Hybrid Competitions (League Phase + Knockout Phase)
  { code: 'CL', name: 'UEFA Champions League', type: 'hybrid', category: 'European Competitions' },
  { code: 'EL', name: 'UEFA Europa League', type: 'hybrid', category: 'European Competitions' },
  { code: 'ECL', name: 'UEFA Conference League', type: 'hybrid', category: 'European Competitions' },

  // Knockout Domestic Cups
  { code: 'FAC', name: 'FA Cup', type: 'cup', category: 'Knockout Cups' },
  { code: 'EFL', name: 'EFL Carabao Cup', type: 'cup', category: 'Knockout Cups' },
  { code: 'CDR', name: 'Copa del Rey', type: 'cup', category: 'Knockout Cups' },
  { code: 'DFB', name: 'DFB-Pokal', type: 'cup', category: 'Knockout Cups' },
  { code: 'CIT', name: 'Coppa Italia', type: 'cup', category: 'Knockout Cups' },

  // South American Competitions
  { code: 'LIB', name: 'CONMEBOL Libertadores', type: 'hybrid', category: 'South American Competitions' },
  { code: 'SUD', name: 'CONMEBOL Sudamericana', type: 'cup', category: 'South American Competitions' },
  { code: 'REC', name: 'CONMEBOL Recopa', type: 'cup', category: 'South American Competitions' },
  { code: 'BRA', name: 'Brasileirão Série A', type: 'league', category: 'South American Competitions' },
  { code: 'ARG', name: 'Argentine Liga Profesional', type: 'league', category: 'South American Competitions' },
];

function normalizeTeamNameForDeduplication(name: string): string {
  return name
    .toLowerCase()
    .replace(/\b(fc|afc|cf|sc|bc|ssc|as|ac|rc|ogc|vfb|tsv|sv|cd|ud|ca|rb|us)\b/gi, '')
    .replace(/balompie/gi, '')
    .replace(/calcio/gi, '')
    .replace(/de barcelona/gi, '')
    .replace(/münchen/gi, 'munich')
    .replace(/manchester/gi, 'man')
    .replace(/tottenham hotspur/gi, 'spurs')
    .replace(/tottenham/gi, 'spurs')
    .replace(/wolverhampton wanderers/gi, 'wolves')
    .replace(/wolverhampton/gi, 'wolves')
    .replace(/saint-germain/gi, 'sg')
    .replace(/saint germain/gi, 'sg')
    .replace(/atletico de madrid/gi, 'atletico')
    .replace(/atlético de madrid/gi, 'atletico')
    .replace(/atletico madrid/gi, 'atletico')
    .replace(/atlético madrid/gi, 'atletico')
    .replace(/athletic club/gi, 'bilbao')
    .replace(/athletic bilbao/gi, 'bilbao')
    .replace(/internazionale/gi, 'inter')
    .replace(/inter milan/gi, 'inter')
    .replace(/[^a-z0-9]/gi, '')
    .trim();
}

function deduplicateStandings(standings: StandingRecord[]): StandingRecord[] {
  if (standings.length <= 1) return standings;

  const grouped = new Map<string, StandingRecord[]>();
  for (const item of standings) {
    const key = normalizeTeamNameForDeduplication(item.team_name || String(item.team_id || item.id));
    if (!grouped.has(key)) {
      grouped.set(key, []);
    }
    grouped.get(key)!.push(item);
  }

  const result: StandingRecord[] = [];
  for (const duplicates of grouped.values()) {
    if (duplicates.length === 1) {
      result.push(duplicates[0]);
    } else {
      const best = duplicates.reduce((prev, curr) => {
        let prevScore = 0;
        let currScore = 0;
        if (prev.played_games > 0) prevScore += 50;
        if (curr.played_games > 0) currScore += 50;
        if (prev.crest_url) prevScore += 20;
        if (curr.crest_url) currScore += 20;
        if (prev.points > 0) prevScore += 10;
        if (curr.points > 0) currScore += 10;
        return currScore > prevScore ? curr : prev;
      }, duplicates[0]);
      result.push(best);
    }
  }

  return result
    .sort((a, b) => (a.position || 0) - (b.position || 0))
    .map((team, idx) => ({ ...team, position: idx + 1 }));
}

export default function StandingsPage() {
  const [selectedComp, setSelectedComp] = useState('PL');
  const { selectedSeason } = useSeason();
  const dynamicCompetitions = useCompetitions();
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [knockoutMatches, setKnockoutMatches] = useState<MatchRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // European Hybrid Stage Selector ('table' = 36-team single league phase, 'knockout' = bracket/rounds)
  const [hybridStage, setHybridStage] = useState<'table' | 'knockout'>('table');

  // Knockout View Mode ('bracket' = visual tree, 'rounds' = match list)
  const [knockoutViewMode, setKnockoutViewMode] = useState<'bracket' | 'rounds'>('bracket');

  const competitionList = useMemo<CompetitionDef[]>(() => {
    if (!dynamicCompetitions || dynamicCompetitions.length <= 1) {
      return ALL_COMPETITIONS;
    }
    const dynamicOnly: CompetitionDef[] = dynamicCompetitions
      .filter((c) => c.competitionId && c.competitionId !== 'ALL')
      .map((c) => {
        const fallback = ALL_COMPETITIONS.find((x) => x.code === c.competitionId);
        const resolvedType = (c.type || fallback?.type || 'league') as 'league' | 'hybrid' | 'cup';
        const resolvedCategory = fallback?.category || (
          resolvedType === 'cup' ? 'Knockout Cups' : resolvedType === 'hybrid' ? 'European Competitions' : 'Domestic Leagues'
        );
        return {
          code: c.competitionId!,
          name: c.name,
          type: resolvedType,
          category: resolvedCategory,
          logo: c.logo || (c.competitionId === 'IDN' ? '/logos/competitions/idn-2026.png' : ''),
        };
      });
    return dynamicOnly.length > 0 ? dynamicOnly : ALL_COMPETITIONS;
  }, [dynamicCompetitions]);

  const activeCompDef = useMemo(
    () => competitionList.find((c) => c.code === selectedComp) || competitionList[0] || ALL_COMPETITIONS[0],
    [competitionList, selectedComp]
  );

  const hasFormData = useMemo(
    () => standings.some((t) => t.form && t.form.trim().length > 0),
    [standings]
  );

  useEffect(() => {
    let isMounted = true;
    async function loadData() {
      setIsLoading(true);

      if (activeCompDef.type === 'league') {
        const data = await getStandingsByCompetition(selectedComp, selectedSeason);
        if (isMounted) {
          setStandings(deduplicateStandings(data));
          setKnockoutMatches([]);
          setIsLoading(false);
        }
      } else if (activeCompDef.type === 'hybrid') {
        // European competitions: fetch both table standings and knockout matches
        const [tableData, matchesData] = await Promise.all([
          getStandingsByCompetition(selectedComp, selectedSeason),
          getKnockoutMatchesByCompetition(selectedComp, selectedSeason),
        ]);
        if (isMounted) {
          setStandings(deduplicateStandings(tableData));
          setKnockoutMatches(matchesData);
          setIsLoading(false);
        }
      } else {
        // Cup competitions: fetch knockout tournament matches
        const matchesData = await getKnockoutMatchesByCompetition(selectedComp, selectedSeason);
        if (isMounted) {
          setStandings([]);
          setKnockoutMatches(matchesData);
          setIsLoading(false);
        }
      }
    }

    loadData();
    return () => {
      isMounted = false;
    };
  }, [selectedComp, selectedSeason, activeCompDef.type]);

  // Determine whether to display the points table or the knockout view
  const showKnockoutView = activeCompDef.type === 'cup' || (activeCompDef.type === 'hybrid' && hybridStage === 'knockout');

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 2xl:px-10 py-6">
      {/* Title & Compact Selectors */}
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Trophy className="w-7 h-7 text-[#7098F8]" />
            <span>{activeCompDef.name}</span>
          </h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">
            {activeCompDef.type === 'cup'
              ? 'Official knockout tournament bracket, round fixtures, and decisive goal highlights.'
              : activeCompDef.type === 'hybrid'
              ? 'Official 36-team single league table phase and knockout tournament bracket.'
              : 'Official domestic league table standings, points, form, and goal difference.'}
          </p>
        </div>

        {/* Compact, Intuitive Selectors */}
        <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
          {/* Global Synchronized Season Selector */}
          <SeasonSelector variant="select" />

          {/* 17 Competitions Selector with Category Badges */}
          <CustomDropdown<string>
            items={competitionList.map((l) => ({
              value: l.code,
              label: l.name,
              badge: l.type === 'cup' ? 'Cup' : l.type === 'hybrid' ? 'Hybrid' : 'League',
              badgeVariant: l.type === 'cup' ? 'amber' : l.type === 'hybrid' ? 'primary' : 'gray',
              icon: l.logo ? (
                <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
                  <div className="relative w-full h-full">
                    <CompetitionLogo src={l.logo} name={l.name} />
                  </div>
                </div>
              ) : (
                <Trophy className="w-4 h-4 text-[#7098F8]" />
              ),
            }))}
            value={selectedComp}
            onChange={(c) => {
              setSelectedComp(c);
              const compDef = competitionList.find((x) => x.code === c);
              if (compDef?.type === 'hybrid') setHybridStage('table');
            }}
            leadingIcon={
              activeCompDef.logo ? (
                <div className="relative w-4 h-4 rounded-lg bg-white/[0.08] backdrop-blur-md p-0.5 flex items-center justify-center shrink-0 shadow-sm border border-white/10">
                  <div className="relative w-full h-full">
                    <CompetitionLogo src={activeCompDef.logo} name={activeCompDef.name} />
                  </div>
                </div>
              ) : (
                <Trophy className="w-4 h-4 text-[#7098F8]" />
              )
            }
            prefix="Comp"
            align="right"
            className="w-auto"
            ariaLabel="Select competition"
          />
        </div>
      </header>

      {/* European Hybrid Stage Toggle (League Phase Table vs Knockout Bracket & Rounds) */}
      {activeCompDef.type === 'hybrid' && (
        <div className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-2 rounded-2xl bg-[#181C20] border border-[#334155] shadow-md">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setHybridStage('table')}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                hybridStage === 'table'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <TableProperties className="w-4 h-4" />
              <span>League Phase Table (36 Teams)</span>
            </button>
            <button
              onClick={() => setHybridStage('knockout')}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                hybridStage === 'knockout'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <GitBranch className="w-4 h-4" />
              <span>Knockout Bracket & Rounds</span>
            </button>
          </div>

          {hybridStage === 'knockout' && (
            <div className="flex items-center gap-1.5 self-start sm:self-auto border-t sm:border-t-0 pt-2 sm:pt-0 border-[#334155]/60">
              <button
                onClick={() => setKnockoutViewMode('bracket')}
                className={`p-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer flex items-center gap-1 ${
                  knockoutViewMode === 'bracket' ? 'bg-[#1E3A8A] text-white' : 'text-[#94A3B8] hover:text-white'
                }`}
                title="Bracket Tree View"
              >
                <GitBranch className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Bracket</span>
              </button>
              <button
                onClick={() => setKnockoutViewMode('rounds')}
                className={`p-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer flex items-center gap-1 ${
                  knockoutViewMode === 'rounds' ? 'bg-[#1E3A8A] text-white' : 'text-[#94A3B8] hover:text-white'
                }`}
                title="Rounds List View"
              >
                <ListFilter className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Rounds</span>
              </button>
            </div>
          )}
        </div>
      )}

      {/* Knockout Cups View Mode Toggle (Bracket Tree vs Rounds List) */}
      {activeCompDef.type === 'cup' && (
        <div className="mb-6 flex items-center justify-between p-2 rounded-2xl bg-[#181C20] border border-[#334155] shadow-md">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setKnockoutViewMode('bracket')}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                knockoutViewMode === 'bracket'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <GitBranch className="w-4 h-4" />
              <span>Tournament Bracket Tree</span>
            </button>
            <button
              onClick={() => setKnockoutViewMode('rounds')}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                knockoutViewMode === 'rounds'
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
              }`}
            >
              <ListFilter className="w-4 h-4" />
              <span>Rounds Match List</span>
            </button>
          </div>

          <span className="hidden sm:flex items-center gap-1 text-xs text-[#94A3B8] px-3 font-medium">
            <Sparkles className="w-3.5 h-3.5 text-[#7098F8]" />
            Official Cup Draw & Replays
          </span>
        </div>
      )}

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* View Content Area: Either Knockout Views or Points Table */}
      {showKnockoutView ? (
        <div className="my-6">
          {isLoading ? (
            <div className="py-20 text-center text-[#94A3B8] bg-[#181C20] border border-[#334155] rounded-3xl animate-pulse">
              Loading tournament matches...
            </div>
          ) : knockoutViewMode === 'bracket' ? (
            <KnockoutBracketView matches={knockoutMatches} competitionName={activeCompDef.name} />
          ) : (
            <KnockoutRoundsView matches={knockoutMatches} competitionName={activeCompDef.name} />
          )}
        </div>
      ) : (
        /* Standings Table for Domestic Leagues & European League Phase */
        <div className="bg-[#181C20] border border-[#334155] rounded-3xl overflow-hidden my-6 shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs sm:text-sm text-[#F1F5F9]">
              <thead className="bg-[#101418] text-[11px] uppercase tracking-wider text-[#94A3B8] border-b border-[#334155]">
                <tr>
                  <th className="py-3.5 px-3 sm:px-4 font-bold text-center w-12">Pos</th>
                  <th className="py-3.5 px-4 font-bold">Club</th>
                  <th className="py-3.5 px-3 font-semibold text-center">P</th>
                  <th className="py-3.5 px-3 font-semibold text-center">W</th>
                  <th className="py-3.5 px-3 font-semibold text-center">D</th>
                  <th className="py-3.5 px-3 font-semibold text-center">L</th>
                  <th className="py-3.5 px-3 font-semibold text-center hidden md:table-cell">GD</th>
                  <th className="py-3.5 px-4 font-bold text-center text-white">Pts</th>
                  {hasFormData && (
                    <th className="py-3.5 px-4 font-semibold text-center hidden sm:table-cell">Form</th>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-[#334155]/50 font-medium">
                {isLoading ? (
                  [...Array(10)].map((_, i) => (
                    <tr key={i} className="animate-pulse">
                      <td colSpan={hasFormData ? 9 : 8} className="py-4 px-4 bg-[#1E2227]/30"></td>
                    </tr>
                  ))
                ) : standings.length === 0 ? (
                  <tr>
                    <td colSpan={hasFormData ? 9 : 8} className="py-12 text-center text-[#94A3B8]">
                      No standings table data available for this competition currently.
                    </td>
                  </tr>
                ) : (
                  standings.map((team) => (
                    <tr
                      key={`${team.team_id || team.id}-${team.position}`}
                      className="hover:bg-[#1E2227]/80 transition-colors group"
                    >
                      <td className="py-3 px-3 sm:px-4 text-center">
                        <span
                          className={`inline-flex items-center justify-center w-6 h-6 rounded-full font-bold text-xs ${
                            team.position <= 4
                              ? 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm'
                              : team.position >= (standings.length - 2) && standings.length >= 18
                              ? 'bg-rose-950/40 text-rose-400 border border-rose-800/40'
                              : 'text-[#94A3B8]'
                          }`}
                        >
                          {team.position}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-3">
                          <GlassLogoBadge
                            src={team.crest_url}
                            alt={team.team_name}
                            size={28}
                            imageClassName="object-contain p-0.5"
                          />
                          <span className="font-bold text-white group-hover:text-[#7098F8] transition-colors truncate">
                            {team.team_name}
                          </span>
                        </div>
                      </td>
                      <td className="py-3 px-3 text-center text-[#94A3B8]">{team.played_games}</td>
                      <td className="py-3 px-3 text-center text-[#94A3B8]">{team.won}</td>
                      <td className="py-3 px-3 text-center text-[#94A3B8]">{team.draw}</td>
                      <td className="py-3 px-3 text-center text-[#94A3B8]">{team.lost}</td>
                      <td className="py-3 px-3 text-center text-[#94A3B8] hidden md:table-cell font-mono">
                        {team.goal_difference > 0 ? `+${team.goal_difference}` : team.goal_difference}
                      </td>
                      <td className="py-3 px-4 text-center font-black text-white text-sm sm:text-base">
                        {team.points}
                      </td>
                      {hasFormData && (
                        <td className="py-3 px-4 text-center hidden sm:table-cell">
                          <div className="flex items-center justify-center gap-1">
                            {(team.form || '')
                              .split(',')
                              .slice(-5)
                              .map((f, idx) => {
                                const formChar = f.trim().toUpperCase();
                                const colorClass =
                                  formChar === 'W'
                                    ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                                    : formChar === 'D'
                                    ? 'bg-zinc-700/50 text-zinc-300 border-zinc-600/40'
                                    : 'bg-rose-500/20 text-rose-400 border-rose-500/40';
                                return (
                                  <span
                                    key={idx}
                                    className={`w-5 h-5 rounded flex items-center justify-center text-[10px] font-black border ${colorClass}`}
                                  >
                                    {formChar || '-'}
                                  </span>
                                );
                              })}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

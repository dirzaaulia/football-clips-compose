'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import {
  ALL_SUPPORTED_COMPETITIONS,
  FALLBACK_TACTICAL_PROFILES,
  ClubTacticalProfile,
  fetchLiveStandings,
  standingToTacticalProfile,
} from '@/lib/tools-data';
import { MatchRecord } from '@/lib/database';
import { fetchEspnMatchSummary, EspnH2HMatch } from '@/lib/espn';
import { fetchHistoricalH2HFromDatabase } from '@/lib/free-stats';
import H2HHistoricalClashes from '@/components/tools/H2HHistoricalClashes';
import { Swords } from 'lucide-react';

interface H2HComparatorProps {
  initialComp?: string | null;
  initialHome?: string | null;
  initialAway?: string | null;
  initialMatchId?: string | null;
}

export default function H2HComparator({
  initialComp,
  initialHome,
  initialAway,
  initialMatchId,
}: H2HComparatorProps) {
  const validInitialComp = useMemo(() => {
    if (!initialComp) return 'PL';
    const found = ALL_SUPPORTED_COMPETITIONS.find(
      (c) => c.code.toUpperCase() === initialComp.toUpperCase(),
    );
    return found ? found.code : 'PL';
  }, [initialComp]);

  const [compACode, setCompACode] = useState(validInitialComp);
  const [compBCode, setCompBCode] = useState(validInitialComp);
  const [teamsA, setTeamsA] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamsB, setTeamsB] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamAId, setTeamAId] = useState<string>(FALLBACK_TACTICAL_PROFILES[0].id);
  const [teamBId, setTeamBId] = useState<string>(FALLBACK_TACTICAL_PROFILES[1].id);
  const [isLoading, setIsLoading] = useState(false);
  const [dbClashes, setDbClashes] = useState<MatchRecord[]>([]);
  const [espnClashes, setEspnClashes] = useState<EspnH2HMatch[]>([]);
  const [seriesSummary, setSeriesSummary] = useState<string | undefined>(undefined);

  useEffect(() => {
    let isMounted = true;
    async function loadTeams() {
      setIsLoading(true);
      const [standingsA, standingsB] = await Promise.all([
        fetchLiveStandings(compACode),
        compACode === compBCode ? Promise.resolve([]) : fetchLiveStandings(compBCode),
      ]);
      if (!isMounted) return;

      const compAName =
        ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compACode)?.name || compACode;
      const compBName =
        ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compBCode)?.name || compBCode;

      const mappedA =
        standingsA.length > 0
          ? standingsA.map((s) => standingToTacticalProfile(s, compAName))
          : FALLBACK_TACTICAL_PROFILES;
      const mappedB =
        compACode === compBCode
          ? mappedA
          : standingsB.length > 0
          ? standingsB.map((s) => standingToTacticalProfile(s, compBName))
          : FALLBACK_TACTICAL_PROFILES;

      setTeamsA(mappedA);
      setTeamsB(mappedB);

      const matchByName = (list: ClubTacticalProfile[], query?: string | null) => {
        if (!query) return null;
        const q = query.toLowerCase().trim();
        return (
          list.find(
            (t) =>
              t.name.toLowerCase().includes(q) ||
              q.includes(t.name.toLowerCase()) ||
              t.shortName.toLowerCase() === q,
          ) || null
        );
      };

      const matchedHome = matchByName(mappedA, initialHome);
      const matchedAway = matchByName(mappedB, initialAway);

      if (matchedHome) {
        setTeamAId(matchedHome.id);
      } else if (!mappedA.some((t) => t.id === teamAId)) {
        setTeamAId(mappedA[0]?.id || '');
      }

      if (matchedAway) {
        setTeamBId(matchedAway.id);
      } else if (!mappedB.some((t) => t.id === teamBId)) {
        setTeamBId(mappedB[1]?.id || mappedB[0]?.id || '');
      }

      setIsLoading(false);
    }
    loadTeams();
    return () => {
      isMounted = false;
    };
  }, [compACode, compBCode, initialHome, initialAway]);

  const teamA = useMemo(
    () => teamsA.find((c) => c.id === teamAId) || teamsA[0] || FALLBACK_TACTICAL_PROFILES[0],
    [teamsA, teamAId],
  );
  const teamB = useMemo(
    () =>
      teamsB.find((c) => c.id === teamBId) ||
      teamsB[1] ||
      teamsB[0] ||
      FALLBACK_TACTICAL_PROFILES[1],
    [teamsB, teamBId],
  );

  useEffect(() => {
    let isCancelled = false;
    async function loadH2H() {
      const [dbRows, espnData] = await Promise.all([
        fetchHistoricalH2HFromDatabase(teamA.name, teamB.name),
        initialMatchId
          ? fetchEspnMatchSummary(initialMatchId, compACode).catch(() => null)
          : Promise.resolve(null),
      ]);
      if (isCancelled) return;
      setDbClashes(dbRows);
      setEspnClashes(espnData?.headToHead?.matches || []);
      setSeriesSummary(espnData?.headToHead?.summary);
    }
    loadH2H();
    return () => {
      isCancelled = true;
    };
  }, [teamA.name, teamB.name, initialMatchId, compACode]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <TeamPickerCard
          label="Club A (Primary Tactical Focus)"
          compCode={compACode}
          onCompChange={setCompACode}
          teamId={teamAId}
          onTeamChange={setTeamAId}
          teams={teamsA}
          disabledTeamId={compACode === compBCode ? teamBId : undefined}
          isLoading={isLoading}
          accent="volt"
        />
        <TeamPickerCard
          label="Club B (Opponent Benchmark)"
          compCode={compBCode}
          onCompChange={setCompBCode}
          teamId={teamBId}
          onTeamChange={setTeamBId}
          teams={teamsB}
          disabledTeamId={compACode === compBCode ? teamAId : undefined}
          isLoading={isLoading}
          accent="sky"
        />
      </div>

      <ClashHeader teamA={teamA} teamB={teamB} />
      <MetricsComparison teamA={teamA} teamB={teamB} />
      <H2HHistoricalClashes
        teamA={teamA}
        teamB={teamB}
        dbClashes={dbClashes}
        espnClashes={espnClashes}
        seriesSummary={seriesSummary}
      />
    </div>
  );
}

interface TeamPickerProps {
  label: string;
  compCode: string;
  onCompChange: (code: string) => void;
  teamId: string;
  onTeamChange: (id: string) => void;
  teams: ClubTacticalProfile[];
  disabledTeamId?: string;
  isLoading: boolean;
  accent: 'volt' | 'sky';
}

function TeamPickerCard({
  label,
  compCode,
  onCompChange,
  teamId,
  onTeamChange,
  teams,
  disabledTeamId,
  isLoading,
  accent,
}: TeamPickerProps) {
  return (
    <div className="p-4 rounded-2xl bg-[#1B1B1F] border border-white/10 space-y-3">
      <div className="flex items-center justify-between">
        <label
          className={`text-xs font-tech font-bold uppercase tracking-wider ${
            accent === 'volt' ? 'text-[#DFFF00]' : 'text-sky-400'
          }`}
        >
          {label}
        </label>
        {isLoading && (
          <span className="text-[10px] text-[#DFFF00] animate-pulse font-tech">
            Syncing Telemetry...
          </span>
        )}
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
        <select
          value={compCode}
          onChange={(e) => onCompChange(e.target.value)}
          className="w-full px-3 py-2.5 rounded-xl bg-[#0D0E11] border border-white/10 text-white text-xs font-tech font-semibold focus:outline-none focus:border-[#DFFF00]"
        >
          {ALL_SUPPORTED_COMPETITIONS.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </select>
        <select
          value={teamId}
          onChange={(e) => onTeamChange(e.target.value)}
          className="w-full px-3 py-2.5 rounded-xl bg-[#0D0E11] border border-white/10 text-white text-xs font-tech font-semibold focus:outline-none focus:border-[#DFFF00]"
        >
          {teams.map((c) => (
            <option key={c.id} value={c.id} disabled={c.id === disabledTeamId}>
              {c.name} ({c.shortName})
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}

function ClashHeader({ teamA, teamB }: { teamA: ClubTacticalProfile; teamB: ClubTacticalProfile }) {
  return (
    <div className="p-6 rounded-2xl bg-gradient-to-r from-[#1B1B1F] via-[#222328] to-[#1B1B1F] border border-white/10 flex flex-col sm:flex-row items-center justify-between gap-6">
      <div className="flex items-center gap-4 text-center sm:text-left">
        <div className="relative w-16 h-16 shrink-0 bg-[#0D0E11] p-2 rounded-2xl border border-white/10">
          <Image src={teamA.emblem} alt={teamA.name} fill sizes="64px" className="object-contain p-1" unoptimized />
        </div>
        <div>
          <h3 className="font-scoreboard text-2xl text-white tracking-wide">{teamA.name}</h3>
          <span className="inline-block px-2.5 py-0.5 rounded-full text-[11px] font-tech font-bold bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 mt-1">
            {teamA.archetype}
          </span>
        </div>
      </div>
      <div className="flex flex-col items-center">
        <div className="w-11 h-11 rounded-full bg-[#DFFF00]/15 border border-[#DFFF00]/40 flex items-center justify-center text-[#DFFF00]">
          <Swords className="w-5 h-5" />
        </div>
        <span className="text-[10px] font-tech font-extrabold text-white/50 tracking-widest uppercase mt-1">
          Head-to-Head Telemetry
        </span>
      </div>
      <div className="flex items-center gap-4 text-center sm:text-right flex-row-reverse sm:flex-row">
        <div>
          <h3 className="font-scoreboard text-2xl text-white tracking-wide">{teamB.name}</h3>
          <span className="inline-block px-2.5 py-0.5 rounded-full text-[11px] font-tech font-bold bg-sky-500/15 text-sky-400 border border-sky-500/30 mt-1">
            {teamB.archetype}
          </span>
        </div>
        <div className="relative w-16 h-16 shrink-0 bg-[#0D0E11] p-2 rounded-2xl border border-white/10">
          <Image src={teamB.emblem} alt={teamB.name} fill sizes="64px" className="object-contain p-1" unoptimized />
        </div>
      </div>
    </div>
  );
}

function MetricsComparison({ teamA, teamB }: { teamA: ClubTacticalProfile; teamB: ClubTacticalProfile }) {
  const metrics = [
    { label: 'Goals Scored / 90', valA: teamA.goalsPerMatch, valB: teamB.goalsPerMatch, unit: '' },
    { label: 'Expected Goals (xG) / 90', valA: teamA.xGPerMatch, valB: teamB.xGPerMatch, unit: '' },
    { label: 'Goals Conceded / 90', valA: teamA.concededPerMatch, valB: teamB.concededPerMatch, unit: '', invertBetter: true },
    { label: 'Average Possession Share', valA: teamA.possessionAvg, valB: teamB.possessionAvg, unit: '%' },
    { label: 'Clean Sheet Ratio', valA: teamA.cleanSheetPct, valB: teamB.cleanSheetPct, unit: '%' },
  ];

  return (
    <div className="rounded-2xl bg-[#1B1B1F] border border-white/10 overflow-hidden shadow-xl">
      <div className="p-4 bg-[#292A2E]/60 border-b border-white/10 flex items-center justify-between font-tech">
        <span className="text-xs font-extrabold text-[#DFFF00] uppercase">{teamA.name}</span>
        <span className="text-xs font-bold text-white/60 uppercase tracking-wider">
          Calibrated Season Benchmark Matrix
        </span>
        <span className="text-xs font-extrabold text-sky-400 uppercase">{teamB.name}</span>
      </div>
      <div className="divide-y divide-white/5 font-tech">
        {metrics.map((m, idx) => {
          const aBetter = m.invertBetter ? m.valA < m.valB : m.valA > m.valB;
          const bBetter = m.invertBetter ? m.valB < m.valA : m.valB > m.valA;
          const total = Math.max(0.1, m.valA + m.valB);
          const pctA = Math.round((m.valA / total) * 100);
          return (
            <div key={idx} className="p-4 space-y-2">
              <div className="flex items-center justify-between text-xs sm:text-sm">
                <span className={`font-scoreboard text-xl ${aBetter ? 'text-[#DFFF00]' : 'text-white'}`}>
                  {m.valA}
                  {m.unit}
                </span>
                <span className="text-white/65 text-xs uppercase tracking-wider font-bold">
                  {m.label}
                </span>
                <span className={`font-scoreboard text-xl ${bBetter ? 'text-sky-400' : 'text-white'}`}>
                  {m.valB}
                  {m.unit}
                </span>
              </div>
              <div className="w-full h-1.5 rounded-full bg-[#0D0E11] overflow-hidden flex gap-0.5">
                <div className="h-full bg-[#DFFF00]" style={{ width: `${pctA}%` }} />
                <div className="h-full bg-sky-400 flex-1" />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

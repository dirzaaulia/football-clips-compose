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
import { Swords, Zap } from 'lucide-react';

export default function H2HComparator() {
  const [compACode, setCompACode] = useState('PL');
  const [compBCode, setCompBCode] = useState('PL');
  const [teamsA, setTeamsA] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamsB, setTeamsB] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamAId, setTeamAId] = useState<string>(FALLBACK_TACTICAL_PROFILES[0].id);
  const [teamBId, setTeamBId] = useState<string>(FALLBACK_TACTICAL_PROFILES[1].id);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    async function loadTeams() {
      setIsLoading(true);
      const [standingsA, standingsB] = await Promise.all([
        fetchLiveStandings(compACode),
        compACode === compBCode ? Promise.resolve([]) : fetchLiveStandings(compBCode),
      ]);
      if (!isMounted) return;

      const compAName = ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compACode)?.name || compACode;
      const compBName = ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compBCode)?.name || compBCode;

      const mappedA = standingsA.length > 0
        ? standingsA.map((s) => standingToTacticalProfile(s, compAName))
        : FALLBACK_TACTICAL_PROFILES;
      const mappedB = compACode === compBCode
        ? mappedA
        : (standingsB.length > 0 ? standingsB.map((s) => standingToTacticalProfile(s, compBName)) : FALLBACK_TACTICAL_PROFILES);

      setTeamsA(mappedA);
      setTeamsB(mappedB);
      if (!mappedA.some((t) => t.id === teamAId)) setTeamAId(mappedA[0]?.id || '');
      if (!mappedB.some((t) => t.id === teamBId)) setTeamBId(mappedB[1]?.id || mappedB[0]?.id || '');
      setIsLoading(false);
    }
    loadTeams();
    return () => { isMounted = false; };
  }, [compACode, compBCode]);

  const teamA = useMemo(() => teamsA.find((c) => c.id === teamAId) || teamsA[0] || FALLBACK_TACTICAL_PROFILES[0], [teamsA, teamAId]);
  const teamB = useMemo(() => teamsB.find((c) => c.id === teamBId) || teamsB[1] || teamsB[0] || FALLBACK_TACTICAL_PROFILES[1], [teamsB, teamBId]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <TeamPickerCard
          label="Team A Selection"
          compCode={compACode}
          onCompChange={setCompACode}
          teamId={teamAId}
          onTeamChange={setTeamAId}
          teams={teamsA}
          disabledTeamId={compACode === compBCode ? teamBId : undefined}
          isLoading={isLoading}
        />
        <TeamPickerCard
          label="Team B Selection"
          compCode={compBCode}
          onCompChange={setCompBCode}
          teamId={teamBId}
          onTeamChange={setTeamBId}
          teams={teamsB}
          disabledTeamId={compACode === compBCode ? teamAId : undefined}
          isLoading={isLoading}
        />
      </div>

      <ClashHeader teamA={teamA} teamB={teamB} />
      <MetricsComparison teamA={teamA} teamB={teamB} />
      <TacticalAnalysisCard teamA={teamA} teamB={teamB} />
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
}

function TeamPickerCard({ label, compCode, onCompChange, teamId, onTeamChange, teams, disabledTeamId, isLoading }: TeamPickerProps) {
  return (
    <div className="p-4 rounded-2xl bg-[#181C20] border border-[#334155]/60 space-y-3">
      <div className="flex items-center justify-between">
        <span className="text-xs font-bold text-[#94A3B8] uppercase tracking-wider">{label}</span>
        {isLoading && <span className="text-[10px] text-[#7098F8] animate-pulse">Syncing...</span>}
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
        <select
          value={compCode}
          aria-label={`${label} League`}
          onChange={(e) => onCompChange(e.target.value)}
          className="w-full bg-[#101418] border border-[#334155] rounded-xl px-3 py-2 text-xs font-semibold text-white focus:outline-none focus:border-[#7098F8]"
        >
          {ALL_SUPPORTED_COMPETITIONS.map((c) => (
            <option key={c.code} value={c.code}>{c.name}</option>
          ))}
        </select>
        <select
          value={teamId}
          aria-label={`${label} Club`}
          onChange={(e) => onTeamChange(e.target.value)}
          className="w-full bg-[#101418] border border-[#334155] rounded-xl px-3 py-2 text-xs font-semibold text-white focus:outline-none focus:border-[#7098F8]"
        >
          {teams.map((t) => (
            <option key={t.id} value={t.id} disabled={t.id === disabledTeamId}>
              {t.name}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}

function ClashHeader({ teamA, teamB }: { teamA: ClubTacticalProfile; teamB: ClubTacticalProfile }) {
  return (
    <div className="p-6 rounded-3xl bg-gradient-to-r from-[#1E293B]/60 via-[#181C20] to-[#1E293B]/60 border border-[#334155] flex flex-col sm:flex-row items-center justify-between gap-6">
      <div className="flex items-center gap-4 text-center sm:text-left">
        <div className="relative w-16 h-16 shrink-0 bg-white/5 p-2 rounded-2xl border border-white/10">
          <Image src={teamA.emblem} alt={teamA.name} fill sizes="64px" className="object-contain p-1" />
        </div>
        <div>
          <h3 className="text-lg font-black text-white">{teamA.name}</h3>
          <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#7098F8]/10 text-[#7098F8] border border-[#7098F8]/30 mt-1">
            {teamA.archetype}
          </span>
        </div>
      </div>
      <div className="flex flex-col items-center">
        <div className="w-10 h-10 rounded-full bg-[#7098F8]/10 border border-[#7098F8]/30 flex items-center justify-center text-[#7098F8]">
          <Swords className="w-5 h-5" />
        </div>
        <span className="text-[11px] font-extrabold text-[#94A3B8] tracking-widest uppercase mt-1">Tactical Duel</span>
      </div>
      <div className="flex items-center gap-4 text-center sm:text-right flex-row-reverse sm:flex-row">
        <div>
          <h3 className="text-lg font-black text-white">{teamB.name}</h3>
          <span className="inline-block px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#A855F7]/10 text-[#A855F7] border border-[#A855F7]/30 mt-1">
            {teamB.archetype}
          </span>
        </div>
        <div className="relative w-16 h-16 shrink-0 bg-white/5 p-2 rounded-2xl border border-white/10">
          <Image src={teamB.emblem} alt={teamB.name} fill sizes="64px" className="object-contain p-1" />
        </div>
      </div>
    </div>
  );
}

function MetricsComparison({ teamA, teamB }: { teamA: ClubTacticalProfile; teamB: ClubTacticalProfile }) {
  const metrics = [
    { label: 'Goals Scored / Match', valA: teamA.goalsPerMatch, valB: teamB.goalsPerMatch, unit: '' },
    { label: 'Expected Goals (xG) / Match', valA: teamA.xGPerMatch, valB: teamB.xGPerMatch, unit: '' },
    { label: 'Goals Conceded / Match', valA: teamA.concededPerMatch, valB: teamB.concededPerMatch, unit: '', invertBetter: true },
    { label: 'Average Possession', valA: teamA.possessionAvg, valB: teamB.possessionAvg, unit: '%' },
    { label: 'Clean Sheet Frequency', valA: teamA.cleanSheetPct, valB: teamB.cleanSheetPct, unit: '%' },
  ];

  return (
    <div className="rounded-2xl bg-[#181C20] border border-[#334155]/60 overflow-hidden">
      <div className="p-4 border-b border-[#334155]/60 flex items-center justify-between">
        <span className="text-xs font-extrabold text-[#7098F8]">{teamA.shortName}</span>
        <span className="text-xs font-bold text-[#94A3B8] uppercase tracking-wider">Season Benchmark Metrics</span>
        <span className="text-xs font-extrabold text-[#A855F7]">{teamB.shortName}</span>
      </div>
      <div className="divide-y divide-[#334155]/40">
        {metrics.map((m, idx) => {
          const aBetter = m.invertBetter ? m.valA < m.valB : m.valA > m.valB;
          const bBetter = m.invertBetter ? m.valB < m.valA : m.valB > m.valA;
          return (
            <div key={idx} className="p-3.5 flex items-center justify-between text-xs sm:text-sm">
              <span className={`w-16 font-extrabold ${aBetter ? 'text-[#7098F8]' : 'text-white'}`}>
                {m.valA}{m.unit}
              </span>
              <span className="text-[#94A3B8] text-center font-medium px-2">{m.label}</span>
              <span className={`w-16 font-extrabold text-right ${bBetter ? 'text-[#A855F7]' : 'text-white'}`}>
                {m.valB}{m.unit}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function TacticalAnalysisCard({ teamA, teamB }: { teamA: ClubTacticalProfile; teamB: ClubTacticalProfile }) {
  return (
    <div className="p-5 rounded-2xl bg-[#181C20] border border-[#334155]/60 space-y-3">
      <div className="flex items-center gap-2 text-white font-extrabold text-sm">
        <Zap className="w-4 h-4 text-[#7098F8]" />
        <h4>Tactical Archetype Breakdown</h4>
      </div>
      <p className="text-xs text-[#94A3B8] leading-relaxed">
        {teamA.name} ({teamA.league}) operates with a <strong className="text-white">{teamA.archetype}</strong> setup with a {teamA.pressIntensity.toLowerCase()} pressing intensity. In contrast, {teamB.name} ({teamB.league}) employs a <strong className="text-white">{teamB.archetype}</strong> posture. When these systems clash, control over transitional corridors and rest-defense compactness in Zone 14 typically determines match possession and decisive counter opportunities.
      </p>
    </div>
  );
}

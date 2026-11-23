'use client';

import React from 'react';
import Image from 'next/image';
import { ALL_SUPPORTED_COMPETITIONS, ClubTacticalProfile } from '@/lib/tools-data';
import { Swords } from 'lucide-react';

export interface TeamPickerProps {
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

export function TeamPickerCard({
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

export function ClashHeader({
  teamA,
  teamB,
}: {
  teamA: ClubTacticalProfile;
  teamB: ClubTacticalProfile;
}) {
  return (
    <div className="p-6 rounded-2xl bg-gradient-to-r from-[#1B1B1F] via-[#222328] to-[#1B1B1F] border border-white/10 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl">
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
          H2H Analytical Matchup
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

export function MetricsComparison({
  teamA,
  teamB,
}: {
  teamA: ClubTacticalProfile;
  teamB: ClubTacticalProfile;
}) {
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
          Season Benchmark Comparison
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

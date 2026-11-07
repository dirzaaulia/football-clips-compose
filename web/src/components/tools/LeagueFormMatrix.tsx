'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  ALL_SUPPORTED_COMPETITIONS,
  SupportedCompetition,
  fetchLiveStandings,
  standingToTacticalProfile,
  ClubTacticalProfile,
} from '@/lib/tools-data';
import { StandingRecord } from '@/lib/database';
import { Activity, ShieldCheck, Loader2 } from 'lucide-react';

export default function LeagueFormMatrix() {
  const [selectedCompCode, setSelectedCompCode] = useState<string>('PL');
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const selectedComp =
    ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === selectedCompCode) ||
    ALL_SUPPORTED_COMPETITIONS[3]; // Default: Premier League

  useEffect(() => {
    let isCancelled = false;
    async function loadData() {
      setIsLoading(true);
      const data = await fetchLiveStandings(selectedCompCode, 2026);
      if (!isCancelled) {
        setStandings(data);
        setIsLoading(false);
      }
    }
    loadData();
    return () => { isCancelled = true; };
  }, [selectedCompCode]);

  return (
    <div className="space-y-5">
      {/* 23-Competition Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl bg-[#181C20] border border-[#334155]/60">
        <label htmlFor="comp-matrix-select" className="text-xs font-bold text-[#94A3B8] uppercase tracking-wider">
          Select Competition (23 Official Leagues &amp; Tournaments)
        </label>
        <select
          id="comp-matrix-select"
          aria-label="Select Competition"
          value={selectedCompCode}
          onChange={(e) => setSelectedCompCode(e.target.value)}
          className="bg-[#101418] border border-[#334155] rounded-xl px-3.5 py-2 text-xs font-bold text-white focus:outline-none focus:border-[#7098F8] cursor-pointer"
        >
          {['European Competitions', 'Domestic Leagues', 'Domestic Cups', 'South American Competitions'].map((cat) => (
            <optgroup key={cat} label={cat}>
              {ALL_SUPPORTED_COMPETITIONS.filter((c) => c.category === cat).map((comp) => (
                <option key={comp.code} value={comp.code}>
                  {comp.name}
                </option>
              ))}
            </optgroup>
          ))}
        </select>
      </div>

      {/* Form Matrix Table Card */}
      <div className="rounded-2xl bg-[#181C20] border border-[#334155]/60 overflow-hidden">
        <div className="p-4 border-b border-[#334155]/60 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="relative w-7 h-7 shrink-0">
              <Image src={selectedComp.emblemUrl} alt={selectedComp.name} fill sizes="28px" className="object-contain" />
            </div>
            <div>
              <h3 className="text-sm font-extrabold text-white">{selectedComp.name}</h3>
              <p className="text-[11px] text-[#94A3B8]">Live 2026/2027 Season Standings &amp; Form Guide</p>
            </div>
          </div>
          <span className="text-[11px] text-[#7098F8] font-bold px-2.5 py-1 rounded-full bg-[#7098F8]/10 border border-[#7098F8]/30">
            {standings.length} Clubs
          </span>
        </div>

        {isLoading ? (
          <div className="py-16 text-center text-[#94A3B8] space-y-2">
            <Loader2 className="w-6 h-6 animate-spin text-[#7098F8] mx-auto" />
            <p className="text-xs">Loading live standings from PostgreSQL 16 datastore...</p>
          </div>
        ) : standings.length === 0 ? (
          <div className="py-14 text-center text-[#94A3B8] space-y-2 px-4">
            <Activity className="w-8 h-8 text-[#94A3B8]/40 mx-auto" />
            <p className="text-sm font-bold text-white">No Standings Table for {selectedComp.name}</p>
            <p className="text-xs text-[#94A3B8] max-w-sm mx-auto">
              This tournament uses a single-elimination knockout format. Explore brackets and fixtures in the Scores &amp; Fixtures hub.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-[#334155]/40 text-[#94A3B8] uppercase text-[10px] font-bold tracking-wider">
                  <th className="py-3 px-3 text-center w-8">#</th>
                  <th className="py-3 px-4">Club</th>
                  <th className="py-3 px-3">Recent Form</th>
                  <th className="py-3 px-2 text-center">PL</th>
                  <th className="py-3 px-2 text-center">W</th>
                  <th className="py-3 px-2 text-center">D</th>
                  <th className="py-3 px-2 text-center">L</th>
                  <th className="py-3 px-2 text-center">GD</th>
                  <th className="py-3 px-3 text-center font-black text-white">PTS</th>
                  <th className="py-3 px-4 text-right">Tactical Style</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#334155]/30">
                {standings.map((s, idx) => {
                  const profile = standingToTacticalProfile(s, selectedComp.name);
                  return (
                    <MatrixRow
                      key={s.id || idx}
                      pos={s.position || idx + 1}
                      standing={s}
                      profile={profile}
                    />
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

interface MatrixRowProps {
  pos: number;
  standing: StandingRecord;
  profile: ClubTacticalProfile;
}

function MatrixRow({ pos, standing, profile }: MatrixRowProps) {
  return (
    <tr className="hover:bg-white/[0.02] transition-colors">
      <td className="py-3 px-3 text-center font-bold text-[#94A3B8]">{pos}</td>
      <td className="py-3 px-4 font-bold text-white flex items-center gap-3">
        <div className="relative w-5 h-5 shrink-0">
          <Image src={profile.emblem} alt={standing.team_name} fill sizes="20px" className="object-contain" />
        </div>
        <span className="truncate max-w-[130px] sm:max-w-none">{standing.team_name}</span>
      </td>
      <td className="py-3 px-3">
        <div className="flex items-center gap-1">
          {profile.recentForm.map((result, i) => (
            <span
              key={i}
              className={`w-5 h-5 rounded-md text-[10px] font-black flex items-center justify-center uppercase ${
                result === 'W'
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                  : result === 'D'
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                  : 'bg-rose-500/20 text-rose-400 border border-rose-500/40'
              }`}
            >
              {result}
            </span>
          ))}
        </div>
      </td>
      <td className="py-3 px-2 text-center text-[#94A3B8]">{standing.played_games}</td>
      <td className="py-3 px-2 text-center text-[#94A3B8]">{standing.won}</td>
      <td className="py-3 px-2 text-center text-[#94A3B8]">{standing.draw}</td>
      <td className="py-3 px-2 text-center text-[#94A3B8]">{standing.lost}</td>
      <td className={`py-3 px-2 text-center font-bold ${(standing.goal_difference || 0) > 0 ? 'text-emerald-400' : (standing.goal_difference || 0) < 0 ? 'text-rose-400' : 'text-[#94A3B8]'}`}>
        {(standing.goal_difference || 0) > 0 ? `+${standing.goal_difference}` : standing.goal_difference}
      </td>
      <td className="py-3 px-3 text-center font-black text-white bg-white/[0.02]">{standing.points}</td>
      <td className="py-3 px-4 text-right">
        <span className="text-[11px] font-semibold text-[#94A3B8]">{profile.archetype}</span>
      </td>
    </tr>
  );
}

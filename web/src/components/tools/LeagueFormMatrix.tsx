'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  ALL_SUPPORTED_COMPETITIONS,
  fetchLiveStandings,
  standingToTacticalProfile,
  ClubTacticalProfile,
} from '@/lib/tools-data';
import { StandingRecord } from '@/lib/database';
import { computeClubAnalyticsFromStandings, ClubAnalyticsSummary } from '@/lib/free-stats';
import { Activity, Sparkles } from 'lucide-react';

export default function LeagueFormMatrix() {
  const [selectedCompCode, setSelectedCompCode] = useState<string>('PL');
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [analytics, setAnalytics] = useState<Record<string, ClubAnalyticsSummary>>({});
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [emblems, setEmblems] = useState<Record<string, string>>({});

  const selectedComp =
    ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === selectedCompCode) ||
    ALL_SUPPORTED_COMPETITIONS[3];

  useEffect(() => {
    fetch('/api/competitions')
      .then((r) => r.json())
      .then((data: Array<{ id: string; code?: string; emblem_url?: string }>) => {
        if (!Array.isArray(data)) return;
        const m: Record<string, string> = {};
        data.forEach((c) => {
          if (c.emblem_url) {
            m[c.id.toUpperCase()] = c.emblem_url;
            if (c.code) m[c.code.toUpperCase()] = c.emblem_url;
          }
        });
        setEmblems(m);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    let isCancelled = false;
    async function loadData() {
      setIsLoading(true);
      const data = await fetchLiveStandings(selectedCompCode, 2026);
      if (!isCancelled) {
        setStandings(data);
        if (data.length > 0) {
          const stats = computeClubAnalyticsFromStandings(data);
          const map: Record<string, ClubAnalyticsSummary> = {};
          stats.forEach((item) => {
            map[item.teamName] = item;
          });
          setAnalytics(map);
        }
        setIsLoading(false);
      }
    }
    loadData();
    return () => { isCancelled = true; };
  }, [selectedCompCode]);

  return (
    <div className="space-y-5 select-none">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-2xl bg-[#1B1B1F] border border-white/10 shadow-lg">
        <label htmlFor="comp-matrix-select" className="text-xs font-bold font-tech text-[#DFFF00] uppercase tracking-wider flex items-center gap-1.5">
          <Sparkles className="w-3.5 h-3.5" />
          Select Official League Benchmark
        </label>
        <select
          id="comp-matrix-select"
          aria-label="Select Competition"
          value={selectedCompCode}
          onChange={(e) => setSelectedCompCode(e.target.value)}
          className="bg-[#0D0E11] border border-white/10 rounded-xl px-3.5 py-2 text-xs font-bold text-white focus:outline-none focus:border-[#DFFF00] cursor-pointer font-tech"
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

      <div className="rounded-2xl bg-[#1B1B1F] border border-white/10 overflow-hidden shadow-2xl">
        <div className="p-4 border-b border-white/10 flex items-center justify-between">
          <div className="flex items-center gap-3">
            {emblems[selectedCompCode.toUpperCase()] ? (
              <div className="relative w-7 h-7 shrink-0">
                <Image src={emblems[selectedCompCode.toUpperCase()]} alt={selectedComp.name} fill sizes="28px" className="object-contain" />
              </div>
            ) : (
              <Activity className="w-5 h-5 text-[#DFFF00] shrink-0" />
            )}
            <div>
              <h3 className="text-sm font-extrabold text-white font-scoreboard">{selectedComp.name}</h3>
              <p className="text-[11px] text-white/50 font-tech">Live Standings &amp; xPts Expected Points Delta</p>
            </div>
          </div>
          <span className="text-[11px] font-tech text-[#DFFF00] font-bold px-2.5 py-1 rounded-full bg-[#DFFF00]/10 border border-[#DFFF00]/30">
            {standings.length} Clubs
          </span>
        </div>

        {isLoading ? (
          <div className="p-6 text-center font-tech text-xs text-white/50 animate-pulse">
            Calibrating league table &amp; expected points telemetry...
          </div>
        ) : standings.length === 0 ? (
          <div className="py-14 text-center text-white/50 space-y-2 px-4 font-tech">
            <Activity className="w-8 h-8 text-white/30 mx-auto" />
            <p className="text-sm font-bold text-white">No Standings Table for {selectedComp.name}</p>
            <p className="text-xs max-w-sm mx-auto">
              This tournament uses a single-elimination knockout format. Explore fixtures in the Fixtures hub.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs font-tech">
              <thead>
                <tr className="border-b border-white/10 text-white/50 uppercase text-[10px] font-bold tracking-wider">
                  <th className="py-3 px-3 text-center w-8">#</th>
                  <th className="py-3 px-4">Club</th>
                  <th className="py-3 px-3">Recent Form</th>
                  <th className="py-3 px-2 text-center">PL</th>
                  <th className="py-3 px-2 text-center">GD</th>
                  <th className="py-3 px-3 text-center font-black text-white">PTS</th>
                  <th className="py-3 px-2 text-center text-[#DFFF00]">xPts</th>
                  <th className="py-3 px-2 text-center text-white/70">Delta</th>
                  <th className="py-3 px-4 text-right">Tactical Style</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {standings.map((s, idx) => {
                  const profile = standingToTacticalProfile(s, selectedComp.name);
                  const stat = analytics[s.team_name];
                  return (
                    <MatrixRow
                      key={s.id || idx}
                      pos={s.position || idx + 1}
                      standing={s}
                      profile={profile}
                      stat={stat}
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

function MatrixRow({
  pos,
  standing,
  profile,
  stat,
}: {
  pos: number;
  standing: StandingRecord;
  profile: ClubTacticalProfile;
  stat?: ClubAnalyticsSummary;
}) {
  const delta = stat ? stat.pointsDelta : 0;

  return (
    <tr className="hover:bg-white/[0.03] transition-colors">
      <td className="py-3 px-3 text-center font-bold text-white/50">{pos}</td>
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
      <td className="py-3 px-2 text-center text-white/60">{standing.played_games}</td>
      <td className={`py-3 px-2 text-center font-bold ${(standing.goal_difference || 0) > 0 ? 'text-emerald-400' : (standing.goal_difference || 0) < 0 ? 'text-rose-400' : 'text-white/60'}`}>
        {(standing.goal_difference || 0) > 0 ? `+${standing.goal_difference}` : standing.goal_difference}
      </td>
      <td className="py-3 px-3 text-center font-black text-white bg-white/[0.02]">{standing.points}</td>
      <td className="py-3 px-2 text-center font-bold text-[#DFFF00]">
        {stat ? stat.expectedPoints : '-'}
      </td>
      <td className={`py-3 px-2 text-center font-bold ${delta > 0 ? 'text-emerald-400' : delta < 0 ? 'text-rose-400' : 'text-white/60'}`}>
        {delta > 0 ? `+${delta}` : delta}
      </td>
      <td className="py-3 px-4 text-right">
        <span className="text-[11px] font-semibold text-white/50">{profile.archetype}</span>
      </td>
    </tr>
  );
}

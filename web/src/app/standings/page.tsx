'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { getStandingsByCompetition, StandingRecord } from '@/lib/supabase';
import { Trophy } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';

const LEAGUES = [
  { code: 'PL', name: 'Premier League' },
  { code: 'CL', name: 'Champions League' },
  { code: 'PD', name: 'La Liga' },
  { code: 'SA', name: 'Serie A' },
  { code: 'BL1', name: 'Bundesliga' },
  { code: 'FL1', name: 'Ligue 1' },
  { code: 'SPL', name: 'Saudi Pro League' },
  { code: 'MLS', name: 'MLS' },
  { code: 'DED', name: 'Eredivisie' },
  { code: 'PPL', name: 'Liga Portugal' },
];

export default function StandingsPage() {
  const [selectedComp, setSelectedComp] = useState('PL');
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const hasFormData = React.useMemo(
    () => standings.some((t) => t.form && t.form.trim().length > 0),
    [standings]
  );

  useEffect(() => {
    async function load() {
      setIsLoading(true);
      const data = await getStandingsByCompetition(selectedComp);
      setStandings(data);
      setIsLoading(false);
    }
    load();
  }, [selectedComp]);

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
      {/* Title & Compact League Selector */}
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Trophy className="w-7 h-7 text-[#7098F8]" />
            <span>League Tables & Standings</span>
          </h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">Official European domestic leagues & UEFA Champions League table.</p>
        </div>

        {/* Compact, Intuitive League Selector Dropdown */}
        <div className="flex items-center gap-2 bg-[#181C20] border border-[#334155] hover:border-[#7098F8]/50 rounded-2xl px-4 py-2 transition-all shadow-md self-start sm:self-auto">
          <Trophy className="w-4 h-4 text-[#7098F8]" />
          <select
            value={selectedComp}
            onChange={(e) => setSelectedComp(e.target.value)}
            className="bg-transparent text-white text-xs sm:text-sm font-bold focus:outline-none cursor-pointer pr-1"
          >
            {LEAGUES.map((l) => (
              <option key={l.code} value={l.code} className="bg-[#181C20] text-white">
                {l.name}
              </option>
            ))}
          </select>
        </div>
      </header>

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* Standings Table */}
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
                    No standings data available for this competition currently.
                  </td>
                </tr>
              ) : (
                standings.map((team) => (
                  <tr key={team.id} className="hover:bg-[#1E2227]/80 transition-colors">
                    <td className="py-3 px-3 sm:px-4 text-center font-bold text-[#94A3B8]">
                      {team.position}
                    </td>
                    <td className="py-3 px-4 font-bold text-white flex items-center gap-3">
                      {team.crest_url && (
                        <div className="relative w-5 h-5 shrink-0">
                          <Image
                            src={team.crest_url}
                            alt={team.team_name}
                            fill
                            className="object-contain"
                            unoptimized
                          />
                        </div>
                      )}
                      <span className="truncate max-w-[180px] sm:max-w-xs">{team.team_name}</span>
                    </td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.played_games}</td>
                    <td className="py-3 px-3 text-center">{team.won}</td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.draw}</td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.lost}</td>
                    <td className="py-3 px-3 text-center hidden md:table-cell text-[#94A3B8] font-mono">
                      {team.goal_difference > 0 ? `+${team.goal_difference}` : team.goal_difference}
                    </td>
                    <td className="py-3 px-4 text-center font-black text-white text-base">
                      {team.points}
                    </td>
                    {hasFormData && (
                      <td className="py-3 px-4 text-center hidden sm:table-cell">
                        {team.form ? (
                          <div className="flex items-center justify-center gap-1">
                            {team.form.split(',').map((f, i) => (
                              <span
                                key={i}
                                className={`w-4 h-4 rounded flex items-center justify-center text-[9px] font-black text-[#101418] ${
                                  f.trim() === 'W'
                                    ? 'bg-emerald-400'
                                    : f.trim() === 'D'
                                    ? 'bg-amber-400'
                                    : 'bg-rose-500 text-white'
                                }`}
                              >
                                {f.trim()}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-[#334155]">-</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

'use client';

import React, { useMemo } from 'react';
import Link from 'next/link';
import { Flame, Shield, TrendingUp, Wrench } from 'lucide-react';
import { StandingRecord } from '@/lib/database';
import { computeClubAnalyticsFromStandings } from '@/lib/free-stats';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface StandingsClubAnalyticsTabProps {
  standings: StandingRecord[];
  competitionCode: string;
}

export default function StandingsClubAnalyticsTab({
  standings,
  competitionCode,
}: StandingsClubAnalyticsTabProps) {
  const analytics = useMemo(
    () => computeClubAnalyticsFromStandings(standings),
    [standings],
  );

  const topAttack = useMemo(
    () => [...analytics].sort((a, b) => b.goalsPerMatch - a.goalsPerMatch).slice(0, 3),
    [analytics],
  );
  const topDefense = useMemo(
    () => [...analytics].sort((a, b) => a.concededPerMatch - b.concededPerMatch).slice(0, 3),
    [analytics],
  );
  const topOverperformers = useMemo(
    () => [...analytics].sort((a, b) => b.pointsDelta - a.pointsDelta).slice(0, 3),
    [analytics],
  );

  if (analytics.length === 0) {
    return (
      <div className="text-center py-16 bg-[#1B1B1F] rounded-xl border border-white/10">
        <p className="font-tech text-sm text-white/50">
          No club telemetry available for this competition.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top 3 Club Telemetry Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {/* Attacking Output Leaders */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/10 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-tech font-bold uppercase tracking-widest text-[#DFFF00]">
              Attacking Output Leaders
            </span>
            <Flame className="w-4 h-4 text-[#DFFF00]" />
          </div>
          <div className="space-y-3">
            {topAttack.map((c) => (
              <div key={c.teamName} className="flex items-center justify-between text-xs font-tech">
                <div className="flex items-center gap-2 min-w-0">
                  <GlassLogoBadge src={c.crestUrl} alt={c.teamName} size={22} />
                  <span className="font-bold text-white truncate">{c.teamName}</span>
                </div>
                <span className="text-[#DFFF00] font-bold">
                  {c.goalsFor} GF ({c.goalsPerMatch}/90)
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Defensive Wall Leaders */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/10 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-tech font-bold uppercase tracking-widest text-emerald-400">
              Defensive Wall &amp; Clean Sheets
            </span>
            <Shield className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="space-y-3">
            {topDefense.map((c) => (
              <div key={c.teamName} className="flex items-center justify-between text-xs font-tech">
                <div className="flex items-center gap-2 min-w-0">
                  <GlassLogoBadge src={c.crestUrl} alt={c.teamName} size={22} />
                  <span className="font-bold text-white truncate">{c.teamName}</span>
                </div>
                <span className="text-emerald-400 font-bold">
                  {c.goalsAgainst} GA (~{c.cleanSheetEst} CS)
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Pythagorean xPTS Overperformers */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/10 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-tech font-bold uppercase tracking-widest text-sky-400">
              Clinical xPTS Overperformers
            </span>
            <TrendingUp className="w-4 h-4 text-sky-400" />
          </div>
          <div className="space-y-3">
            {topOverperformers.map((c) => (
              <div key={c.teamName} className="flex items-center justify-between text-xs font-tech">
                <div className="flex items-center gap-2 min-w-0">
                  <GlassLogoBadge src={c.crestUrl} alt={c.teamName} size={22} />
                  <span className="font-bold text-white truncate">{c.teamName}</span>
                </div>
                <span className="text-sky-400 font-bold">
                  {c.points} PTS ({c.pointsDelta >= 0 ? `+${c.pointsDelta}` : c.pointsDelta} vs xPTS)
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Full Club Statistical Telemetry Matrix */}
      <div className="bg-[#1B1B1F] rounded-xl border border-white/10 overflow-hidden shadow-xl">
        <div className="overflow-x-auto scrollbar-none">
          <table className="w-full text-left border-collapse min-w-[780px]">
            <thead>
              <tr className="bg-[#292A2E]/70 border-b border-white/10 text-[11px] font-tech uppercase tracking-wider text-white/55">
                <th className="py-3 pl-5 pr-2 w-12">Pos</th>
                <th className="py-3 px-3">Club</th>
                <th className="py-3 px-3 text-center">Goals / 90</th>
                <th className="py-3 px-3 text-center">GA / 90</th>
                <th className="py-3 px-3 text-center">Est. xG</th>
                <th className="py-3 px-3 text-center">Est. CS</th>
                <th className="py-3 px-3 text-center">PPG</th>
                <th className="py-3 px-3 text-center">xPTS</th>
                <th className="py-3 px-3 text-center">Delta</th>
                <th className="py-3 pl-3 pr-5 text-right">Tactical Lab</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 font-tech text-xs sm:text-sm">
              {analytics.map((row) => (
                <tr key={row.teamName} className="hover:bg-[#24252A] transition-colors">
                  <td className="py-3 pl-5 pr-2 font-bold text-white/60">#{row.position}</td>
                  <td className="py-3 px-3">
                    <div className="flex items-center gap-2.5">
                      <GlassLogoBadge src={row.crestUrl} alt={row.teamName} size={26} />
                      <span className="font-bold text-white">{row.teamName}</span>
                    </div>
                  </td>
                  <td className="py-3 px-3 text-center text-[#DFFF00] font-bold">
                    {row.goalsPerMatch.toFixed(2)}
                  </td>
                  <td className="py-3 px-3 text-center text-white/75">
                    {row.concededPerMatch.toFixed(2)}
                  </td>
                  <td className="py-3 px-3 text-center font-mono text-sky-400">
                    {row.estimatedXg.toFixed(1)}
                  </td>
                  <td className="py-3 px-3 text-center text-emerald-400 font-bold">
                    {row.cleanSheetEst}
                  </td>
                  <td className="py-3 px-3 text-center font-mono text-white">
                    {row.ppg.toFixed(2)}
                  </td>
                  <td className="py-3 px-3 text-center font-mono text-white/60">
                    {row.expectedPoints.toFixed(1)}
                  </td>
                  <td className="py-3 px-3 text-center">
                    <span
                      className={`px-2 py-0.5 rounded font-bold text-xs ${
                        row.pointsDelta >= 0
                          ? 'bg-emerald-500/15 text-emerald-400'
                          : 'bg-red-500/15 text-red-400'
                      }`}
                    >
                      {row.pointsDelta >= 0 ? `+${row.pointsDelta}` : row.pointsDelta}
                    </span>
                  </td>
                  <td className="py-3 pl-3 pr-5 text-right">
                    <Link
                      href={`/tools?comp=${encodeURIComponent(competitionCode)}&home=${encodeURIComponent(row.teamName)}`}
                      className="inline-flex items-center gap-1 px-2.5 py-1 rounded bg-[#0D0E11] hover:bg-[#DFFF00] text-white/75 hover:text-[#0A0A0C] text-[10px] font-tech font-bold uppercase tracking-wider border border-white/10 transition-colors"
                    >
                      <Wrench className="w-3 h-3" />
                      <span>Compare</span>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

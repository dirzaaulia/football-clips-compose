'use client';

import React from 'react';
import Link from 'next/link';
import { Wrench } from 'lucide-react';
import { StandingRecord } from '@/lib/database';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import { getZoneForPosition } from '@/lib/standingsQualification';

interface StandingsTableProps {
  standings: StandingRecord[];
  competitionCode: string;
}

export default function StandingsTable({ standings, competitionCode }: StandingsTableProps) {
  const totalTeams = standings.length || 20;

  return (
    <div className="w-full bg-[#1B1B1F] rounded-xl border border-white/10 overflow-hidden shadow-2xl">
      <div className="overflow-x-auto scrollbar-none">
        <table className="w-full text-left border-collapse min-w-[820px]">
          <thead>
            <tr className="bg-[#292A2E]/70 border-b border-white/10 text-[11px] font-tech uppercase tracking-wider text-white/55">
              <th className="py-3.5 pl-5 pr-2 w-14">Pos</th>
              <th className="py-3.5 px-3">Club</th>
              <th className="py-3.5 px-3 text-center">P</th>
              <th className="py-3.5 px-3 text-center">W</th>
              <th className="py-3.5 px-3 text-center">D</th>
              <th className="py-3.5 px-3 text-center">L</th>
              <th className="py-3.5 px-3 text-center hidden md:table-cell">GF</th>
              <th className="py-3.5 px-3 text-center hidden md:table-cell">GA</th>
              <th className="py-3.5 px-3 text-center">GD</th>
              <th className="py-3.5 px-4 text-center text-[#DFFF00] font-bold">PTS</th>
              <th className="py-3.5 pl-3 pr-5 text-right">Form (Last 5)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5 font-tech text-xs sm:text-sm">
            {standings.map((row) => {
              const zone = getZoneForPosition(competitionCode, row.position, totalTeams);
              const form = (row.form || 'WWDWL')
                .replace(/[^WDL]/gi, '')
                .toUpperCase()
                .split('')
                .slice(-5);
              const isLeader = row.position === 1;
              const isUcl = row.position >= 1 && row.position <= 4;
              const isRelegation = row.position > totalTeams - 3 && totalTeams >= 16;

              const posBadgeClass = isLeader
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_10px_rgba(223,255,0,0.3)]'
                : isUcl
                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                : isRelegation
                ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                : zone
                ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
                : 'text-white/60';

              const subLabel = isLeader
                ? 'League Leader • Top Seed'
                : zone?.label || (isRelegation ? 'Relegation Zone' : 'Official League Member');

              return (
                <tr
                  key={row.id || `${row.position}-${row.team_name}`}
                  className="hover:bg-[#24252A] transition-colors group"
                  style={{
                    borderLeftWidth: zone ? '4px' : '4px',
                    borderLeftColor: isLeader
                      ? '#DFFF00'
                      : zone?.dotColor || 'transparent',
                  }}
                >
                  {/* POS */}
                  <td className="py-3.5 pl-4 pr-2 font-bold">
                    <span
                      className={`inline-flex items-center justify-center w-6 h-6 rounded font-extrabold text-xs ${posBadgeClass}`}
                    >
                      {row.position}
                    </span>
                  </td>

                  {/* CLUB */}
                  <td className="py-3.5 px-3">
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-3 min-w-0">
                        <GlassLogoBadge
                          src={row.crest_url}
                          alt={row.team_name}
                          size={34}
                          imageClassName="object-contain p-1"
                        />
                        <div className="min-w-0">
                          <span className="font-bold text-white block truncate group-hover:text-[#DFFF00] transition-colors">
                            {row.team_name}
                          </span>
                          <span
                            className={`text-[10px] block truncate ${
                              isLeader ? 'text-[#DFFF00]' : 'text-white/45'
                            }`}
                          >
                            {subLabel}
                          </span>
                        </div>
                      </div>

                      <Link
                        href={`/tools?comp=${encodeURIComponent(competitionCode)}&home=${encodeURIComponent(row.team_name)}`}
                        className="opacity-0 group-hover:opacity-100 px-2 py-1 rounded bg-[#0D0E11] hover:bg-[#DFFF00] text-white/60 hover:text-[#0A0A0C] text-[10px] font-tech font-bold uppercase tracking-wider transition-all flex items-center gap-1 shrink-0"
                        title={`Compare ${row.team_name} in Tactical Tools`}
                      >
                        <Wrench className="w-3 h-3" />
                        <span className="hidden xl:inline">Compare</span>
                      </Link>
                    </div>
                  </td>

                  {/* P, W, D, L, GF, GA */}
                  <td className="py-3.5 px-3 text-center text-white/70 font-medium">{row.played_games}</td>
                  <td className="py-3.5 px-3 text-center text-white font-semibold">{row.won}</td>
                  <td className="py-3.5 px-3 text-center text-white/60">{row.draw}</td>
                  <td className="py-3.5 px-3 text-center text-white/60">{row.lost}</td>
                  <td className="py-3.5 px-3 text-center text-white/60 hidden md:table-cell">{row.goals_for}</td>
                  <td className="py-3.5 px-3 text-center text-white/60 hidden md:table-cell">{row.goals_against}</td>

                  {/* GD */}
                  <td className="py-3.5 px-3 text-center">
                    <span
                      className={`px-2 py-0.5 rounded font-bold text-xs ${
                        row.goal_difference > 0
                          ? 'bg-emerald-500/15 text-emerald-400'
                          : row.goal_difference < 0
                          ? 'bg-red-500/15 text-red-400'
                          : 'bg-white/5 text-white/60'
                      }`}
                    >
                      {row.goal_difference > 0 ? `+${row.goal_difference}` : row.goal_difference}
                    </span>
                  </td>

                  {/* PTS */}
                  <td className="py-3.5 px-4 text-center">
                    <span
                      className={`font-scoreboard text-2xl tracking-wider ${
                        isLeader ? 'text-[#DFFF00]' : 'text-white'
                      }`}
                    >
                      {row.points}
                    </span>
                  </td>

                  {/* FORM (LAST 5) */}
                  <td className="py-3.5 pl-3 pr-5 text-right">
                    <div className="inline-flex items-center gap-1">
                      {form.map((res, idx) => (
                        <span
                          key={idx}
                          className={`w-6 h-6 rounded flex items-center justify-center text-[11px] font-bold uppercase ${
                            res === 'W'
                              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                              : res === 'D'
                              ? 'bg-zinc-500/20 text-zinc-300 border border-zinc-500/30'
                              : 'bg-red-500/20 text-red-400 border border-red-500/30'
                          }`}
                        >
                          {res}
                        </span>
                      ))}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Full Table Footer Info */}
      <div className="bg-[#131317] px-6 py-3 border-t border-white/10 flex items-center justify-between text-xs text-white/50 font-tech">
        <span>Showing all {standings.length} clubs</span>
        <span className="text-[#DFFF00] font-bold uppercase tracking-wider">
          Official {competitionCode} Standings
        </span>
      </div>
    </div>
  );
}

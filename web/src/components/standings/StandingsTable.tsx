'use client';

import React from 'react';
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
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 overflow-hidden shadow-xl">
      <div className="overflow-x-auto scrollbar-none">
        <table className="w-full text-left border-collapse min-w-[700px]">
          <thead>
            <tr className="border-b border-white/10 bg-[#121316]/80 text-[10px] font-tech uppercase text-white/50 tracking-wider">
              <th className="py-3 px-4 w-12 text-center">Pos</th>
              <th className="py-3 px-4">Club</th>
              <th className="py-3 px-3 text-center w-10">P</th>
              <th className="py-3 px-3 text-center w-10">W</th>
              <th className="py-3 px-3 text-center w-10">D</th>
              <th className="py-3 px-3 text-center w-10">L</th>
              <th className="py-3 px-3 text-center w-12">GD</th>
              <th className="py-3 px-4 text-center w-14 font-bold text-white">PTS</th>
              <th className="py-3 px-4 text-center w-36">Recent Form</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5 font-tech text-xs">
            {standings.map((row) => {
              const zone = getZoneForPosition(competitionCode, row.position, totalTeams);
              const form = (row.form || '').split('').filter(Boolean).slice(-5);

              return (
                <tr
                  key={row.id || `${row.position}-${row.team_name}`}
                  className="hover:bg-white/5 transition-colors group"
                >
                  {/* Position with Qualification Color Strip */}
                  <td className="py-3 px-4 text-center relative">
                    {zone && (
                      <span
                        className="absolute left-0 top-1 bottom-1 w-1 rounded-r"
                        style={{ backgroundColor: zone.dotColor }}
                        title={zone.label}
                      />
                    )}
                    <span className="font-bold text-white/70 group-hover:text-white">
                      {row.position}
                    </span>
                  </td>

                  {/* Team Crest & Name */}
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <GlassLogoBadge
                        src={row.crest_url}
                        alt={row.team_name}
                        size={24}
                        imageClassName="object-contain p-0.5"
                      />
                      <span className="font-semibold text-white group-hover:text-[#DFFF00] transition-colors line-clamp-1">
                        {row.team_name}
                      </span>
                    </div>
                  </td>

                  {/* P, W, D, L */}
                  <td className="py-3 px-3 text-center text-white/60">{row.played_games}</td>
                  <td className="py-3 px-3 text-center text-white/60">{row.won}</td>
                  <td className="py-3 px-3 text-center text-white/60">{row.draw}</td>
                  <td className="py-3 px-3 text-center text-white/60">{row.lost}</td>

                  {/* GD */}
                  <td className="py-3 px-3 text-center font-semibold text-white/80">
                    {row.goal_difference > 0 ? `+${row.goal_difference}` : row.goal_difference}
                  </td>

                  {/* Points */}
                  <td className="py-3 px-4 text-center">
                    <span className="font-scoreboard text-base sm:text-lg text-[#DFFF00] font-bold">
                      {row.points}
                    </span>
                  </td>

                  {/* Form Pills */}
                  <td className="py-3 px-4 text-center">
                    <div className="flex items-center justify-center gap-1">
                      {form.length > 0 ? (
                        form.map((res, idx) => (
                          <span
                            key={idx}
                            className={`w-5 h-5 rounded flex items-center justify-center text-[10px] font-bold uppercase ${
                              res === 'W'
                                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                                : res === 'D'
                                ? 'bg-white/10 text-white/60 border border-white/10'
                                : 'bg-[#FF2E4D]/20 text-[#FF2E4D] border border-[#FF2E4D]/30'
                            }`}
                          >
                            {res}
                          </span>
                        ))
                      ) : (
                        <span className="text-[10px] text-white/30">-</span>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

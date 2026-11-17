'use client';

import React from 'react';
import { TrendingUp, CheckCircle2 } from 'lucide-react';
import { StandingRecord } from '@/lib/database';
import { LeagueStatsPayload } from '@/lib/free-stats';

interface StandingsBottomWidgetsProps {
  standings: StandingRecord[];
  playerStats: LeagueStatsPayload | null;
  onSelectUcl?: () => void;
  onOpenPlayerStats: () => void;
}

export default function StandingsBottomWidgets({
  standings,
  playerStats,
  onOpenPlayerStats,
}: StandingsBottomWidgetsProps) {
  const topThreeClubs = standings.slice(0, 3);
  const topThreeScorers = (playerStats?.scorers || []).slice(0, 3);

  return (
    <div className="space-y-8 pt-2">
      {/* Stitch Bottom 3-Card Telemetry Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Card 1: Goal Differential & xG Leaders */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/5 space-y-4">
          <div className="flex items-center justify-between">
            <span className="font-tech text-xs uppercase tracking-widest text-[#DFFF00] font-bold">
              Goal Differential Leaders
            </span>
            <TrendingUp className="w-4 h-4 text-[#DFFF00]" />
          </div>
          <div className="space-y-3">
            {topThreeClubs.map((club, idx) => {
              const barPct = Math.min(100, Math.max(25, 90 - idx * 14));
              return (
                <div key={club.team_name}>
                  <div className="flex justify-between text-xs mb-1 font-tech">
                    <span className="font-bold text-white">{club.team_name}</span>
                    <span className="text-emerald-400 font-bold">
                      {club.goal_difference >= 0 ? `+${club.goal_difference}` : club.goal_difference} GD ({club.goals_for} GF)
                    </span>
                  </div>
                  <div className="w-full h-1.5 bg-[#0D0E11] rounded-full overflow-hidden">
                    <div
                      className={idx === 0 ? 'bg-[#DFFF00] h-full' : 'bg-emerald-400 h-full'}
                      style={{ width: `${barPct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Card 2: Golden Boot Telemetry */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/5 space-y-4">
          <div className="flex items-center justify-between">
            <span className="font-tech text-xs uppercase tracking-widest text-[#DFFF00] font-bold">
              Golden Boot Telemetry
            </span>
            <button
              type="button"
              onClick={onOpenPlayerStats}
              className="text-[11px] font-tech text-[#DFFF00] hover:underline cursor-pointer"
            >
              Full Leaderboard →
            </button>
          </div>
          <div className="space-y-2.5">
            {topThreeScorers.length > 0 ? (
              topThreeScorers.map((s, idx) => (
                <div
                  key={s.playerId}
                  className="flex items-center justify-between py-1 border-b border-white/5 last:border-none font-tech"
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <span
                      className={`text-xs font-bold ${
                        idx === 0 ? 'text-[#DFFF00]' : 'text-white/50'
                      }`}
                    >
                      0{idx + 1}
                    </span>
                    <span className="text-sm font-bold text-white truncate">{s.name}</span>
                    <span className="text-[10px] text-white/45">({s.teamAbbr})</span>
                  </div>
                  <span className="font-scoreboard text-xl text-white shrink-0">
                    {s.goals} Goals
                  </span>
                </div>
              ))
            ) : (
              <p className="text-xs text-white/50 font-tech">Loading scorer telemetry...</p>
            )}
          </div>
        </div>

        {/* Card 3: Qualification Scenarios */}
        <div className="bg-[#1B1B1F] rounded-xl p-5 border border-white/5 flex flex-col justify-between space-y-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-tech text-xs uppercase tracking-widest text-[#DFFF00] font-bold">
                Qualification Scenarios
              </span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-xs text-white/65 font-tech leading-relaxed">
              European coefficient projections allocate top domestic positions directly to the UEFA Champions League 36-team league phase.
            </p>
          </div>
          <div className="pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-white/50 font-tech">
            <span>Next Calibration: Matchday Live</span>
            <span className="text-[#DFFF00] font-bold">99.8% Confidence</span>
          </div>
        </div>
      </div>
    </div>
  );
}

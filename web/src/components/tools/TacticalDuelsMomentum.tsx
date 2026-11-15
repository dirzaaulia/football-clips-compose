'use client';

import React from 'react';
import { Swords, Activity, Zap } from 'lucide-react';

interface DuelMatchup {
  playerA: { name: string; club: string; rating: number };
  playerB: { name: string; club: string; rating: number };
  attribute: string;
  winRate: string;
}

const DUELS: DuelMatchup[] = [
  {
    playerA: { name: 'Vinícius Jr', club: 'Real Madrid', rating: 9.2 },
    playerB: { name: 'Kyle Walker', club: 'Man City', rating: 8.8 },
    attribute: '1v1 Take-On Sprint Duels',
    winRate: '54% - 46%',
  },
  {
    playerA: { name: 'Jude Bellingham', club: 'Real Madrid', rating: 8.9 },
    playerB: { name: 'Rodri', club: 'Man City', rating: 9.1 },
    attribute: 'Zone 14 Ground Interceptions',
    winRate: '48% - 52%',
  },
];

export default function TacticalDuelsMomentum() {
  return (
    <div className="w-full space-y-6">
      {/* Duels Grid */}
      <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Swords className="w-4 h-4 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
              Head-to-Head Tactical Duels
            </h3>
          </div>
          <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase">
            Direct Matchups
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {DUELS.map((d) => (
            <div
              key={d.attribute}
              className="p-4 rounded-xl bg-[#121316] border border-white/10 flex flex-col justify-between gap-3"
            >
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-tech uppercase text-white/50 tracking-wider">
                  {d.attribute}
                </span>
                <span className="text-xs font-tech font-bold text-[#DFFF00]">
                  {d.winRate}
                </span>
              </div>

              <div className="flex items-center justify-between gap-4">
                <div className="flex flex-col">
                  <span className="font-tech text-sm font-bold text-white leading-tight">
                    {d.playerA.name}
                  </span>
                  <span className="text-[10px] text-white/40">{d.playerA.club}</span>
                </div>

                <div className="px-2 py-0.5 rounded bg-white/5 border border-white/10 font-tech text-[10px] font-bold text-white/60">
                  VS
                </div>

                <div className="flex flex-col items-end">
                  <span className="font-tech text-sm font-bold text-white leading-tight">
                    {d.playerB.name}
                  </span>
                  <span className="text-[10px] text-white/40">{d.playerB.club}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Momentum Wave Graph (90-Minute Simulation) */}
      <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Activity className="w-4 h-4 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
              Momentum Wave Graph (Last 90 Minutes Simulation)
            </h3>
          </div>
          <span className="text-[10px] font-tech text-white/50 uppercase tracking-wider">
            Tick-by-Tick Dominance
          </span>
        </div>

        {/* Momentum Visualizer */}
        <div className="relative h-28 w-full rounded-xl bg-[#0A0A0C] border border-white/10 flex items-center justify-between px-4 overflow-hidden">
          {/* Center Zero Line */}
          <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-px bg-white/20" />
          {/* Simulated Bars */}
          <div className="relative z-10 w-full h-full flex items-center justify-between gap-1 pt-2 pb-2">
            {[40, 65, 80, 55, 30, 20, 70, 85, 90, 60, 45, 50, 75, 85, 95, 40, 60, 70].map((val, i) => (
              <div
                key={i}
                className="flex-1 flex flex-col justify-center items-center h-full"
              >
                <div
                  className={`w-full rounded-sm transition-all duration-300 ${
                    i % 2 === 0
                      ? 'bg-[#DFFF00] self-start mb-auto'
                      : 'bg-[#38BDF8] self-end mt-auto'
                  }`}
                  style={{ height: `${val * 0.45}%` }}
                />
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-between text-[11px] font-tech text-white/50 px-1">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-sm bg-[#DFFF00]" />
            <span>Home Dominance</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-sm bg-[#38BDF8]" />
            <span>Away Dominance</span>
          </div>
        </div>
      </div>
    </div>
  );
}

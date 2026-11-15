'use client';

import React from 'react';
import { Trophy, Award } from 'lucide-react';

interface ScorerEntry {
  rank: number;
  name: string;
  club: string;
  goals: number;
  assists: number;
  penalties: number;
}

const DEFAULT_SCORERS: ScorerEntry[] = [
  { rank: 1, name: 'Erling Haaland', club: 'Manchester City', goals: 11, assists: 3, penalties: 2 },
  { rank: 2, name: 'Robert Lewandowski', club: 'FC Barcelona', goals: 9, assists: 2, penalties: 1 },
  { rank: 3, name: 'Harry Kane', club: 'Bayern Munich', goals: 8, assists: 4, penalties: 2 },
  { rank: 4, name: 'Mateo Retegui', club: 'Atalanta', goals: 7, assists: 1, penalties: 0 },
];

export default function StandingsGoldenBootCard() {
  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Trophy className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Continental Top Scorers
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase">
          Live Telemetry
        </span>
      </div>

      <div className="divide-y divide-white/5">
        {DEFAULT_SCORERS.map((s) => (
          <div key={s.name} className="py-2.5 flex items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <span className={`w-5 font-tech text-xs font-black ${s.rank === 1 ? 'text-[#DFFF00]' : 'text-white/40'}`}>
                #{s.rank}
              </span>
              <div className="flex flex-col">
                <span className="font-tech text-xs sm:text-sm font-bold text-white leading-tight">
                  {s.name}
                </span>
                <span className="text-[11px] text-white/50">{s.club}</span>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <div className="flex flex-col items-end">
                <span className="font-scoreboard text-base sm:text-lg text-[#DFFF00] leading-none">
                  {s.goals} G
                </span>
                <span className="text-[10px] font-tech text-white/40">
                  {s.assists} A
                </span>
              </div>
              {s.rank === 1 && <Award className="w-4 h-4 text-[#DFFF00] shrink-0" />}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

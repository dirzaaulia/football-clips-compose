'use client';

import React, { useState, useEffect } from 'react';
import { Swords, Activity, Zap, Trophy, Shield } from 'lucide-react';
import { fetchLeaguePlayerStats, LeaguePlayerLeader } from '@/lib/free-stats';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

const LEAGUES = [
  { code: 'PL', name: 'Premier League' },
  { code: 'PD', name: 'La Liga' },
  { code: 'CL', name: 'Champions League' },
  { code: 'SA', name: 'Serie A' },
  { code: 'BL1', name: 'Bundesliga' },
];

function DuelProgressBar({
  valA,
  valB,
  label,
}: {
  valA: number;
  valB: number;
  label: string;
}) {
  const total = valA + valB;
  const pctA = total > 0 ? Math.round((valA / total) * 100) : 50;
  const pctB = Math.max(0, 100 - pctA);

  return (
    <div className="space-y-1 font-tech">
      <div className="flex justify-between text-xs text-white/60">
        <span className="font-bold text-white">{valA}</span>
        <span className="text-[10px] uppercase tracking-wider text-white/40">{label}</span>
        <span className="font-bold text-white">{valB}</span>
      </div>
      <div className="w-full h-1.5 bg-[#0D0E11] rounded-full overflow-hidden flex">
        <div className="bg-[#DFFF00] h-full transition-all duration-500" style={{ width: `${pctA}%` }} />
        <div className="bg-sky-400 h-full transition-all duration-500" style={{ width: `${pctB}%` }} />
      </div>
    </div>
  );
}

function PlayerDuelCard({
  playerA,
  playerB,
  metricTitle,
}: {
  playerA: LeaguePlayerLeader;
  playerB: LeaguePlayerLeader;
  metricTitle: string;
}) {
  const winA = playerA.goals >= playerB.goals;

  return (
    <div className="p-4 rounded-xl bg-[#121316] border border-white/10 flex flex-col justify-between gap-4 shadow-md">
      <div className="flex items-center justify-between border-b border-white/5 pb-2">
        <span className="text-[11px] font-tech uppercase text-[#DFFF00] font-bold tracking-wider">
          {metricTitle}
        </span>
        <span className="text-xs font-tech font-bold text-white/60">
          {playerA.teamAbbr} vs {playerB.teamAbbr}
        </span>
      </div>

      {/* Players Row */}
      <div className="grid grid-cols-2 gap-4 items-center font-tech">
        {/* Player A */}
        <div className="flex items-center gap-2.5">
          <GlassLogoBadge src={playerA.teamLogo} alt={playerA.teamName} size={32} />
          <div className="min-w-0">
            <span className="text-sm font-bold text-white block truncate">{playerA.name}</span>
            <span className="text-[10px] text-white/50 block truncate">{playerA.teamName}</span>
          </div>
        </div>

        {/* Player B */}
        <div className="flex items-center justify-end gap-2.5 text-right">
          <div className="min-w-0">
            <span className="text-sm font-bold text-white block truncate">{playerB.name}</span>
            <span className="text-[10px] text-white/50 block truncate">{playerB.teamName}</span>
          </div>
          <GlassLogoBadge src={playerB.teamLogo} alt={playerB.teamName} size={32} />
        </div>
      </div>

      {/* Duel Comparison Bars */}
      <div className="space-y-2 pt-1">
        <DuelProgressBar valA={playerA.goals} valB={playerB.goals} label="Total Goals" />
        <DuelProgressBar valA={playerA.assists} valB={playerB.assists} label="Total Assists" />
        <DuelProgressBar
          valA={playerA.appearances}
          valB={playerB.appearances}
          label="Match Appearances"
        />
      </div>

      <div className="pt-2 border-t border-white/5 flex items-center justify-between text-[11px] font-tech text-white/40">
        <span>Contributions / Match:</span>
        <span className="text-white font-bold">
          {playerA.perMatch.toFixed(2)} vs {playerB.perMatch.toFixed(2)}
        </span>
      </div>
    </div>
  );
}

export default function TacticalDuelsMomentum() {
  const [selectedLeague, setSelectedLeague] = useState('PL');
  const [scorers, setScorers] = useState<LeaguePlayerLeader[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isCancelled = false;
    async function loadStats() {
      setIsLoading(true);
      const data = await fetchLeaguePlayerStats(selectedLeague);
      if (!isCancelled) {
        setScorers(data.scorers || []);
        setIsLoading(false);
      }
    }
    loadStats();
    return () => {
      isCancelled = true;
    };
  }, [selectedLeague]);

  const p1 = scorers[0];
  const p2 = scorers[1];
  const p3 = scorers[2];
  const p4 = scorers[3];

  return (
    <div className="w-full space-y-6">
      <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
        {/* Header with League Switcher */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <Swords className="w-4 h-4 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
              Head-to-Head Player Statistical Duels
            </h3>
          </div>

          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0 scrollbar-none font-tech text-xs">
            {LEAGUES.map((lg) => (
              <button
                key={lg.code}
                type="button"
                onClick={() => setSelectedLeague(lg.code)}
                className={`px-3 py-1 rounded-lg font-bold uppercase transition-colors cursor-pointer ${
                  selectedLeague === lg.code
                    ? 'bg-[#DFFF00] text-[#0A0A0C]'
                    : 'bg-[#121316] text-white/60 hover:text-white border border-white/5'
                }`}
              >
                {lg.code}
              </button>
            ))}
          </div>
        </div>

        {isLoading ? (
          <div className="py-16 text-center font-tech text-xs text-white/50">
            Calibrating official player telemetry for {selectedLeague}...
          </div>
        ) : scorers.length >= 2 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {p1 && p2 && (
              <PlayerDuelCard
                playerA={p1}
                playerB={p2}
                metricTitle="Title Fight • #1 vs #2 Scorer"
              />
            )}
            {p3 && p4 && (
              <PlayerDuelCard
                playerA={p3}
                playerB={p4}
                metricTitle="Challenger Duel • #3 vs #4 Scorer"
              />
            )}
          </div>
        ) : (
          <div className="py-12 text-center font-tech text-xs text-white/50">
            No player telemetry available for this competition.
          </div>
        )}
      </div>
    </div>
  );
}

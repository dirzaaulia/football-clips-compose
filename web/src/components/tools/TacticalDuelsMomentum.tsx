'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { Swords, Activity, Sparkles } from 'lucide-react';
import { fetchLeaguePlayerStats, LeaguePlayerLeader } from '@/lib/free-stats';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import PlayerRadarSpiderChart from '@/components/tools/PlayerRadarSpiderChart';

const LEAGUES = [
  { code: 'PL', name: 'Premier League' },
  { code: 'PD', name: 'La Liga' },
  { code: 'CL', name: 'Champions League' },
  { code: 'SA', name: 'Serie A' },
  { code: 'BL1', name: 'Bundesliga' },
];

function DuelProgressBar({ valA, valB, label }: { valA: number; valB: number; label: string }) {
  const total = valA + valB;
  const pctA = total > 0 ? Math.round((valA / total) * 100) : 50;
  const pctB = Math.max(0, 100 - pctA);

  return (
    <div className="space-y-1 font-tech">
      <div className="flex justify-between text-xs text-white/60">
        <span className="font-bold text-[#DFFF00]">{valA}</span>
        <span className="text-[10px] uppercase tracking-wider text-white/40">{label}</span>
        <span className="font-bold text-sky-400">{valB}</span>
      </div>
      <div className="w-full h-1.5 bg-[#0D0E11] rounded-full overflow-hidden flex gap-0.5">
        <div className="bg-[#DFFF00] h-full transition-all duration-500" style={{ width: `${pctA}%` }} />
        <div className="bg-sky-400 h-full transition-all duration-500" style={{ width: `${pctB}%` }} />
      </div>
    </div>
  );
}

export default function TacticalDuelsMomentum() {
  const [selectedLeague, setSelectedLeague] = useState('PL');
  const [scorers, setScorers] = useState<LeaguePlayerLeader[]>([]);
  const [playerAId, setPlayerAId] = useState<string>('');
  const [playerBId, setPlayerBId] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isCancelled = false;
    async function loadStats() {
      setIsLoading(true);
      const data = await fetchLeaguePlayerStats(selectedLeague);
      if (!isCancelled) {
        const leaders = data.scorers || [];
        setScorers(leaders);
        if (leaders.length >= 2) {
          setPlayerAId(leaders[0].playerId);
          setPlayerBId(leaders[1].playerId);
        }
        setIsLoading(false);
      }
    }
    loadStats();
    return () => {
      isCancelled = true;
    };
  }, [selectedLeague]);

  const playerA = useMemo(() => scorers.find((p) => p.playerId === playerAId) || scorers[0], [scorers, playerAId]);
  const playerB = useMemo(() => scorers.find((p) => p.playerId === playerBId) || scorers[1] || scorers[0], [scorers, playerBId]);

  return (
    <div className="w-full space-y-6 select-none">
      <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
        {/* Header with League Switcher */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <Swords className="w-4 h-4 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
              Player Statistical Duels &amp; Attacking Efficiency
            </h3>
          </div>

          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0 scrollbar-none font-tech text-xs">
            {LEAGUES.map((lg) => (
              <button
                key={lg.code}
                type="button"
                onClick={() => setSelectedLeague(lg.code)}
                className={`px-3 py-1 rounded-lg font-bold uppercase transition-colors cursor-pointer shrink-0 ${
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

        {/* Dynamic Player Pickers */}
        {scorers.length >= 2 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 font-tech text-xs">
            <div className="space-y-1">
              <label className="text-[10px] uppercase font-bold text-[#DFFF00] tracking-wider block">
                Player A (Benchmark Target)
              </label>
              <select
                value={playerAId}
                onChange={(e) => setPlayerAId(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-[#0D0E11] border border-white/10 text-white font-semibold focus:outline-none focus:border-[#DFFF00]"
              >
                {scorers.map((p) => (
                  <option key={p.playerId} value={p.playerId}>
                    #{p.rank} {p.name} ({p.teamName}) • {p.goals} G
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-[10px] uppercase font-bold text-sky-400 tracking-wider block">
                Player B (Opponent Contender)
              </label>
              <select
                value={playerBId}
                onChange={(e) => setPlayerBId(e.target.value)}
                className="w-full px-3 py-2 rounded-xl bg-[#0D0E11] border border-white/10 text-white font-semibold focus:outline-none focus:border-sky-400"
              >
                {scorers.map((p) => (
                  <option key={p.playerId} value={p.playerId}>
                    #{p.rank} {p.name} ({p.teamName}) • {p.goals} G
                  </option>
                ))}
              </select>
            </div>
          </div>
        )}

        {/* Duel Showcase Card */}
        {isLoading ? (
          <div className="py-16 flex flex-col items-center justify-center gap-2 font-tech text-xs text-white/50 animate-pulse">
            <Activity className="w-5 h-5 text-[#DFFF00] animate-spin" />
            <span>Calibrating Opta-grade player telemetry...</span>
          </div>
        ) : playerA && playerB ? (
          <div className="p-5 rounded-xl bg-[#121316] border border-white/10 space-y-5 shadow-2xl">
            <div className="grid grid-cols-2 gap-4 items-center font-tech">
              <div className="flex items-center gap-3">
                <GlassLogoBadge src={playerA.teamLogo} alt={playerA.teamName} size={40} />
                <div className="min-w-0">
                  <span className="text-base font-bold text-white block truncate">{playerA.name}</span>
                  <span className="text-xs text-[#DFFF00] block truncate">#{playerA.rank} Top Scorer • {playerA.teamName}</span>
                </div>
              </div>
              <div className="flex items-center justify-end gap-3 text-right">
                <div className="min-w-0">
                  <span className="text-base font-bold text-white block truncate">{playerB.name}</span>
                  <span className="text-xs text-sky-400 block truncate">#{playerB.rank} Top Scorer • {playerB.teamName}</span>
                </div>
                <GlassLogoBadge src={playerB.teamLogo} alt={playerB.teamName} size={40} />
              </div>
            </div>

            {/* Duel Metric Bars */}
            <div className="space-y-3 pt-2 border-t border-white/5">
              <DuelProgressBar valA={playerA.goals} valB={playerB.goals} label="League Goals" />
              <DuelProgressBar valA={playerA.assists} valB={playerB.assists} label="Goal Assists" />
              <DuelProgressBar valA={playerA.contributions} valB={playerB.contributions} label="Total G+A Contributions" />
              <DuelProgressBar valA={playerA.appearances} valB={playerB.appearances} label="Appearances" />
            </div>

            {/* Opta / FBref 5-Axis Spider Radar */}
            <PlayerRadarSpiderChart playerA={playerA} playerB={playerB} />

            <div className="pt-3 border-t border-white/5 flex items-center justify-between text-xs font-tech text-white/50">
              <span className="flex items-center gap-1 text-white/70">
                <Sparkles className="w-3.5 h-3.5 text-[#DFFF00]" />
                Direct Goal Contribution Rate / 90:
              </span>
              <span className="font-scoreboard text-base text-white">
                <strong className="text-[#DFFF00]">{playerA.perMatch.toFixed(2)}</strong> vs{' '}
                <strong className="text-sky-400">{playerB.perMatch.toFixed(2)}</strong>
              </span>
            </div>
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

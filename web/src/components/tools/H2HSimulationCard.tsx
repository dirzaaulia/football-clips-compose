'use client';

import React, { useMemo } from 'react';
import { ClubTacticalProfile } from '@/lib/tools-data';
import { Calculator, Sparkles } from 'lucide-react';
import H2HTacticalRadar from '@/components/tools/H2HTacticalRadar';

function factorial(n: number): number {
  if (n <= 1) return 1;
  let res = 1;
  for (let i = 2; i <= n; i++) res *= i;
  return res;
}

function poisson(k: number, lambda: number): number {
  return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
}

export default function H2HSimulationCard({
  teamA,
  teamB,
}: {
  teamA: ClubTacticalProfile;
  teamB: ClubTacticalProfile;
}) {
  const sim = useMemo(() => {
    // Attack strength vs defense conceded
    const lambdaA = Math.max(0.4, (teamA.xGPerMatch * 0.6 + teamB.concededPerMatch * 0.4) * 1.05);
    const lambdaB = Math.max(0.3, teamB.xGPerMatch * 0.6 + teamA.concededPerMatch * 0.4);

    let pHome = 0;
    let pDraw = 0;
    let pAway = 0;
    let pOver25 = 0;
    let maxProb = 0;
    let predictedScore = '1 - 1';

    for (let i = 0; i <= 6; i++) {
      for (let j = 0; j <= 6; j++) {
        const prob = poisson(i, lambdaA) * poisson(j, lambdaB);
        if (i > j) pHome += prob;
        else if (i === j) pDraw += prob;
        else pAway += prob;

        if (i + j > 2.5) pOver25 += prob;
        if (prob > maxProb) {
          maxProb = prob;
          predictedScore = `${i} - ${j}`;
        }
      }
    }

    const total = pHome + pDraw + pAway || 1;
    const homePct = Math.round((pHome / total) * 100);
    const drawPct = Math.round((pDraw / total) * 100);
    const awayPct = Math.max(0, 100 - homePct - drawPct);
    const bttsPct = Math.round((1 - poisson(0, lambdaA)) * (1 - poisson(0, lambdaB)) * 100);
    const over25Pct = Math.round(pOver25 * 100);

    return {
      lambdaA: lambdaA.toFixed(2),
      lambdaB: lambdaB.toFixed(2),
      homePct,
      drawPct,
      awayPct,
      predictedScore,
      bttsPct,
      over25Pct,
    };
  }, [teamA, teamB]);

  return (
    <div className="rounded-2xl bg-[#1B1B1F] border border-white/10 p-5 space-y-4 shadow-xl">
      <div className="flex items-center justify-between border-b border-white/10 pb-3">
        <div className="flex items-center gap-2">
          <Calculator className="w-4 h-4 text-[#DFFF00]" />
          <h4 className="font-scoreboard text-lg text-white uppercase tracking-wider">
            Poisson Expected Score Simulation
          </h4>
        </div>
        <span className="text-[10px] font-tech font-extrabold uppercase px-2 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30">
          Algorithmic Model
        </span>
      </div>

      {/* Win Probability Tri-Bar */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-xs font-tech font-bold">
          <span className="text-[#DFFF00]">{teamA.shortName} Win: {sim.homePct}%</span>
          <span className="text-white/60">Draw: {sim.drawPct}%</span>
          <span className="text-sky-400">{teamB.shortName} Win: {sim.awayPct}%</span>
        </div>
        <div className="w-full h-3 rounded-full bg-[#0D0E11] overflow-hidden flex gap-1 p-0.5 border border-white/5">
          <div className="h-full bg-[#DFFF00] rounded-l-full transition-all duration-500" style={{ width: `${sim.homePct}%` }} />
          <div className="h-full bg-white/30 transition-all duration-500" style={{ width: `${sim.drawPct}%` }} />
          <div className="h-full bg-sky-400 rounded-r-full transition-all duration-500" style={{ width: `${sim.awayPct}%` }} />
        </div>
      </div>

      {/* Analytical Insets */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 font-tech">
        <div className="p-3 rounded-xl bg-[#121316] border border-white/5 space-y-1">
          <span className="text-[10px] uppercase text-white/40 font-bold block">Most Likely Score</span>
          <span className="font-scoreboard text-2xl text-white block">{sim.predictedScore}</span>
          <span className="text-[9px] text-[#DFFF00] flex items-center gap-1">
            <Sparkles className="w-2.5 h-2.5" /> High Confidence
          </span>
        </div>
        <div className="p-3 rounded-xl bg-[#121316] border border-white/5 space-y-1">
          <span className="text-[10px] uppercase text-white/40 font-bold block">Expected Goals (xG)</span>
          <span className="font-scoreboard text-lg text-white block">{sim.lambdaA} vs {sim.lambdaB}</span>
          <span className="text-[9px] text-white/50">Adjusted for opponent</span>
        </div>
        <div className="p-3 rounded-xl bg-[#121316] border border-white/5 space-y-1">
          <span className="text-[10px] uppercase text-white/40 font-bold block">Over 2.5 Goals</span>
          <span className="font-scoreboard text-2xl text-white block">{sim.over25Pct}%</span>
          <span className="text-[9px] text-white/50">Aggregate goal trend</span>
        </div>
        <div className="p-3 rounded-xl bg-[#121316] border border-white/5 space-y-1">
          <span className="text-[10px] uppercase text-white/40 font-bold block">Both Teams To Score</span>
          <span className="font-scoreboard text-2xl text-white block">{sim.bttsPct}%</span>
          <span className="text-[9px] text-white/50">Combined attack index</span>
        </div>
      </div>

      {/* 5-Axis Tactical Radar Benchmark & 15-Minute Phases */}
      <H2HTacticalRadar teamA={teamA} teamB={teamB} />
    </div>
  );
}

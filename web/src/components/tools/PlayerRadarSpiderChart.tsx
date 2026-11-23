'use client';

import React, { useMemo } from 'react';
import { LeaguePlayerLeader } from '@/lib/free-stats';
import { Target, Sparkles } from 'lucide-react';

interface PlayerRadarSpiderChartProps {
  playerA: LeaguePlayerLeader;
  playerB: LeaguePlayerLeader;
}

export default function PlayerRadarSpiderChart({
  playerA,
  playerB,
}: PlayerRadarSpiderChartProps) {
  const radar = useMemo(() => {
    // Calibrate 5 normalized dimensions (0-100)
    const g90A = Math.min(100, Math.round(((playerA.goals / Math.max(1, playerA.appearances)) / 1.1) * 100));
    const g90B = Math.min(100, Math.round(((playerB.goals / Math.max(1, playerB.appearances)) / 1.1) * 100));

    const a90A = Math.min(100, Math.round(((playerA.assists / Math.max(1, playerA.appearances)) / 0.6) * 100));
    const a90B = Math.min(100, Math.round(((playerB.assists / Math.max(1, playerB.appearances)) / 0.6) * 100));

    const contA = Math.min(100, Math.round((playerA.perMatch / 1.4) * 100));
    const contB = Math.min(100, Math.round((playerB.perMatch / 1.4) * 100));

    const appA = Math.min(100, Math.round((playerA.appearances / 38) * 100));
    const appB = Math.min(100, Math.round((playerB.appearances / 38) * 100));

    const volA = Math.min(100, Math.round((playerA.goals / 25) * 100));
    const volB = Math.min(100, Math.round((playerB.goals / 25) * 100));

    return {
      labels: ['Goals/90', 'Assists/90', 'G+A Rate', 'Durability', 'Total Output'],
      valuesA: [g90A, a90A, contA, appA, volA],
      valuesB: [g90B, a90B, contB, appB, volB],
    };
  }, [playerA, playerB]);

  const getPoints = (vals: number[]) => {
    const center = 50;
    const maxR = 38;
    return vals
      .map((v, i) => {
        const angle = (Math.PI * 2 / 5) * i - Math.PI / 2;
        const r = (v / 100) * maxR;
        const x = center + r * Math.cos(angle);
        const y = center + r * Math.sin(angle);
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  };

  const polyA = getPoints(radar.valuesA);
  const polyB = getPoints(radar.valuesB);

  return (
    <div className="p-4 rounded-xl bg-[#0D0E11] border border-white/5 space-y-3 font-tech">
      <div className="flex items-center justify-between border-b border-white/5 pb-2">
        <span className="text-xs font-bold uppercase tracking-wider text-white/70 flex items-center gap-1.5">
          <Target className="w-3.5 h-3.5 text-[#DFFF00]" />
          Opta / FBref Player Radar Polygon
        </span>
        <div className="flex items-center gap-3 text-[10px] font-bold">
          <span className="text-[#DFFF00] flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#DFFF00]" /> {playerA.name}
          </span>
          <span className="text-sky-400 flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-sky-400" /> {playerB.name}
          </span>
        </div>
      </div>

      <div className="relative aspect-square max-w-[240px] mx-auto w-full py-2">
        <svg viewBox="0 0 100 100" className="w-full h-full overflow-visible">
          {/* Concentric Grid Rings */}
          {[0.25, 0.5, 0.75, 1.0].map((ring, idx) => (
            <circle
              key={idx}
              cx="50"
              cy="50"
              r={38 * ring}
              fill="none"
              stroke="rgba(255,255,255,0.08)"
              strokeWidth="0.5"
            />
          ))}

          {/* 5 Spoke Axis Lines & Labels */}
          {radar.labels.map((lbl, i) => {
            const angle = (Math.PI * 2 / 5) * i - Math.PI / 2;
            const x2 = 50 + 38 * Math.cos(angle);
            const y2 = 50 + 38 * Math.sin(angle);
            const lx = 50 + 44 * Math.cos(angle);
            const ly = 50 + 44 * Math.sin(angle);

            return (
              <g key={i}>
                <line x1="50" y1="50" x2={x2} y2={y2} stroke="rgba(255,255,255,0.12)" strokeWidth="0.5" />
                <text
                  x={lx}
                  y={ly + 1}
                  fontSize="2.8"
                  textAnchor="middle"
                  fill="rgba(255,255,255,0.5)"
                  fontWeight="700"
                  className="uppercase font-mono"
                >
                  {lbl}
                </text>
              </g>
            );
          })}

          {/* Translucent Polygons */}
          <polygon points={polyA} fill="#DFFF00" fillOpacity="0.28" stroke="#DFFF00" strokeWidth="1.2" />
          <polygon points={polyB} fill="#38BDF8" fillOpacity="0.28" stroke="#38BDF8" strokeWidth="1.2" />
        </svg>
      </div>
    </div>
  );
}

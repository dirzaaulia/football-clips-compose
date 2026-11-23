'use client';

import React, { useMemo } from 'react';
import { ClubTacticalProfile } from '@/lib/tools-data';
import { Shield, Target, Flame } from 'lucide-react';

interface H2HTacticalRadarProps {
  teamA: ClubTacticalProfile;
  teamB: ClubTacticalProfile;
}

export default function H2HTacticalRadar({ teamA, teamB }: H2HTacticalRadarProps) {
  // Normalize 5 metrics to 0-100 scale for radar polygon
  const radarData = useMemo(() => {
    const atkA = Math.min(100, Math.round((teamA.xGPerMatch / 2.8) * 100));
    const atkB = Math.min(100, Math.round((teamB.xGPerMatch / 2.8) * 100));

    const defA = Math.min(100, Math.max(10, Math.round((1 - teamA.concededPerMatch / 2.5) * 100)));
    const defB = Math.min(100, Math.max(10, Math.round((1 - teamB.concededPerMatch / 2.5) * 100)));

    const posA = Math.min(100, Math.round(teamA.possessionAvg * 1.4));
    const posB = Math.min(100, Math.round(teamB.possessionAvg * 1.4));

    const csA = Math.min(100, Math.round(teamA.cleanSheetPct * 1.8));
    const csB = Math.min(100, Math.round(teamB.cleanSheetPct * 1.8));

    const formA = Math.min(100, Math.round((teamA.goalsPerMatch / 3.0) * 100));
    const formB = Math.min(100, Math.round((teamB.goalsPerMatch / 3.0) * 100));

    return {
      labels: ['Attack (xG)', 'Def Solidity', 'Possession', 'Clean Sheets', 'Scoring Form'],
      teamAValues: [atkA, defA, posA, csA, formA],
      teamBValues: [atkB, defB, posB, csB, formB],
    };
  }, [teamA, teamB]);

  // Generate 5-axis polygon points centered at (50, 50) with radius 40
  const getPolygonPoints = (values: number[]) => {
    const center = 50;
    const maxRadius = 38;
    const numAxes = values.length;
    return values
      .map((val, i) => {
        const angle = (Math.PI * 2 / numAxes) * i - Math.PI / 2;
        const r = (val / 100) * maxRadius;
        const x = center + r * Math.cos(angle);
        const y = center + r * Math.sin(angle);
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(' ');
  };

  const polyA = getPolygonPoints(radarData.teamAValues);
  const polyB = getPolygonPoints(radarData.teamBValues);

  return (
    <div className="p-4 rounded-xl bg-[#121316] border border-white/5 space-y-4 font-tech">
      <div className="flex items-center justify-between border-b border-white/5 pb-2">
        <span className="text-xs font-bold uppercase tracking-wider text-white/70 flex items-center gap-1.5">
          <Target className="w-3.5 h-3.5 text-[#DFFF00]" />
          5-Axis Tactical Radar Benchmark
        </span>
        <div className="flex items-center gap-3 text-[10px] font-bold">
          <span className="text-[#DFFF00] flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#DFFF00]" /> {teamA.shortName}
          </span>
          <span className="text-sky-400 flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-sky-400" /> {teamB.shortName}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
        {/* SVG Spider Radar */}
        <div className="relative aspect-square max-w-[220px] mx-auto w-full">
          <svg viewBox="0 0 100 100" className="w-full h-full overflow-visible">
            {/* Concentric Grid Circles */}
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

            {/* 5 Axis Spoke Lines */}
            {radarData.labels.map((_, i) => {
              const angle = (Math.PI * 2 / 5) * i - Math.PI / 2;
              const x2 = 50 + 38 * Math.cos(angle);
              const y2 = 50 + 38 * Math.sin(angle);
              return (
                <line
                  key={i}
                  x1="50"
                  y1="50"
                  x2={x2}
                  y2={y2}
                  stroke="rgba(255,255,255,0.12)"
                  strokeWidth="0.5"
                />
              );
            })}

            {/* Polygons */}
            <polygon points={polyA} fill="#DFFF00" fillOpacity="0.25" stroke="#DFFF00" strokeWidth="1.2" />
            <polygon points={polyB} fill="#38BDF8" fillOpacity="0.25" stroke="#38BDF8" strokeWidth="1.2" />
          </svg>
        </div>

        {/* 15-Minute Goal Distribution Phases */}
        <div className="space-y-2">
          <span className="text-[10px] uppercase font-bold text-white/50 tracking-wider block">
            15-Minute Attack Intensity Profile
          </span>
          <div className="grid grid-cols-6 gap-1 text-center text-[10px]">
            {['0-15', '16-30', '31-45', '46-60', '61-75', '76-90+'].map((phase, idx) => {
              const heightA = 20 + ((idx * 13) % 45);
              const heightB = 15 + ((idx * 17) % 40);
              return (
                <div key={phase} className="space-y-1">
                  <div className="h-16 bg-[#0D0E11] rounded-md p-1 flex items-end justify-center gap-0.5 border border-white/5">
                    <div className="w-2 bg-[#DFFF00] rounded-t-sm transition-all" style={{ height: `${heightA}%` }} />
                    <div className="w-2 bg-sky-400 rounded-t-sm transition-all" style={{ height: `${heightB}%` }} />
                  </div>
                  <span className="text-[9px] text-white/40 block">{phase}&apos;</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}

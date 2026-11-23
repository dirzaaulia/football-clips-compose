'use client';

import React, { useMemo } from 'react';
import { WebPitchPlay } from './WebPitchRadar';
import { calculateShotXg } from './WebPitchXgHelper';
import { calculateShotDistanceMeters } from './WebPitchShotMapOverlay';
import { Target, Activity } from 'lucide-react';

interface WebPitchCanvasHUDProps {
  plays: WebPitchPlay[];
  homeTeam: string;
  awayTeam: string;
  homeColor: string;
  awayColor: string;
  viewMode: 'FORMATIONS' | 'SHOTS' | 'ZONES';
}

export default function WebPitchCanvasHUD({
  plays,
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
  viewMode,
}: WebPitchCanvasHUDProps) {
  const stats = useMemo(() => {
    let hXg = 0;
    let aXg = 0;
    let hShots = 0;
    let aShots = 0;
    let hDistSum = 0;
    let aDistSum = 0;

    plays.forEach((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      if (!t.includes('shot') && !t.includes('goal') && !t.includes('attempt')) return;

      const isHome = p.team_name
        ? p.team_name.toLowerCase().includes(homeTeam.toLowerCase())
        : (p.period !== 2);

      const xg = calculateShotXg(p, isHome);
      const dist = calculateShotDistanceMeters(p.x_percent ?? 50, p.y_percent ?? 34, isHome);

      if (isHome) {
        hXg += xg;
        hShots++;
        hDistSum += dist;
      } else {
        aXg += xg;
        aShots++;
        aDistSum += dist;
      }
    });

    const hAvgDist = hShots > 0 ? (hDistSum / hShots).toFixed(1) : '16.5';
    const aAvgDist = aShots > 0 ? (aDistSum / aShots).toFixed(1) : '18.2';

    return {
      hXg: hXg.toFixed(2),
      aXg: aXg.toFixed(2),
      hShots,
      aShots,
      hAvgDist,
      aAvgDist,
    };
  }, [plays, homeTeam]);

  if (viewMode !== 'SHOTS') return null;

  return (
    <>
      {/* Top HUD Ribbon: Match Shooting Telemetry */}
      <div className="absolute top-2.5 left-2.5 right-2.5 z-20 flex items-center justify-between px-3.5 py-2 rounded-xl bg-[#0B0C10]/90 backdrop-blur-md border border-white/10 font-tech text-xs pointer-events-none shadow-lg">
        <div className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: homeColor }} />
          <span className="font-extrabold text-white uppercase">{homeTeam.split(' ')[0]}</span>
          <span className="px-2 py-0.5 rounded bg-white/10 font-black text-[#DFFF00]">
            {stats.hXg} xG
          </span>
          <span className="text-white/80 hidden sm:inline font-medium">
            ({stats.hShots} shots • avg {stats.hAvgDist}m)
          </span>
        </div>

        <div className="flex items-center gap-1.5 text-white/70 uppercase font-black tracking-widest text-xs">
          <Activity className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Opta Shot Dossier</span>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-white/80 hidden sm:inline font-medium">
            (avg {stats.aAvgDist}m • {stats.aShots} shots)
          </span>
          <span className="px-2 py-0.5 rounded bg-white/10 font-black text-sky-400">
            {stats.aXg} xG
          </span>
          <span className="font-extrabold text-white uppercase">{awayTeam.split(' ')[0]}</span>
          <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: awayColor }} />
        </div>
      </div>

      {/* Bottom-Left Opta Legend */}
      <div className="absolute bottom-2.5 left-2.5 z-20 flex flex-wrap items-center gap-2.5 px-3 py-1.5 rounded-xl bg-[#0B0C10]/90 backdrop-blur-md border border-white/10 font-tech text-xs pointer-events-none text-white/85 shadow-lg font-bold">
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#DFFF00]" /> Goal
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#38BDF8]" /> Saved
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#F59E0B]" /> Blocked
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#A855F7]" /> Post
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#FF2E4D]" /> Missed
        </span>
        <span className="text-white/70 ml-1 hidden sm:inline font-medium">
          • Size = Chance xG • Vector = Trajectory
        </span>
      </div>
    </>
  );
}

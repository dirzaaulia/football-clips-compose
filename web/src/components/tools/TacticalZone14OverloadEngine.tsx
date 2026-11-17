'use client';

import React, { useState } from 'react';
import { Network, Activity, Crosshair, Compass } from 'lucide-react';

type CorridorType = 'zone14' | 'leftHalf' | 'rightHalf' | 'shotAssists';

interface ClubProfile {
  id: string;
  name: string;
  league: string;
  zone14Infiltration: number;
  passRetention: string;
  leftFlankPct: number;
  zone14Pct: number;
  rightFlankPct: number;
  xgCreated: string;
  boxEntries: number;
}

const CLUBS: ClubProfile[] = [
  {
    id: 'mci',
    name: 'Manchester City',
    league: 'Premier League',
    zone14Infiltration: 48,
    passRetention: '91.4%',
    leftFlankPct: 28,
    zone14Pct: 46,
    rightFlankPct: 26,
    xgCreated: '2.42 xG',
    boxEntries: 34,
  },
  {
    id: 'rma',
    name: 'Real Madrid',
    league: 'La Liga',
    zone14Infiltration: 42,
    passRetention: '89.6%',
    leftFlankPct: 42,
    zone14Pct: 38,
    rightFlankPct: 20,
    xgCreated: '2.18 xG',
    boxEntries: 31,
  },
  {
    id: 'ars',
    name: 'Arsenal',
    league: 'Premier League',
    zone14Infiltration: 44,
    passRetention: '88.8%',
    leftFlankPct: 30,
    zone14Pct: 42,
    rightFlankPct: 28,
    xgCreated: '2.05 xG',
    boxEntries: 29,
  },
  {
    id: 'bar',
    name: 'FC Barcelona',
    league: 'La Liga',
    zone14Infiltration: 46,
    passRetention: '90.2%',
    leftFlankPct: 34,
    zone14Pct: 44,
    rightFlankPct: 22,
    xgCreated: '2.35 xG',
    boxEntries: 33,
  },
];

export default function TacticalZone14OverloadEngine() {
  const [activeCorridor, setActiveCorridor] = useState<CorridorType>('zone14');
  const [selectedClubId, setSelectedClubId] = useState('mci');

  const currentClub = CLUBS.find((c) => c.id === selectedClubId) || CLUBS[0];

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Network className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Attacking Zones &amp; Final Third Breakdown
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase self-start sm:self-auto">
          OPTA SPATIAL GRID
        </span>
      </div>

      {/* Club Selector Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none font-tech text-xs">
        <span className="text-white/40 uppercase font-bold text-[10px] shrink-0">Club Benchmark:</span>
        {CLUBS.map((c) => (
          <button
            key={c.id}
            type="button"
            onClick={() => setSelectedClubId(c.id)}
            className={`px-3 py-1 rounded-lg font-bold uppercase transition-colors cursor-pointer shrink-0 ${
              selectedClubId === c.id
                ? 'bg-[#DFFF00] text-[#0A0A0C]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/5'
            }`}
          >
            {c.name}
          </button>
        ))}
      </div>

      {/* Corridor Switch Buttons */}
      <div className="flex flex-wrap gap-2">
        {(['zone14', 'leftHalf', 'rightHalf', 'shotAssists'] as CorridorType[]).map((type) => (
          <button
            key={type}
            type="button"
            onClick={() => setActiveCorridor(type)}
            className={`px-3 py-1.5 rounded-xl font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              activeCorridor === type
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/10'
            }`}
          >
            {type === 'zone14'
              ? 'Zone 14 Corridor'
              : type === 'leftHalf'
              ? 'Left Wing Channel'
              : type === 'rightHalf'
              ? 'Right Wing Channel'
              : 'Penalty Box Entries'}
          </button>
        ))}
      </div>

      {/* Pitch Simulator View */}
      <div className="relative aspect-[16/9] w-full rounded-xl bg-[#0B0C10] border border-white/10 overflow-hidden flex flex-col justify-between p-4">
        {/* Alternating Dark Turf Bands */}
        <div className="absolute inset-0 opacity-20 pointer-events-none flex">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className={`flex-1 h-full ${i % 2 === 0 ? 'bg-white/5' : 'bg-transparent'}`}
            />
          ))}
        </div>

        {/* Pitch Lines */}
        <div className="absolute inset-3 border border-white/20 rounded-lg pointer-events-none">
          <div className="absolute left-1/2 top-0 bottom-0 w-px bg-white/20" />
          <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-16 h-16 rounded-full border border-white/20" />
          <div className="absolute right-0 top-1/2 -translate-y-1/2 w-14 h-24 border-l border-t border-b border-white/20" />

          {/* Active Highlight Corridor */}
          <div
            className={`absolute border rounded-lg transition-all duration-300 flex items-center justify-center ${
              activeCorridor === 'zone14'
                ? 'right-14 top-1/2 -translate-y-1/2 w-16 h-20 bg-[#DFFF00]/15 border-dashed border-[#DFFF00]/60'
                : activeCorridor === 'leftHalf'
                ? 'right-14 top-2 h-16 w-28 bg-sky-400/15 border-sky-400/60'
                : activeCorridor === 'rightHalf'
                ? 'right-14 bottom-2 h-16 w-28 bg-emerald-500/15 border-emerald-500/60'
                : 'right-3 top-1/2 -translate-y-1/2 w-24 h-28 bg-rose-500/15 border-rose-500/60'
            }`}
          >
            <span className="text-[9px] font-tech font-extrabold uppercase text-[#DFFF00] tracking-wider px-2 py-0.5 rounded bg-black/60">
              {activeCorridor === 'zone14'
                ? 'ZONE 14'
                : activeCorridor === 'leftHalf'
                ? 'LEFT FLANK'
                : activeCorridor === 'rightHalf'
                ? 'RIGHT FLANK'
                : 'BOX ENTRY'}
            </span>
          </div>
        </div>

        {/* Telemetry Stats Overlay Top */}
        <div className="relative z-10 flex items-center justify-between font-tech text-xs">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#121316]/90 border border-white/10 text-white/70">
            <Compass className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>{currentClub.name} Attack Profile</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-1 rounded-lg bg-[#121316]/90 border border-white/10 text-white">
              {currentClub.passRetention} Pass Accuracy
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-[#121316]/90 border border-[#DFFF00]/40 text-[#DFFF00] font-bold">
              {currentClub.xgCreated}
            </span>
          </div>
        </div>

        {/* Telemetry Stats Overlay Bottom */}
        <div className="relative z-10 flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-white/5 font-tech text-xs">
          <div className="flex items-center gap-2 text-white/60">
            <Crosshair className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>Infiltration Volume:</span>
            <strong className="text-white">{currentClub.zone14Infiltration} entries</strong>
          </div>
          <div className="flex items-center gap-3 text-[11px] text-white/50">
            <span>Left: <strong className="text-white">{currentClub.leftFlankPct}%</strong></span>
            <span>Zone 14: <strong className="text-[#DFFF00]">{currentClub.zone14Pct}%</strong></span>
            <span>Right: <strong className="text-white">{currentClub.rightFlankPct}%</strong></span>
          </div>
        </div>
      </div>
    </div>
  );
}

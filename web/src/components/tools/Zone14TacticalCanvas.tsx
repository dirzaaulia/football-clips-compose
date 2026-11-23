'use client';

import React from 'react';
import { Compass, Crosshair, Zap, AlertTriangle } from 'lucide-react';

export type CorridorType = 'zone14' | 'leftHalf' | 'rightHalf' | 'leftWing' | 'rightWing' | 'box';

export interface DynamicClubTactics {
  id: string;
  name: string;
  shortName: string;
  league: string;
  zone14Infiltration: number;
  passRetention: string;
  leftFlankPct: number;
  zone14Pct: number;
  rightFlankPct: number;
  xgCreated: string;
  boxEntries: number;
}

export default function Zone14TacticalCanvas({
  currentClub,
  activeCorridor,
}: {
  currentClub: DynamicClubTactics;
  activeCorridor: CorridorType;
}) {
  const dangerRating = activeCorridor === 'zone14'
    ? '8.9 / 10 • Critical Corridor'
    : activeCorridor === 'box'
    ? '9.4 / 10 • Maximum Threat'
    : activeCorridor.includes('Half')
    ? '7.8 / 10 • Half-Space Overload'
    : '6.9 / 10 • Flank Crossing Channel';

  return (
    <div className="relative aspect-[16/9] w-full rounded-xl bg-[#0B0C10] border border-white/10 overflow-hidden flex flex-col justify-between p-4 shadow-2xl">
      {/* Alternating Dark Turf Bands */}
      <div className="absolute inset-0 opacity-20 pointer-events-none flex">
        {[...Array(6)].map((_, i) => (
          <div key={i} className={`flex-1 h-full ${i % 2 === 0 ? 'bg-white/5' : 'bg-transparent'}`} />
        ))}
      </div>

      {/* Pitch Lines & Goal Areas */}
      <div className="absolute inset-3 border border-white/20 rounded-lg pointer-events-none">
        <div className="absolute left-1/2 top-0 bottom-0 w-px bg-white/20" />
        <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-16 h-16 rounded-full border border-white/20" />
        <div className="absolute right-0 top-1/2 -translate-y-1/2 w-16 h-28 border-l border-t border-b border-white/20" />
        <div className="absolute right-0 top-1/2 -translate-y-1/2 w-6 h-14 border-l border-t border-b border-white/20" />

        {/* 5-Corridor Highlight Overlay */}
        <CorridorHighlight activeCorridor={activeCorridor} />
      </div>

      {/* Top HUD Stats Overlay */}
      <div className="relative z-10 flex items-center justify-between font-tech text-xs">
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#121316]/90 border border-white/10 text-white/70">
          <Compass className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>{currentClub.name} Spatial Attack Profile</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="px-2.5 py-1 rounded-lg bg-[#121316]/90 border border-white/10 text-white">
            {currentClub.passRetention} Retention
          </span>
          <span className="px-2.5 py-1 rounded-lg bg-[#121316]/90 border border-[#DFFF00]/40 text-[#DFFF00] font-bold">
            {currentClub.xgCreated}
          </span>
        </div>
      </div>

      {/* Bottom HUD Metrics Overlay */}
      <div className="relative z-10 flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-white/5 font-tech text-xs">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5 text-white/70">
            <Crosshair className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>Volume: <strong className="text-white">{currentClub.zone14Infiltration} entries</strong></span>
          </div>
          <span className="px-2 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 font-bold text-[10px]">
            {dangerRating}
          </span>
        </div>
        <div className="flex items-center gap-3 text-[11px] text-white/50">
          <span>Left Wing: <strong className="text-white">{currentClub.leftFlankPct}%</strong></span>
          <span>Zone 14: <strong className="text-[#DFFF00]">{currentClub.zone14Pct}%</strong></span>
          <span>Right Wing: <strong className="text-white">{currentClub.rightFlankPct}%</strong></span>
        </div>
      </div>
    </div>
  );
}

function CorridorHighlight({ activeCorridor }: { activeCorridor: CorridorType }) {
  const getStyle = () => {
    switch (activeCorridor) {
      case 'zone14':
        return 'right-16 top-1/2 -translate-y-1/2 w-16 h-20 bg-[#DFFF00]/15 border-dashed border-[#DFFF00]/70';
      case 'leftHalf':
        return 'right-16 top-2 h-14 w-24 bg-sky-400/15 border-dashed border-sky-400/60';
      case 'rightHalf':
        return 'right-16 bottom-2 h-14 w-24 bg-emerald-500/15 border-dashed border-emerald-500/60';
      case 'leftWing':
        return 'right-4 top-1 h-10 w-36 bg-amber-500/15 border-amber-500/50';
      case 'rightWing':
        return 'right-4 bottom-1 h-10 w-36 bg-amber-500/15 border-amber-500/50';
      default:
        return 'right-2 top-1/2 -translate-y-1/2 w-20 h-28 bg-rose-500/15 border-rose-500/70';
    }
  };

  const getLabel = () => {
    switch (activeCorridor) {
      case 'zone14':
        return 'ZONE 14 CENTRAL DANGER';
      case 'leftHalf':
        return 'LEFT HALF-SPACE CHANNEL';
      case 'rightHalf':
        return 'RIGHT HALF-SPACE CHANNEL';
      case 'leftWing':
        return 'LEFT FLANK OVERLAP';
      case 'rightWing':
        return 'RIGHT FLANK OVERLAP';
      default:
        return 'PENALTY BOX PENETRATION';
    }
  };

  return (
    <div className={`absolute border rounded-lg transition-all duration-300 flex items-center justify-center ${getStyle()}`}>
      <span className="text-[9px] font-tech font-extrabold uppercase text-[#DFFF00] tracking-wider px-2 py-0.5 rounded bg-black/75 shadow-lg">
        {getLabel()}
      </span>
    </div>
  );
}

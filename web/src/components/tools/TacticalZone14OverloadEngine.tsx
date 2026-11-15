'use client';

import React, { useState } from 'react';
import { Network, Activity, Crosshair, ArrowRight } from 'lucide-react';

type CorridorType = 'zone14' | 'leftHalf' | 'rightHalf' | 'shotAssists';

export default function TacticalZone14OverloadEngine() {
  const [activeCorridor, setActiveCorridor] = useState<CorridorType>('zone14');

  const corridorData = {
    zone14: {
      title: 'Zone 14 Saturation',
      desc: 'Central 20m high-danger corridor outside the 18-yard box. 42 progressive entries, 84% pass completion.',
      stat1: '42 Entries',
      stat2: '0.84 xG Chain',
    },
    leftHalf: {
      title: 'Left Half-Space Overload',
      desc: 'Dynamic isolations down the left channel pinning the opposition right-back in 2v1 overloads.',
      stat1: '28 Overloads',
      stat2: '14 Cutbacks',
    },
    rightHalf: {
      title: 'Right Half-Space Underlap',
      desc: 'Midfield rotations creating space for direct through-balls between center-back and full-back.',
      stat1: '19 Rotations',
      stat2: '6 Key Passes',
    },
    shotAssists: {
      title: 'Key Shot Assists',
      desc: 'Penetrative final balls delivered inside the penalty box leading directly to shots on target.',
      stat1: '8 Big Chances',
      stat2: '2.14 Total xG',
    },
  };

  const current = corridorData[activeCorridor];

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <Network className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Zone 14 &amp; Half-Space Overload Engine
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase self-start sm:self-auto">
          Opta v4.8 Spatial Feed
        </span>
      </div>

      {/* Corridor Switch Buttons */}
      <div className="flex flex-wrap gap-2">
        {(['zone14', 'leftHalf', 'rightHalf', 'shotAssists'] as CorridorType[]).map((type) => (
          <button
            key={type}
            onClick={() => setActiveCorridor(type)}
            className={`px-3 py-1.5 rounded-xl font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              activeCorridor === type
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/10'
            }`}
          >
            {type === 'zone14'
              ? 'Zone 14 Saturation'
              : type === 'leftHalf'
              ? 'Left Half-Space'
              : type === 'rightHalf'
              ? 'Right Half-Space'
              : 'Shot Assists'}
          </button>
        ))}
      </div>

      {/* Pitch Simulator View */}
      <div className="relative aspect-[16/9] w-full rounded-xl bg-[#0A0A0C] border border-white/10 overflow-hidden flex flex-col justify-between p-4">
        {/* Pitch Lines */}
        <div className="absolute inset-4 border border-white/15 rounded-lg pointer-events-none">
          <div className="absolute left-1/2 top-0 bottom-0 w-px bg-white/15" />
          <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-20 h-20 rounded-full border border-white/15" />
          {/* Active Highlight Corridor */}
          <div
            className={`absolute top-2 bottom-2 border rounded-lg transition-all duration-300 flex items-center justify-center ${
              activeCorridor === 'zone14'
                ? 'right-14 w-28 bg-[#DFFF00]/15 border-[#DFFF00]/60'
                : activeCorridor === 'leftHalf'
                ? 'right-14 top-2 h-20 w-32 bg-[#38BDF8]/15 border-[#38BDF8]/60'
                : activeCorridor === 'rightHalf'
                ? 'right-14 bottom-2 h-20 w-32 bg-emerald-500/15 border-emerald-500/60'
                : 'right-4 w-40 bg-[#FF2E4D]/15 border-[#FF2E4D]/60'
            }`}
          >
            <span className="text-[10px] font-tech font-bold uppercase text-[#DFFF00] tracking-wider px-2 py-0.5 rounded bg-black/60">
              {current.title}
            </span>
          </div>
        </div>

        {/* Telemetry Stats Overlay */}
        <div className="relative z-10 flex items-center justify-between">
          <span className="text-[10px] font-tech text-white/50 uppercase tracking-wider">
            Spatial Radar Active
          </span>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-1 rounded-lg bg-[#1A1C22]/90 border border-white/10 font-tech text-xs text-white">
              {current.stat1}
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-[#1A1C22]/90 border border-[#DFFF00]/40 font-tech text-xs text-[#DFFF00] font-bold">
              {current.stat2}
            </span>
          </div>
        </div>

        <p className="relative z-10 text-xs text-white/70 max-w-lg leading-relaxed bg-[#0A0A0C]/80 p-2 rounded-lg border border-white/5">
          {current.desc}
        </p>
      </div>
    </div>
  );
}

'use client';

import React from 'react';
import { Network, Activity, Crosshair } from 'lucide-react';

export default function AnalysisZone14Card() {
  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Network className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Passing Network &amp; Zone 14 Infiltration
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase">
          LIDAR 25 FPS
        </span>
      </div>

      <p className="text-xs text-white/60 leading-relaxed">
        Spatial pass velocity mapping showing progressive carry entries into Zone 14 (central 20m area outside the 18-yard box).
      </p>

      {/* Mini Pitch Simulation Box */}
      <div className="relative aspect-[16/9] w-full rounded-xl bg-[#0A0A0C] border border-white/10 overflow-hidden flex items-center justify-center">
        {/* Pitch Lines */}
        <div className="absolute inset-4 border border-white/20 rounded-lg">
          <div className="absolute left-1/2 top-0 bottom-0 w-px bg-white/20" />
          <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-16 h-16 rounded-full border border-white/20" />
          {/* Zone 14 Highlighting Area */}
          <div className="absolute right-12 top-1/2 -translate-y-1/2 w-20 h-16 bg-[#DFFF00]/15 border border-[#DFFF00]/50 rounded flex items-center justify-center">
            <span className="text-[9px] font-tech font-bold text-[#DFFF00]">ZONE 14</span>
          </div>
        </div>

        {/* Telemetry Dots & Velocity Arrows */}
        <div className="relative z-10 flex items-center gap-4 text-xs font-tech text-white">
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#1A1C22]/90 border border-[#DFFF00]/40">
            <Crosshair className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>42 Central Infiltrations</span>
          </div>
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#1A1C22]/90 border border-white/10">
            <Activity className="w-3.5 h-3.5 text-[#38BDF8]" />
            <span>89.2% Pass Retention</span>
          </div>
        </div>
      </div>
    </div>
  );
}

'use client';

import React from 'react';
import { Gamepad2, ArrowRight, ShieldCheck } from 'lucide-react';

export default function StandingsPartnerBanner() {
  return (
    <div className="relative w-full rounded-2xl overflow-hidden bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] border border-white/10 p-4 sm:p-6 shadow-xl">
      <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-col gap-1.5 max-w-2xl">
          <div className="flex items-center gap-2 text-[10px] font-tech uppercase tracking-wider text-white/50">
            <ShieldCheck className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>Official Gaming Partner • PlayStation 5 Pro × UEFA Champions League</span>
          </div>
          <h3 className="font-scoreboard text-2xl sm:text-3xl text-white tracking-wide uppercase leading-none">
            Feel Every Blade of Grass in 4K 120FPS
          </h3>
          <p className="text-xs sm:text-sm text-white/70 line-clamp-2">
            Witness the hybrid 36-team tournament phase with instant ray-traced spatial audio and ultra-low latency streams.
          </p>
        </div>

        <div className="flex items-center gap-3 shrink-0">
          <a
            href="https://www.playstation.com"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-tech text-xs font-bold uppercase tracking-wider transition-all shadow-[0_0_16px_rgba(223,255,0,0.25)] cursor-pointer"
          >
            <Gamepad2 className="w-4 h-4" />
            <span>Explore DualSense Edge</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </div>
  );
}

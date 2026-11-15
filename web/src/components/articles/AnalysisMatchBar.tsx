'use client';

import React, { useState } from 'react';
import { Swords } from 'lucide-react';

const FEATURED_MATCHES = [
  { id: 'rma-mci', label: 'RMA 3-3 MCI', full: 'Real Madrid vs Manchester City', venue: 'Santiago Bernabéu' },
  { id: 'ars-bay', label: 'ARS 2-2 BAY', full: 'Arsenal vs Bayern Munich', venue: 'Emirates Stadium' },
  { id: 'psg-bar', label: 'PSG 2-3 BAR', full: 'Paris SG vs FC Barcelona', venue: 'Parc des Princes' },
  { id: 'int-atm', label: 'INT 1-0 ATM', full: 'Inter Milan vs Atlético Madrid', venue: 'San Siro' },
];

export default function AnalysisMatchBar() {
  const [selectedMatch, setSelectedMatch] = useState('rma-mci');
  const current = FEATURED_MATCHES.find((m) => m.id === selectedMatch) || FEATURED_MATCHES[0];

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-4 sm:p-5 shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
      {/* Current Match Headline */}
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-2 text-[10px] font-tech text-[#DFFF00] uppercase tracking-widest font-bold">
          <Swords className="w-3.5 h-3.5" />
          <span>MATCH DEEP DIVE • UEFA ELITE MATRIX</span>
        </div>
        <h2 className="font-scoreboard text-2xl sm:text-3xl text-white uppercase tracking-wide leading-none">
          {current.full}
        </h2>
        <span className="text-xs text-white/50 font-tech">
          {current.venue} • Official UEFA Tactical Dossier
        </span>
      </div>

      {/* Quick Switch Pills */}
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-[10px] font-tech text-white/40 uppercase font-bold mr-1">
          Quick Switch:
        </span>
        {FEATURED_MATCHES.map((m) => (
          <button
            key={m.id}
            onClick={() => setSelectedMatch(m.id)}
            className={`px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              selectedMatch === m.id
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                : 'bg-[#121316] text-white/70 hover:text-white border border-white/10'
            }`}
          >
            {m.label}
          </button>
        ))}
      </div>
    </div>
  );
}

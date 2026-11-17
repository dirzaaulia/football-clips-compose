'use client';

import React from 'react';
import { Target, Users, Shield, X } from 'lucide-react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchRadarControlsProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay | null) => void;
  viewMode: 'FORMATIONS' | 'SHOTS' | 'ZONES';
  onSelectViewMode: (mode: 'FORMATIONS' | 'SHOTS' | 'ZONES') => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  onSelectHeatmapTeam: (t: 'ALL' | 'HOME' | 'AWAY') => void;
  homeTeam: string;
  awayTeam: string;
  selectedPlayerName?: string | null;
  onClearPlayerFilter?: () => void;
}

export default function WebPitchRadarControls({
  plays,
  viewMode,
  onSelectViewMode,
  heatmapTeam,
  onSelectHeatmapTeam,
  homeTeam,
  awayTeam,
  selectedPlayerName,
  onClearPlayerFilter,
}: WebPitchRadarControlsProps) {
  const shotCount = plays.filter((p) => {
    const t = (p.type_text || p.type || '').toLowerCase();
    return t.includes('shot') || t.includes('attempt');
  }).length;

  const goalCount = plays.filter((p) => {
    const t = (p.type_text || p.type || '').toLowerCase();
    return t.includes('goal');
  }).length;

  return (
    <div className="space-y-3 font-tech select-none">
      {/* 3-Mode Primary Segmented Switcher */}
      <div className="grid grid-cols-3 gap-1 p-1 bg-[#0A0A0C] border border-white/10 rounded-xl">
        <button
          type="button"
          onClick={() => onSelectViewMode('FORMATIONS')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'FORMATIONS'
              ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
              : 'text-white/60 hover:text-white'
          }`}
        >
          <Users className="w-3.5 h-3.5" />
          <span>Tactical Shape</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('SHOTS')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'SHOTS'
              ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
              : 'text-white/60 hover:text-white'
          }`}
        >
          <Target className="w-3.5 h-3.5" />
          <span>Shot &amp; Goal Map</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('ZONES')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'ZONES'
              ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
              : 'text-white/60 hover:text-white'
          }`}
        >
          <Shield className="w-3.5 h-3.5" />
          <span>Zone 14 &amp; Thirds</span>
        </button>
      </div>

      {/* Mode 1: Tactical Formations Sub-Bar */}
      {viewMode === 'FORMATIONS' && (
        <div className="flex items-center justify-between px-3.5 py-2 bg-[#0A0A0C] border border-white/10 rounded-xl text-xs">
          <span className="text-[11px] text-white/50">
            Tap or hover any player jersey on the pitch to inspect tactical lineup details.
          </span>
          {selectedPlayerName && (
            <div className="flex items-center gap-1.5 bg-[#DFFF00]/15 border border-[#DFFF00]/30 px-2 py-0.5 rounded-lg text-[10px] text-[#DFFF00] font-bold">
              <span>{selectedPlayerName}</span>
              <button type="button" onClick={onClearPlayerFilter} className="hover:text-rose-400 cursor-pointer">
                <X className="w-3 h-3" />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Mode 2: Shot Map Sub-Bar */}
      {viewMode === 'SHOTS' && (
        <div className="flex items-center justify-between px-3.5 py-2 bg-[#0A0A0C] border border-white/10 rounded-xl text-xs">
          <div className="flex items-center gap-4 text-[11px] font-bold text-white/70">
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#DFFF00]" />
              <span>Goals: <strong className="text-[#DFFF00]">{goalCount}</strong></span>
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#38BDF8]" />
              <span>Attempts: <strong className="text-[#38BDF8]">{shotCount}</strong></span>
            </span>
          </div>
          <span className="text-[10px] text-white/40 hidden sm:inline">
            Click any shot node on the pitch to trace goal vector
          </span>
        </div>
      )}

      {/* Mode 3: Tactical Zones Sub-Bar */}
      {viewMode === 'ZONES' && (
        <div className="flex flex-wrap items-center justify-between gap-2 bg-[#0A0A0C] border border-white/10 rounded-xl px-3 py-2 text-xs">
          <div className="flex items-center gap-1.5">
            <span className="text-[10px] text-white/40 font-bold uppercase">Focus:</span>
            {(['ALL', 'HOME', 'AWAY'] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => onSelectHeatmapTeam(t)}
                className={`px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase transition-all cursor-pointer ${
                  heatmapTeam === t
                    ? 'bg-[#DFFF00] text-[#0A0A0C] font-black'
                    : 'bg-[#16181D] text-white/60 hover:text-white border border-white/5'
                }`}
              >
                {t === 'ALL' ? 'Both Zones' : t === 'HOME' ? homeTeam.split(' ')[0] : awayTeam.split(' ')[0]}
              </button>
            ))}
          </div>
          <span className="text-[10px] text-white/40">
            Zone 14 highlights pivotal area outside penalty box
          </span>
        </div>
      )}
    </div>
  );
}

'use client';

import React, { useMemo } from 'react';
import { Target, Users, Shield, X, Info } from 'lucide-react';
import { WebPitchPlay } from './WebPitchRadar';

export type ShotFilterType = 'ALL' | 'HOME' | 'AWAY' | 'GOALS';

interface WebPitchRadarControlsProps {
  plays: WebPitchPlay[];
  allPlays?: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay | null) => void;
  onJumpToMinute?: (minute: number) => void;
  viewMode: 'FORMATIONS' | 'SHOTS' | 'ZONES';
  onSelectViewMode: (mode: 'FORMATIONS' | 'SHOTS' | 'ZONES') => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  onSelectHeatmapTeam: (t: 'ALL' | 'HOME' | 'AWAY') => void;
  homeTeam: string;
  awayTeam: string;
  homeColor?: string;
  awayColor?: string;
  shotFilter: ShotFilterType;
  onSelectShotFilter: (f: ShotFilterType) => void;
  selectedPlayerName?: string | null;
  onClearPlayerFilter?: () => void;
}

const MODES = [
  { key: 'FORMATIONS', label: 'Tactical Shape', Icon: Users },
  { key: 'SHOTS', label: 'Shot & Goal Map', Icon: Target },
  { key: 'ZONES', label: 'Zone 14 & Thirds', Icon: Shield },
] as const;

export default function WebPitchRadarControls({
  plays,
  allPlays = [],
  selectedPlay,
  onSelectPlay,
  onJumpToMinute,
  viewMode,
  onSelectViewMode,
  heatmapTeam,
  onSelectHeatmapTeam,
  homeTeam,
  awayTeam,
  homeColor = '#DFFF00',
  awayColor = '#38BDF8',
  shotFilter,
  onSelectShotFilter,
  selectedPlayerName,
  onClearPlayerFilter,
}: WebPitchRadarControlsProps) {
  const allGoals = useMemo(() => {
    const source = allPlays.length > 0 ? allPlays : plays;
    return source.filter((p) => (p.type_text || p.type || '').toLowerCase().includes('goal'));
  }, [allPlays, plays]);

  return (
    <div className="space-y-3 font-tech select-none">
      {/* 3-Mode Primary Segmented Switcher */}
      <div className="grid grid-cols-3 gap-1.5 p-1.5 bg-[#0A0A0C] border border-white/10 rounded-xl">
        {MODES.map(({ key, label, Icon }) => (
          <button
            key={key}
            type="button"
            onClick={() => onSelectViewMode(key)}
            className={`flex items-center justify-center gap-2 py-2.5 rounded-lg text-xs sm:text-sm font-extrabold uppercase tracking-wider transition-all cursor-pointer ${
              viewMode === key ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-md' : 'text-white/70 hover:text-white'
            }`}
          >
            <Icon className="w-4 h-4" />
            <span>{label}</span>
          </button>
        ))}
      </div>

      {/* Mode 1: Tactical Formations Sub-Bar */}
      {viewMode === 'FORMATIONS' && (
        <div className="flex flex-wrap items-center justify-between gap-2 px-4 py-2.5 bg-[#0A0A0C] border border-white/10 rounded-xl text-xs sm:text-sm">
          <span className="text-xs text-white/60">
            Geser timeline untuk mengamati kerapatan blok taktis tim dan rotasi pemain.
          </span>
          {selectedPlayerName && (
            <div className="flex items-center gap-2 bg-[#DFFF00]/15 border border-[#DFFF00]/40 px-2.5 py-1 rounded-lg text-xs text-[#DFFF00] font-black">
              <span>{selectedPlayerName}</span>
              <button type="button" onClick={onClearPlayerFilter} className="hover:text-rose-400 cursor-pointer">
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          )}
        </div>
      )}

      {/* Mode 2: Shot Map Sub-Bar with Filter Chips & Goals */}
      {viewMode === 'SHOTS' && (
        <div className="space-y-2.5 bg-[#0A0A0C] border border-white/10 rounded-xl p-3 sm:p-3.5 text-xs sm:text-sm">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex flex-wrap items-center gap-1.5">
              <span className="text-xs text-white/50 font-bold uppercase mr-1">Filter:</span>
              <button
                type="button"
                onClick={() => onSelectShotFilter('ALL')}
                className={`px-3 py-1.5 rounded-lg text-xs font-black uppercase transition-all cursor-pointer ${
                  shotFilter === 'ALL' ? 'bg-white text-black shadow-sm' : 'bg-[#16181D] text-white/70 hover:text-white border border-white/10'
                }`}
              >
                Semua Tembakan
              </button>
              <button
                type="button"
                onClick={() => onSelectShotFilter('HOME')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black uppercase transition-all cursor-pointer ${
                  shotFilter === 'HOME' ? 'text-black shadow-sm font-black' : 'bg-[#16181D] text-white/70 hover:text-white border border-white/10'
                }`}
                style={{ backgroundColor: shotFilter === 'HOME' ? homeColor : undefined }}
              >
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: homeColor }} />
                <span>{homeTeam.split(' ')[0]}</span>
              </button>
              <button
                type="button"
                onClick={() => onSelectShotFilter('AWAY')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black uppercase transition-all cursor-pointer ${
                  shotFilter === 'AWAY' ? 'text-black shadow-sm font-black' : 'bg-[#16181D] text-white/70 hover:text-white border border-white/10'
                }`}
                style={{ backgroundColor: shotFilter === 'AWAY' ? awayColor : undefined }}
              >
                <span className="w-2 h-2 rounded-full" style={{ backgroundColor: awayColor }} />
                <span>{awayTeam.split(' ')[0]}</span>
              </button>
              <button
                type="button"
                onClick={() => onSelectShotFilter('GOALS')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black uppercase transition-all cursor-pointer ${
                  shotFilter === 'GOALS' ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-sm font-black' : 'bg-[#16181D] text-white/70 hover:text-white border border-white/10'
                }`}
              >
                <span className="w-2 h-2 rounded-full bg-[#DFFF00]" />
                <span>Gol Saja ({allGoals.length})</span>
              </button>
            </div>
          </div>

          {allGoals.length > 0 && (
            <div className="flex items-center gap-2 overflow-x-auto no-scrollbar pt-2 border-t border-white/5">
              <span className="text-xs text-[#DFFF00] uppercase font-black tracking-wider shrink-0 mr-1">
                Lompat ke Gol:
              </span>
              {allGoals.map((g) => {
                const isSelected = selectedPlay?.id === g.id;
                const m = g.clock.match(/(\d+)/);
                const min = m ? parseInt(m[1], 10) : 0;
                const scorer = g.player?.trim().split(' ').slice(-1)[0] || 'Goal';
                const isHome = (g.team_name || '').toLowerCase().includes(homeTeam.toLowerCase());
                return (
                  <button
                    key={g.id}
                    type="button"
                    onClick={() => {
                      onSelectPlay(g);
                      onJumpToMinute?.(Math.min(95, min));
                    }}
                    className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-black uppercase tracking-wide transition cursor-pointer shrink-0 ${
                      isSelected ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-md font-black' : 'bg-white/5 hover:bg-white/10 text-white/90 border border-white/10'
                    }`}
                  >
                    <span className="w-2 h-2 rounded-full" style={{ backgroundColor: isHome ? homeColor : awayColor }} />
                    <span>{g.clock} {scorer}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Mode 3: Tactical Zones Sub-Bar & Educational Legend */}
      {viewMode === 'ZONES' && (
        <div className="space-y-2.5 bg-[#0A0A0C] border border-white/10 rounded-xl p-3 sm:p-3.5 text-xs sm:text-sm">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-1.5">
              <span className="text-xs text-white/50 font-bold uppercase mr-1">Fokus Tim:</span>
              {(['ALL', 'HOME', 'AWAY'] as const).map((t) => {
                const isSelected = heatmapTeam === t;
                const label = t === 'ALL' ? 'Kedua Tim' : t === 'HOME' ? homeTeam.split(' ')[0] : awayTeam.split(' ')[0];
                const activeBg = t === 'HOME' ? homeColor : t === 'AWAY' ? awayColor : '#DFFF00';
                return (
                  <button
                    key={t}
                    type="button"
                    onClick={() => onSelectHeatmapTeam(t)}
                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-black uppercase transition-all cursor-pointer ${
                      isSelected ? 'text-[#0A0A0C] shadow-sm font-black' : 'bg-[#16181D] text-white/70 hover:text-white border border-white/10'
                    }`}
                    style={{ backgroundColor: isSelected ? activeBg : undefined }}
                  >
                    {t !== 'ALL' && (
                      <span className="w-2 h-2 rounded-full" style={{ backgroundColor: t === 'HOME' ? homeColor : awayColor }} />
                    )}
                    <span>{label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-2 border-t border-white/5 text-xs">
            <div className="flex items-start gap-2 bg-white/[0.03] border border-white/5 p-2 rounded-lg">
              <Info className="w-4 h-4 text-[#DFFF00] shrink-0 mt-0.5" />
              <div>
                <span className="font-extrabold text-white block">Koridor Emas Zone 14</span>
                <span className="text-white/60 leading-relaxed block">
                  Area sentral di luar kotak 16m tempat asal assist dan umpan terobosan paling mematikan.
                </span>
              </div>
            </div>
            <div className="flex items-start gap-2 bg-white/[0.03] border border-white/5 p-2 rounded-lg">
              <Shield className="w-4 h-4 text-[#38BDF8] shrink-0 mt-0.5" />
              <div>
                <span className="font-extrabold text-white block">Pitch Thirds &amp; Tilt</span>
                <span className="text-white/60 leading-relaxed block">
                  Persentase aksi di wilayah bertahan, tengah, dan serang serta dominasi territorial tilt sepertiga akhir.
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

'use client';

import React, { useState, useMemo } from 'react';
import { Crosshair } from 'lucide-react';
import { MatchFormations } from '@/lib/database';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls from './WebPitchRadarControls';

export interface WebPitchPlay {
  id: string;
  clock: string;
  type: string;
  type_text: string;
  text: string;
  player?: string;
  assist?: string;
  team_name?: string;
  x_percent: number;
  y_percent: number;
  target_x?: number | null;
  target_y?: number | null;
  period?: number | string;
}

interface WebPitchRadarProps {
  plays: WebPitchPlay[];
  homeTeam: string;
  awayTeam: string;
  isLive?: boolean;
  homeColor?: string | null;
  awayColor?: string | null;
  formations?: MatchFormations | null;
}

export default function WebPitchRadar({
  plays,
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
  formations,
}: WebPitchRadarProps) {
  const hasLineups = Boolean(
    formations?.home?.starters?.length || formations?.away?.starters?.length,
  );
  const [viewMode, setViewMode] = useState<'FORMATIONS' | 'SHOTS' | 'ZONES'>(
    hasLineups ? 'FORMATIONS' : 'SHOTS',
  );
  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(() => {
    return plays.find((p) => p.type.toLowerCase().includes('goal')) || plays[0] || null;
  });
  const [heatmapTeam, setHeatmapTeam] = useState<'ALL' | 'HOME' | 'AWAY'>('ALL');
  const [selectedPlayerName, setSelectedPlayerName] = useState<string | null>(null);

  const shotPlays = useMemo(() => {
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      return t.includes('shot') || t.includes('goal') || t.includes('attempt');
    });
  }, [plays]);

  return (
    <div className="space-y-3.5 bg-[#121316] border border-white/10 rounded-2xl p-3.5 sm:p-4 shadow-xl select-none">
      <div className="flex items-center justify-between gap-2 text-xs text-white/50 font-tech">
        <div className="flex items-center gap-2">
          <Crosshair className="w-4 h-4 text-[#DFFF00]" />
          <span className="font-extrabold text-white tracking-wide uppercase">Tactical Match Centre</span>
          <span className="hidden sm:inline text-[11px] text-white/40">
            ({homeTeam} vs {awayTeam})
          </span>
        </div>
        <div className="text-[10px] font-mono text-white/40 font-bold uppercase tracking-wider">
          Opta-Style 2D Radar
        </div>
      </div>

      <WebPitchRadarCanvas
        plays={shotPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        homeColor={homeColor}
        awayColor={awayColor}
        viewMode={viewMode}
        formations={formations}
        selectedPlayerName={selectedPlayerName}
        onSelectPlayerName={(name) => setSelectedPlayerName(name)}
        heatmapTeam={heatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
      />

      <WebPitchRadarControls
        plays={shotPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        viewMode={viewMode}
        onSelectViewMode={(mode) => {
          setViewMode(mode);
          if (mode === 'SHOTS' && !selectedPlay && shotPlays.length > 0) {
            setSelectedPlay(shotPlays[0]);
          }
        }}
        heatmapTeam={heatmapTeam}
        onSelectHeatmapTeam={setHeatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        selectedPlayerName={selectedPlayerName}
        onClearPlayerFilter={() => setSelectedPlayerName(null)}
      />
    </div>
  );
}

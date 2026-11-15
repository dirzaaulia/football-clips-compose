'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { Crosshair } from 'lucide-react';
import { MatchFormations } from '@/lib/database';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls, { PlayCategory } from './WebPitchRadarControls';

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
  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<PlayCategory>('ALL');
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [viewMode, setViewMode] = useState<'REPLAY' | 'SHOTS' | 'HEATMAP'>('REPLAY');
  const [heatmapTeam, setHeatmapTeam] = useState<'ALL' | 'HOME' | 'AWAY'>('ALL');
  const [playbackSpeed, setPlaybackSpeed] = useState<1 | 2 | 4>(1);
  const [selectedPlayerName, setSelectedPlayerName] = useState<string | null>(null);

  const filteredPlays = useMemo(() => {
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      if (selectedCategory === 'GOALS') return t.includes('goal');
      if (selectedCategory === 'SHOTS') return t.includes('shot') || t.includes('attempt');
      if (selectedCategory === 'CARDS') return t.includes('card');
      if (selectedCategory === 'SUBS') return t.includes('sub');
      return true;
    });
  }, [plays, selectedCategory]);

  const handleSelectPlay = (play: WebPitchPlay | null) => {
    setSelectedPlay(play);
    setIsPlaying(false);
  };

  useEffect(() => {
    if (!isPlaying || filteredPlays.length === 0) return;
    const intervalMs = Math.max(400, Math.floor(1800 / playbackSpeed));
    const timer = setInterval(() => {
      setSelectedPlay((current) => {
        if (!current) return filteredPlays[0] || null;
        const idx = filteredPlays.findIndex((p) => p.id === current.id);
        if (idx === -1 || idx >= filteredPlays.length - 1) {
          setIsPlaying(false);
          return current;
        }
        return filteredPlays[idx + 1];
      });
    }, intervalMs);
    return () => clearInterval(timer);
  }, [isPlaying, filteredPlays, playbackSpeed]);

  const getMarkerColor = (play: WebPitchPlay): string => {
    const t = (play.type_text || play.type || '').toLowerCase();
    if (t.includes('goal')) return '#DFFF00';
    if (t.includes('shot') || t.includes('attempt')) return '#38BDF8';
    if (t.includes('card')) return '#EAB308';
    if (t.includes('corner')) return '#A855F7';
    if (t.includes('sub')) return '#F97316';
    return '#94A3B8';
  };

  return (
    <div className="space-y-3.5 bg-[#121316] border border-white/10 rounded-2xl p-3.5 sm:p-4 shadow-xl select-none">
      <div className="flex items-center justify-between gap-2 text-xs text-white/50 font-tech">
        <div className="flex items-center gap-2">
          <Crosshair className="w-4 h-4 text-[#DFFF00]" />
          <span className="font-extrabold text-white tracking-wide uppercase">Tactical Pitch Analytics</span>
          <span className="hidden sm:inline text-[11px] text-white/40">
            ({homeTeam} vs {awayTeam})
          </span>
        </div>
      </div>

      <WebPitchRadarCanvas
        plays={filteredPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={handleSelectPlay}
        getMarkerColor={getMarkerColor}
        homeColor={homeColor}
        awayColor={awayColor}
        viewMode={viewMode}
        formations={formations}
        selectedPlayerName={selectedPlayerName}
        onSelectPlayerName={(name) => {
          setSelectedPlayerName(name);
          if (name) setViewMode('HEATMAP');
        }}
        heatmapTeam={heatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
      />

      <WebPitchRadarControls
        plays={filteredPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={handleSelectPlay}
        selectedCategory={selectedCategory}
        onSelectCategory={setSelectedCategory}
        isPlaying={isPlaying}
        onTogglePlay={() => setIsPlaying((p) => !p)}
        onReset={() => setSelectedPlay(filteredPlays[0] || null)}
        getMarkerColor={getMarkerColor}
        viewMode={viewMode}
        onSelectViewMode={setViewMode}
        heatmapTeam={heatmapTeam}
        onSelectHeatmapTeam={setHeatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        playbackSpeed={playbackSpeed}
        onToggleSpeed={() => setPlaybackSpeed((s) => (s === 1 ? 2 : s === 2 ? 4 : 1))}
        selectedPlayerName={selectedPlayerName}
        onClearPlayerFilter={() => setSelectedPlayerName(null)}
      />
    </div>
  );
}

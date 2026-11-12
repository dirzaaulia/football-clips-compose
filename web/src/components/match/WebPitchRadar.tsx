'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Crosshair } from 'lucide-react';
import { MatchFormations } from '@/lib/database';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls from './WebPitchRadarControls';

export interface WebPitchPlay {
  id: string;
  clock: string;
  period?: number;
  type: string;
  type_text?: string;
  text: string;
  team_name?: string;
  player?: string | null;
  assist?: string | null;
  x_percent?: number | null;
  y_percent?: number | null;
  target_x?: number | null;
  target_y?: number | null;
}

interface WebPitchRadarProps {
  plays?: WebPitchPlay[];
  homeTeam: string;
  awayTeam: string;
  isLive?: boolean;
  homeColor?: string | null;
  awayColor?: string | null;
  formations?: MatchFormations | null;
}

export default function WebPitchRadar({
  plays = [],
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
  formations,
}: WebPitchRadarProps) {
  const [viewMode, setViewMode] = useState<'REPLAY' | 'SHOTS' | 'HEATMAP'>('REPLAY');
  const [heatmapTeam, setHeatmapTeam] = useState<'ALL' | 'HOME' | 'AWAY'>('ALL');
  const [selectedPlayerName, setSelectedPlayerName] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [isPlaying, setIsPlaying] = useState(false);
  const [playbackSpeed, setPlaybackSpeed] = useState<1 | 2>(1);

  const filteredPlays = useMemo(() => {
    if (selectedCategory === 'ALL') return plays;
    const cat = selectedCategory.toLowerCase();
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      if (cat === 'goals') return t.includes('goal');
      if (cat === 'shots') return t.includes('shot') || t.includes('attempt');
      if (cat === 'cards') return t.includes('card');
      if (cat === 'corners') return t.includes('corner');
      if (cat === 'subs') return t.includes('sub');
      return true;
    });
  }, [plays, selectedCategory]);

  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(
    filteredPlays[filteredPlays.length - 1] || null
  );

  useEffect(() => {
    if (filteredPlays.length > 0 && (!selectedPlay || !filteredPlays.some((p) => p.id === selectedPlay.id))) {
      setSelectedPlay(filteredPlays[filteredPlays.length - 1] || null);
    }
  }, [filteredPlays, selectedPlay]);

  const handleSelectPlay = useCallback((play: WebPitchPlay) => {
    setSelectedPlay(play);
    if (play.player) setSelectedPlayerName(play.player);
  }, []);

  useEffect(() => {
    if (!isPlaying || filteredPlays.length === 0) return;
    const intervalMs = Math.round(1600 / playbackSpeed);
    const timer = setInterval(() => {
      setSelectedPlay((prev) => {
        const curIdx = filteredPlays.findIndex((p) => p.id === prev?.id);
        const nextIdx = curIdx + 1 < filteredPlays.length ? curIdx + 1 : 0;
        const nextPlay = filteredPlays[nextIdx] || null;
        if (nextIdx === filteredPlays.length - 1) setIsPlaying(false);
        return nextPlay;
      });
    }, intervalMs);
    return () => clearInterval(timer);
  }, [isPlaying, filteredPlays, playbackSpeed]);

  const getMarkerColor = (play: WebPitchPlay): string => {
    const t = (play.type_text || play.type || '').toLowerCase();
    if (t.includes('goal')) return '#22C55E';
    if (t.includes('shot') || t.includes('attempt')) return '#38BDF8';
    if (t.includes('card')) return '#EAB308';
    if (t.includes('corner')) return '#A855F7';
    if (t.includes('sub')) return '#F97316';
    return '#94A3B8';
  };

  return (
    <div className="space-y-3.5 bg-[#101418] border border-[#334155]/60 rounded-2xl p-3.5 sm:p-4 shadow-xl">
      <div className="flex items-center justify-between gap-2 text-xs text-[#94A3B8]">
        <div className="flex items-center gap-2">
          <Crosshair className="w-4 h-4 text-[#7098F8]" />
          <span className="font-extrabold text-white tracking-wide">Tactical Pitch Analytics</span>
          <span className="hidden sm:inline text-[11px] text-[#64748B]">
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
        onReset={() => {
          setIsPlaying(false);
          if (filteredPlays.length > 0) handleSelectPlay(filteredPlays[0]!);
        }}
        playbackSpeed={playbackSpeed}
        onToggleSpeed={() => setPlaybackSpeed((s) => (s === 1 ? 2 : 1))}
        getMarkerColor={getMarkerColor}
        viewMode={viewMode}
        onSelectViewMode={setViewMode}
        heatmapTeam={heatmapTeam}
        onSelectHeatmapTeam={setHeatmapTeam}
        selectedPlayerName={selectedPlayerName}
        onClearPlayerFilter={() => setSelectedPlayerName(null)}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
      />
    </div>
  );
}

'use client';

import React from 'react';
import { Play, Pause, RotateCcw, Video, Target, Flame, X } from 'lucide-react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchRadarControlsProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  selectedCategory: string;
  onSelectCategory: (category: string) => void;
  isPlaying: boolean;
  onTogglePlay: () => void;
  onReset: () => void;
  playbackSpeed?: 1 | 2;
  onToggleSpeed?: () => void;
  getMarkerColor: (play: WebPitchPlay) => string;
  viewMode: 'REPLAY' | 'SHOTS' | 'HEATMAP';
  onSelectViewMode: (mode: 'REPLAY' | 'SHOTS' | 'HEATMAP') => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  onSelectHeatmapTeam: (team: 'ALL' | 'HOME' | 'AWAY') => void;
  selectedPlayerName?: string | null;
  onClearPlayerFilter?: () => void;
  homeTeam: string;
  awayTeam: string;
}

const CATEGORIES = ['ALL', 'GOALS', 'SHOTS', 'CARDS', 'CORNERS', 'SUBS'] as const;

export default function WebPitchRadarControls({
  plays,
  selectedPlay,
  onSelectPlay,
  selectedCategory,
  onSelectCategory,
  isPlaying,
  onTogglePlay,
  onReset,
  playbackSpeed = 1,
  onToggleSpeed,
  getMarkerColor,
  viewMode,
  onSelectViewMode,
  heatmapTeam,
  onSelectHeatmapTeam,
  selectedPlayerName,
  onClearPlayerFilter,
  homeTeam,
  awayTeam,
}: WebPitchRadarControlsProps) {
  const currentIndex = plays.findIndex((p) => p.id === selectedPlay?.id);

  const handleSliderChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const idx = parseInt(e.target.value, 10);
    const target = plays[idx];
    if (target) onSelectPlay(target);
  };

  const shotCount = plays.filter((p) => (p.type_text || p.type || '').toLowerCase().includes('shot')).length;
  const goalCount = plays.filter((p) => (p.type_text || p.type || '').toLowerCase().includes('goal')).length;

  return (
    <div className="space-y-3">
      {/* 3-Mode Primary Segmented Switcher */}
      <div className="grid grid-cols-3 gap-1 p-1 bg-[#12161A] border border-[#334155]/60 rounded-xl">
        <button
          type="button"
          onClick={() => onSelectViewMode('REPLAY')}
          className={`flex items-center justify-center gap-1.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
            viewMode === 'REPLAY' ? 'bg-[#7098F8] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
          }`}
        >
          <Video className="w-3.5 h-3.5" />
          <span>2D Replay</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('SHOTS')}
          className={`flex items-center justify-center gap-1.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
            viewMode === 'SHOTS' ? 'bg-[#7098F8] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
          }`}
        >
          <Target className="w-3.5 h-3.5" />
          <span>Shot Map</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('HEATMAP')}
          className={`flex items-center justify-center gap-1.5 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
            viewMode === 'HEATMAP' ? 'bg-[#7098F8] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
          }`}
        >
          <Flame className="w-3.5 h-3.5" />
          <span>Heatmap</span>
        </button>
      </div>

      {/* Mode Sub-Bars */}
      {viewMode === 'HEATMAP' && (
        <div className="flex flex-wrap items-center justify-between gap-2 bg-[#181C20] border border-[#334155]/60 rounded-xl p-2 text-xs">
          <div className="flex items-center gap-1.5">
            <span className="text-[10px] text-[#64748B] font-bold uppercase">Territory:</span>
            {(['ALL', 'HOME', 'AWAY'] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => onSelectHeatmapTeam(t)}
                className={`px-2 py-0.5 rounded text-[10px] font-bold transition-all cursor-pointer ${
                  heatmapTeam === t ? 'bg-[#3B82F6] text-white' : 'bg-[#12161A] text-[#94A3B8] hover:text-white'
                }`}
              >
                {t === 'ALL' ? 'Both' : t === 'HOME' ? homeTeam.split(' ')[0] : awayTeam.split(' ')[0]}
              </button>
            ))}
          </div>

          {selectedPlayerName && (
            <div className="flex items-center gap-1.5 bg-[#7098F8]/20 border border-[#7098F8]/40 px-2 py-0.5 rounded text-[10px] text-white font-bold">
              <span>Player: {selectedPlayerName}</span>
              <button type="button" onClick={onClearPlayerFilter} className="hover:text-rose-400 cursor-pointer">
                <X className="w-3 h-3" />
              </button>
            </div>
          )}
        </div>
      )}

      {viewMode === 'SHOTS' && (
        <div className="flex items-center justify-between px-3 py-1.5 bg-[#181C20] border border-[#334155]/60 rounded-xl text-xs">
          <div className="flex items-center gap-3 text-[11px] font-bold text-[#94A3B8]">
            <span>Goals: <strong className="text-emerald-400">{goalCount}</strong></span>
            <span>Attempts: <strong className="text-[#38BDF8]">{shotCount}</strong></span>
          </div>
          <span className="text-[10px] text-[#64748B]">Click any shot on pitch to inspect</span>
        </div>
      )}

      {viewMode === 'REPLAY' && (
        <>
          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
            {CATEGORIES.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => onSelectCategory(cat)}
                className={`px-2.5 py-1 rounded-lg text-[10px] font-bold tracking-wider uppercase transition-all cursor-pointer ${
                  selectedCategory === cat
                    ? 'bg-[#7098F8] text-white shadow-sm'
                    : 'bg-[#181C20] text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-[#334155]/60'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {selectedPlay && (
            <div className="bg-[#12161A] border border-[#334155]/70 rounded-xl px-3.5 py-2 flex items-center justify-between text-xs gap-3">
              <div className="flex items-center gap-2.5 min-w-0">
                <span className="font-mono font-bold text-[#7098F8] shrink-0">{selectedPlay.clock}</span>
                <span className="text-white truncate font-medium">{selectedPlay.text}</span>
              </div>
              <span
                className="text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-md shrink-0"
                style={{
                  backgroundColor: `${getMarkerColor(selectedPlay)}22`,
                  color: getMarkerColor(selectedPlay),
                  border: `1px solid ${getMarkerColor(selectedPlay)}44`,
                }}
              >
                {selectedPlay.type_text || selectedPlay.type}
              </span>
            </div>
          )}

          {plays.length > 0 && (
            <div className="flex items-center gap-2.5 pt-1">
              <button
                type="button"
                onClick={onTogglePlay}
                className={`w-7 h-7 rounded-lg flex items-center justify-center transition-all cursor-pointer shrink-0 ${
                  isPlaying ? 'bg-amber-500/20 text-amber-400 border border-amber-500/40' : 'bg-[#7098F8]/20 text-[#7098F8] hover:bg-[#7098F8]/30 border border-[#7098F8]/40'
                }`}
                title={isPlaying ? 'Pause auto-replay' : 'Start auto-replay'}
              >
                {isPlaying ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5 fill-current" />}
              </button>
              {onToggleSpeed && (
                <button
                  type="button"
                  onClick={onToggleSpeed}
                  className="px-2 h-7 rounded-lg bg-[#181C20] hover:bg-[#1E2227] text-white border border-[#334155]/60 text-[10px] font-black font-mono flex items-center justify-center transition-all cursor-pointer shrink-0"
                  title="Toggle FM replay speed"
                >
                  {playbackSpeed}x
                </button>
              )}
              <button
                type="button"
                onClick={onReset}
                className="w-7 h-7 rounded-lg bg-[#181C20] hover:bg-[#1E2227] text-[#94A3B8] hover:text-white border border-[#334155]/60 flex items-center justify-center transition-all cursor-pointer shrink-0"
                title="Reset to first play"
              >
                <RotateCcw className="w-3 h-3" />
              </button>
              <div className="flex-1 flex items-center gap-2">
                <input
                  type="range"
                  min={0}
                  max={Math.max(0, plays.length - 1)}
                  value={currentIndex >= 0 ? currentIndex : 0}
                  onChange={handleSliderChange}
                  className="w-full h-1.5 bg-[#1E2227] rounded-lg appearance-none cursor-pointer accent-[#7098F8]"
                />
                <span className="text-[10px] font-mono text-[#94A3B8] shrink-0">
                  {currentIndex + 1}/{plays.length}
                </span>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

'use client';

import React from 'react';
import { Target, Video, Flame, X, Play, Pause, RotateCcw } from 'lucide-react';
import { WebPitchPlay } from './WebPitchRadar';

export type PlayCategory = 'ALL' | 'GOALS' | 'SHOTS' | 'CARDS' | 'SUBS';

interface WebPitchRadarControlsProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay | null) => void;
  selectedCategory: PlayCategory;
  onSelectCategory: (cat: PlayCategory) => void;
  isPlaying: boolean;
  onTogglePlay: () => void;
  onReset: () => void;
  getMarkerColor: (play: WebPitchPlay) => string;
  viewMode: 'REPLAY' | 'SHOTS' | 'HEATMAP';
  onSelectViewMode: (mode: 'REPLAY' | 'SHOTS' | 'HEATMAP') => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  onSelectHeatmapTeam: (t: 'ALL' | 'HOME' | 'AWAY') => void;
  homeTeam: string;
  awayTeam: string;
  playbackSpeed?: 1 | 2 | 4;
  onToggleSpeed?: () => void;
  selectedPlayerName?: string | null;
  onClearPlayerFilter?: () => void;
}

const CATEGORIES: PlayCategory[] = ['ALL', 'GOALS', 'SHOTS', 'CARDS', 'SUBS'];

export default function WebPitchRadarControls({
  plays,
  selectedPlay,
  onSelectPlay,
  selectedCategory,
  onSelectCategory,
  isPlaying,
  onTogglePlay,
  onReset,
  getMarkerColor,
  viewMode,
  onSelectViewMode,
  heatmapTeam,
  onSelectHeatmapTeam,
  homeTeam,
  awayTeam,
  playbackSpeed = 1,
  onToggleSpeed,
  selectedPlayerName,
  onClearPlayerFilter,
}: WebPitchRadarControlsProps) {
  const currentIndex = selectedPlay ? plays.findIndex((p) => p.id === selectedPlay.id) : -1;

  const handleSliderChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const idx = parseInt(e.target.value, 10);
    if (plays[idx]) onSelectPlay(plays[idx]);
  };

  const shotCount = plays.filter((p) => (p.type_text || p.type || '').toLowerCase().includes('shot')).length;
  const goalCount = plays.filter((p) => (p.type_text || p.type || '').toLowerCase().includes('goal')).length;

  return (
    <div className="space-y-3 font-tech">
      {/* 3-Mode Primary Segmented Switcher */}
      <div className="grid grid-cols-3 gap-1 p-1 bg-[#0A0A0C] border border-white/10 rounded-xl">
        <button
          type="button"
          onClick={() => onSelectViewMode('REPLAY')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'REPLAY' ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm' : 'text-white/60 hover:text-white'
          }`}
        >
          <Video className="w-3.5 h-3.5" />
          <span>2D Replay</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('SHOTS')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'SHOTS' ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm' : 'text-white/60 hover:text-white'
          }`}
        >
          <Target className="w-3.5 h-3.5" />
          <span>Shot Map</span>
        </button>
        <button
          type="button"
          onClick={() => onSelectViewMode('HEATMAP')}
          className={`flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
            viewMode === 'HEATMAP' ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm' : 'text-white/60 hover:text-white'
          }`}
        >
          <Flame className="w-3.5 h-3.5" />
          <span>Heatmap</span>
        </button>
      </div>

      {/* Mode Sub-Bars */}
      {viewMode === 'HEATMAP' && (
        <div className="flex flex-wrap items-center justify-between gap-2 bg-[#121316] border border-white/10 rounded-xl p-2.5 text-xs">
          <div className="flex items-center gap-1.5">
            <span className="text-[10px] text-white/40 font-bold uppercase">Territory:</span>
            {(['ALL', 'HOME', 'AWAY'] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => onSelectHeatmapTeam(t)}
                className={`px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase transition-all cursor-pointer ${
                  heatmapTeam === t ? 'bg-[#DFFF00] text-[#0A0A0C] font-black' : 'bg-[#1A1C22] text-white/60 hover:text-white border border-white/5'
                }`}
              >
                {t === 'ALL' ? 'Both' : t === 'HOME' ? homeTeam.split(' ')[0] : awayTeam.split(' ')[0]}
              </button>
            ))}
          </div>

          {selectedPlayerName && (
            <div className="flex items-center gap-1.5 bg-[#DFFF00]/15 border border-[#DFFF00]/30 px-2.5 py-1 rounded-lg text-[10px] text-[#DFFF00] font-bold">
              <span>Player: {selectedPlayerName}</span>
              <button type="button" onClick={onClearPlayerFilter} className="hover:text-rose-400 cursor-pointer">
                <X className="w-3 h-3" />
              </button>
            </div>
          )}
        </div>
      )}

      {viewMode === 'SHOTS' && (
        <div className="flex items-center justify-between px-3 py-2 bg-[#121316] border border-white/10 rounded-xl text-xs">
          <div className="flex items-center gap-3 text-[11px] font-bold text-white/70">
            <span>Goals: <strong className="text-emerald-400">{goalCount}</strong></span>
            <span>Attempts: <strong className="text-[#38BDF8]">{shotCount}</strong></span>
          </div>
          <span className="text-[10px] text-white/40">Click any shot on pitch to inspect</span>
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
                className={`px-3 py-1 rounded-lg text-[10px] font-bold tracking-wider uppercase transition-all cursor-pointer ${
                  selectedCategory === cat
                    ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
                    : 'bg-[#121316] text-white/60 hover:text-white hover:bg-[#1A1C22] border border-white/10'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {selectedPlay && (
            <div className="bg-[#0A0A0C] border border-white/10 rounded-xl px-3.5 py-2.5 flex items-center justify-between text-xs gap-3">
              <div className="flex items-center gap-2.5 min-w-0">
                <span className="font-mono font-bold text-[#DFFF00] shrink-0">{selectedPlay.clock}</span>
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
                className={`w-8 h-8 rounded-xl flex items-center justify-center transition-all cursor-pointer shrink-0 ${
                  isPlaying ? 'bg-amber-500/20 text-amber-400 border border-amber-500/40' : 'bg-[#DFFF00] text-[#0A0A0C] shadow-md'
                }`}
                title={isPlaying ? 'Pause auto-replay' : 'Start auto-replay'}
              >
                {isPlaying ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5 fill-current" />}
              </button>
              {onToggleSpeed && (
                <button
                  type="button"
                  onClick={onToggleSpeed}
                  className="px-2.5 h-8 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-white border border-white/10 text-[10px] font-black font-mono flex items-center justify-center transition-all cursor-pointer shrink-0"
                  title="Toggle replay speed"
                >
                  {playbackSpeed}x
                </button>
              )}
              <button
                type="button"
                onClick={onReset}
                className="w-8 h-8 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-white/60 hover:text-white border border-white/10 flex items-center justify-center transition-all cursor-pointer shrink-0"
                title="Reset to first play"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
              <div className="flex-1 flex items-center gap-2">
                <input
                  type="range"
                  min={0}
                  max={Math.max(0, plays.length - 1)}
                  value={currentIndex >= 0 ? currentIndex : 0}
                  onChange={handleSliderChange}
                  className="w-full h-1.5 bg-[#1A1C22] rounded-lg appearance-none cursor-pointer accent-[#DFFF00]"
                />
                <span className="text-[10px] font-mono text-white/50 shrink-0">
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

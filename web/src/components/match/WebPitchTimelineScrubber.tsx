'use client';

import React, { useMemo } from 'react';
import { Play, Pause, RotateCcw } from 'lucide-react';
import { MomentumPoint } from './WebPitchXgHelper';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchTimelineScrubberProps {
  momentum: MomentumPoint[];
  currentMinute: number;
  onMinuteChange: (minute: number) => void;
  isPlaying: boolean;
  onTogglePlay: () => void;
  onReset: () => void;
  plays: WebPitchPlay[];
  homeColor: string;
  awayColor: string;
}

export default function WebPitchTimelineScrubber({
  momentum,
  currentMinute,
  onMinuteChange,
  isPlaying,
  onTogglePlay,
  onReset,
  plays,
  homeColor,
  awayColor,
}: WebPitchTimelineScrubberProps) {
  const eventPips = useMemo(() => {
    return plays
      .map((p) => {
        const m = p.clock.match(/(\d+)/);
        const min = m ? parseInt(m[1], 10) : -1;
        const t = (p.type_text || p.type || '').toLowerCase();
        const isGoal = t.includes('goal');
        const isCard = t.includes('card');
        return { id: p.id, minute: min, isGoal, isCard };
      })
      .filter((p) => p.minute >= 0 && p.minute <= 95);
  }, [plays]);

  // Generate SVG path for SofaScore-style momentum wave
  const wavePath = useMemo(() => {
    if (momentum.length === 0) return '';
    const points = momentum.map((pt) => {
      const x = (pt.minute / 95) * 100;
      // y ranges from 0 to 30. Center is 15. Net momentum -10..10 maps to 26..4
      const y = 15 - (pt.netMomentum / 10) * 11;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    });
    return `M 0,15 L ${points.join(' L ')} L 100,15 Z`;
  }, [momentum]);

  return (
    <div className="bg-[#0E1116] border border-white/10 rounded-2xl p-3 space-y-2 select-none shadow-md">
      {/* Top Header: Momentum Wave Legend & Controls */}
      <div className="flex items-center justify-between gap-2 text-xs font-tech">
        <div className="flex items-center gap-2">
          <button
            onClick={onTogglePlay}
            className="w-7 h-7 rounded-lg bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition cursor-pointer"
            aria-label={isPlaying ? 'Pause replay' : 'Play replay'}
          >
            {isPlaying ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
          </button>
          <button
            onClick={onReset}
            className="w-7 h-7 rounded-lg bg-white/5 hover:bg-white/10 text-white/60 hover:text-white flex items-center justify-center transition cursor-pointer"
            aria-label="Reset replay"
          >
            <RotateCcw className="w-3 h-3" />
          </button>
          <span className="font-mono font-black text-white text-xs sm:text-sm">{currentMinute}&apos;</span>
        </div>

        <div className="flex items-center gap-3 text-xs text-white/60 font-tech">
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full" style={{ backgroundColor: homeColor }} />
            <span>Home Dominance</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full" style={{ backgroundColor: awayColor }} />
            <span>Away Dominance</span>
          </span>
        </div>
      </div>

      {/* SofaScore-Style Momentum Wave Graph Canvas */}
      <div className="relative w-full h-8 bg-black/40 rounded-lg overflow-hidden border border-white/5">
        <svg viewBox="0 0 100 30" preserveAspectRatio="none" className="w-full h-full">
          <line x1="0" y1="15" x2="100" y2="15" stroke="rgba(255,255,255,0.15)" strokeWidth="0.5" strokeDasharray="1,1" />
          <path d={wavePath} fill="rgba(223,255,0,0.18)" stroke="#DFFF00" strokeWidth="0.8" />
          {/* Active Minute Indicator Line */}
          <line
            x1={(currentMinute / 95) * 100}
            y1="0"
            x2={(currentMinute / 95) * 100}
            y2="30"
            stroke="#FFFFFF"
            strokeWidth="0.8"
          />
        </svg>

        {/* Event Pips on Wave */}
        {eventPips.map((ep) => (
          <div
            key={ep.id}
            className="absolute top-1/2 -translate-y-1/2 w-1.5 h-1.5 rounded-full pointer-events-none"
            style={{
              left: `${(ep.minute / 95) * 100}%`,
              backgroundColor: ep.isGoal ? '#DFFF00' : ep.isCard ? '#F59E0B' : '#38BDF8',
            }}
          />
        ))}
      </div>

      {/* Scrubbing Range Slider */}
      <input
        type="range"
        min={0}
        max={95}
        value={currentMinute}
        onChange={(e) => onMinuteChange(parseInt(e.target.value, 10))}
        className="w-full h-1.5 bg-white/10 rounded-lg appearance-none cursor-pointer accent-[#DFFF00]"
        aria-label="Match timeline minute scrubber"
      />
    </div>
  );
}

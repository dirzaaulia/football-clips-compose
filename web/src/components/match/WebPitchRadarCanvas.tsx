'use client';

import React from 'react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchRadarCanvasProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  getMarkerColor: (play: WebPitchPlay) => string;
}

export default function WebPitchRadarCanvas({
  plays,
  selectedPlay,
  onSelectPlay,
  getMarkerColor,
}: WebPitchRadarCanvasProps) {
  const activeColor = selectedPlay ? getMarkerColor(selectedPlay) : '#22C55E';
  const sx = selectedPlay?.x_percent != null ? selectedPlay.x_percent * 1.05 : null;
  const sy = selectedPlay?.y_percent != null ? selectedPlay.y_percent * 0.68 : null;

  const tx = selectedPlay?.target_x != null
    ? selectedPlay.target_x * 1.05
    : selectedPlay?.type?.includes('shot')
      ? (selectedPlay.x_percent && selectedPlay.x_percent > 50 ? 103 : 2)
      : null;
  const ty = selectedPlay?.target_y != null ? selectedPlay.target_y * 0.68 : (tx != null ? 34 : null);

  return (
    <div className="relative aspect-[1.54/1] w-full rounded-2xl overflow-hidden bg-gradient-to-b from-[#0D1F12] via-[#112617] to-[#16331F] border border-emerald-950/80 shadow-[inset_0_2px_12px_rgba(0,0,0,0.8)]">
      <svg className="absolute inset-0 w-full h-full stroke-white/25 fill-none select-none" viewBox="0 0 105 68">
        <defs>
          <radialGradient id="pitchGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="#22C55E" stopOpacity="0.08" />
            <stop offset="100%" stopColor="#22C55E" stopOpacity="0" />
          </radialGradient>
        </defs>

        <rect x="0" y="0" width="105" height="68" fill="url(#pitchGlow)" stroke="none" />
        <rect x="2" y="2" width="101" height="64" strokeWidth="0.8" rx="2" />
        <line x1="52.5" y1="2" x2="52.5" y2="66" strokeWidth="0.8" />
        <circle cx="52.5" cy="34" r="9.15" strokeWidth="0.8" />
        <circle cx="52.5" cy="34" r="0.6" fill="white" className="opacity-60" />

        <rect x="2" y="14" width="16.5" height="40" strokeWidth="0.8" />
        <rect x="2" y="23" width="5.5" height="22" strokeWidth="0.8" />
        <circle cx="11" cy="34" r="0.8" fill="white" className="opacity-40" />

        <rect x="86.5" y="14" width="16.5" height="40" strokeWidth="0.8" />
        <rect x="97.5" y="23" width="5.5" height="22" strokeWidth="0.8" />
        <circle cx="94" cy="34" r="0.8" fill="white" className="opacity-40" />

        {/* Trajectory vector arc for active shot or corner */}
        {sx != null && sy != null && tx != null && ty != null && (
          <path
            d={`M ${sx} ${sy} Q ${(sx + tx) / 2} ${(sy + ty) / 2 - 6} ${tx} ${ty}`}
            stroke={activeColor}
            strokeWidth="1.2"
            strokeDasharray="2,2"
            strokeLinecap="round"
            className="opacity-80"
          />
        )}

        {/* Inactive play nodes on the pitch */}
        {plays.map((play) => {
          if (play.x_percent == null || play.y_percent == null) return null;
          if (play.id === selectedPlay?.id) return null;
          const px = play.x_percent * 1.05;
          const py = play.y_percent * 0.68;
          const col = getMarkerColor(play);
          return (
            <g
              key={play.id}
              onClick={() => onSelectPlay(play)}
              className="cursor-pointer group"
            >
              <circle cx={px} cy={py} r="2.8" fill="transparent" />
              <circle
                cx={px}
                cy={py}
                r="1.4"
                fill={col}
                className="opacity-70 group-hover:opacity-100 group-hover:scale-125 transition-transform"
              />
            </g>
          );
        })}

        {/* Active Selected Play Node with Smooth Pulse Ripple */}
        {sx != null && sy != null && (
          <g>
            <circle cx={sx} cy={sy} r="4.5" fill={activeColor} opacity="0.25">
              <animate attributeName="r" values="3;7;3" dur="2s" repeatCount="indefinite" />
              <animate attributeName="opacity" values="0.4;0.05;0.4" dur="2s" repeatCount="indefinite" />
            </circle>
            <circle cx={sx} cy={sy} r="2.5" fill={activeColor} stroke="#ffffff" strokeWidth="0.7" />
          </g>
        )}
      </svg>
    </div>
  );
}

'use client';

import React from 'react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchShotMapOverlayProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  homeColor: string;
  awayColor: string;
}

export function resolveShotOutcome(play: WebPitchPlay): {
  label: string;
  color: string;
  radius: number;
} {
  const t = (play.type_text || play.type || '').toLowerCase();
  const text = (play.text || '').toLowerCase();

  if (t.includes('goal') || text.includes('goal')) {
    return { label: 'Goal', color: '#22C55E', radius: 3.2 };
  }
  if (text.includes('saved') || text.includes('save') || text.includes('parried')) {
    return { label: 'Saved', color: '#38BDF8', radius: 2.2 };
  }
  if (text.includes('blocked') || text.includes('deflected')) {
    return { label: 'Blocked', color: '#F59E0B', radius: 2.0 };
  }
  return { label: 'Off-Target', color: '#94A3B8', radius: 1.8 };
}

export default function WebPitchShotMapOverlay({
  plays,
  selectedPlay,
  onSelectPlay,
}: WebPitchShotMapOverlayProps) {
  const shotPlays = plays.filter((p) => {
    const t = (p.type_text || p.type || '').toLowerCase();
    return t.includes('shot') || t.includes('goal') || t.includes('attempt');
  });

  return (
    <g className="shot-map-overlay">
      <defs>
        <marker
          id="shotArrow"
          viewBox="0 0 6 6"
          refX="4"
          refY="3"
          markerWidth="4"
          markerHeight="4"
          orient="auto-start-reverse"
        >
          <path d="M 0 0 L 6 3 L 0 6 z" fill="rgba(255,255,255,0.7)" />
        </marker>
      </defs>

      {shotPlays.map((p) => {
        if (p.x_percent == null || p.y_percent == null) return null;
        const sx = p.x_percent * 1.05;
        const sy = p.y_percent * 0.68;
        const targetGoalX = sx > 52.5 ? 103 : 2;
        const tx = p.target_x != null ? p.target_x * 1.05 : targetGoalX;
        const ty = p.target_y != null ? p.target_y * 0.68 : 34;

        const outcome = resolveShotOutcome(p);
        const isSelected = selectedPlay?.id === p.id;

        return (
          <g
            key={p.id}
            onClick={(e) => {
              e.stopPropagation();
              onSelectPlay(p);
            }}
            className="cursor-pointer group"
          >
            {/* Trajectory Vector to Target */}
            <line
              x1={sx}
              y1={sy}
              x2={tx}
              y2={ty}
              stroke={outcome.color}
              strokeWidth={isSelected ? '0.8' : '0.4'}
              strokeDasharray={outcome.label === 'Goal' ? 'none' : '1.5,1'}
              opacity={isSelected ? 1 : 0.6}
              markerEnd="url(#shotArrow)"
            />

            {/* Glowing Halo for Goals */}
            {outcome.label === 'Goal' && (
              <circle
                cx={sx}
                cy={sy}
                r={outcome.radius * 2}
                fill={outcome.color}
                opacity="0.25"
                className="animate-pulse"
              />
            )}

            {/* Shot Origin Node */}
            <circle
              cx={sx}
              cy={sy}
              r={isSelected ? outcome.radius * 1.3 : outcome.radius}
              fill={outcome.color}
              stroke="#FFFFFF"
              strokeWidth={isSelected ? '0.6' : '0.3'}
              className="transition-transform group-hover:scale-125"
            />

            {/* Minute Badge */}
            {isSelected && (
              <g transform={`translate(${sx}, ${sy - 3.5})`}>
                <rect
                  x="-12"
                  y="-2.5"
                  width="24"
                  height="4.2"
                  rx="1.5"
                  fill="#0F172A"
                  fillOpacity="0.95"
                  stroke={outcome.color}
                  strokeWidth="0.3"
                />
                <text
                  x="0"
                  y="0.3"
                  fontSize="1.8"
                  textAnchor="middle"
                  fill="#FFFFFF"
                  fontWeight="800"
                >
                  {p.player ? p.player.split(' ').slice(-1)[0] : 'Shot'} • {outcome.label}
                </text>
              </g>
            )}
          </g>
        );
      })}
    </g>
  );
}

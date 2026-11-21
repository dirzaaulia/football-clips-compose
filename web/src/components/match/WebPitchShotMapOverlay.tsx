'use client';

import React from 'react';
import { WebPitchPlay } from './WebPitchRadar';
import { calculateShotXg, calculateShotXgot } from './WebPitchXgHelper';

interface WebPitchShotMapOverlayProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  homeColor: string;
  awayColor: string;
  homeTeam?: string;
  awayTeam?: string;
  isHalfPitch?: boolean;
}

export function resolveShotOutcome(play: WebPitchPlay): {
  label: string;
  color: string;
} {
  const t = (play.type_text || play.type || '').toLowerCase();
  const text = (play.text || '').toLowerCase();

  if (t.includes('goal') || text.includes('goal')) {
    return { label: 'Goal', color: '#DFFF00' };
  }
  if (text.includes('saved') || text.includes('save') || text.includes('parried')) {
    return { label: 'Saved', color: '#38BDF8' };
  }
  if (text.includes('blocked') || text.includes('deflected')) {
    return { label: 'Blocked', color: '#F59E0B' };
  }
  return { label: 'Off-Target', color: '#94A3B8' };
}

export default function WebPitchShotMapOverlay({
  plays,
  selectedPlay,
  onSelectPlay,
  homeTeam = 'Home',
  isHalfPitch = false,
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

        const isHome = p.team_name
          ? p.team_name.toLowerCase().includes(homeTeam.toLowerCase())
          : (p.period !== 2);

        // Normalize X for Half Pitch vs Full Pitch
        const rawX = isHalfPitch ? Math.max(50, p.x_percent) : p.x_percent;
        const mappedX = isHalfPitch ? ((rawX - 50) / 50) * 100 : rawX;

        const sx = 2 + (mappedX / 100) * 101;
        const sy = 2 + (p.y_percent / 100) * 64;

        const targetGoalX = isHome ? 102.5 : 2.5;
        const rawTx = p.target_x != null ? (2 + (p.target_x / 100) * 101) : targetGoalX;
        const tx = isHalfPitch ? 102.5 : rawTx;
        const ty = p.target_y != null ? (2 + (p.target_y / 100) * 64) : 34.0;

        const outcome = resolveShotOutcome(p);
        const xg = calculateShotXg(p, isHome);
        const radius = Math.max(1.8, Math.min(4.8, 1.6 + xg * 3.4));
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
            {/* Real Trajectory Vector */}
            <line
              x1={sx}
              y1={sy}
              x2={tx}
              y2={ty}
              stroke={outcome.color}
              strokeWidth={isSelected ? '0.9' : '0.45'}
              strokeDasharray={outcome.label === 'Goal' ? 'none' : '1.5,1'}
              opacity={isSelected ? 1 : 0.65}
              markerEnd="url(#shotArrow)"
            />

            {/* Glowing Halo for Goals & Selected Shots */}
            {(outcome.label === 'Goal' || isSelected) && (
              <circle
                cx={sx}
                cy={sy}
                r={radius * 1.9}
                fill={outcome.color}
                opacity={isSelected ? 0.35 : 0.2}
                className={outcome.label === 'Goal' ? 'animate-pulse' : ''}
              />
            )}

            {/* Shot Origin Node Sized Proportionally to xG */}
            <circle
              cx={sx}
              cy={sy}
              r={isSelected ? radius * 1.25 : radius}
              fill={outcome.color}
              stroke="#FFFFFF"
              strokeWidth={isSelected ? '0.6' : '0.3'}
              className="transition-transform group-hover:scale-125"
            />

            {/* Shot Badge Indicator */}
            {isSelected && (
              <g transform={`translate(${sx}, ${sy - 3.8})`}>
                <rect
                  x="-14"
                  y="-2.5"
                  width="28"
                  height="4.6"
                  rx="1.5"
                  fill="#0F172A"
                  fillOpacity="0.95"
                  stroke={outcome.color}
                  strokeWidth="0.3"
                />
                <text
                  x="0"
                  y="0.5"
                  fontSize="1.7"
                  textAnchor="middle"
                  fill="#FFFFFF"
                  fontWeight="800"
                >
                  {p.player ? p.player.split(' ').slice(-1)[0] : 'Shot'} • {p.clock} ({xg.toFixed(2)} xG)
                </text>
              </g>
            )}
          </g>
        );
      })}
    </g>
  );
}

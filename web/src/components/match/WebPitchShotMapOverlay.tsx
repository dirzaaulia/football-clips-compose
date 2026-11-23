'use client';

import React, { useMemo } from 'react';
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
  priority: number;
} {
  const t = (play.type_text || play.type || '').toLowerCase();
  const text = (play.text || '').toLowerCase();

  if (t.includes('goal') || text.includes('goal')) {
    return { label: 'Goal', color: '#DFFF00', priority: 4 };
  }
  if (text.includes('saved') || text.includes('save') || text.includes('parried')) {
    return { label: 'Saved', color: '#38BDF8', priority: 3 };
  }
  if (text.includes('blocked') || text.includes('deflected')) {
    return { label: 'Blocked', color: '#F59E0B', priority: 2 };
  }
  if (text.includes('post') || text.includes('crossbar') || text.includes('woodwork')) {
    return { label: 'Woodwork', color: '#A855F7', priority: 2 };
  }
  return { label: 'Off-Target', color: '#FF2E4D', priority: 1 };
}

export function calculateShotDistanceMeters(xPct: number, yPct: number, isHome: boolean): number {
  const targetX = isHome ? 105 : 0;
  const targetY = 34;
  const currX = (xPct / 100) * 105;
  const currY = (yPct / 100) * 68;
  const dist = Math.sqrt(Math.pow(targetX - currX, 2) + Math.pow(targetY - currY, 2));
  return Number(dist.toFixed(1));
}

export default function WebPitchShotMapOverlay({
  plays,
  selectedPlay,
  onSelectPlay,
  homeColor,
  awayColor,
  homeTeam = 'Home',
  isHalfPitch = false,
}: WebPitchShotMapOverlayProps) {
  const shotNodes = useMemo(() => {
    const rawShots = plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      return (t.includes('shot') || t.includes('goal') || t.includes('attempt')) && p.x_percent != null && p.y_percent != null;
    });

    // 1. Calculate raw origin coordinates
    const initial = rawShots.map((p) => {
      const isHome = p.team_name ? p.team_name.toLowerCase().includes(homeTeam.toLowerCase()) : p.period !== 2;
      const rawX = isHalfPitch ? Math.max(50, p.x_percent) : p.x_percent;
      const mappedX = isHalfPitch ? ((rawX - 50) / 50) * 100 : rawX;
      const ox = 2 + (mappedX / 100) * 101;
      const oy = 2 + (p.y_percent / 100) * 64;
      const targetGoalX = isHome ? 102.5 : 2.5;
      const rawTx = p.target_x != null ? (2 + (p.target_x / 100) * 101) : targetGoalX;
      const tx = isHalfPitch ? 102.5 : rawTx;
      const ty = p.target_y != null ? (2 + (p.target_y / 100) * 64) : 34.0;
      const outcome = resolveShotOutcome(p);
      const xg = calculateShotXg(p, isHome);
      const xgot = calculateShotXgot(xg, p.goal_position_y);
      const distM = calculateShotDistanceMeters(p.x_percent, p.y_percent, isHome);
      const radius = Math.max(1.6, Math.min(3.2, 1.4 + xg * 2.2));
      return { p, isHome, ox, oy, tx, ty, outcome, xg, xgot, distM, radius, dx: ox, dy: oy, isClustered: false };
    });

    // 2. Spider rosette fan-out for colliding shots (threshold < 2.4 units)
    for (let i = 0; i < initial.length; i++) {
      const cluster = [initial[i]];
      for (let j = i + 1; j < initial.length; j++) {
        const dist = Math.hypot(initial[i].ox - initial[j].ox, initial[i].oy - initial[j].oy);
        if (dist < 2.4) cluster.push(initial[j]);
      }
      if (cluster.length > 1) {
        cluster.forEach((item, cIdx) => {
          const angle = (2 * Math.PI * cIdx) / cluster.length - Math.PI / 2;
          item.dx = Math.max(3, Math.min(102, item.ox + 2.0 * Math.cos(angle)));
          item.dy = Math.max(4, Math.min(64, item.oy + 2.0 * Math.sin(angle)));
          item.isClustered = true;
        });
      }
    }

    // 3. Z-Index Sort: Goals on top, selected on very top
    return initial.sort((a, b) => {
      if (selectedPlay?.id === a.p.id) return 1;
      if (selectedPlay?.id === b.p.id) return -1;
      return a.outcome.priority - b.outcome.priority;
    });
  }, [plays, selectedPlay, homeTeam, isHalfPitch]);

  return (
    <g className="shot-map-overlay select-none">
      <defs>
        <marker id="shotArrow" viewBox="0 0 6 6" refX="4" refY="3" markerWidth="4" markerHeight="4" orient="auto-start-reverse">
          <path d="M 0 0 L 6 3 L 0 6 z" fill="rgba(255,255,255,0.7)" />
        </marker>
      </defs>

      {shotNodes.map((n) => {
        const isSelected = selectedPlay?.id === n.p.id;
        const teamColor = n.isHome ? homeColor : awayColor;

        return (
          <g key={n.p.id} onClick={(e) => { e.stopPropagation(); onSelectPlay(n.p); }} className="cursor-pointer group">
            {/* Trajectory Vector */}
            <line
              x1={n.dx}
              y1={n.dy}
              x2={n.tx}
              y2={n.ty}
              stroke={n.outcome.color}
              strokeWidth={isSelected ? '0.9' : '0.4'}
              strokeDasharray={n.outcome.label === 'Goal' ? 'none' : '1.5,1'}
              opacity={isSelected ? 1 : 0.6}
              markerEnd="url(#shotArrow)"
            />

            {/* Hairline anchor connector for rosette clustered shots */}
            {n.isClustered && (
              <line x1={n.ox} y1={n.oy} x2={n.dx} y2={n.dy} stroke={teamColor} strokeWidth="0.25" opacity="0.45" strokeDasharray="0.8,0.8" />
            )}

            {/* Glowing Halo for Goals & Selected Shots (Centered, zero coordinate jump) */}
            {(n.outcome.label === 'Goal' || isSelected) && (
              <circle cx={n.dx} cy={n.dy} r={n.radius + 1.4} fill={n.outcome.color} opacity={isSelected ? 0.45 : 0.25} />
            )}

            {/* Calibrated Shot Node (Zero CSS scale to ensure 100% stable hover) */}
            <circle
              cx={n.dx}
              cy={n.dy}
              r={n.radius}
              fill={n.outcome.color}
              stroke={isSelected ? '#FFFFFF' : teamColor}
              strokeWidth={isSelected ? '0.8' : '0.45'}
            />

            {/* Enhanced Tooltip HUD on Selected Shot with Readable Typography */}
            {isSelected && (
              <g transform={`translate(${Math.min(84, Math.max(20, n.dx))}, ${Math.max(8, n.dy - 6)})`}>
                <rect x="-19" y="-4.0" width="38" height="8.0" rx="1.8" fill="#0A0A0C" fillOpacity="0.95" stroke={teamColor} strokeWidth="0.5" />
                <text x="0" y="-1.2" fontSize="2.2" textAnchor="middle" fill="#FFFFFF" fontWeight="900" fontFamily="sans-serif">
                  {n.p.player || 'Shot'} • {n.p.clock}
                </text>
                <text x="0" y="2.0" fontSize="1.8" textAnchor="middle" fill="#DFFF00" fontWeight="bold" fontFamily="monospace">
                  xG: {n.xg.toFixed(2)} • {n.distM}m • {n.outcome.label}
                </text>
              </g>
            )}
          </g>
        );
      })}
    </g>
  );
}

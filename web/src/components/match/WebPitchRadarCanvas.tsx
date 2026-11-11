'use client';

import React, { useRef } from 'react';
import { WebPitchPlay } from './WebPitchRadar';
import { ensureLegibleHex } from '@/lib/colors';

interface WebPitchRadarCanvasProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  getMarkerColor: (play: WebPitchPlay) => string;
  homeColor?: string | null;
  awayColor?: string | null;
}

export default function WebPitchRadarCanvas({
  plays,
  selectedPlay,
  onSelectPlay,
  getMarkerColor,
  homeColor,
  awayColor,
}: WebPitchRadarCanvasProps) {
  const svgRef = useRef<SVGSVGElement | null>(null);

  const hColor = ensureLegibleHex(homeColor, '#3B82F6');
  const aColor = ensureLegibleHex(awayColor, '#EC4899');

  const sx = selectedPlay?.x_percent != null ? selectedPlay.x_percent * 1.05 : null;
  const sy = selectedPlay?.y_percent != null ? selectedPlay.y_percent * 0.68 : null;

  const isHomePlay = selectedPlay?.team_name?.includes(plays[0]?.team_name || '') ?? true;
  const targetGoalX = sx && sx > 52.5 ? 103 : 2;

  const tx = selectedPlay?.target_x != null
    ? selectedPlay.target_x * 1.05
    : (selectedPlay?.type?.includes('shot') || selectedPlay?.type?.includes('goal'))
      ? targetGoalX
      : null;
  const ty = selectedPlay?.target_y != null ? selectedPlay.target_y * 0.68 : (tx != null ? 34 : null);

  const trajectoryPath = sx != null && sy != null && tx != null && ty != null
    ? `M ${sx} ${sy} Q ${(sx + tx) / 2} ${(sy + ty) / 2 - 5} ${tx} ${ty}`
    : null;

  const isShot = Boolean(selectedPlay?.type?.includes('shot') || selectedPlay?.type?.includes('goal'));
  const activeTokenColor = isHomePlay ? hColor : aColor;

  const handlePitchClick = (e: React.MouseEvent<SVGSVGElement>) => {
    if (!svgRef.current || plays.length === 0) return;
    const rect = svgRef.current.getBoundingClientRect();
    const clickX = ((e.clientX - rect.left) / rect.width) * 105;
    const clickY = ((e.clientY - rect.top) / rect.height) * 68;

    let closest: WebPitchPlay | null = null;
    let minDistance = Infinity;

    for (const p of plays) {
      if (p.x_percent == null || p.y_percent == null) continue;
      const px = p.x_percent * 1.05;
      const py = p.y_percent * 0.68;
      const dist = Math.hypot(px - clickX, py - clickY);
      if (dist < minDistance) {
        minDistance = dist;
        closest = p;
      }
    }

    if (closest && minDistance < 18) {
      onSelectPlay(closest);
    }
  };

  return (
    <div className="relative aspect-[1.54/1] w-full rounded-2xl overflow-hidden bg-[#0A1A0E] border border-emerald-900/60 shadow-[inset_0_2px_14px_rgba(0,0,0,0.85)]">
      <svg
        ref={svgRef}
        onClick={handlePitchClick}
        className="absolute inset-0 w-full h-full select-none cursor-crosshair"
        viewBox="0 0 105 68"
      >
        <defs>
          <linearGradient id="dangerConeGrad" x1={sx ? `${(sx / 105) * 100}%` : '50%'} y1="50%" x2={tx && tx > 50 ? '100%' : '0%'} y2="50%">
            <stop offset="0%" stopColor="#EF4444" stopOpacity="0.45" />
            <stop offset="100%" stopColor="#EF4444" stopOpacity="0.03" />
          </linearGradient>
          <radialGradient id="ballShade" cx="35%" cy="35%" r="65%">
            <stop offset="0%" stopColor="#FFFFFF" />
            <stop offset="70%" stopColor="#E2E8F0" />
            <stop offset="100%" stopColor="#1E293B" />
          </radialGradient>
        </defs>

        {/* FM Alternating Turf Lawn Stripes */}
        {Array.from({ length: 9 }).map((_, i) => (
          <rect
            key={i}
            x={i * 11.66}
            y={0}
            width={11.66}
            height={68}
            fill={i % 2 === 0 ? '#0B2213' : '#0F2C18'}
          />
        ))}

        {/* Pitch Lines */}
        <g stroke="rgba(255,255,255,0.35)" strokeWidth="0.7" fill="none">
          <rect x="2" y="2" width="101" height="64" rx="1.5" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" />
          <circle cx="52.5" cy="34" r="9.15" />
          <circle cx="52.5" cy="34" r="0.6" fill="white" className="opacity-70" />

          {/* Goal boxes */}
          <rect x="2" y="14" width="16.5" height="40" />
          <rect x="2" y="23" width="5.5" height="22" />
          <circle cx="11" cy="34" r="0.7" fill="white" className="opacity-50" />

          <rect x="86.5" y="14" width="16.5" height="40" />
          <rect x="97.5" y="23" width="5.5" height="22" />
          <circle cx="94" cy="34" r="0.7" fill="white" className="opacity-50" />
        </g>

        {/* Shooting Danger Cone */}
        {isShot && sx != null && sy != null && tx != null && (
          <polygon
            points={`${sx},${sy} ${tx},23 ${tx},45`}
            fill="url(#dangerConeGrad)"
            className="animate-pulse"
          />
        )}

        {/* Goalkeeper Token on Goal Line */}
        {isShot && tx != null && (
          <g>
            <circle cx={tx > 50 ? 102.5 : 2.5} cy={34} r="2.4" fill="#F59E0B" stroke="#FFFFFF" strokeWidth="0.6" />
            <text x={tx > 50 ? 102.5 : 2.5} y={35} fontSize="1.8" textAnchor="middle" fill="#000000" fontWeight="900">
              1
            </text>
          </g>
        )}

        {/* Ball Trajectory Curved Arc */}
        {trajectoryPath && (
          <path
            d={trajectoryPath}
            stroke={getMarkerColor(selectedPlay!)}
            strokeWidth="1.1"
            strokeDasharray="2,1.5"
            strokeLinecap="round"
            fill="none"
            className="opacity-90"
          />
        )}

        {/* Animated Soccer Ball along Trajectory */}
        {trajectoryPath && (
          <g>
            <circle r="1.3" fill="url(#ballShade)" stroke="#0F172A" strokeWidth="0.3">
              <animateMotion
                path={trajectoryPath}
                dur="1.2s"
                repeatCount="indefinite"
                rotate="auto"
              />
            </circle>
          </g>
        )}

        {/* Discovered Play Nodes */}
        {plays.map((play) => {
          if (play.x_percent == null || play.y_percent == null) return null;
          if (play.id === selectedPlay?.id) return null;
          const px = play.x_percent * 1.05;
          const py = play.y_percent * 0.68;
          const col = getMarkerColor(play);
          return (
            <g
              key={play.id}
              onClick={(e) => {
                e.stopPropagation();
                onSelectPlay(play);
              }}
              className="cursor-pointer group"
            >
              <circle cx={px} cy={py} r="2.8" fill="transparent" />
              <circle
                cx={px}
                cy={py}
                r="1.3"
                fill={col}
                className="opacity-75 group-hover:opacity-100 group-hover:scale-125 transition-transform"
              />
            </g>
          );
        })}

        {/* Active FM Player Token & Floating Name Tag */}
        {sx != null && sy != null && selectedPlay && (
          <g>
            <circle cx={sx} cy={sy} r="4.8" fill={activeTokenColor} opacity="0.2">
              <animate attributeName="r" values="3.5;6.5;3.5" dur="1.8s" repeatCount="indefinite" />
              <animate attributeName="opacity" values="0.35;0.05;0.35" dur="1.8s" repeatCount="indefinite" />
            </circle>
            <circle cx={sx} cy={sy} r="2.8" fill={activeTokenColor} stroke="#FFFFFF" strokeWidth="0.7" />
            <text x={sx} y={sy + 1} fontSize="2.2" textAnchor="middle" fill="#FFFFFF" fontWeight="900">
              {selectedPlay.period ?? 9}
            </text>

            {/* Floating Player Name Pill */}
            <g transform={`translate(${sx}, ${sy < 18 ? sy + 6 : sy - 5})`}>
              <rect
                x="-14"
                y="-3.2"
                width="28"
                height="5"
                rx="2"
                fill="#0F172A"
                fillOpacity="0.9"
                stroke="rgba(255,255,255,0.2)"
                strokeWidth="0.4"
              />
              <text
                x="0"
                y="0.2"
                fontSize="2.1"
                textAnchor="middle"
                fill="#FFFFFF"
                fontWeight="700"
              >
                {selectedPlay.player ? selectedPlay.player.split(' ').slice(-1)[0] : 'Player'} • {selectedPlay.clock}
              </text>
            </g>
          </g>
        )}
      </svg>
    </div>
  );
}

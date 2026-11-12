'use client';

import React, { useRef, useMemo } from 'react';
import { WebPitchPlay } from './WebPitchRadar';
import { ensureLegibleHex } from '@/lib/colors';
import { MatchFormations } from '@/lib/database';
import { calculateStarterPositions } from './WebPitchFormationHelper';
import WebPitchPlayersOverlay from './WebPitchPlayersOverlay';
import WebPitchShotMapOverlay from './WebPitchShotMapOverlay';
import WebPitchHeatmapOverlay from './WebPitchHeatmapOverlay';

interface WebPitchRadarCanvasProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  getMarkerColor: (play: WebPitchPlay) => string;
  homeColor?: string | null;
  awayColor?: string | null;
  viewMode: 'REPLAY' | 'SHOTS' | 'HEATMAP';
  formations?: MatchFormations | null;
  selectedPlayerName?: string | null;
  onSelectPlayerName?: (name: string | null) => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  homeTeam: string;
  awayTeam: string;
}

export default function WebPitchRadarCanvas({
  plays,
  selectedPlay,
  onSelectPlay,
  getMarkerColor,
  homeColor,
  awayColor,
  viewMode,
  formations,
  selectedPlayerName,
  onSelectPlayerName,
  heatmapTeam,
  homeTeam,
  awayTeam,
}: WebPitchRadarCanvasProps) {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const hColor = ensureLegibleHex(homeColor, '#3B82F6');
  const aColor = ensureLegibleHex(awayColor, '#EC4899');

  const homeNodes = useMemo(() => calculateStarterPositions(formations?.home, true), [formations?.home]);
  const awayNodes = useMemo(() => calculateStarterPositions(formations?.away, false), [formations?.away]);

  const rawSx = selectedPlay?.x_percent != null ? selectedPlay.x_percent * 1.05 : null;
  const rawSy = selectedPlay?.y_percent != null ? selectedPlay.y_percent * 0.68 : null;
  const sx = rawSx != null ? Math.max(6.5, Math.min(98.5, rawSx)) : null;
  const sy = rawSy != null ? Math.max(5.5, Math.min(62.5, rawSy)) : null;

  const isHomePlay = selectedPlay?.team_name?.includes(homeTeam) ?? true;
  const targetGoalX = sx && sx > 52.5 ? 102.0 : 3.0;

  const rawTx = selectedPlay?.target_x != null
    ? selectedPlay.target_x * 1.05
    : (selectedPlay?.type?.includes('shot') || selectedPlay?.type?.includes('goal'))
      ? targetGoalX
      : null;
  const tx = rawTx != null ? Math.max(3.0, Math.min(102.0, rawTx)) : null;
  const rawTy = selectedPlay?.target_y != null ? selectedPlay.target_y * 0.68 : (tx != null ? 34 : null);
  const ty = rawTy != null ? Math.max(16.0, Math.min(52.0, rawTy)) : null;

  const trajectoryPath = sx != null && sy != null && tx != null && ty != null
    ? `M ${sx} ${sy} Q ${(sx + tx) / 2} ${(sy + ty) / 2 - 4} ${tx} ${ty}`
    : null;

  const isShot = Boolean(selectedPlay?.type?.includes('shot') || selectedPlay?.type?.includes('goal'));
  const activeTokenColor = isHomePlay ? hColor : aColor;

  const handlePitchClick = (e: React.MouseEvent<SVGSVGElement>) => {
    if (!svgRef.current || plays.length === 0) return;
    const rect = svgRef.current.getBoundingClientRect();
    const cx = ((e.clientX - rect.left) / rect.width) * 105;
    const cy = ((e.clientY - rect.top) / rect.height) * 68;

    let closest: WebPitchPlay | null = null;
    let minDist = Infinity;
    for (const p of plays) {
      if (p.x_percent == null || p.y_percent == null) continue;
      const dist = Math.hypot(p.x_percent * 1.05 - cx, p.y_percent * 0.68 - cy);
      if (dist < minDist) {
        minDist = dist;
        closest = p;
      }
    }
    if (closest && minDist < 18) onSelectPlay(closest);
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
          <filter id="ballShadow" x="-50%" y="-50%" width="200%" height="200%">
            <feDropShadow dx="0" dy="0.3" stdDeviation="0.4" floodColor="#000000" floodOpacity="0.8" />
          </filter>
          <linearGradient id="dangerConeGrad" x1={sx ? `${(sx / 105) * 100}%` : '50%'} y1="50%" x2={tx && tx > 50 ? '100%' : '0%'} y2="50%">
            <stop offset="0%" stopColor="#F59E0B" stopOpacity="0.45" />
            <stop offset="100%" stopColor="#FBBF24" stopOpacity="0.08" />
          </linearGradient>
          <radialGradient id="ballShade" cx="35%" cy="35%" r="65%">
            <stop offset="0%" stopColor="#FFFFFF" />
            <stop offset="65%" stopColor="#F8FAFC" />
            <stop offset="100%" stopColor="#E2E8F0" />
          </radialGradient>
        </defs>

        {/* FM Alternating Turf Stripes */}
        {Array.from({ length: 9 }).map((_, i) => (
          <rect key={i} x={i * 11.66} y={0} width={11.66} height={68} fill={i % 2 === 0 ? '#0B2213' : '#0F2C18'} />
        ))}

        {/* Standard Pitch Lines */}
        <g stroke="rgba(255,255,255,0.35)" strokeWidth="0.7" fill="none">
          <rect x="2" y="2" width="101" height="64" rx="1.5" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" />
          <circle cx="52.5" cy="34" r="9.15" />
          <circle cx="52.5" cy="34" r="0.6" fill="white" className="opacity-70" />
          <rect x="2" y="14" width="16.5" height="40" />
          <rect x="2" y="23" width="5.5" height="22" />
          <circle cx="11" cy="34" r="0.7" fill="white" className="opacity-50" />
          <rect x="86.5" y="14" width="16.5" height="40" />
          <rect x="97.5" y="23" width="5.5" height="22" />
          <circle cx="94" cy="34" r="0.7" fill="white" className="opacity-50" />
        </g>

        {/* View Mode 1: Shot & Goal Map */}
        {viewMode === 'SHOTS' && (
          <WebPitchShotMapOverlay
            plays={plays}
            selectedPlay={selectedPlay}
            onSelectPlay={onSelectPlay}
            homeColor={hColor}
            awayColor={aColor}
          />
        )}

        {/* View Mode 2: Density / Player Heatmap */}
        {viewMode === 'HEATMAP' && (
          <WebPitchHeatmapOverlay
            plays={plays}
            selectedPlayerName={selectedPlayerName}
            heatmapTeam={heatmapTeam}
            homeTeam={homeTeam}
            awayTeam={awayTeam}
            homeColor={hColor}
            awayColor={aColor}
          />
        )}

        {/* Formation Players (Rendered in REPLAY or Heatmap mode) */}
        {(viewMode === 'REPLAY' || (viewMode === 'HEATMAP' && selectedPlayerName != null)) && (
          <WebPitchPlayersOverlay
            homeNodes={homeNodes}
            awayNodes={awayNodes}
            homeColor={hColor}
            awayColor={aColor}
            selectedPlayerName={selectedPlayerName}
            onPlayerClick={(node) => onSelectPlayerName?.(node.player.name)}
            activePlayPlayer={selectedPlay?.player}
          />
        )}

        {/* View Mode 3: 2D Play-by-Play Replay Specifics */}
        {viewMode === 'REPLAY' && (
          <>
            {/* Shooting Danger Cone */}
            {isShot && sx != null && sy != null && tx != null && (
              <polygon
                points={`${sx},${sy} ${tx},23 ${tx},45`}
                fill="url(#dangerConeGrad)"
                stroke="rgba(251,191,36,0.75)"
                strokeWidth="0.5"
                strokeDasharray="2,1.5"
                className="animate-pulse"
              />
            )}

            {/* Reactive Goalkeeper Token */}
            {isShot && tx != null && (
              <g>
                <circle cx={tx > 50 ? 101.5 : 3.5} cy={ty ?? 34} r="2.4" fill="#F59E0B" stroke="#FFFFFF" strokeWidth="0.6" />
                <text x={tx > 50 ? 101.5 : 3.5} y={(ty ?? 34) + 0.8} fontSize="1.8" textAnchor="middle" fill="#000000" fontWeight="900">1</text>
              </g>
            )}

            {/* Ball Trajectory Curved Arc */}
            {trajectoryPath && (
              <path d={trajectoryPath} stroke={getMarkerColor(selectedPlay!)} strokeWidth="1.1" strokeDasharray="2,1.5" strokeLinecap="round" fill="none" className="opacity-90" />
            )}

            {/* Animated Travelling Soccer Ball */}
            {trajectoryPath && (
              <g filter="url(#ballShadow)">
                <circle r="1.4" fill="url(#ballShade)" stroke="#FFFFFF" strokeWidth="0.3">
                  <animateMotion path={trajectoryPath} dur="1.2s" repeatCount="indefinite" rotate="auto" />
                </circle>
              </g>
            )}

            {/* Active Kicker Disc & Floating Name Tag */}
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
                <g transform={`translate(${sx}, ${sy < 18 ? sy + 6 : sy - 5})`}>
                  <rect x="-14" y="-3.2" width="28" height="5" rx="2" fill="#0F172A" fillOpacity="0.9" stroke="rgba(255,255,255,0.2)" strokeWidth="0.4" />
                  <text x="0" y="0.2" fontSize="2.1" textAnchor="middle" fill="#FFFFFF" fontWeight="700">
                    {selectedPlay.player ? selectedPlay.player.split(' ').slice(-1)[0] : 'Player'} • {selectedPlay.clock}
                  </text>
                </g>
              </g>
            )}
          </>
        )}
      </svg>
    </div>
  );
}

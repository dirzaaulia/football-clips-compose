'use client';

import React from 'react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchHeatmapOverlayProps {
  plays: WebPitchPlay[];
  selectedPlayerName?: string | null;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  homeTeam: string;
  awayTeam: string;
  homeColor: string;
  awayColor: string;
}

export default function WebPitchHeatmapOverlay({
  plays,
  selectedPlayerName,
  heatmapTeam,
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
}: WebPitchHeatmapOverlayProps) {
  const filtered = plays.filter((p) => {
    if (p.x_percent == null || p.y_percent == null) return false;
    if (selectedPlayerName && selectedPlayerName !== 'ALL') {
      return p.player?.toLowerCase().includes(selectedPlayerName.toLowerCase());
    }
    if (heatmapTeam === 'HOME') return p.team_name?.includes(homeTeam);
    if (heatmapTeam === 'AWAY') return p.team_name?.includes(awayTeam);
    return true;
  });

  return (
    <g className="heatmap-overlay">
      <defs>
        <filter id="heatBlur" x="-30%" y="-30%" width="160%" height="160%">
          <feGaussianBlur stdDeviation="3.5" result="blur" />
        </filter>
        <radialGradient id="heatHotHome" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#EF4444" stopOpacity="0.85" />
          <stop offset="35%" stopColor="#F59E0B" stopOpacity="0.65" />
          <stop offset="70%" stopColor={homeColor} stopOpacity="0.35" />
          <stop offset="100%" stopColor={homeColor} stopOpacity="0" />
        </radialGradient>
        <radialGradient id="heatHotAway" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#EF4444" stopOpacity="0.85" />
          <stop offset="35%" stopColor="#F59E0B" stopOpacity="0.65" />
          <stop offset="70%" stopColor={awayColor} stopOpacity="0.35" />
          <stop offset="100%" stopColor={awayColor} stopOpacity="0" />
        </radialGradient>
      </defs>

      {/* Render Action Density Heat Blobs */}
      <g filter="url(#heatBlur)">
        {filtered.map((p, idx) => {
          const x = (p.x_percent ?? 50) * 1.05;
          const y = (p.y_percent ?? 50) * 0.68;
          const isHome = p.team_name?.includes(homeTeam) ?? true;
          const gradId = isHome ? 'url(#heatHotHome)' : 'url(#heatHotAway)';
          const r = p.type?.includes('goal') || p.type?.includes('shot') ? 9 : 6.5;

          return (
            <circle
              key={`heat-${p.id || idx}`}
              cx={x}
              cy={y}
              r={r}
              fill={gradId}
              opacity="0.8"
            />
          );
        })}
      </g>

      {/* Heatmap Legend Inset */}
      <g transform="translate(4, 62)">
        <rect
          x="0"
          y="0"
          width="48"
          height="4.5"
          rx="2"
          fill="#0F172A"
          fillOpacity="0.8"
          stroke="rgba(255,255,255,0.15)"
          strokeWidth="0.3"
        />
        <text x="3" y="3" fontSize="1.8" fill="#94A3B8" fontWeight="600">
          Activity Density:
        </text>
        <circle cx="24" cy="2.2" r="1.3" fill="#3B82F6" />
        <text x="27" y="3" fontSize="1.7" fill="#CBD5E1">Low</text>
        <circle cx="34" cy="2.2" r="1.3" fill="#F59E0B" />
        <text x="37" y="3" fontSize="1.7" fill="#CBD5E1">Med</text>
        <circle cx="43" cy="2.2" r="1.3" fill="#EF4444" />
        <text x="45.5" y="3" fontSize="1.7" fill="#CBD5E1">High</text>
      </g>
    </g>
  );
}

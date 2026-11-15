'use client';

import React, { useMemo } from 'react';
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
  const filtered = useMemo(() => {
    return plays.filter((p) => {
      if (p.x_percent == null || p.y_percent == null) return false;
      if (selectedPlayerName && selectedPlayerName !== 'ALL') {
        return p.player?.toLowerCase().includes(selectedPlayerName.toLowerCase());
      }
      if (heatmapTeam === 'HOME') return p.team_name?.includes(homeTeam);
      if (heatmapTeam === 'AWAY') return p.team_name?.includes(awayTeam);
      return true;
    });
  }, [plays, selectedPlayerName, heatmapTeam, homeTeam, awayTeam]);

  const { homeTilt, awayTilt } = useMemo(() => {
    if (filtered.length === 0) return { homeTilt: 50, awayTilt: 50 };
    const homeHalfCount = filtered.filter((p) => (p.x_percent ?? 50) >= 50).length;
    const h = Math.round((homeHalfCount / filtered.length) * 100);
    return { homeTilt: h, awayTilt: 100 - h };
  }, [filtered]);

  return (
    <g className="heatmap-overlay">
      <defs>
        <filter id="heatBlur" x="-30%" y="-30%" width="160%" height="160%">
          <feGaussianBlur stdDeviation="3.8" result="blur" />
        </filter>
        <radialGradient id="heatHotHome" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#DFFF00" stopOpacity="0.95" />
          <stop offset="25%" stopColor="#F59E0B" stopOpacity="0.75" />
          <stop offset="55%" stopColor={homeColor} stopOpacity="0.45" />
          <stop offset="85%" stopColor="#38BDF8" stopOpacity="0.15" />
          <stop offset="100%" stopColor="#38BDF8" stopOpacity="0" />
        </radialGradient>
        <radialGradient id="heatHotAway" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#DFFF00" stopOpacity="0.95" />
          <stop offset="25%" stopColor="#F59E0B" stopOpacity="0.75" />
          <stop offset="55%" stopColor={awayColor} stopOpacity="0.45" />
          <stop offset="85%" stopColor="#38BDF8" stopOpacity="0.15" />
          <stop offset="100%" stopColor="#38BDF8" stopOpacity="0" />
        </radialGradient>
      </defs>

      {/* Render Action Density Gaussian Heat Fields */}
      <g filter="url(#heatBlur)">
        {filtered.map((p, idx) => {
          const x = (p.x_percent ?? 50) * 1.05;
          const y = (p.y_percent ?? 50) * 0.68;
          const isHome = p.team_name?.includes(homeTeam) ?? true;
          const gradId = isHome ? 'url(#heatHotHome)' : 'url(#heatHotAway)';
          const r = p.type?.includes('goal') || p.type?.includes('shot') ? 9.5 : 7.0;

          return (
            <circle
              key={`heat-${p.id || idx}`}
              cx={x}
              cy={y}
              r={r}
              fill={gradId}
              opacity="0.85"
            />
          );
        })}
      </g>

      {/* Industry-Grade HUD Legend Inset */}
      <g transform="translate(3, 61.5)">
        <rect
          x="0"
          y="0"
          width="54"
          height="5"
          rx="1.5"
          fill="#0A0A0C"
          fillOpacity="0.9"
          stroke="rgba(255,255,255,0.18)"
          strokeWidth="0.3"
        />
        <text x="2.5" y="3.3" fontSize="1.8" fill="#A1A1AA" fontWeight="bold" fontFamily="monospace">
          DENSITY:
        </text>
        <circle cx="16" cy="2.5" r="1.1" fill="#38BDF8" />
        <text x="18" y="3.3" fontSize="1.6" fill="#E4E4E7" fontFamily="monospace">Low</text>
        <circle cx="24.5" cy="2.5" r="1.1" fill="#F59E0B" />
        <text x="26.5" y="3.3" fontSize="1.6" fill="#E4E4E7" fontFamily="monospace">Med</text>
        <circle cx="33" cy="2.5" r="1.1" fill="#DFFF00" />
        <text x="35" y="3.3" fontSize="1.6" fill="#E4E4E7" fontFamily="monospace">High</text>
        <line x1="41" y1="1" x2="41" y2="4" stroke="rgba(255,255,255,0.15)" strokeWidth="0.3" />
        <text x="42.5" y="3.3" fontSize="1.5" fill="#DFFF00" fontWeight="bold" fontFamily="monospace">
          {homeTilt}% TILT
        </text>
      </g>
    </g>
  );
}

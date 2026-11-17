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
  heatmapTeam,
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
}: WebPitchHeatmapOverlayProps) {
  const { homeThird, middleThird, awayThird, homeTilt, awayTilt } = useMemo(() => {
    const validPlays = plays.filter((p) => p.x_percent != null);
    if (validPlays.length === 0) {
      return { homeThird: 33, middleThird: 34, awayThird: 33, homeTilt: 52, awayTilt: 48 };
    }
    const dCount = validPlays.filter((p) => (p.x_percent ?? 50) < 33).length;
    const mCount = validPlays.filter((p) => (p.x_percent ?? 50) >= 33 && (p.x_percent ?? 50) <= 66).length;
    const aCount = validPlays.filter((p) => (p.x_percent ?? 50) > 66).length;
    const total = validPlays.length || 1;

    const hThird = Math.round((dCount / total) * 100);
    const midThird = Math.round((mCount / total) * 100);
    const aThird = 100 - hThird - midThird;

    const homePlays = validPlays.filter((p) => (p.team_name || '').toLowerCase().includes(homeTeam.toLowerCase()));
    const homeFinalThird = homePlays.filter((p) => (p.x_percent ?? 50) > 66).length;
    const awayPlays = validPlays.filter((p) => (p.team_name || '').toLowerCase().includes(awayTeam.toLowerCase()));
    const awayFinalThird = awayPlays.filter((p) => (p.x_percent ?? 50) < 33).length;

    const tiltTotal = homeFinalThird + awayFinalThird || 1;
    const hTilt = Math.min(85, Math.max(15, Math.round((homeFinalThird / tiltTotal) * 100)));

    return {
      homeThird: hThird,
      middleThird: midThird,
      awayThird: aThird,
      homeTilt: hTilt,
      awayTilt: 100 - hTilt,
    };
  }, [plays, homeTeam, awayTeam]);

  return (
    <g className="tactical-zones-overlay select-none">
      {/* 3 Pitch Thirds Separator Lines & Percentage Overlays */}
      <line x1="35.6" y1="2" x2="35.6" y2="66" stroke="rgba(255,255,255,0.12)" strokeWidth="0.4" strokeDasharray="2,2" />
      <line x1="69.3" y1="2" x2="69.3" y2="66" stroke="rgba(255,255,255,0.12)" strokeWidth="0.4" strokeDasharray="2,2" />

      {/* Thirds Territory Labels */}
      <text x="18.8" y="6" fontSize="1.8" textAnchor="middle" fill="#94A3B8" fontWeight="bold" fontFamily="monospace">
        DEF 1/3 ({homeThird}%)
      </text>
      <text x="52.5" y="6" fontSize="1.8" textAnchor="middle" fill="#94A3B8" fontWeight="bold" fontFamily="monospace">
        MID 1/3 ({middleThird}%)
      </text>
      <text x="86.1" y="6" fontSize="1.8" textAnchor="middle" fill="#94A3B8" fontWeight="bold" fontFamily="monospace">
        ATT 1/3 ({awayThird}%)
      </text>

      {/* Zone 14 Danger Corridors (Outside Penalty Boxes) */}
      {(heatmapTeam === 'ALL' || heatmapTeam === 'HOME') && (
        <g className="zone14-home">
          <rect
            x="70.0"
            y="23.0"
            width="16.5"
            height="22.0"
            fill="#DFFF00"
            fillOpacity="0.04"
            stroke="#DFFF00"
            strokeOpacity="0.30"
            strokeWidth="0.5"
            strokeDasharray="2,1.5"
            rx="1.0"
          />
          <text x="78.2" y="34.5" fontSize="2.0" textAnchor="middle" fill="#DFFF00" fontWeight="900" opacity="0.6">
            ZONE 14
          </text>
        </g>
      )}

      {(heatmapTeam === 'ALL' || heatmapTeam === 'AWAY') && (
        <g className="zone14-away">
          <rect
            x="18.5"
            y="23.0"
            width="16.5"
            height="22.0"
            fill="#38BDF8"
            fillOpacity="0.04"
            stroke="#38BDF8"
            strokeOpacity="0.30"
            strokeWidth="0.5"
            strokeDasharray="2,1.5"
            rx="1.0"
          />
          <text x="26.7" y="34.5" fontSize="2.0" textAnchor="middle" fill="#38BDF8" fontWeight="900" opacity="0.6">
            ZONE 14
          </text>
        </g>
      )}

      {/* Industry-Grade Tactical Territorial Tilt HUD */}
      <g transform="translate(3, 60.5)">
        <rect
          x="0"
          y="0"
          width="58"
          height="5.5"
          rx="1.2"
          fill="#0A0A0C"
          fillOpacity="0.95"
          stroke="rgba(255,255,255,0.18)"
          strokeWidth="0.3"
        />
        <text x="2.5" y="3.6" fontSize="1.7" fill="#A1A1AA" fontWeight="bold" fontFamily="monospace">
          TERRITORIAL TILT:
        </text>
        <circle cx="27.5" cy="2.8" r="1.0" fill={homeColor} />
        <text x="29.5" y="3.6" fontSize="1.6" fill="#FFFFFF" fontWeight="bold" fontFamily="monospace">
          {homeTilt}%
        </text>
        <line x1="39" y1="1" x2="39" y2="4.5" stroke="rgba(255,255,255,0.15)" strokeWidth="0.3" />
        <circle cx="43" cy="2.8" r="1.0" fill={awayColor} />
        <text x="45" y="3.6" fontSize="1.6" fill="#FFFFFF" fontWeight="bold" fontFamily="monospace">
          {awayTilt}%
        </text>
      </g>
    </g>
  );
}

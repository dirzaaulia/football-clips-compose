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
  const filteredPlays = useMemo(() => {
    return plays.filter((p) => {
      if (p.x_percent == null || p.y_percent == null) return false;
      const tName = (p.team_name || '').toLowerCase();
      if (heatmapTeam === 'HOME') return tName.includes(homeTeam.toLowerCase());
      if (heatmapTeam === 'AWAY') return tName.includes(awayTeam.toLowerCase());
      return true;
    });
  }, [plays, heatmapTeam, homeTeam, awayTeam]);

  const { homeZ14Count, awayZ14Count, homeZ14Shots, awayZ14Shots } = useMemo(() => {
    let hCount = 0;
    let aCount = 0;
    let hShots = 0;
    let aShots = 0;

    plays.forEach((p) => {
      if (p.x_percent == null || p.y_percent == null) return;
      const isShot = (p.type_text || p.type || '').toLowerCase().includes('shot');
      const inHomeZ14 = p.x_percent >= 66 && p.x_percent <= 84 && p.y_percent >= 30 && p.y_percent <= 70;
      const inAwayZ14 = p.x_percent >= 16 && p.x_percent <= 34 && p.y_percent >= 30 && p.y_percent <= 70;
      const tName = (p.team_name || '').toLowerCase();

      if (tName.includes(homeTeam.toLowerCase()) && inHomeZ14) {
        hCount++;
        if (isShot) hShots++;
      } else if (tName.includes(awayTeam.toLowerCase()) && inAwayZ14) {
        aCount++;
        if (isShot) aShots++;
      }
    });

    return {
      homeZ14Count: hCount,
      awayZ14Count: aCount,
      homeZ14Shots: hShots,
      awayZ14Shots: aShots,
    };
  }, [plays, homeTeam, awayTeam]);

  const { homeThird, middleThird, awayThird, homeTilt, awayTilt } = useMemo(() => {
    const validPlays = plays.filter((p) => p.x_percent != null);
    if (validPlays.length === 0) {
      return { homeThird: 33, middleThird: 34, awayThird: 33, homeTilt: 50, awayTilt: 50 };
    }
    const dCount = validPlays.filter((p) => (p.x_percent ?? 50) < 33).length;
    const mCount = validPlays.filter((p) => (p.x_percent ?? 50) >= 33 && (p.x_percent ?? 50) <= 66).length;
    const aCount = validPlays.filter((p) => (p.x_percent ?? 50) > 66).length;
    const total = validPlays.length || 1;

    const homePlays = validPlays.filter((p) => (p.team_name || '').toLowerCase().includes(homeTeam.toLowerCase()));
    const homeFinalThird = homePlays.filter((p) => (p.x_percent ?? 50) > 66).length;
    const awayPlays = validPlays.filter((p) => (p.team_name || '').toLowerCase().includes(awayTeam.toLowerCase()));
    const awayFinalThird = awayPlays.filter((p) => (p.x_percent ?? 50) < 33).length;

    const tiltTotal = homeFinalThird + awayFinalThird || 1;
    const hTilt = Math.min(85, Math.max(15, Math.round((homeFinalThird / tiltTotal) * 100)));

    return {
      homeThird: Math.round((dCount / total) * 100),
      middleThird: Math.round((mCount / total) * 100),
      awayThird: Math.round((aCount / total) * 100),
      homeTilt: hTilt,
      awayTilt: 100 - hTilt,
    };
  }, [plays, homeTeam, awayTeam]);

  return (
    <g className="tactical-zones-overlay select-none">
      <defs>
        {/* Dynamic Team Touch Gradients */}
        <radialGradient id="homeHeatGradient" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor={homeColor} stopOpacity="0.5" />
          <stop offset="45%" stopColor={homeColor} stopOpacity="0.25" />
          <stop offset="85%" stopColor={homeColor} stopOpacity="0.06" />
          <stop offset="100%" stopColor={homeColor} stopOpacity="0" />
        </radialGradient>
        <radialGradient id="awayHeatGradient" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor={awayColor} stopOpacity="0.5" />
          <stop offset="45%" stopColor={awayColor} stopOpacity="0.25" />
          <stop offset="85%" stopColor={awayColor} stopOpacity="0.06" />
          <stop offset="100%" stopColor={awayColor} stopOpacity="0" />
        </radialGradient>
      </defs>

      {/* Dynamic Team-Colored Touch Hotspots */}
      {filteredPlays.slice(0, 45).map((p) => {
        const sx = 2 + (p.x_percent / 100) * 101;
        const sy = 2 + (p.y_percent / 100) * 64;
        const isHome = (p.team_name || '').toLowerCase().includes(homeTeam.toLowerCase());
        const grad = isHome ? 'url(#homeHeatGradient)' : 'url(#awayHeatGradient)';
        return <circle key={p.id} cx={sx} cy={sy} r="9.0" fill={grad} pointerEvents="none" />;
      })}

      {/* 3 Pitch Thirds Separator Lines */}
      <line x1="35.6" y1="2" x2="35.6" y2="66" stroke="rgba(255,255,255,0.22)" strokeWidth="0.5" strokeDasharray="2.5,2" />
      <line x1="69.3" y1="2" x2="69.3" y2="66" stroke="rgba(255,255,255,0.22)" strokeWidth="0.5" strokeDasharray="2.5,2" />

      {/* Thirds Territory Headers with Readable Font Scaling */}
      <g transform="translate(18.8, 6)">
        <rect x="-14" y="-2.5" width="28" height="5.0" rx="1.4" fill="#0A0A0C" fillOpacity="0.9" stroke="rgba(255,255,255,0.18)" strokeWidth="0.3" />
        <text x="0" y="0.8" fontSize="2.2" textAnchor="middle" fill="#FFFFFF" fontWeight="800" fontFamily="sans-serif">
          DEF 1/3 ({homeThird}%)
        </text>
      </g>
      <g transform="translate(52.5, 6)">
        <rect x="-14" y="-2.5" width="28" height="5.0" rx="1.4" fill="#0A0A0C" fillOpacity="0.9" stroke="rgba(255,255,255,0.18)" strokeWidth="0.3" />
        <text x="0" y="0.8" fontSize="2.2" textAnchor="middle" fill="#FFFFFF" fontWeight="800" fontFamily="sans-serif">
          MID 1/3 ({middleThird}%)
        </text>
      </g>
      <g transform="translate(86.1, 6)">
        <rect x="-14" y="-2.5" width="28" height="5.0" rx="1.4" fill="#0A0A0C" fillOpacity="0.9" stroke="rgba(255,255,255,0.18)" strokeWidth="0.3" />
        <text x="0" y="0.8" fontSize="2.2" textAnchor="middle" fill="#FFFFFF" fontWeight="800" fontFamily="sans-serif">
          ATT 1/3 ({awayThird}%)
        </text>
      </g>

      {/* Zone 14 Danger Corridor - Home Attack */}
      {(heatmapTeam === 'ALL' || heatmapTeam === 'HOME') && (
        <g className="zone14-home">
          <rect
            x="70.0"
            y="21.5"
            width="16.5"
            height="25.0"
            fill={homeColor}
            fillOpacity="0.08"
            stroke={homeColor}
            strokeOpacity="0.55"
            strokeWidth="0.6"
            strokeDasharray="2,1.5"
            rx="1.4"
          />
          <text x="78.2" y="32.0" fontSize="2.2" textAnchor="middle" fill={homeColor} fontWeight="900" fontFamily="sans-serif">
            ZONE 14
          </text>
          <text x="78.2" y="35.5" fontSize="1.6" textAnchor="middle" fill="#FFFFFF" fontWeight="bold">
            {homeZ14Count} Entri • {homeZ14Shots} Tembakan
          </text>
          <line x1="84.5" y1="34" x2="88" y2="34" stroke={homeColor} strokeWidth="0.6" markerEnd="url(#shotArrow)" />
        </g>
      )}

      {/* Zone 14 Danger Corridor - Away Attack */}
      {(heatmapTeam === 'ALL' || heatmapTeam === 'AWAY') && (
        <g className="zone14-away">
          <rect
            x="18.5"
            y="21.5"
            width="16.5"
            height="25.0"
            fill={awayColor}
            fillOpacity="0.08"
            stroke={awayColor}
            strokeOpacity="0.55"
            strokeWidth="0.6"
            strokeDasharray="2,1.5"
            rx="1.4"
          />
          <text x="26.7" y="32.0" fontSize="2.2" textAnchor="middle" fill={awayColor} fontWeight="900" fontFamily="sans-serif">
            ZONE 14
          </text>
          <text x="26.7" y="35.5" fontSize="1.6" textAnchor="middle" fill="#FFFFFF" fontWeight="bold">
            {awayZ14Count} Entri • {awayZ14Shots} Tembakan
          </text>
          <line x1="20.5" y1="34" x2="17" y2="34" stroke={awayColor} strokeWidth="0.6" markerEnd="url(#shotArrow)" />
        </g>
      )}

      {/* Territorial Tilt HUD with Scaled Text */}
      <g transform="translate(3, 59.5)">
        <rect x="0" y="0" width="64" height="6.5" rx="1.4" fill="#0A0A0C" fillOpacity="0.95" stroke="rgba(255,255,255,0.22)" strokeWidth="0.35" />
        <text x="3.0" y="4.2" fontSize="2.0" fill="#A1A1AA" fontWeight="bold" fontFamily="sans-serif">
          TERRITORIAL TILT:
        </text>
        <circle cx="31" cy="3.3" r="1.2" fill={homeColor} />
        <text x="33.5" y="4.2" fontSize="2.0" fill="#FFFFFF" fontWeight="900" fontFamily="monospace">
          {homeTilt}%
        </text>
        <line x1="43.5" y1="1.2" x2="43.5" y2="5.3" stroke="rgba(255,255,255,0.2)" strokeWidth="0.35" />
        <circle cx="48" cy="3.3" r="1.2" fill={awayColor} />
        <text x="50.5" y="4.2" fontSize="2.0" fill="#FFFFFF" fontWeight="900" fontFamily="monospace">
          {awayTilt}%
        </text>
      </g>
    </g>
  );
}

'use client';

import React from 'react';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchAnimatedTraceProps {
  play: WebPitchPlay;
  teamColor: string;
}

export default function WebPitchAnimatedTrace({ play, teamColor }: WebPitchAnimatedTraceProps) {
  const x1 = (play.x_percent / 100) * 105;
  const y1 = (play.y_percent / 100) * 68;

  const isHome = play.team_name ? !play.team_name.toLowerCase().includes('away') : true;
  const defaultTargetX = isHome ? 103 : 2;
  const defaultTargetY = 34;

  const x2 = play.target_x != null ? (play.target_x / 100) * 105 : defaultTargetX;
  const y2 = play.target_y != null ? (play.target_y / 100) * 68 : defaultTargetY;

  const isGoal = (play.type || play.type_text || '').toLowerCase().includes('goal');
  const accentColor = isGoal ? '#DFFF00' : teamColor;

  return (
    <g className="pointer-events-none transition-all duration-300">
      {/* Animated Dash Path from Shot Origin to Goal/Target */}
      <line
        x1={x1}
        y1={y1}
        x2={x2}
        y2={y2}
        stroke={accentColor}
        strokeWidth="0.8"
        strokeDasharray="2 1.5"
        strokeLinecap="round"
        className="opacity-80"
      />

      {/* Gliding Ball Motion Simulation */}
      <circle r="2.2" fill={accentColor} opacity="0.35">
        <animate attributeName="cx" from={x1} to={x2} dur="1.2s" repeatCount="indefinite" />
        <animate attributeName="cy" from={y1} to={y2} dur="1.2s" repeatCount="indefinite" />
      </circle>
      <circle r="1.1" fill="#FFFFFF" stroke="#000000" strokeWidth="0.2">
        <animate attributeName="cx" from={x1} to={x2} dur="1.2s" repeatCount="indefinite" />
        <animate attributeName="cy" from={y1} to={y2} dur="1.2s" repeatCount="indefinite" />
      </circle>

      {/* Origin Shooter Ring */}
      <circle cx={x1} cy={y1} r="2.4" fill="none" stroke={accentColor} strokeWidth="0.5" className="opacity-75" />
      <circle cx={x1} cy={y1} r="1.0" fill={accentColor} />

      {/* Target Impact Marker */}
      <circle cx={x2} cy={y2} r="2.0" fill="none" stroke={isGoal ? '#DFFF00' : '#FF2E4D'} strokeWidth="0.6">
        <animate attributeName="r" values="1.2;3.6;1.2" dur="1.6s" repeatCount="indefinite" />
        <animate attributeName="opacity" values="0.9;0.15;0.9" dur="1.6s" repeatCount="indefinite" />
      </circle>
      <circle cx={x2} cy={y2} r="0.9" fill={isGoal ? '#DFFF00' : '#FF2E4D'} />
    </g>
  );
}

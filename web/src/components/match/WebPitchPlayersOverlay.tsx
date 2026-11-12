'use client';

import React from 'react';
import { FormationNode } from './WebPitchFormationHelper';

interface WebPitchPlayersOverlayProps {
  homeNodes: FormationNode[];
  awayNodes: FormationNode[];
  homeColor: string;
  awayColor: string;
  selectedPlayerName?: string | null;
  onPlayerClick?: (node: FormationNode) => void;
  activePlayPlayer?: string | null;
}

export default function WebPitchPlayersOverlay({
  homeNodes,
  awayNodes,
  homeColor,
  awayColor,
  selectedPlayerName,
  onPlayerClick,
  activePlayPlayer,
}: WebPitchPlayersOverlayProps) {
  const allNodes = [...homeNodes, ...awayNodes];

  return (
    <g className="formation-players-overlay">
      {allNodes.map((node) => {
        const isSelected = Boolean(
          selectedPlayerName &&
            node.player.name.toLowerCase().includes(selectedPlayerName.toLowerCase())
        );
        const isActiveKicker = Boolean(
          activePlayPlayer &&
            node.player.name.toLowerCase().includes(activePlayPlayer.toLowerCase())
        );
        const isGK = node.player.position === 'GK' || node.player.position === 'G';
        const teamColor = node.isHome ? homeColor : awayColor;
        const jerseyColor = isGK ? '#F59E0B' : teamColor;
        const shortName = node.player.name.trim().split(' ').slice(-1)[0] || 'Player';

        return (
          <g
            key={`${node.isHome ? 'h' : 'a'}-${node.player.name}-${node.player.jersey || '0'}`}
            onClick={(e) => {
              e.stopPropagation();
              onPlayerClick?.(node);
            }}
            className="cursor-pointer group select-none"
          >
            {/* Active Glow Halo */}
            {(isSelected || isActiveKicker) && (
              <circle cx={node.x} cy={node.y} r="4.6" fill={jerseyColor} opacity="0.35">
                <animate attributeName="r" values="3.6;5.6;3.6" dur="1.5s" repeatCount="indefinite" />
                <animate attributeName="opacity" values="0.45;0.1;0.45" dur="1.5s" repeatCount="indefinite" />
              </circle>
            )}

            {/* UEFA / Premier League Style Squad Kit Jersey */}
            <g transform={`translate(${node.x}, ${node.y})`} className="transition-transform group-hover:scale-110">
              {/* Jersey Body & Sleeves Silhouette */}
              <path
                d="M -2.1 -2.3 L -1.0 -2.7 L -0.5 -2.2 L 0.5 -2.2 L 1.0 -2.7 L 2.1 -2.3 L 2.6 -1.0 L 1.7 -0.7 L 1.7 2.1 L -1.7 2.1 L -1.7 -0.7 L -2.6 -1.0 Z"
                fill={isSelected ? '#FFFFFF' : jerseyColor}
                stroke={isSelected ? jerseyColor : '#FFFFFF'}
                strokeWidth="0.35"
              />
              {/* Collar Detail */}
              <path
                d="M -0.6 -2.2 Q 0 -1.6 0.6 -2.2"
                stroke={isSelected ? jerseyColor : '#FFFFFF'}
                strokeWidth="0.3"
                fill="none"
              />
              {/* Squad Number */}
              <text
                x="0"
                y="1.0"
                fontSize="1.7"
                textAnchor="middle"
                fill={isSelected ? '#0F172A' : '#FFFFFF'}
                fontWeight="900"
              >
                {node.player.jersey || (node.player.position ? node.player.position.slice(0, 2) : '•')}
              </text>
            </g>

            {/* Frosted Glass Surname Badge */}
            <g transform={`translate(${node.x}, ${node.y < 12 ? node.y + 4.4 : node.y - 3.8})`}>
              <rect
                x="-8.5"
                y="-1.7"
                width="17"
                height="3.4"
                rx="1.2"
                fill="#0F172A"
                fillOpacity="0.9"
                stroke={isSelected ? jerseyColor : 'rgba(255,255,255,0.22)'}
                strokeWidth="0.3"
              />
              <text
                x="0"
                y="0.4"
                fontSize="1.4"
                textAnchor="middle"
                fill="#FFFFFF"
                fontWeight="800"
              >
                {shortName}
              </text>
            </g>
          </g>
        );
      })}
    </g>
  );
}

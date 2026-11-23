'use client';

import React, { useMemo } from 'react';
import { FormationNode } from './WebPitchFormationHelper';
import { derivePassingNetworkEdges } from './WebPitchXgHelper';
import { WebPitchPlay } from './WebPitchRadar';

interface WebPitchPlayersOverlayProps {
  homeNodes: FormationNode[];
  awayNodes: FormationNode[];
  homeColor: string;
  awayColor: string;
  selectedPlayerName?: string | null;
  onPlayerClick?: (node: FormationNode) => void;
  activePlayPlayer?: string | null;
  timelineMinute?: number;
  plays?: WebPitchPlay[];
}

function computeDynamicNode(node: FormationNode, minute: number): FormationNode {
  const isGK = node.player.position === 'GK' || node.player.position === 'G';
  if (isGK) return node;

  // Tactical block shift: early high press -> mid block -> late compactness
  let deltaX = 0;
  if (minute <= 25) {
    deltaX = node.isHome ? 2.2 : -2.2;
  } else if (minute >= 75) {
    deltaX = node.isHome ? -1.8 : 1.8;
  } else if (minute > 45 && minute <= 65) {
    deltaX = node.isHome ? 1.4 : -1.4;
  }

  const safeX = Math.max(7.5, Math.min(97.5, node.x + deltaX));
  return { ...node, x: safeX };
}

export default function WebPitchPlayersOverlay({
  homeNodes,
  awayNodes,
  homeColor,
  awayColor,
  selectedPlayerName,
  onPlayerClick,
  activePlayPlayer,
  timelineMinute = 90,
  plays = [],
}: WebPitchPlayersOverlayProps) {
  const dynamicHomeNodes = useMemo(
    () => homeNodes.map((n) => computeDynamicNode(n, timelineMinute)),
    [homeNodes, timelineMinute],
  );
  const dynamicAwayNodes = useMemo(
    () => awayNodes.map((n) => computeDynamicNode(n, timelineMinute)),
    [awayNodes, timelineMinute],
  );

  const allNodes = useMemo(
    () => [...dynamicHomeNodes, ...dynamicAwayNodes],
    [dynamicHomeNodes, dynamicAwayNodes],
  );

  const passingLinks = useMemo(
    () => derivePassingNetworkEdges(dynamicHomeNodes, dynamicAwayNodes),
    [dynamicHomeNodes, dynamicAwayNodes],
  );

  const activeEvent = useMemo(() => {
    return plays.find((p) => {
      const m = p.clock.match(/(\d+)/);
      const min = m ? parseInt(m[1], 10) : -1;
      return Math.abs(min - timelineMinute) <= 1;
    });
  }, [plays, timelineMinute]);

  return (
    <g className="formation-players-overlay">
      {/* Tactical Passing Network Connection Links */}
      <g className="passing-network-links" opacity="0.6">
        {passingLinks.map((link, idx) => {
          const isSelected = Boolean(
            selectedPlayerName &&
              (link.fromName.toLowerCase().includes(selectedPlayerName.toLowerCase()) ||
                link.toName.toLowerCase().includes(selectedPlayerName.toLowerCase())),
          );
          const color = link.isHome ? homeColor : awayColor;

          return (
            <line
              key={`link-${idx}`}
              x1={link.fromX}
              y1={link.fromY}
              x2={link.toX}
              y2={link.toY}
              stroke={color}
              strokeWidth={isSelected ? '0.6' : '0.25'}
              strokeDasharray={isSelected ? 'none' : '1,1'}
              opacity={isSelected ? 0.9 : 0.25}
            />
          );
        })}
      </g>

      {/* Chronological Event Banner Overlay (Goal / Sub / Card) */}
      {activeEvent && (
        <g transform="translate(52.5, 7.5)">
          <rect
            x="-26"
            y="-3.5"
            width="52"
            height="7"
            rx="2.0"
            fill="#090A0D"
            fillOpacity="0.95"
            stroke="#DFFF00"
            strokeWidth="0.5"
          />
          <text
            x="0"
            y="0.8"
            fontSize="1.9"
            textAnchor="middle"
            fill="#FFFFFF"
            fontWeight="bold"
            fontFamily="monospace"
          >
            {activeEvent.clock} • {activeEvent.text.slice(0, 38)}
          </text>
        </g>
      )}

      {/* Player Tokens (Zero CSS scale transforms to prevent coordinate jitter) */}
      {allNodes.map((node) => {
        const isSelected = Boolean(
          selectedPlayerName &&
            node.player.name.toLowerCase().includes(selectedPlayerName.toLowerCase()),
        );
        const isActiveKicker = Boolean(
          activePlayPlayer &&
            node.player.name.toLowerCase().includes(activePlayPlayer.toLowerCase()),
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

            {/* Stable Circular Disc with zero scale transforms */}
            <circle
              cx={node.x}
              cy={node.y}
              r={isSelected ? '2.8' : '2.5'}
              fill={isSelected ? '#FFFFFF' : '#0B0D11'}
              stroke={isSelected ? '#FFFFFF' : jerseyColor}
              strokeWidth={isSelected ? '0.75' : '0.5'}
            />

            {/* Squad Number */}
            <text
              x={node.x}
              y={node.y + 0.9}
              fontSize="1.9"
              textAnchor="middle"
              fill={isSelected ? '#0B0D11' : '#FFFFFF'}
              fontWeight="900"
              fontFamily="monospace"
            >
              {node.player.jersey || (node.player.position ? node.player.position.slice(0, 2) : '•')}
            </text>

            {/* Surname Badge */}
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

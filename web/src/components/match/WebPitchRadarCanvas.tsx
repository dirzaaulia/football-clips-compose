'use client';

import React, { useRef, useMemo } from 'react';
import { WebPitchPlay } from './WebPitchRadar';
import { ensureLegibleHex } from '@/lib/colors';
import { MatchFormations } from '@/lib/database';
import { calculateStarterPositions } from './WebPitchFormationHelper';
import WebPitchPlayersOverlay from './WebPitchPlayersOverlay';
import WebPitchShotMapOverlay from './WebPitchShotMapOverlay';
import WebPitchHeatmapOverlay from './WebPitchHeatmapOverlay';
import WebPitchPlayerCardHUD from './WebPitchPlayerCardHUD';
import WebPitchGoalmouthHUD from './WebPitchGoalmouthHUD';
import { calculateShotXg, calculateShotXgot } from './WebPitchXgHelper';

interface WebPitchRadarCanvasProps {
  plays: WebPitchPlay[];
  selectedPlay: WebPitchPlay | null;
  onSelectPlay: (play: WebPitchPlay) => void;
  homeColor?: string | null;
  awayColor?: string | null;
  viewMode: 'FORMATIONS' | 'SHOTS' | 'ZONES';
  formations?: MatchFormations | null;
  selectedPlayerName?: string | null;
  onSelectPlayerName?: (name: string | null) => void;
  heatmapTeam: 'ALL' | 'HOME' | 'AWAY';
  homeTeam: string;
  awayTeam: string;
  isHalfPitch?: boolean;
}

export default function WebPitchRadarCanvas({
  plays,
  selectedPlay,
  onSelectPlay,
  homeColor,
  awayColor,
  viewMode,
  formations,
  selectedPlayerName,
  onSelectPlayerName,
  heatmapTeam,
  homeTeam,
  awayTeam,
  isHalfPitch = false,
}: WebPitchRadarCanvasProps) {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const hColor = ensureLegibleHex(homeColor, '#DFFF00');
  const aColor = ensureLegibleHex(awayColor, '#38BDF8');

  const homeNodes = useMemo(() => calculateStarterPositions(formations?.home, true), [formations?.home]);
  const awayNodes = useMemo(() => calculateStarterPositions(formations?.away, false), [formations?.away]);

  const selectedNode = useMemo(() => {
    if (!selectedPlayerName) return null;
    return [...homeNodes, ...awayNodes].find((n) =>
      n.player.name.toLowerCase().includes(selectedPlayerName.toLowerCase()),
    );
  }, [selectedPlayerName, homeNodes, awayNodes]);

  const viewBox = isHalfPitch ? '50 0 55 68' : '0 0 105 68';

  return (
    <div className="relative aspect-[1.54/1] w-full rounded-2xl overflow-hidden bg-[#0A0A0C] border border-white/10 shadow-[inset_0_2px_24px_rgba(0,0,0,0.95)] select-none">
      <svg ref={svgRef} className="absolute inset-0 w-full h-full" viewBox={viewBox}>
        {/* Obsidian Dark Turf Alternating Bands */}
        {Array.from({ length: 9 }).map((_, i) => (
          <rect
            key={i}
            x={i * 11.66}
            y={0}
            width={11.66}
            height={68}
            fill={i % 2 === 0 ? '#0B0C10' : '#111318'}
          />
        ))}

        {/* High-Precision Opta Pitch Markings */}
        <g stroke="rgba(255,255,255,0.22)" strokeWidth="0.6" fill="none">
          <rect x="2" y="2" width="101" height="64" rx="1.5" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" />
          <circle cx="52.5" cy="34" r="9.15" />
          <circle cx="52.5" cy="34" r="0.6" fill="white" className="opacity-50" />

          {/* Corner Arcs */}
          <path d="M 2 3.5 A 1.5 1.5 0 0 0 3.5 2" />
          <path d="M 2 64.5 A 1.5 1.5 0 0 1 3.5 66" />
          <path d="M 103 3.5 A 1.5 1.5 0 0 1 101.5 2" />
          <path d="M 103 64.5 A 1.5 1.5 0 0 0 101.5 66" />

          {/* Penalty Boxes & D-Box Arcs */}
          <rect x="2" y="14" width="16.5" height="40" />
          <rect x="2" y="23" width="5.5" height="22" />
          <circle cx="11" cy="34" r="0.7" fill="white" className="opacity-40" />
          <path d="M 18.5 27.5 A 9.15 9.15 0 0 1 18.5 40.5" />

          <rect x="86.5" y="14" width="16.5" height="40" />
          <rect x="97.5" y="23" width="5.5" height="22" />
          <circle cx="94" cy="34" r="0.7" fill="white" className="opacity-40" />
          <path d="M 86.5 27.5 A 9.15 9.15 0 0 0 86.5 40.5" />

          {/* Goal Net Mouths */}
          <rect x="0.4" y="30.34" width="1.6" height="7.32" stroke="rgba(255,255,255,0.4)" strokeWidth="0.4" />
          <rect x="103.0" y="30.34" width="1.6" height="7.32" stroke="rgba(255,255,255,0.4)" strokeWidth="0.4" />
        </g>

        {viewMode === 'FORMATIONS' && (
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

        {viewMode === 'SHOTS' && (
          <WebPitchShotMapOverlay
            plays={plays}
            selectedPlay={selectedPlay}
            onSelectPlay={onSelectPlay}
            homeColor={hColor}
            awayColor={aColor}
            homeTeam={homeTeam}
            awayTeam={awayTeam}
            isHalfPitch={isHalfPitch}
          />
        )}

        {viewMode === 'ZONES' && (
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
      </svg>

      {/* Selected Player HUD Card */}
      {viewMode === 'FORMATIONS' && selectedNode && (
        <WebPitchPlayerCardHUD
          node={selectedNode}
          onClose={() => onSelectPlayerName?.(null)}
          teamColor={selectedNode.isHome ? hColor : aColor}
        />
      )}

      {/* Selected Shot Goalmouth HUD Inset */}
      {viewMode === 'SHOTS' && selectedPlay && (
        <div className="absolute bottom-3 right-3 z-30 max-w-[200px] w-full">
          <WebPitchGoalmouthHUD
            goalPosY={selectedPlay.goal_position_y}
            outcomeLabel={selectedPlay.type_text || selectedPlay.type || 'Shot'}
            xg={calculateShotXg(selectedPlay, true)}
            xgot={calculateShotXgot(calculateShotXg(selectedPlay, true), selectedPlay.goal_position_y)}
          />
        </div>
      )}
    </div>
  );
}

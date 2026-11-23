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
import WebPitchAnimatedTrace from './WebPitchAnimatedTrace';
import WebPitchCanvasHUD from './WebPitchCanvasHUD';
import { calculateShotXg, calculateShotXgot } from './WebPitchXgHelper';

interface WebPitchRadarCanvasProps {
  plays: WebPitchPlay[];
  allPlays?: WebPitchPlay[];
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
  timelineMinute?: number;
}

export default function WebPitchRadarCanvas({
  plays,
  allPlays = [],
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
  timelineMinute = 90,
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

  const isSelectedHome = selectedPlay?.team_name?.toLowerCase().includes(homeTeam.toLowerCase()) ?? true;

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
          <rect x="2" y="2" width="101" height="64" rx="2" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" />
          <circle cx="52.5" cy="34" r="9.15" />
          <circle cx="52.5" cy="34" r="0.6" fill="rgba(255,255,255,0.8)" />

          {/* Left Penalty Area */}
          <rect x="2" y="13.84" width="16.5" height="40.32" />
          <rect x="2" y="24.84" width="5.5" height="18.32" />
          <circle cx="13" cy="34" r="0.5" fill="rgba(255,255,255,0.8)" />
          <path d="M 18.5 27.5 A 9.15 9.15 0 0 1 18.5 40.5" />

          {/* Right Penalty Area */}
          <rect x="86.5" y="13.84" width="16.5" height="40.32" />
          <rect x="97.5" y="24.84" width="5.5" height="18.32" />
          <circle cx="92" cy="34" r="0.5" fill="rgba(255,255,255,0.8)" />
          <path d="M 86.5 27.5 A 9.15 9.15 0 0 0 86.5 40.5" />

          {/* Corner Arcs */}
          <path d="M 2 3.5 A 1.5 1.5 0 0 0 3.5 2" />
          <path d="M 2 64.5 A 1.5 1.5 0 0 1 3.5 66" />
          <path d="M 103 3.5 A 1.5 1.5 0 0 1 101.5 2" />
          <path d="M 103 64.5 A 1.5 1.5 0 0 0 101.5 66" />
        </g>

        {/* View Mode Layers */}
        {viewMode === 'FORMATIONS' && (
          <WebPitchPlayersOverlay
            homeNodes={homeNodes}
            awayNodes={awayNodes}
            homeColor={hColor}
            awayColor={aColor}
            selectedPlayerName={selectedPlayerName}
            onPlayerClick={(node) => onSelectPlayerName?.(node.player.name)}
            activePlayPlayer={selectedPlay?.player}
            timelineMinute={timelineMinute}
            plays={allPlays.length > 0 ? allPlays : plays}
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

        {/* Animated Shot Trajectory Vector (Strictly in SHOTS mode) */}
        {viewMode === 'SHOTS' && selectedPlay && (
          <WebPitchAnimatedTrace
            play={selectedPlay}
            teamColor={isSelectedHome ? hColor : aColor}
          />
        )}
      </svg>

      {/* Selected Player HUD Card */}
      {viewMode === 'FORMATIONS' && selectedNode && (
        <WebPitchPlayerCardHUD
          node={selectedNode}
          teamColor={selectedNode.isHome ? hColor : aColor}
          onClose={() => onSelectPlayerName?.(null)}
        />
      )}

      {/* Goalmouth 3D Frame HUD */}
      {viewMode === 'SHOTS' && selectedPlay && (() => {
        const xgVal = calculateShotXg(selectedPlay, isSelectedHome);
        const xgotVal = calculateShotXgot(xgVal, selectedPlay.goal_position_y);
        const outcomeLabel =
          (selectedPlay.type_text || selectedPlay.type || '').toLowerCase().includes('goal')
            ? 'Goal'
            : (selectedPlay.type_text || selectedPlay.type || '').toLowerCase().includes('saved')
            ? 'Saved'
            : 'Off Target';

        return (
          <div className="absolute bottom-3 right-3 z-20 w-44 sm:w-52">
            <WebPitchGoalmouthHUD
              goalPosY={selectedPlay.goal_position_y}
              outcomeLabel={outcomeLabel}
              xg={xgVal}
              xgot={xgotVal}
            />
          </div>
        );
      })()}

      {/* Tactical Canvas Legend HUD */}
      <WebPitchCanvasHUD
        plays={plays}
        viewMode={viewMode}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        homeColor={hColor}
        awayColor={aColor}
      />
    </div>
  );
}

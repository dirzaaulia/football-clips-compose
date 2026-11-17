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
  homeColor?: string | null;
  awayColor?: string | null;
  viewMode: 'FORMATIONS' | 'SHOTS' | 'ZONES';
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

  return (
    <div className="relative aspect-[1.54/1] w-full rounded-2xl overflow-hidden bg-[#0A0A0C] border border-white/10 shadow-[inset_0_2px_24px_rgba(0,0,0,0.95)] select-none">
      <svg
        ref={svgRef}
        className="absolute inset-0 w-full h-full"
        viewBox="0 0 105 68"
      >
        <defs>
          <filter id="pitchVignette" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="0" stdDeviation="2.5" floodColor="#000000" floodOpacity="0.85" />
          </filter>
        </defs>

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

        {/* High-Precision Pitch Lines */}
        <g stroke="rgba(255,255,255,0.22)" strokeWidth="0.6" fill="none">
          <rect x="2" y="2" width="101" height="64" rx="1.5" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" />
          <circle cx="52.5" cy="34" r="9.15" />
          <circle cx="52.5" cy="34" r="0.6" fill="white" className="opacity-50" />
          {/* Penalty Areas */}
          <rect x="2" y="14" width="16.5" height="40" />
          <rect x="2" y="23" width="5.5" height="22" />
          <circle cx="11" cy="34" r="0.7" fill="white" className="opacity-40" />
          <rect x="86.5" y="14" width="16.5" height="40" />
          <rect x="97.5" y="23" width="5.5" height="22" />
          <circle cx="94" cy="34" r="0.7" fill="white" className="opacity-40" />
          {/* Goal Net Mouths */}
          <rect x="0.4" y="30.34" width="1.6" height="7.32" stroke="rgba(255,255,255,0.4)" strokeWidth="0.4" />
          <rect x="103.0" y="30.34" width="1.6" height="7.32" stroke="rgba(255,255,255,0.4)" strokeWidth="0.4" />
        </g>

        {/* Mode 1: Tactical Formations */}
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

        {/* Mode 2: Real Shot & Goal Map */}
        {viewMode === 'SHOTS' && (
          <WebPitchShotMapOverlay
            plays={plays}
            selectedPlay={selectedPlay}
            onSelectPlay={onSelectPlay}
            homeColor={hColor}
            awayColor={aColor}
            homeTeam={homeTeam}
            awayTeam={awayTeam}
          />
        )}

        {/* Mode 3: Tactical Zones & Thirds Dominance */}
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

      {/* Selected Player Tactical HUD Card */}
      {viewMode === 'FORMATIONS' && selectedNode && (
        <div className="absolute bottom-3 left-3 bg-[#0A0A0C]/95 border border-white/15 px-3 py-2 rounded-xl shadow-2xl flex items-center gap-3 backdrop-blur-md">
          <div
            className="w-7 h-7 rounded-lg flex items-center justify-center font-mono font-black text-xs border"
            style={{
              backgroundColor: `${selectedNode.isHome ? hColor : aColor}20`,
              color: selectedNode.isHome ? hColor : aColor,
              borderColor: `${selectedNode.isHome ? hColor : aColor}60`,
            }}
          >
            {selectedNode.player.jersey || '•'}
          </div>
          <div>
            <div className="text-xs font-bold text-white leading-tight font-tech">
              {selectedNode.player.name}
            </div>
            <div className="text-[10px] text-white/50 font-tech">
              {selectedNode.isHome ? homeTeam : awayTeam} • {selectedNode.player.position || 'Starter'}
            </div>
          </div>
          <button
            type="button"
            onClick={() => onSelectPlayerName?.(null)}
            className="text-white/40 hover:text-white text-xs px-1.5 py-0.5 rounded cursor-pointer"
          >
            &times;
          </button>
        </div>
      )}
    </div>
  );
}

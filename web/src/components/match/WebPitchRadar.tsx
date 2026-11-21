'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { Crosshair, Maximize2, Minimize2 } from 'lucide-react';
import { MatchFormations } from '@/lib/database';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls from './WebPitchRadarControls';
import WebPitchTimelineScrubber from './WebPitchTimelineScrubber';
import { computeAttackMomentum } from './WebPitchXgHelper';
import { ensureLegibleHex } from '@/lib/colors';

export interface WebPitchPlay {
  id: string;
  clock: string;
  type: string;
  type_text: string;
  text: string;
  player?: string;
  assist?: string;
  team_name?: string;
  x_percent: number;
  y_percent: number;
  target_x?: number | null;
  target_y?: number | null;
  goal_position_y?: number | null;
  period?: number | string;
  seconds?: number | null;
}

interface WebPitchRadarProps {
  plays: WebPitchPlay[];
  homeTeam: string;
  awayTeam: string;
  isLive?: boolean;
  homeColor?: string | null;
  awayColor?: string | null;
  formations?: MatchFormations | null;
}

export default function WebPitchRadar({
  plays,
  homeTeam,
  awayTeam,
  homeColor,
  awayColor,
  formations,
}: WebPitchRadarProps) {
  const hasLineups = Boolean(formations?.home?.starters?.length || formations?.away?.starters?.length);
  const [viewMode, setViewMode] = useState<'FORMATIONS' | 'SHOTS' | 'ZONES'>(
    hasLineups ? 'FORMATIONS' : 'SHOTS',
  );
  const [isHalfPitch, setIsHalfPitch] = useState(false);
  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(() => {
    return plays.find((p) => p.type.toLowerCase().includes('goal')) || plays[0] || null;
  });
  const [heatmapTeam, setHeatmapTeam] = useState<'ALL' | 'HOME' | 'AWAY'>('ALL');
  const [selectedPlayerName, setSelectedPlayerName] = useState<string | null>(null);

  // Timeline scrubbing & momentum state
  const [timelineMinute, setTimelineMinute] = useState(90);
  const [isPlayingTimeline, setIsPlayingTimeline] = useState(false);

  const hColor = ensureLegibleHex(homeColor, '#DFFF00');
  const aColor = ensureLegibleHex(awayColor, '#38BDF8');

  const momentum = useMemo(() => computeAttackMomentum(plays), [plays]);

  const shotPlays = useMemo(() => {
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      const isShot = t.includes('shot') || t.includes('goal') || t.includes('attempt');
      if (!isShot) return false;
      const m = p.clock.match(/(\d+)/);
      const min = m ? parseInt(m[1], 10) : 0;
      return min <= timelineMinute;
    });
  }, [plays, timelineMinute]);

  // Auto-play timeline ticker
  useEffect(() => {
    if (!isPlayingTimeline) return;
    const interval = setInterval(() => {
      setTimelineMinute((prev) => {
        if (prev >= 95) {
          setIsPlayingTimeline(false);
          return 95;
        }
        return prev + 1;
      });
    }, 400);
    return () => clearInterval(interval);
  }, [isPlayingTimeline]);

  return (
    <div className="space-y-3.5 bg-[#121316] border border-white/10 rounded-2xl p-3.5 sm:p-4 shadow-xl select-none">
      <div className="flex items-center justify-between gap-2 text-xs text-white/50 font-tech">
        <div className="flex items-center gap-2">
          <Crosshair className="w-4 h-4 text-[#DFFF00]" />
          <span className="font-extrabold text-white tracking-wide uppercase">Tactical Match Centre</span>
          <span className="hidden sm:inline text-[11px] text-white/40">
            ({homeTeam} vs {awayTeam})
          </span>
        </div>

        <div className="flex items-center gap-2">
          {viewMode === 'SHOTS' && (
            <button
              onClick={() => setIsHalfPitch((prev) => !prev)}
              className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-white/70 hover:text-white border border-white/10 text-[10px] font-bold font-mono transition cursor-pointer"
            >
              {isHalfPitch ? <Minimize2 className="w-3 h-3" /> : <Maximize2 className="w-3 h-3" />}
              <span>{isHalfPitch ? 'Full Pitch' : 'Half Pitch Focus'}</span>
            </button>
          )}
          <div className="text-[10px] font-mono text-[#DFFF00] font-black uppercase tracking-wider">
            Opta &amp; SofaScore Engine
          </div>
        </div>
      </div>

      <WebPitchRadarCanvas
        plays={shotPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        homeColor={homeColor}
        awayColor={awayColor}
        viewMode={viewMode}
        formations={formations}
        selectedPlayerName={selectedPlayerName}
        onSelectPlayerName={(name) => setSelectedPlayerName(name)}
        heatmapTeam={heatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        isHalfPitch={isHalfPitch}
      />

      <WebPitchTimelineScrubber
        momentum={momentum}
        currentMinute={timelineMinute}
        onMinuteChange={(m) => setTimelineMinute(m)}
        isPlaying={isPlayingTimeline}
        onTogglePlay={() => setIsPlayingTimeline((prev) => !prev)}
        onReset={() => {
          setIsPlayingTimeline(false);
          setTimelineMinute(90);
        }}
        plays={plays}
        homeColor={hColor}
        awayColor={aColor}
      />

      <WebPitchRadarControls
        plays={shotPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        viewMode={viewMode}
        onSelectViewMode={(mode) => {
          setViewMode(mode);
          if (mode === 'SHOTS' && !selectedPlay && shotPlays.length > 0) {
            setSelectedPlay(shotPlays[0]);
          }
        }}
        heatmapTeam={heatmapTeam}
        onSelectHeatmapTeam={setHeatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        selectedPlayerName={selectedPlayerName}
        onClearPlayerFilter={() => setSelectedPlayerName(null)}
      />
    </div>
  );
}

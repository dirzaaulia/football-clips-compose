'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { Crosshair, Maximize2, Minimize2 } from 'lucide-react';
import { MatchFormations } from '@/lib/database';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls, { ShotFilterType } from './WebPitchRadarControls';
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
  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(null);
  const [selectedPlayerName, setSelectedPlayerName] = useState<string | null>(null);
  const [heatmapTeam, setHeatmapTeam] = useState<'ALL' | 'HOME' | 'AWAY'>('ALL');
  const [shotFilter, setShotFilter] = useState<ShotFilterType>('ALL');
  const [isHalfPitch, setIsHalfPitch] = useState(false);
  const [timelineMinute, setTimelineMinute] = useState(90);
  const [isPlayingTimeline, setIsPlayingTimeline] = useState(false);

  const hColor = ensureLegibleHex(homeColor, '#DFFF00');
  const aColor = ensureLegibleHex(awayColor, '#38BDF8');
  const momentum = useMemo(() => computeAttackMomentum(plays), [plays]);

  const shotPlays = useMemo(() => {
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      const isGoal = t.includes('goal');
      const isShot = t.includes('shot') || isGoal || t.includes('attempt');
      if (!isShot) return false;

      const m = p.clock.match(/(\d+)/);
      const min = m ? parseInt(m[1], 10) : 0;
      if (min > timelineMinute) return false;

      if (shotFilter === 'GOALS') return isGoal;
      const tName = (p.team_name || '').toLowerCase();
      if (shotFilter === 'HOME') return tName.includes(homeTeam.toLowerCase());
      if (shotFilter === 'AWAY') return tName.includes(awayTeam.toLowerCase());
      return true;
    });
  }, [plays, timelineMinute, shotFilter, homeTeam, awayTeam]);

  const handleMinuteChange = (minute: number) => {
    setTimelineMinute(minute);
    if (selectedPlay) {
      const m = selectedPlay.clock.match(/(\d+)/);
      const playMin = m ? parseInt(m[1], 10) : 0;
      if (playMin > minute) {
        const latestValid = plays
          .filter((p) => {
            const t = (p.type_text || p.type || '').toLowerCase();
            const isShot = t.includes('shot') || t.includes('goal') || t.includes('attempt');
            if (!isShot) return false;
            const matchMin = p.clock.match(/(\d+)/);
            return matchMin ? parseInt(matchMin[1], 10) <= minute : false;
          })
          .slice(-1)[0] || null;
        setSelectedPlay(latestValid);
      }
    }
  };

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
          <span className="hidden sm:inline text-xs text-white/40">
            ({homeTeam} vs {awayTeam})
          </span>
        </div>

        <div className="flex items-center gap-2">
          {viewMode === 'SHOTS' && (
            <button
              onClick={() => setIsHalfPitch((prev) => !prev)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-white/80 hover:text-white border border-white/10 text-xs font-bold font-mono transition cursor-pointer"
            >
              {isHalfPitch ? <Minimize2 className="w-3.5 h-3.5" /> : <Maximize2 className="w-3.5 h-3.5" />}
              <span>{isHalfPitch ? 'Full Pitch' : 'Half Pitch Focus'}</span>
            </button>
          )}
          <div className="text-xs font-mono text-[#DFFF00] font-black uppercase tracking-wider">
            Opta &amp; SofaScore Engine
          </div>
        </div>
      </div>

      <WebPitchRadarCanvas
        plays={shotPlays}
        allPlays={plays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        homeColor={hColor}
        awayColor={aColor}
        viewMode={viewMode}
        formations={formations}
        selectedPlayerName={selectedPlayerName}
        onSelectPlayerName={(name) => setSelectedPlayerName(name)}
        heatmapTeam={heatmapTeam}
        homeTeam={homeTeam}
        awayTeam={awayTeam}
        isHalfPitch={isHalfPitch}
        timelineMinute={timelineMinute}
      />

      <WebPitchTimelineScrubber
        momentum={momentum}
        currentMinute={timelineMinute}
        onMinuteChange={handleMinuteChange}
        isPlaying={isPlayingTimeline}
        onTogglePlay={() => setIsPlayingTimeline((prev) => !prev)}
        onReset={() => {
          setIsPlayingTimeline(false);
          handleMinuteChange(90);
        }}
        plays={plays}
        homeColor={hColor}
        awayColor={aColor}
      />

      <WebPitchRadarControls
        plays={shotPlays}
        allPlays={plays}
        selectedPlay={selectedPlay}
        onSelectPlay={(p) => setSelectedPlay(p)}
        onJumpToMinute={handleMinuteChange}
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
        homeColor={hColor}
        awayColor={aColor}
        shotFilter={shotFilter}
        onSelectShotFilter={setShotFilter}
        selectedPlayerName={selectedPlayerName}
        onClearPlayerFilter={() => setSelectedPlayerName(null)}
      />
    </div>
  );
}

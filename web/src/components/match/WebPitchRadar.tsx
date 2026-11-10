'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Crosshair, Volume2, VolumeX } from 'lucide-react';
import WebPitchRadarCanvas from './WebPitchRadarCanvas';
import WebPitchRadarControls from './WebPitchRadarControls';

export interface WebPitchPlay {
  id: string;
  clock: string;
  period?: number;
  type: string;
  type_text?: string;
  text: string;
  team_name?: string;
  player?: string | null;
  x_percent?: number | null;
  y_percent?: number | null;
  target_x?: number | null;
  target_y?: number | null;
}

interface WebPitchRadarProps {
  plays?: WebPitchPlay[];
  homeTeam: string;
  awayTeam: string;
  isLive?: boolean;
}

export default function WebPitchRadar({
  plays = [],
  homeTeam,
  awayTeam,
  isLive = false,
}: WebPitchRadarProps) {
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [isVoiceEnabled, setIsVoiceEnabled] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);

  const filteredPlays = useMemo(() => {
    if (selectedCategory === 'ALL') return plays;
    const cat = selectedCategory.toLowerCase();
    return plays.filter((p) => {
      const t = (p.type_text || p.type || '').toLowerCase();
      if (cat === 'goals') return t.includes('goal');
      if (cat === 'shots') return t.includes('shot') || t.includes('attempt');
      if (cat === 'cards') return t.includes('card');
      if (cat === 'corners') return t.includes('corner');
      if (cat === 'subs') return t.includes('sub');
      return true;
    });
  }, [plays, selectedCategory]);

  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(
    filteredPlays[filteredPlays.length - 1] || null
  );

  useEffect(() => {
    if (filteredPlays.length > 0 && (!selectedPlay || !filteredPlays.some((p) => p.id === selectedPlay.id))) {
      setSelectedPlay(filteredPlays[filteredPlays.length - 1] || null);
    }
  }, [filteredPlays, selectedPlay]);

  const speakPlay = useCallback((play: WebPitchPlay) => {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) return;
    try {
      window.speechSynthesis.cancel();
      const textToSpeak = `${play.clock ? play.clock + '. ' : ''}${play.text}`;
      const utterance = new SpeechSynthesisUtterance(textToSpeak);
      utterance.rate = 1.05;
      utterance.pitch = 1.0;
      const voices = window.speechSynthesis.getVoices();
      const englishVoice = voices.find(
        (v) => v.lang.startsWith('en-GB') || v.lang.startsWith('en')
      );
      if (englishVoice) utterance.voice = englishVoice;
      window.speechSynthesis.speak(utterance);
    } catch {
      // Safe fallback if speech synthesis is not allowed or supported
    }
  }, []);

  const handleSelectPlay = useCallback((play: WebPitchPlay) => {
    setSelectedPlay(play);
    if (isVoiceEnabled) {
      speakPlay(play);
    }
  }, [isVoiceEnabled, speakPlay]);

  // Auto-play replay interval timer
  useEffect(() => {
    if (!isPlaying || filteredPlays.length === 0) return;

    const timer = setInterval(() => {
      setSelectedPlay((prev) => {
        const curIdx = filteredPlays.findIndex((p) => p.id === prev?.id);
        const nextIdx = curIdx + 1 < filteredPlays.length ? curIdx + 1 : 0;
        const nextPlay = filteredPlays[nextIdx] || null;
        if (nextPlay && isVoiceEnabled) speakPlay(nextPlay);
        if (nextIdx === filteredPlays.length - 1) setIsPlaying(false);
        return nextPlay;
      });
    }, 1600);

    return () => clearInterval(timer);
  }, [isPlaying, filteredPlays, isVoiceEnabled, speakPlay]);

  // Clean up speech on unmount
  useEffect(() => {
    return () => {
      if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  const toggleVoiceCommentary = () => {
    const next = !isVoiceEnabled;
    setIsVoiceEnabled(next);
    if (!next) {
      if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    } else if (selectedPlay) {
      speakPlay(selectedPlay);
    }
  };

  const getMarkerColor = (play: WebPitchPlay): string => {
    const t = (play.type_text || play.type || '').toLowerCase();
    if (t.includes('goal')) return '#22C55E';
    if (t.includes('shot') || t.includes('attempt')) return '#38BDF8';
    if (t.includes('card')) return '#EAB308';
    if (t.includes('corner')) return '#A855F7';
    if (t.includes('sub')) return '#F97316';
    return '#94A3B8';
  };

  return (
    <div className="rounded-2xl bg-[#0F141A] border border-[#334155]/70 p-4 space-y-3.5 shadow-md">
      {/* Header with Title and Voice Toggle */}
      <div className="flex items-center justify-between gap-2 text-xs text-[#94A3B8]">
        <div className="flex items-center gap-2">
          <Crosshair className="w-4 h-4 text-[#7098F8]" />
          <span className="font-extrabold text-white tracking-wide">Interactive 2D Pitch Radar</span>
          <span className="hidden sm:inline text-[11px] text-[#64748B]">
            ({homeTeam} vs {awayTeam})
          </span>
        </div>

        <button
          type="button"
          onClick={toggleVoiceCommentary}
          className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-bold transition-all border cursor-pointer ${
            isVoiceEnabled
              ? 'bg-[#7098F8]/20 text-[#7098F8] border-[#7098F8]/40 shadow-sm'
              : 'bg-[#181C20] text-[#94A3B8] border-[#334155]/60 hover:text-white hover:bg-[#1E2227]'
          }`}
          title={isVoiceEnabled ? 'Voice Commentary Active' : 'Enable Voice Commentary'}
        >
          {isVoiceEnabled ? <Volume2 className="w-3.5 h-3.5 text-[#7098F8]" /> : <VolumeX className="w-3.5 h-3.5" />}
          <span>{isVoiceEnabled ? 'Audio ON' : 'Audio Replay'}</span>
        </button>
      </div>

      {/* Interactive Pitch Canvas with Trajectories and Nodes */}
      <WebPitchRadarCanvas
        plays={filteredPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={handleSelectPlay}
        getMarkerColor={getMarkerColor}
      />

      {/* Filters, Scrubber Slider and Playback Controls */}
      <WebPitchRadarControls
        plays={filteredPlays}
        selectedPlay={selectedPlay}
        onSelectPlay={handleSelectPlay}
        selectedCategory={selectedCategory}
        onSelectCategory={setSelectedCategory}
        isPlaying={isPlaying}
        onTogglePlay={() => setIsPlaying((p) => !p)}
        onReset={() => {
          setIsPlaying(false);
          if (filteredPlays.length > 0) handleSelectPlay(filteredPlays[0]!);
        }}
        getMarkerColor={getMarkerColor}
      />
    </div>
  );
}

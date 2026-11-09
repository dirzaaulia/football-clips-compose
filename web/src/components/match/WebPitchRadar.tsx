'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Crosshair, Volume2, VolumeX } from 'lucide-react';

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
  const [selectedPlay, setSelectedPlay] = useState<WebPitchPlay | null>(plays[plays.length - 1] || null);
  const [isVoiceEnabled, setIsVoiceEnabled] = useState(false);

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

  const handleSelectPlay = (play: WebPitchPlay) => {
    setSelectedPlay(play);
    if (isVoiceEnabled) {
      speakPlay(play);
    }
  };

  useEffect(() => {
    if (isVoiceEnabled && isLive && plays.length > 0) {
      const latest = plays[plays.length - 1];
      if (latest && latest.id !== selectedPlay?.id) {
        setSelectedPlay(latest);
        speakPlay(latest);
      }
    }
  }, [plays, isVoiceEnabled, isLive, selectedPlay?.id, speakPlay]);

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

  const getMarkerColor = (play: WebPitchPlay) => {
    const t = (play.type_text || play.type || '').toLowerCase();
    if (t.includes('goal')) return '#22C55E';
    if (t.includes('shot')) return '#38BDF8';
    if (t.includes('card')) return '#EAB308';
    return '#F97316';
  };

  return (
    <div className="rounded-2xl bg-[#0B0F17] border border-slate-800 p-4 space-y-3">
      {/* Header with 2D Radar Title and Voice Commentary Toggle */}
      <div className="flex items-center justify-between gap-2 text-xs font-mono text-slate-400">
        <span className="flex items-center gap-1.5 font-bold text-slate-200">
          <Crosshair className="w-3.5 h-3.5 text-cyan-400" />
          2D Pitch Radar
        </span>
        <div className="flex items-center gap-2">
          <span className="hidden sm:inline text-[11px] text-slate-500">
            {homeTeam} vs {awayTeam}
          </span>
          <button
            type="button"
            onClick={toggleVoiceCommentary}
            className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold transition-all border ${
              isVoiceEnabled
                ? 'bg-blue-600/20 text-[#7098F8] border-blue-500/40 shadow-sm'
                : 'bg-slate-800/60 text-slate-400 border-slate-700/60 hover:text-white'
            }`}
            title={isVoiceEnabled ? 'Voice Commentary Active' : 'Enable Voice Commentary'}
          >
            {isVoiceEnabled ? <Volume2 className="w-3.5 h-3.5 text-[#7098F8]" /> : <VolumeX className="w-3.5 h-3.5" />}
            <span>{isVoiceEnabled ? 'Audio ON' : 'Audio Replay'}</span>
          </button>
        </div>
      </div>

      {/* SVG Pitch Canvas */}
      <div className="relative aspect-[1.54/1] w-full rounded-xl overflow-hidden bg-gradient-to-b from-[#112216] to-[#18301F] border border-emerald-950 shadow-inner">
        <svg className="absolute inset-0 w-full h-full stroke-white/30 fill-none" viewBox="0 0 105 68">
          <rect x="2" y="2" width="101" height="64" strokeWidth="0.8" rx="2" />
          <line x1="52.5" y1="2" x2="52.5" y2="66" strokeWidth="0.8" />
          <circle cx="52.5" cy="34" r="9.15" strokeWidth="0.8" />
          <rect x="2" y="14" width="16.5" height="40" strokeWidth="0.8" />
          <rect x="2" y="23" width="5.5" height="22" strokeWidth="0.8" />
          <circle cx="11" cy="34" r="0.8" fill="white" className="opacity-40" />
          <rect x="86.5" y="14" width="16.5" height="40" strokeWidth="0.8" />
          <rect x="97.5" y="23" width="5.5" height="22" strokeWidth="0.8" />
          <circle cx="94" cy="34" r="0.8" fill="white" className="opacity-40" />

          {selectedPlay && selectedPlay.x_percent != null && selectedPlay.y_percent != null && (
            <g>
              <circle
                cx={selectedPlay.x_percent * 1.05}
                cy={selectedPlay.y_percent * 0.68}
                r="4.5"
                fill={getMarkerColor(selectedPlay)}
                className="animate-ping opacity-75"
              />
              <circle
                cx={selectedPlay.x_percent * 1.05}
                cy={selectedPlay.y_percent * 0.68}
                r="2.2"
                fill={getMarkerColor(selectedPlay)}
                stroke="white"
                strokeWidth="0.6"
              />
            </g>
          )}
        </svg>

        {/* Live Play Banner */}
        {selectedPlay && (
          <div className="absolute bottom-2 left-2 right-2 bg-slate-900/90 backdrop-blur-md border border-slate-700/80 rounded-lg px-3 py-1.5 flex items-center justify-between text-xs">
            <div className="flex items-center gap-2 truncate">
              <span className="font-bold text-cyan-400 font-mono">{selectedPlay.clock}</span>
              <span className="text-white truncate font-medium">{selectedPlay.text}</span>
            </div>
            <span
              className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded shrink-0"
              style={{ backgroundColor: `${getMarkerColor(selectedPlay)}25`, color: getMarkerColor(selectedPlay) }}
            >
              {selectedPlay.type_text || selectedPlay.type}
            </span>
          </div>
        )}
      </div>

      {/* Horizontal Play Scrubber */}
      {plays.length > 0 && (
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
          {plays.slice(-8).reverse().map((p) => {
            const isSelected = selectedPlay?.id === p.id;
            return (
              <button
                key={p.id}
                type="button"
                onClick={() => handleSelectPlay(p)}
                className={`px-2.5 py-1 rounded-md text-[11px] font-mono shrink-0 transition ${
                  isSelected
                    ? 'bg-blue-600 text-white font-bold shadow-sm'
                    : 'bg-slate-800/80 text-slate-400 hover:text-white hover:bg-slate-700'
                }`}
              >
                {p.clock} {p.type_text?.slice(0, 6) || p.type?.slice(0, 6)}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

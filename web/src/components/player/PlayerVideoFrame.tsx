'use client';

import React from 'react';
import { Play } from 'lucide-react';
import PlayerErrorOverlay, { PlayerErrorType } from './PlayerErrorOverlay';

interface PlayerVideoFrameProps {
  activeVideoId: string | null;
  homeName: string;
  awayName: string;
  country: string | null;
  errorState: PlayerErrorType;
  retryNonce: number;
  onRetry: () => void;
  hasSwitchedToBackup: boolean;
  isLoading: boolean;
  onIframeLoad?: () => void;
}

export default function PlayerVideoFrame({
  activeVideoId,
  homeName,
  awayName,
  country,
  errorState,
  retryNonce,
  onRetry,
  hasSwitchedToBackup,
  isLoading,
  onIframeLoad,
}: PlayerVideoFrameProps) {
  return (
    <div className="space-y-3 relative group">
      {/* Ambient Glow Backlight */}
      <div className="absolute -inset-1 bg-gradient-to-r from-[#7098F8]/20 via-blue-600/10 to-[#7098F8]/20 rounded-3xl blur-xl pointer-events-none -z-10 opacity-70 group-hover:opacity-100 transition-opacity duration-500" />

      {/* Glass Chassis Video Frame */}
      <div className="relative aspect-video w-full rounded-2xl overflow-hidden bg-black border border-white/10 shadow-[0_0_40px_rgba(0,0,0,0.8)]">
        <PlayerErrorOverlay
          errorState={errorState}
          country={country}
          activeVideoId={activeVideoId}
          onRetry={onRetry}
        />

        {/* Dedicated Loading State */}
        {isLoading && errorState === 'NONE' && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center p-6 text-center bg-[#0A0E14] transition-opacity duration-300">
            <div className="relative mb-3 flex items-center justify-center">
              <div className="w-12 h-12 rounded-full border-2 border-[#7098F8]/20 border-t-[#7098F8] animate-spin" />
              <Play className="w-5 h-5 text-[#7098F8] absolute fill-[#7098F8]/30" />
            </div>
            <span className="text-xs font-bold text-white tracking-tight">Connecting to official highlight stream...</span>
            <span className="text-[11px] text-[#94A3B8] mt-1">{homeName} vs {awayName}</span>
          </div>
        )}

        {errorState === 'NONE' && activeVideoId && (
          <iframe
            key={`${activeVideoId}-${retryNonce}`}
            className="absolute inset-0 w-full h-full"
            src={`https://www.youtube.com/embed/${activeVideoId}?autoplay=1&rel=0&modestbranding=1&enablejsapi=1`}
            title={`${homeName} vs ${awayName} Highlights`}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
            onLoad={onIframeLoad}
          />
        )}
      </div>

      {/* Compliant External Control Pill Desk */}
      <div className="flex items-center justify-between gap-3 px-3 py-2 bg-[#181C20]/90 backdrop-blur-md border border-white/10 rounded-xl text-xs">
        <div className="flex items-center gap-2">
          {hasSwitchedToBackup && errorState === 'NONE' ? (
            <span className="flex items-center gap-1.5 text-[#7098F8] font-medium">
              <span className="w-2 h-2 rounded-full bg-[#7098F8] animate-pulse" />
              Playing Alternative Stream
            </span>
          ) : (
            <span className="flex items-center gap-1.5 text-emerald-400 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-400" />
              Official Broadcaster Stream
            </span>
          )}
        </div>

        {activeVideoId && (
          <a
            href={`https://www.youtube.com/watch?v=${activeVideoId}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-red-600/20 hover:bg-red-600/30 text-red-400 border border-red-500/30 font-semibold transition text-[11px]"
            title="Watch on Official YouTube"
          >
            <span>Watch on YouTube</span>
          </a>
        )}
      </div>
    </div>
  );
}

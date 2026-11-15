'use client';

import React from 'react';
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
      <div className="absolute -inset-1 bg-gradient-to-r from-[#DFFF00]/10 via-[#38BDF8]/10 to-[#DFFF00]/10 rounded-3xl blur-xl pointer-events-none -z-10 opacity-70 group-hover:opacity-100 transition-opacity duration-500" />

      {/* Glass Chassis Video Frame */}
      <div className="relative aspect-video w-full rounded-2xl overflow-hidden bg-black border border-white/10 shadow-[0_0_40px_rgba(0,0,0,0.8)]">
        <PlayerErrorOverlay
          errorState={errorState}
          country={country}
          activeVideoId={activeVideoId}
          onRetry={onRetry}
        />

        {/* Dedicated Loading State: 16:9 Buffering Shimmer Skeleton with Acoustic Wave Bars */}
        {isLoading && errorState === 'NONE' && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center p-6 text-center bg-[#0A0A0C] transition-opacity duration-300">
            <div className="relative mb-3 flex items-center justify-center">
              {/* Pulsing Acoustic Glow Ring */}
              <div className="w-14 h-14 rounded-2xl bg-[#121316] border border-[#DFFF00]/30 shadow-[0_0_20px_rgba(223,255,0,0.2)] flex items-center justify-center">
                <div className="flex items-end gap-1 h-5">
                  <span className="w-1 bg-[#DFFF00] rounded-full animate-[bounce_1s_infinite_100ms] h-3" />
                  <span className="w-1 bg-[#DFFF00] rounded-full animate-[bounce_1s_infinite_200ms] h-5" />
                  <span className="w-1 bg-[#DFFF00] rounded-full animate-[bounce_1s_infinite_300ms] h-4" />
                  <span className="w-1 bg-[#DFFF00] rounded-full animate-[bounce_1s_infinite_150ms] h-2" />
                </div>
              </div>
            </div>
            <span className="text-xs font-tech font-bold text-white tracking-wide uppercase">Connecting to official highlight stream...</span>
            <span className="text-[11px] font-tech text-white/50 mt-1">{homeName} vs {awayName}</span>
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
      <div className="flex items-center justify-between gap-3 px-3 py-2 bg-[#121316] backdrop-blur-md border border-white/10 rounded-xl text-xs font-tech">
        <div className="flex items-center gap-2">
          {hasSwitchedToBackup && errorState === 'NONE' ? (
            <span className="flex items-center gap-1.5 text-[#38BDF8] font-medium">
              <span className="w-2 h-2 rounded-full bg-[#38BDF8] animate-pulse" />
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

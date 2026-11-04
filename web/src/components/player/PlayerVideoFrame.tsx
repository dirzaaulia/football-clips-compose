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
    <div className="space-y-3">
      <div className="relative aspect-video w-full rounded-2xl overflow-hidden bg-black border border-[#334155]/60 shadow-2xl">
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

      {/* Subtle indicator when playing backup stream */}
      {hasSwitchedToBackup && errorState === 'NONE' && (
        <div className="flex items-center gap-2 px-3 py-2 bg-[#181C20] border border-[#7098F8]/40 rounded-xl text-xs text-[#7098F8] font-semibold">
          <span className="w-2 h-2 rounded-full bg-[#7098F8] animate-pulse" />
          <span>Playing alternative official highlight stream</span>
        </div>
      )}
    </div>
  );
}

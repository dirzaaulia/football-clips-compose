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
}: PlayerVideoFrameProps) {
  return (
    <div className="space-y-3">
      <div className="relative aspect-video w-full rounded-2xl overflow-hidden bg-black/90 border border-[#334155]/60 shadow-2xl">
        <PlayerErrorOverlay
          errorState={errorState}
          country={country}
          activeVideoId={activeVideoId}
          onRetry={onRetry}
        />

        {errorState === 'NONE' && activeVideoId && (
          <iframe
            key={`${activeVideoId}-${retryNonce}`}
            className="absolute inset-0 w-full h-full"
            src={`https://www.youtube.com/embed/${activeVideoId}?autoplay=1&rel=0&modestbranding=1&enablejsapi=1`}
            title={`${homeName} vs ${awayName} Highlights`}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
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

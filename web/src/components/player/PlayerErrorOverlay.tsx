'use client';

import React from 'react';
import { Globe, Play, ExternalLink, AlertCircle, RefreshCw } from 'lucide-react';

export type PlayerErrorType = 'NONE' | 'REGION_BLOCKED' | 'NOT_EMBEDDABLE' | 'USER_SIDE_ERROR';

interface PlayerErrorOverlayProps {
  errorState: PlayerErrorType;
  country: string | null;
  activeVideoId: string | null;
  onRetry: () => void;
}

export default function PlayerErrorOverlay({
  errorState,
  country,
  activeVideoId,
  onRetry,
}: PlayerErrorOverlayProps) {
  if (errorState === 'NONE') return null;

  if (errorState === 'REGION_BLOCKED') {
    return (
      <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center bg-[#101418]">
        <Globe className="w-10 h-10 text-[#F59E0B] mb-2.5 animate-pulse" />
        <h4 className="text-sm font-bold text-white mb-1">Not Available in Your Region</h4>
        <p className="text-xs text-[#94A3B8] max-w-[300px]">
          This official match highlight is restricted in your country {country ? `(${country})` : ''} due to regional broadcasting rights.
        </p>
      </div>
    );
  }

  if (errorState === 'NOT_EMBEDDABLE') {
    return (
      <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center bg-[#101418]">
        <div className="w-10 h-10 rounded-full bg-[#EF4444]/20 border border-[#EF4444]/40 flex items-center justify-center mb-2.5">
          <Play className="w-5 h-5 text-[#EF4444] ml-0.5 fill-[#EF4444]" />
        </div>
        <h4 className="text-sm font-bold text-white mb-1">Playback Restricted in Embeds</h4>
        <p className="text-xs text-[#94A3B8] max-w-[300px] mb-4">
          The rights holder requires this highlight to be watched directly on official YouTube channels.
        </p>
        <a
          href={`https://www.youtube.com/watch?v=${activeVideoId}`}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-[#EF4444] hover:bg-[#DC2626] text-white shadow-lg transition-all"
        >
          <span>Watch on YouTube</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    );
  }

  if (errorState === 'USER_SIDE_ERROR') {
    return (
      <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center bg-[#101418]">
        <AlertCircle className="w-10 h-10 text-[#7098F8] mb-2.5" />
        <h4 className="text-sm font-bold text-white mb-1">Playback Error</h4>
        <p className="text-xs text-[#94A3B8] max-w-[300px] mb-4">
          Unable to load video. Please check your internet connection and try again.
        </p>
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-[#1E3A8A] hover:bg-[#7098F8] text-white transition-all cursor-pointer shadow-md"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Try Again</span>
        </button>
      </div>
    );
  }

  return null;
}

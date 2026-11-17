'use client';

import React from 'react';
import { Share2, ExternalLink, Volume2, VolumeX } from 'lucide-react';

interface ShortsActionRailProps {
  videoId: string;
  isMuted: boolean;
  onToggleMute: () => void;
  onShare: () => void;
}

export default function ShortsActionRail({
  videoId,
  isMuted,
  onToggleMute,
  onShare,
}: ShortsActionRailProps) {
  return (
    <div className="flex flex-col items-center gap-3 shrink-0">
      {/* Share Button */}
      <button
        onClick={onShare}
        className="flex flex-col items-center gap-1 group transition-transform active:scale-90"
        title="Share Clip"
      >
        <div className="p-3 rounded-full bg-black/60 backdrop-blur-md text-white border border-white/10 group-hover:bg-black/80 transition-colors">
          <Share2 className="w-5 h-5" />
        </div>
        <span className="text-[11px] font-mono font-semibold text-white drop-shadow">
          Share
        </span>
      </button>

      {/* Mute / Unmute Button */}
      <button
        onClick={onToggleMute}
        className="flex flex-col items-center gap-1 group transition-transform active:scale-90"
        title={isMuted ? 'Unmute' : 'Mute'}
      >
        <div className="p-3 rounded-full bg-black/60 backdrop-blur-md text-white border border-white/10 group-hover:bg-black/80 transition-colors">
          {isMuted ? (
            <VolumeX className="w-5 h-5 text-amber-400" />
          ) : (
            <Volume2 className="w-5 h-5 text-[#DFFF00]" />
          )}
        </div>
        <span className="text-[11px] font-mono font-semibold text-white drop-shadow">
          {isMuted ? 'Muted' : 'Sound'}
        </span>
      </button>

      {/* YouTube Official Outlink Button */}
      <a
        href={`https://www.youtube.com/shorts/${videoId}`}
        target="_blank"
        rel="noopener noreferrer"
        className="flex flex-col items-center gap-1 group transition-transform active:scale-90"
        title="Watch on Official YouTube"
      >
        <div className="p-3 rounded-full bg-red-600/80 backdrop-blur-md text-white border border-red-500/40 group-hover:bg-red-600 transition-colors shadow-[0_0_12px_rgba(239,68,68,0.3)]">
          <ExternalLink className="w-5 h-5" />
        </div>
        <span className="text-[10px] font-mono font-bold text-white drop-shadow">
          YouTube
        </span>
      </a>
    </div>
  );
}
